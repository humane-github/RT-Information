package infoclerk.chat;

import infoclerk.InfoClerkConfigSerializer;
import infoclerk.InfoClerkManagerImpl;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import javax.imageio.ImageIO;

import jp.co.humane.hcharencoder.HCharEncoder;
import jp.co.humane.opencvlib.MatFactory;
import jp.co.humane.opencvlib.MatFactory.MatType;
import jp.co.humane.xml.rtc.RTCML;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class InfoClerkTcpServer
{
	//�p�P�b�g�T�C�Y
	private int m_buffSize = 0;
	//�|�[�g
	private int m_port = 0;
	//�Z���N�^�[
	private Selector m_selector = null;
	private ServerSocketChannel m_channel = null;
	//�N���C�A���g�ɑ���f��
	private byte[] m_senddata = null;
	public void setSendData(byte[] d){m_senddata = d;}
	//�N���C�A���g����̐ڑ��҂������X���b�h
	private Thread m_acceptThread = null;
	//InfoClerk�̃��C���N���X
	private InfoClerkManagerImpl m_owner = null;
	private String m_encoding = null;
	//�R���t�B�O���[�V�����X�V�v��XML
	private String m_updateConfigRtcml = null;
	public String getUpdateConfigRtcml(){return m_updateConfigRtcml;}
	public void resetUpdateConfigRtcml(){m_updateConfigRtcml = null;}
	
	/**
	 * �R���X�g���N�^
	 * **/
	public InfoClerkTcpServer(InfoClerkManagerImpl owner,int port,int buffsize,String encoding)
	{
		m_owner = owner;
		m_port = port;
		m_buffSize = buffsize;
		m_encoding = encoding;
	}
	
	/**
	 * TCP�T�[�o�[���J�n����
	 * **/
	public void start()
	{
		m_acceptThread = new Thread()
		{
			public void run()
			{
				
				try
				{
					m_selector = Selector.open();
					m_channel = ServerSocketChannel.open();
					m_channel.configureBlocking(false);
					m_channel.socket().bind(new InetSocketAddress(m_port));
					m_channel.register(m_selector,SelectionKey.OP_ACCEPT);
					while( m_selector.select() > 0 )
					{
						Iterator it = null;
						for( it = m_selector.selectedKeys().iterator(); it.hasNext(); )
						{
							SelectionKey key = (SelectionKey)it.next();
							it.remove();
							if( key.isAcceptable() )
							{
								accept((ServerSocketChannel)key.channel());
							}
							else if( key.isReadable() )
							{
								int len = read((SocketChannel)key.channel());
								if( len < 1 )
								{
									key.channel().close();
								}
							}
						}
					}
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		};
		m_acceptThread.start();
	}
	
	public void stop()
	{
		if( m_acceptThread != null )
		{
			try {
				m_channel.close();
				m_selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			m_acceptThread = null;
		}
	}
	
	/**
	 * �N���C�A���g�Ɛڑ�����
	 * **/
	public void accept(ServerSocketChannel serverChannel)
	{
		try
		{
			SocketChannel channel = serverChannel.accept();
			channel.configureBlocking(false);
			channel.register(m_selector, SelectionKey.OP_READ);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * �N���C�A���g����d�����󂯎��
	 * **/
	public int read(SocketChannel channel)
	{
		ByteBuffer buffer = ByteBuffer.allocate(m_buffSize);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			int len = 0;

			while((len = channel.read(buffer)) > 0)
			{
				baos.write(buffer.array(),0,len);
			}
			//�p�P�b�g���
			DataPacket packet = new DataPacket(baos.toByteArray());
			//�f���擾�R�}���h��M
			if( packet.isGetIMG() )
			{
				DataPacket sendPacket = new DataPacket(DataPacket.TYPE_SENDIMG, m_senddata);					
				channel.write(ByteBuffer.wrap(sendPacket.toByteArray()));
			}
			//�v���p�e�B�擾�R�}���h��M
			else if( packet.isGetPROPERTY() )
			{
				StringBuffer response = new StringBuffer();
				for( RTCML rtcml : m_owner.getConfigrations().values())
				{
					response.append(rtcml.serializeComponentTag());
				}
				String data = String.format(RTCML.FORMAT,RTCML.CMD_RESPONSE,response.toString());				
				//String data = InfoClerkManagerImpl.Config().serialize(new InfoClerkConfigSerializer());
				System.out.println("xml="+data);
				DataPacket sendPacket = new DataPacket(DataPacket.TYPE_SENDPROPERTY, data.getBytes());
				channel.write(ByteBuffer.wrap(sendPacket.toByteArray()));
			}
			else if( packet.isSendPROPERTY() )
			{
            	String xml = HCharEncoder.byte2Str(packet.getBody(), m_encoding);
            	System.out.println("sendproperty="+xml);
            	m_updateConfigRtcml = xml;
			}
			return packet.Length();
		}
		catch (IOException e)
		{
			//e.printStackTrace();
			System.out.println(e.getMessage());
			return -1;
		}
	}
	
	/**
	 * �摜�𑗐M����
	 * 
	 * @param	width	�摜�̕�
	 * @param	height	�摜�̍���
	 * @param	bpp	
	 * @param	pixels	�摜�f�[�^
	 * **/
	public void setCameraImage(int width,int height,int bpp,byte[] pixels)
	{
		try
		{
    		Mat cameraMat = MatFactory.create(width,
						        				height,
						        				bpp,
												pixels);
			Mat smallImg = MatFactory.create(cameraMat.width()/4, cameraMat.height()/4, MatType.COLOR_8BIT);
			Imgproc.resize(cameraMat, smallImg, smallImg.size(),0,0,Imgproc.INTER_LINEAR);

			m_senddata = getImageBytes(matToBufferedImage(cameraMat),"jpeg");
		}
		catch( IOException ioe )
		{
			ioe.printStackTrace();
		}
	}
	
	/**  
	 * Converts/writes a Mat into a BufferedImage.  
	 *  
	 * @param matrix Mat of type CV_8UC3 or CV_8UC1  
	 * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY  
	 */  
	public BufferedImage matToBufferedImage(Mat matrix) {  
		int cols = matrix.cols();  
		int rows = matrix.rows();  
		int elemSize = (int)matrix.elemSize();  
		byte[] data = new byte[cols * rows * elemSize];  
		int type;  
		matrix.get(0, 0, data);  
		switch (matrix.channels()) {  
		case 1:  
			type = BufferedImage.TYPE_BYTE_GRAY;  
			break;  
		case 3:  
			type = BufferedImage.TYPE_3BYTE_BGR;  
			// bgr to rgb  
			byte b;  
			for(int i=0; i<data.length; i=i+3) {  
				b = data[i];  
				data[i] = data[i+2];  
				data[i+2] = b;  
			}  
			break;  
		default:  
			return null;  
		}
		BufferedImage image2 = new BufferedImage(cols, rows, type);
		image2.getRaster().setDataElements(0, 0, cols, rows, data);
		return image2;
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
		try
		{
			//�f���p�T�[�o�[�J�n
			InfoClerkTcpServer imgServer = new InfoClerkTcpServer(null,10020,512,"utf-8");
			byte[] img = imgServer.getImageBytes(ImageIO.read(new File("C:\\DEV\\17.InformationClerk\\80.sandbox\\org.jpg")),"jpeg");
			imgServer.setSendData(img);
			DataPacket p = new DataPacket(DataPacket.TYPE_SENDIMG, img);
			System.out.println(p.toString());
			imgServer.start();
			//�R�}���h�p�T�[�o�[�J�n
			InfoClerkTcpServer cmdServer = new InfoClerkTcpServer(null,10021,512,"utf-8");
			cmdServer.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
