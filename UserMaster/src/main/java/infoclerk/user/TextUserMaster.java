package infoclerk.user;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import jp.co.humane.io.TextFileReader;

public class TextUserMaster implements UserMaster
{
	private String m_path = null;
	private HashMap<String,UserInfo> m_userMaster = new HashMap<String,UserInfo>();
	
	/**
	 * コンストラクタ
	 * 
	 * @param	path	ユーザー情報ファイルのパス
	 * **/
	public TextUserMaster(String path)
	{
		m_path = path;
		initialize();
	}
	
	/**
	 * ユーザー情報を取得する
	 * 
	 * @param	key	ユーザー名
	 * **/
	public UserInfo[] get(String key)
	{
		return new UserInfo[]{m_userMaster.get(key)};
	}
	
	/**
	 * 全ユーザーを取得する
	 * **/
	public UserInfo[] getAll()
	{
		UserInfo[] result = new UserInfo[m_userMaster.size()];
		result = (UserInfo[])m_userMaster.values().toArray(result);
		return result;
	}
	
	/**
	 * ユーザー数を取得する
	 * **/
	public int size()
	{
		return m_userMaster.size();
	}
	
	/**
	 * ユーザー情報ファイルの読み込み
	 * **/
	public int initialize()
	{
		int res = 0;
		TextFileReader file = new TextFileReader(m_path);
		try {
			file.open();
			String line = null;
			while((line = file.read()) != null )
			{
				String[] tokens = line.split(",");
				if( tokens == null || tokens.length != 2 ){continue;}
				m_userMaster.put(tokens[0],new UserInfo( tokens[0], tokens[1]));				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			res = -1;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			res = -2;
		}
		return res;
	}
	
	/**
	 * 終了処理
	 * **/
	public int terminate()
	{
		m_userMaster = new HashMap<String,UserInfo>();
		return 0;
	}
}
