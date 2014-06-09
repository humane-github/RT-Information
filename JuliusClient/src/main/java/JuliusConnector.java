import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import jp.co.humane.logger.Logger;
import jp.co.humane.xml.XMLException;
import jp.co.humane.xml.Xml;

import org.w3c.dom.Element;

public class JuliusConnector
{
	private static String STATUS_SUCCESS = "SUCCESS";
	private static String STATUS_FAILED = "FAILED";
	private static String STATUS_REJECTED = "REJECTED";
	
	private static String RESULT_FORMAT="CLASSID=%s:VOICE=%s:SCORE=%s";
	
	// Julius�v���Z�X�I�u�W�F�N�g
	private Process m_julius = null;
	// Julius�Ƃ̒ʐM�p�\�P�b�g
	private Socket m_socket = null;
	// Julius�̎��s�z�X�g��
	private String m_hostname = null;
	// �����F���̗L���X�R�A
	private float m_availableSocre = 0;
	// JuliusModule�|�[�g�ԍ�
	private int m_modulePort = 0;
	// Julius���s�X���b�h
	private Thread m_thread = null;
	// Julius���s�X���b�h�̋N�����
	private boolean m_halt = false;
	// Julius�̔F�����ʑ��M��
	private ArrayList<JuliusListener> m_listenerList = new ArrayList<JuliusListener>();
	// �擾�Ώۂ̃N���XID
	private String[] m_targetClassId = new String[]{};
	// ���O�o�͗p
	private Logger m_logger = null;
	
	/**
	 * �R���X�g���N�^
	 * 
	 * @param 	j 				Julius�v���Z�X�I�u�W�F�N�g
	 * @param	hostname		Julius���s�z�X�g��
	 * @param	port			Julius��Module�|�[�g�ԍ�
	 * @param	availableScore	�L���X�R�A
	 * **/
	public JuliusConnector(Process j,String hostname,int moduleport,float score)
	{
		this.m_julius = j;
		this.m_hostname = hostname;
		this.m_modulePort = moduleport;
		this.m_availableSocre = score;
		this.m_logger = Logger.create();
	}
	
	/**
	 * JuliusListener��o�^����
	 * **/
	public void addListener(JuliusListener listener)
	{
		m_listenerList.add(listener);
	}
	/**
	 * �����Ώۂ̃N���XID��ݒ肷��
	 * **/
	public void setTargetClassId(String[] classid)
	{
		m_targetClassId = classid;
	}
	
	public int initialize()
	{	
    	m_thread = new Thread()
    	{
    		public void run()
    		{
    			BufferedReader br = null;
    			String line = null;
    			String result = null;
    			try
    			{
        			br = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
    			}
    			catch( IOException ex )
    			{
    				ex.printStackTrace();
    			}

    			while(m_halt)
    			{
    				if( m_julius != null )
    		    	{
    		    		try
    		    		{
    		    			String buffer = "";  		    			
    	        			while((line = br.readLine()) != null)
    	        			{
    	        				if( line.endsWith(".") )
    	        				{
    	        					result = parse(buffer);
    	        					if( result != null )
    	        					{
    	        						for( JuliusListener l : m_listenerList )
    	        						{
    	        							l.onVoiceData(result,STATUS_SUCCESS);
    	        						}
    	        					}
    	        					break;
    	        				}
    	        				buffer += line+"\n";
    	        			}
    		    		}
    		    		catch( IOException e )
    		    		{
    		    			e.printStackTrace();
    		    			m_halt = false;
    		    		}
    		    	}
    			}
    		}    	
    	};
    	return 0;
	}
	
	/**
	 * Julius����̃��X�|���X����͂���
	 * 
	 * @param	xml	�i��������������󂯎�����F������XML
	 * **/
	private String parse(String xml)
	{
		System.out.println(xml);
		String resultString = null;
		if( xml == null || xml.length() < 1 ){return resultString;}
		try
		{
			Xml XML = new Xml();
			XML.parse(xml);
			List<Element> result = new ArrayList<Element>();
			for( String classid : m_targetClassId )
			{
				XML.searchElement(XML.getRootElement(),"WHYPO", "CLASSID", classid, result, true);				
			}
			if( result == null || result.size() < 1 )
			{
				//System.out.println("WHYPO count 0");
				return resultString;
			}
			String name = result.get(0).getAttribute("WORD");
			String score = result.get(0).getAttribute("CM");
			String classid = result.get(0).getAttribute("CLASSID");
			m_logger.trace(String.format("Word=%s Score=%s ClassID=%s", name,score,classid));
			resultString = String.format(RESULT_FORMAT,classid,name,score);
		}
		catch( XMLException ex)
		{
			ex.printStackTrace();
		}
		return resultString;
	}
	
	/**
	 * Julius�Ƃ̒ʐM���J�n����
	 * **/
	public int start()
	{
		try
		{
			open(m_hostname,m_modulePort);
		}
		catch( IOException e )
		{
			e.printStackTrace();
			return -1;
		}
		m_halt = true;
		m_thread.start();
		return 0;
	}
	
	/**
	 * Julius�Ƃ̒ʐM���I����
	 * **/
	public void stop()
	{
		m_halt = false;
		m_thread.interrupt();
		m_julius = null;
		close();
	}	
	
	/**
	 * Julius�Ƃ̐ڑ����m������
	 * 
	 * @param host	Julius�����s���Ă���z�X�g��
	 * @param port	Julius�Ƃ̐ڑ��|�[�g�ԍ�
	 * **/
    private void open(String host,int port) throws IOException
    {
    	try
    	{
        	m_socket = new Socket();
        	m_socket.connect(new InetSocketAddress(host, port));
    	}
    	catch( ConnectException e1 )
    	{
    		System.out.println("Julius module socket is closed");
    		e1.printStackTrace();
    	}
    }
    
    /**
     * Julius�Ƃ�Socket�����
     * **/
    private void close()
    {
    	try
    	{
        	if( m_socket != null && (m_socket.isConnected() || !m_socket.isClosed()) ) {m_socket.close();}
    	}
    	catch( Exception e )
    	{
    		e.printStackTrace();
    	}    	
    }
}
