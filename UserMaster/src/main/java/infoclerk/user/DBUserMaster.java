package infoclerk.user;

import java.util.List;

import org.apache.commons.dbutils.handlers.ScalarHandler;

import jp.co.humane.dbutil.commonsIF.DBAccessor;
import jp.co.humane.dbutil.commonsIF.Database;
import jp.co.humane.dbutil.exception.DBUtilException;
import jp.co.humane.logger.Logger;

public class DBUserMaster implements UserMaster
{
	//DBホスト名
	private String m_host = null;
	//DB名
	private String m_dbname = null;
	//DBユーザー名
	private String m_username = null;
	//ログインパスワード
	private String m_password = null;
	//DB操作オブジェクト
	private Database m_db = null;
	//ログ出力用
	private Logger m_Logger = null;
	/**
	 * コンストラクタ
	 * 
	 * @param	host		DBホスト名
	 * @param	dbname		DB名
	 * @param	username	ユーザー名
	 * @param	password	パスワード
	 * **/
	public DBUserMaster(String host,String dbname,String username,String password)
	{
		//DB接続情報を設定
		m_host = host;
		m_dbname = dbname;
		m_username = username;
		m_password = password;
		//初期化
		initialize();
	}
	
	/**
	 * 初期化処理
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
	 * 終了処理
	 * **/
	public int terminate()
	{
		m_db.logout();
		return 0;
	}
	
	/**
	 * ユーザー情報を取得する
	 * 
	 * @param	key	ユーザーID
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
			//トランザクション開始
			ac.beginTransaction();
			//SQL生成
			if( key != null )
			{
				sql += where + whereLike;				
			}
			m_Logger.trace(sql);
			//SQL実行
			List<UserMasterBean> beans = ac.executeQuery(sql,null, UserMasterBean.class);
			//結果取得
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
	 * 全ユーザーを取得する
	 * **/
	public UserInfo[] getAll()
	{
		return get(null);
	}
	
	/**
	 * ユーザー数を取得する
	 * **/
	public int size()
	{
		int result = 0;
		DBAccessor ac = m_db.getAccessor();
		String sql = "select count(*) from usermaster";
		try
		{
			//トランザクション開始
			ac.beginTransaction();
			//SQL実行
			 long res = (long)ac.executeQuery(sql,null, new ScalarHandler(1));
			 result = (int)res;
		}
		catch (DBUtilException e)
		{
			m_Logger.trace("SQLの実行に失敗しました："+sql);
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
		UserInfo[] res = m.get("鈴木");
		for( UserInfo u : res )
		{
			System.out.println(String.format("username=%s password=%s", u.username(),u.ipaddress()));
		}
	}
}
