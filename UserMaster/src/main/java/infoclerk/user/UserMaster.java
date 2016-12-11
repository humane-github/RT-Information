package infoclerk.user;


public interface UserMaster
{
	/**
	 * ユーザー情報を取得する
	 * 
	 * @param	key	ユーザー名
	 * **/
	public UserInfo[] get(String key);
	/**
	 * 全ユーザーを取得する
	 * **/
	public UserInfo[] getAll();
	/**
	 * ユーザー数を取得する
	 * **/
	public int size();
	/**
	 * 初期化処理
	 * 
	 * @return	int	エラーコード
	 * **/
	public int initialize();
	
	/**
	 * 終了処理
	 * 
	 * @return	int	エラーコード
	 * **/
	public int terminate();
}
