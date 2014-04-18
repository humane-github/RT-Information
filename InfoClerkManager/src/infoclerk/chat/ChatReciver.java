package infoclerk.chat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

import jp.co.humane.hcharencoder.HCharEncoder;

public class ChatReciver
{
	private SocketAddress m_socketAddress = null;
	private Selector m_selector = null;
	private DatagramChannel m_channel = null;
	private String m_localhost = null;
	private String m_encoding = null;
	private int m_buffSize = 0;
	private int m_port = 0;
	private String m_recvData = null;
	private boolean m_runnable = false;
	
	/**
	 * コンストラクタ
	 * **/
	public ChatReciver(int port,String encoding,int buffSize)
	{
		m_port = port;
		m_buffSize = buffSize;
		m_encoding = encoding;
    	try {
			m_localhost = InetAddress.getLocalHost().getHostName();
			m_channel = DatagramChannel.open();
			m_socketAddress = new InetSocketAddress(m_localhost,m_port);
			m_channel.socket().bind(m_socketAddress);
			m_channel.configureBlocking(false);
			m_selector = Selector.open();
			m_channel.register(m_selector, SelectionKey.OP_READ);
			m_runnable = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String recv()
	{
		if( !m_runnable ){return null;}
    	try
    	{
			if(m_selector.selectNow()>0)
			{
				for(Iterator<SelectionKey> it = m_selector.selectedKeys().iterator();it.hasNext();)
				{
					SelectionKey selectionKey = it.next();
					it.remove();
					if( selectionKey.isReadable())
					{
						doReceive(selectionKey);
					}
				}
			}
		}
    	catch (IOException e1)
    	{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	if( m_recvData != null )
    	{
    		String tmp = m_recvData;
    		m_recvData = null;
    		return tmp;
    	}
    	else
    	{
    		return null;
    	}
	}
	
	public void close()
	{
    	try {
			m_selector.close();
			m_channel.close();
			m_recvData = null;
			m_runnable = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("ChatReciver Deactivated");
    	m_socketAddress = null;
	}
	
    private void doReceive(SelectionKey selectionKey)
    {
        DatagramChannel datagramChannel 
                    = (DatagramChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(m_buffSize);
        try {
            datagramChannel.receive(byteBuffer);
            byteBuffer.flip();
            DataPacket packet = new DataPacket(byteBuffer.array());
            if( packet.isSendMSG() )
            {
            	m_recvData = HCharEncoder.byte2Str(packet.getBody(), m_encoding);            	
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		ChatReciver recver = new ChatReciver(10020, "utf-8", 8192);
		while(true)
		{
			String d = recver.recv();
			if( d == null ){continue;}
			System.out.println(d);
		}
	}

}
