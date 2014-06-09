package infoclerk.user;

public class UserInfo
{

	private String m_username = null;
	private String m_ipaddress = null;
	
	/**
	 * コンストラクタ
	 * 
	 * @param	username	ユーザー名
	 * @param	ipaddress	ユーザーPCのIPアドレス
	 * **/
	public UserInfo(String username,String ipaddress)
	{
		m_username = username;
		m_ipaddress = ipaddress;
	}
	
	public String username(){return m_username;}
	public String ipaddress(){return m_ipaddress;}
}
