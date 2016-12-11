package infoclerk.user;

import jp.co.humane.logger.Logger;

public class UserMasterFactory
{
	public static enum TYPE
	{
		TEXT,
		DB
	}
	
	/**
	 * UserMasterオブジェクトを生成する
	 * 
	 * @param	type	UserMasterの種類
	 * @param	args	UserMasterオブジェクトの生成に必要な初期パラメータ
	 * **/
	public static UserMaster create(TYPE type,UserMasterArgs arg )
	{
		Logger log = Logger.create();
		String format = "Read UserMaster type=%s,path=%s,dhost=%s,dbname=%s,dbuser=%s,dbpassword=%s";
		log.trace(String.format(format,type.name(),arg.getFilepath(),arg.getDbhostname(),arg.getDbname(),arg.getDbusername(),arg.getDbpassword()));
		
		UserMaster result = null;
		switch(type)
		{
		case TEXT:
			log.trace("TEXTのUserMaster読み込み開始");
			result = new TextUserMaster(arg.getFilepath());
			break;
		case DB:
			log.trace("DBのUserMaster読み込み開始");
			result = new DBUserMaster(arg.getDbhostname(),
										arg.getDbname(),
										arg.getDbusername(),
										arg.getDbpassword());
		}
		log.trace("UserMaster読み込み完了");
		return result;
	}
}
