package infoclerk.stat;
import infoclerk.InfoClerkManagerImpl;
import infoclerk.user.UserInfo;
import jp.co.humane.configlib.ConfigFile;
import jp.co.humane.statemachine.BaseWorker;


public class InfoClerkManagerState extends BaseWorker
{
	private InfoClerkManagerImpl m_owner = null;
	private UserInfo m_noticeUserInfo = null;
	
	/**
	 * �R���X�g���N�^
	 * **/
	public InfoClerkManagerState(InfoClerkManagerImpl o)
	{
		super();
		m_owner = o;
	}
	
	
	
	/**
	 * �����������s��
	 * 
	 * @param	data	�����������s��������
	 * **/
	public void speech(String data)
	{
		Owner().speech(data);
	}
	
	/**
	 * ���O�̏o�͗p
	 * 
	 * @param	log	���O�o�͕�����
	 * **/	
	public void log(String log)
	{
		Owner().log(log);
	}
	
	/**
	 * ���[�U�[�����擾����
	 * 
	 * @param	name		���[�U�[��
	 * @return	UserInfo[]	���[�U�[���
	 * **/
	public UserInfo[] getUserInfo(String name)
	{
		return InfoClerkManagerImpl.UserMaster().get(name);
	}
		
	public void reset()
	{
		Owner().reset();
	}
	
	/**
	 * RT�R���|�[�l���g��IN�|�[�g�A�N�Z�T
	 * **/	
	public boolean noticeUsernamIsNew()
	{
		return Owner().m_juliusVoiceRecognitionIn.isNew();
	}
	public void noticeUsernameRead()
	{
		Owner().m_juliusVoiceRecognitionIn.read();
	}
	public String getNoticeUsername()
	{
		return Owner().m_juliusVoiceRecognition.v.data;
	}
	public void noticeUsernameClear()
	{
		Owner().portClear(Owner().m_juliusVoiceRecognitionIn);
	}
	public boolean facesIsNew()
	{
		return Owner().m_FacesIn.isNew();
	}
	public void facesRead()
	{
		Owner().m_FacesIn.read();
	}
	public int getFaces()
	{
		return Owner().m_Faces.v.data;
	}
	public boolean detectMotionIsNew()
	{
		return Owner().m_detectMotionIn.isNew();
	}
	public void detectMotionRead()
	{
		Owner().m_detectMotionIn.read();
	}
	public boolean getDetectMotion()
	{
		return Owner().m_detectMotion.v.data;
	}
	public boolean androidVoiceRecognitionIsNew()
	{
		return Owner().m_androidVoiceRecognitionIn.isNew();
	}
	public void androidVoiceRecognitionInRead()
	{
		Owner().m_androidVoiceRecognitionIn.read();
	}
	public String getAndroidVoiceRecognitionIn()
	{
		return Owner().m_androidVoiceRecognition.v.data;
	}

	/**
	 * RT�R���|�[�l���g��OUT�|�[�g�A�N�Z�T
	 * **/
	public void writeSpeech(String data)
	{
		Owner().m_speechOut.v.data = data;
		Owner().m_speechOutOut.write();
	}
	public void writeWakeupSignal(boolean data)
	{
		Owner().m_faceDetectWakeupSignal.v.data = data;
		Owner().m_faceDetectWakeupSignalOut.write();
	}
	public void writeMotionDetectWakeupSignal(boolean data)
	{
		Owner().m_motionDetectWakeupSignal.v.data = data;
		Owner().m_motionDetectWakeupSignalOut.write();
	}
	
	/**
	 * Setter
	 * **/
	public void setNoticeUserInfo(UserInfo u){m_noticeUserInfo = u;}
	/**
	 * Getter
	 * **/
	public UserInfo NoticeUserInfo(){return m_noticeUserInfo;}	
	public InfoClerkManagerImpl Owner(){return m_owner;}
}
