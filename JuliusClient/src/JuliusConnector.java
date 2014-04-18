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
	
	// Juliusプロセスオブジェクト
	private Process m_julius = null;
	// Juliusとの通信用ソケット
	private Socket m_socket = null;
	// Juliusの実行ホスト名
	private String m_hostname = null;
	// 音声認識の有効スコア
	private float m_availableSocre = 0;
	// JuliusModuleポート番号
	private int m_modulePort = 0;
	// Julius実行スレッド
	private Thread m_thread = null;
	// Julius実行スレッドの起動状態
	private boolean m_halt = false;
	// Juliusの認識結果送信先
	private ArrayList<JuliusListener> m_listenerList = new ArrayList<JuliusListener>();
	// 取得対象のクラスID
	private String[] m_targetClassId = new String[]{};
	// ログ出力用
	private Logger m_logger = null;
	
	/**
	 * コンストラクタ
	 * 
	 * @param 	j 				Juliusプロセスオブジェクト
	 * @param	hostname		Julius実行ホスト名
	 * @param	port			JuliusのModuleポート番号
	 * @param	availableScore	有効スコア
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
	 * JuliusListenerを登録する
	 * **/
	public void addListener(JuliusListener listener)
	{
		m_listenerList.add(listener);
	}
	/**
	 * 処理対象のクラスIDを設定する
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
	 * Juliusからのレスポンスを解析する
	 * 
	 * @param	xml	Ｊｕｌｉｕｓから受け取った認識結果XML
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
	 * Juliusとの通信を開始する
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
	 * Juliusとの通信を終える
	 * **/
	public void stop()
	{
		m_halt = false;
		m_thread.interrupt();
		m_julius = null;
		close();
	}	
	
	/**
	 * Juliusとの接続を確立する
	 * 
	 * @param host	Juliusが実行しているホスト名
	 * @param port	Juliusとの接続ポート番号
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
     * JuliusとのSocketを閉じる
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
