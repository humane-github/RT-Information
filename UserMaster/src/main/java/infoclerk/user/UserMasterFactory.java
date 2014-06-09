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
	 * UserMaster�I�u�W�F�N�g�𐶐�����
	 * 
	 * @param	type	UserMaster�̎��
	 * @param	args	UserMaster�I�u�W�F�N�g�̐����ɕK�v�ȏ����p�����[�^
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
			log.trace("TEXT��UserMaster�ǂݍ��݊J�n");
			result = new TextUserMaster(arg.getFilepath());
			break;
		case DB:
			log.trace("DB��UserMaster�ǂݍ��݊J�n");
			result = new DBUserMaster(arg.getDbhostname(),
										arg.getDbname(),
										arg.getDbusername(),
										arg.getDbpassword());
		}
		log.trace("UserMaster�ǂݍ��݊���");
		return result;
	}
}
