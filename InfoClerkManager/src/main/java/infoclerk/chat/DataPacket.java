package infoclerk.chat;

import java.nio.charset.Charset;

import jp.co.humane.hcharencoder.HCharEncoder;

public class DataPacket
{
    //ヘッダの長さ
    private final static int HEADER_LENGTH = 10;
	private final static int TYPE_SIZE = 3;
	private final static int LENGTH_SIZE = 4;
	
	private Charset m_charset = Charset.forName("ascii");
	/**
	 * Headerの仕様
	 * [0]:種別
	 * [1-4]:電文長
	 * [5-10]:予備
	 * **/
	private byte[] m_data = null;
	private byte[] m_header = null;
	private byte[] m_body = null;
	
	public static byte[] TYPE_GETIMG = new byte[]{0x41,0x30,0x31};//A01
	public static byte[] TYPE_SENDIMG = new byte[]{0x41,0x30,0x32};//A02
	public static byte[] TYPE_GETPROPERTY = new byte[]{0x42,0x30,0x31};//B01
	public static byte[] TYPE_SENDPROPERTY = new byte[]{0x42,0x30,0x32};//B02
    public static byte[] TYPE_SENDMSG = new byte[] { 0x43, 0x30, 0x31 };//C01
	private static byte RESERVE = 0x20;
		
	private static byte ACK = 0x06;
	private static byte NAK = 0x15;
	private static byte STX = 0x02;
	private static byte ETX = 0x03;

	/**
	 * コンストラクタ
	 * **/
    public DataPacket(byte[] type,byte[] body)
    {
        //初期化
        int idx = 0;
        m_data = new byte[HEADER_LENGTH+body.length];
        for (int i = 0; i < m_data.length; i++)
        {
            m_data[i] = RESERVE;
        }
        //ヘッダー生成
        for (int i = 0; i < type.length; i++)
        {
            m_data[i] = type[i];
            idx++;
        }
        byte[] lenByteArray = HCharEncoder.int2byte(body.length);
        for (int i = 0; i < lenByteArray.length; i++)
        {
            m_data[idx + i] = lenByteArray[i];
        }
        //ボディ生成
        for (int i = 0; i < body.length; i++)
        {
            m_data[i + HEADER_LENGTH] = body[i];
        }
    }
    
    /**
     * コンストラクタ
     * **/
    public DataPacket(byte[] data)
    {
    	if( data == null || data.length < 1 ){return;}
        //ヘッダ部抽出
        m_header = new byte[HEADER_LENGTH];
        m_data = data;
        for (int i = 0; i < HEADER_LENGTH; i++)
        {
            m_header[i] = m_data[i];
        }
        //ボディ部抽出
        m_body = new byte[m_data.length - HEADER_LENGTH];
        for (int i = HEADER_LENGTH; i < m_data.length; i++)
        {
            m_body[i-HEADER_LENGTH] = m_data[i];
        }
    }
    
	public boolean isGetPROPERTY()
	{
		if( m_data == null || m_data.length < 3 ){return false;}
		return (m_data[0] == TYPE_GETPROPERTY[0] &&
				m_data[1] == TYPE_GETPROPERTY[1] &&
				m_data[2] == TYPE_GETPROPERTY[2]);
	}
	
	public boolean isSendPROPERTY()
	{
		if( m_data == null || m_data.length < 3 ){return false;}
		return (m_data[0] == TYPE_SENDPROPERTY[0] &&
				m_data[1] == TYPE_SENDPROPERTY[1] &&
				m_data[2] == TYPE_SENDPROPERTY[2]);
	}
	
	public boolean isGetIMG()
	{
		if( m_data == null || m_data.length < 3 ){return false;}
		return (m_data[0] == TYPE_GETIMG[0] &&
				m_data[1] == TYPE_GETIMG[1] &&
				m_data[2] == TYPE_GETIMG[2]);
	}
	
	public boolean isSendIMG()
	{
		if( m_data == null || m_data.length < 3 ){return false;}
		return (m_data[0] == TYPE_SENDIMG[0] &&
				m_data[1] == TYPE_SENDIMG[1] &&
				m_data[2] == TYPE_SENDIMG[2]);
	}
	
    public boolean isSendMSG()
    {
		if( m_data == null || m_data.length < 3 ){return false;}
        return (m_data[0] == TYPE_SENDMSG[0] &&
        		m_data[1] == TYPE_SENDMSG[1] &&
        		m_data[2] == TYPE_SENDMSG[2]);
    }

    public byte[] getBody()
    {
    	return m_body;
    }
    
	public byte[] toByteArray()
	{
		return m_data;
	}
	
	public int Length()
	{
		if( m_data == null ){return 0;}
		return m_data.length;
	}
	
	/**
	 * パケットの文字列化
	 * **/
	public String toString()
	{
		if( m_data == null ){return "packet is null";}
		StringBuffer buff = new StringBuffer();
		buff.append("[");
		for(byte b : m_data)
		{			
			buff.append(b);			
		}
		buff.append("]");
		return buff.toString();
	}
}
