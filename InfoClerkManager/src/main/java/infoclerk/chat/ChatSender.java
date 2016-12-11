package infoclerk.chat;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class ChatSender
{
	private String m_ipaddress = null;
	public void setIpaddress(String ipaddr){m_ipaddress = ipaddr;}
	private int m_sendPort = 0;
	private String m_encoding = null;
	private int m_buffSize = 0;

	/**
	 * コンストラクタ
	 * 
	 * @param	ipaddress	UDP接続先ホスト
	 * @param	sendport	UDPポート（送信用）
	 * @param	encoding	通信時に使用する円コーディング
	 * @param	buffSize	パケットサイズ
	 * **/
	public ChatSender(String ipaddress,int sendport,String encoding,int buffSize)
	{
		m_ipaddress = ipaddress;
		m_sendPort = sendport;
		m_encoding = encoding;
		m_buffSize = buffSize;
	}
	
	/**
	 * テキストを送信する
	 * 
	 * @param	msg	送信する文字列
	 * **/
	public void send(String msg)
	{		
		//メッセージ送信
		try
		{
			DataPacket packet = new DataPacket(DataPacket.TYPE_SENDMSG, msg.getBytes(m_encoding));
			send(m_ipaddress,m_sendPort,m_buffSize,packet.toByteArray());
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
	
    private void send(String host,int port,int buffsize,byte[] data)
    {
		try(DatagramChannel channel = DatagramChannel.open())
		{
			SocketAddress address = new InetSocketAddress(host,port);
			channel.configureBlocking(false);
			channel.connect(address);
			while(!channel.isConnected());
			
			ByteBuffer buffer = ByteBuffer.allocate(buffsize);

			buffer.clear();
			//byte[] wordBynary = message.getBytes(StandardCharsets.UTF_8);
			buffer.put(data);
			buffer.flip();
			while(channel.send(buffer, address) == 0);
			channel.close();
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}    	
    }
       
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		ChatSender sender = new ChatSender("suzuki-PC", 10000, "utf-8", 8192);
		sender.send("来客です");
	}

}
