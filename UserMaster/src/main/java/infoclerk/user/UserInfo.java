package infoclerk.user;

public class UserInfo
{

	private String m_username = null;
	private String m_ipaddress = null;
	
	/**
	 * �R���X�g���N�^
	 * 
	 * @param	username	���[�U�[��
	 * @param	ipaddress	���[�U�[PC��IP�A�h���X
	 * **/
	public UserInfo(String username,String ipaddress)
	{
		m_username = username;
		m_ipaddress = ipaddress;
	}
	
	public String username(){return m_username;}
	public String ipaddress(){return m_ipaddress;}
}
