package infoclerk.user;

public class UserMasterArgs
{
	private String filepath = null;
	private String dbhostname = null;
	private String dbname = null;
	private String dbusername = null;
	private String dbpassword = null;
	
	public UserMasterArgs(String filepath, String dbhostname,
			String dbname, String dbusername, String dbpassword) {
		super();
		this.filepath = filepath;
		this.dbhostname = dbhostname;
		this.dbname = dbname;
		this.dbusername = dbusername;
		this.dbpassword = dbpassword;
	}
	public String getFilepath() {
		return filepath;
	}
	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
	public String getDbhostname() {
		return dbhostname;
	}
	public void setDbhostname(String dbhostname) {
		this.dbhostname = dbhostname;
	}
	public String getDbname() {
		return dbname;
	}
	public void setDbname(String dbname) {
		this.dbname = dbname;
	}
	public String getDbusername() {
		return dbusername;
	}
	public void setDbusername(String dbusername) {
		this.dbusername = dbusername;
	}
	public String getDbpassword() {
		return dbpassword;
	}
	public void setDbpassword(String dbpassword) {
		this.dbpassword = dbpassword;
	}
	
}