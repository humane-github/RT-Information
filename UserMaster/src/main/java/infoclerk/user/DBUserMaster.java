package infoclerk.user;

import java.util.List;

import org.apache.commons.dbutils.handlers.ScalarHandler;

import jp.co.humane.dbutil.commonsIF.DBAccessor;
import jp.co.humane.dbutil.commonsIF.Database;
import jp.co.humane.dbutil.exception.DBUtilException;
import jp.co.humane.logger.Logger;

public class DBUserMaster implements UserMaster
{
	//DB�z�X�g��
	private String m_host = null;
	//DB��
	private String m_dbname = null;
	//DB���[�U�[��
	private String m_username = null;
	//���O�C���p�X���[�h
	private String m_password = null;
	//DB����I�u�W�F�N�g
	private Database m_db = null;
	//���O�o�͗p
	private Logger m_Logger = null;
	/**
	 * �R���X�g���N�^
	 * 
	 * @param	host		DB�z�X�g��
	 * @param	dbname		DB��
	 * @param	username	���[�U�[��
	 * @param	password	�p�X���[�h
	 * **/
	public DBUserMaster(String host,String dbname,String username,String password)
	{
		//DB�ڑ�����ݒ�
		m_host = host;
		m_dbname = dbname;
		m_username = username;
		m_password = password;
		//������
		initialize();
	}
	
	/**
	 * ����������
	 * **/
	public int initialize()
	{
		int res = 0;
		try
		{
			m_db = new Database();
			m_db.login(m_host, m_dbname, m_username, m_password);
			m_Logger = Logger.create();
		}
		catch( DBUtilException ex )
		{
			ex.printStackTrace();
			res = -3;
		}
		return res;
	}
	
	/**
	 * �I������
	 * **/
	public int terminate()
	{
		m_db.logout();
		return 0;
	}
	
	/**
	 * ���[�U�[�����擾����
	 * 
	 * @param	key	���[�U�[ID
	 * **/
	public UserInfo[] get(String key)
	{
		DBAccessor ac = m_db.getAccessor();
		UserInfo[] userinfolist = null;
		String sql = "select * from usermaster ";
		String where = "where name ";
		String whereEquals = "=?";
		String whereLike = "like '%"+key+"%'";
		try
		{
			//�g�����U�N�V�����J�n
			ac.beginTransaction();
			//SQL����
			if( key != null )
			{
				sql += where + whereLike;				
			}
			m_Logger.trace(sql);
			//SQL���s
			List<UserMasterBean> beans = ac.executeQuery(sql,null, UserMasterBean.class);
			//���ʎ擾
			userinfolist = new UserInfo[beans.size()];
			for( int i=0 ; i < userinfolist.length ; i++ )
			{
				UserMasterBean b = beans.get(i);
				userinfolist[i] = new UserInfo(b.getName(),b.getIpaddress());
			}			
		}
		catch (DBUtilException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try {
				ac.finishTransaction();
			} catch (DBUtilException e) {
				e.printStackTrace();
			}
		}
		return userinfolist;
	}
	
	/**
	 * �S���[�U�[���擾����
	 * **/
	public UserInfo[] getAll()
	{
		return get(null);
	}
	
	/**
	 * ���[�U�[�����擾����
	 * **/
	public int size()
	{
		int result = 0;
		DBAccessor ac = m_db.getAccessor();
		String sql = "select count(*) from usermaster";
		try
		{
			//�g�����U�N�V�����J�n
			ac.beginTransaction();
			//SQL���s
			 long res = (long)ac.executeQuery(sql,null, new ScalarHandler(1));
			 result = (int)res;
		}
		catch (DBUtilException e)
		{
			m_Logger.trace("SQL�̎��s�Ɏ��s���܂����F"+sql);
			e.printStackTrace();
		}
		finally
		{
			try {
				ac.finishTransaction();
			} catch (DBUtilException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static void main(String[] args )
	{
		DBUserMaster m = new DBUserMaster("localhost", "InfoClerk", "postgres", "root");
		m.initialize();
		UserInfo[] res = m.get("���");
		for( UserInfo u : res )
		{
			System.out.println(String.format("username=%s password=%s", u.username(),u.ipaddress()));
		}
	}
}
