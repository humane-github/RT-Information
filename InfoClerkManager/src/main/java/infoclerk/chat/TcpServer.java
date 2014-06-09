package infoclerk.chat;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import jp.co.humane.xml.rtc.AttributeNotFoundException;
import jp.co.humane.xml.rtc.RTCML;

public class TcpServer
{
	private final static String CMD_GETIMG = "CMD_GETIMG";
	private final static int PORT = 11000;
	private final static int PORT_HEARTBEAT = 11001;
	//�T�[�o�[�̏�ԃt���O
	private boolean m_sleep = false;
	//�N���C�A���g�Ƃ̐ڑ��m�F�p
	private byte[] m_alive = new byte[]{0,1};
	//�T�[�o�[�\�P�b�g
	private ServerSocket m_serverSocket = null;
	private ServerSocket m_hbServerSocket = null;
	//�ڑ����Ă���N���C�A���g��IP�A�h���X���L�[�Ƃ���Map
	private HashMap<String,ServerThread> m_sockets = new HashMap<String,ServerThread>();
	private byte[] m_senddata = null;
	public void setSendData(byte[] d){m_senddata = d;}
	
	/**
	 * �R���X�g���N�^
	 * **/
	public TcpServer(){}
	
	/**
	 * �T�[�o�[�J�n
	 * **/
	public void start()
	{
		try
		{
			m_serverSocket = new ServerSocket(PORT);
			m_hbServerSocket = new ServerSocket(PORT_HEARTBEAT);
			while(!m_sleep)
			{
				Socket socket = m_serverSocket.accept();
				Socket hbsocket = m_hbServerSocket.accept();
				ServerThread th = new ServerThread(socket,hbsocket);
				th.setSendData(m_senddata);
				th.start();
				m_sockets.put(socket.getInetAddress().getHostAddress(),th);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}		
	}
	
	public class ServerThread extends Thread
	{
		private RTCML m_rtcml = null;
		private Socket m_socket = null;
		private Socket m_hertbeatSocket = null;
		private byte[] m_sendData = null;
		
		/**
		 * �R���X�g���N�^
		 * 
		 * @param	socket	�ڑ��N���C�A���g��Socket
		 * **/
		public ServerThread(Socket socket,Socket hsocket)
		{
			m_socket = socket;
			m_hertbeatSocket = hsocket;
			m_rtcml = new RTCML();
		}
		
		public void run()
		{
			while(true)
			{
				if( m_sendData == null ){return;}
				try
				{
					//�N���C�A���g�Ƃ̐ڑ��m�F
					m_hertbeatSocket.getOutputStream().write(m_alive);
					m_hertbeatSocket.getOutputStream().flush();
					
					//�N���C�A���g����̃R�}���h��M
					BufferedReader reader = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
					String recvdata = reader.readLine();
					while(m_socket.getInputStream().available() != 0 )
					{
						recvdata += reader.readLine();
					}
					if( recvdata == null || recvdata.trim().length() < 1 ){continue;}
					//RTCML�̉��
					m_rtcml.parse(recvdata);
					String cmd = m_rtcml.getString("command");
					//�R�}���h�ɑΉ������������s��
					if( CMD_GETIMG.equals(cmd))
					{
						m_socket.getOutputStream().write(m_sendData);
						m_socket.getOutputStream().flush();
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (AttributeNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		}
		
		public void setSendData(byte[] d)
		{
			m_sendData = d;
		}
	}
	
	public byte[] getImageBytes(BufferedImage image,String imageFormat) throws IOException
	{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedOutputStream os = new BufferedOutputStream(bos);
        image.flush();
        ImageIO.write(image, imageFormat, os);
        os.flush();
        os.close();
        return bos.toByteArray();
    }
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try {
			TcpServer server = new TcpServer();
			byte[] img = server.getImageBytes(ImageIO.read(new File("C:\\DEV\\17.InformationClerk\\80.sandbox\\org.jpg")),"jpeg");
			server.setSendData(img);
			server.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO Auto-generated method stub

	}

}
