package infoclerk.user;


public interface UserMaster
{
	/**
	 * ���[�U�[�����擾����
	 * 
	 * @param	key	���[�U�[��
	 * **/
	public UserInfo[] get(String key);
	/**
	 * �S���[�U�[���擾����
	 * **/
	public UserInfo[] getAll();
	/**
	 * ���[�U�[�����擾����
	 * **/
	public int size();
	/**
	 * ����������
	 * 
	 * @return	int	�G���[�R�[�h
	 * **/
	public int initialize();
	
	/**
	 * �I������
	 * 
	 * @return	int	�G���[�R�[�h
	 * **/
	public int terminate();
}
