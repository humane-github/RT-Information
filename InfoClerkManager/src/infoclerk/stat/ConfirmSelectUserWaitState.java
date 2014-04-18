package infoclerk.stat;

import infoclerk.InfoClerkManagerImpl;
import infoclerk.VoiceDataInfo;
import infoclerk.chat.ChatSender;
import infoclerk.user.UserInfo;
import jp.co.humane.msg.Msg;
import jp.co.humane.statemachine.BaseState;
import jp.co.humane.statemachine.BaseWorker;
import jp.co.humane.statemachine.StateMessage;

public class ConfirmSelectUserWaitState implements BaseState
{
	private ChatSender m_chatClient = null;
	private static BaseState m_self = null;
	public static BaseState Instance()
	{
		if( m_self == null ){m_self = new ConfirmSelectUserWaitState();}
		return m_self;
	}
	
	private InfoClerkManagerState m_owner = null;
	
	@Override
	public void entry(BaseWorker worker)
	{
		m_owner = ((InfoClerkManagerState)worker);
		m_owner.log("ConfirmSelectUserWait entry");
		m_owner.log("ConfirmSelectUserWait timeout(ms)="+InfoClerkManagerImpl.Config().getInt("CMN_CONFIRMSELECTUSERWAIT_TIMEOUT"));
		m_chatClient = new ChatSender("",
				InfoClerkManagerImpl.Config().getInt("CHAT_SENDPORT"),
				InfoClerkManagerImpl.Config().getString("CHAT_ENCODING"),
				InfoClerkManagerImpl.Config().getInt("CHAT_PACKETSIZE"));
		worker.StateMachine().setTimeoutTimer(InfoClerkManagerImpl.Config().getInt("CMN_CONFIRMSELECTUSERWAIT_TIMEOUT"));
		m_owner.log("ConfirmSelectUserWait entry end");
	}

	@Override
	public void exit(BaseWorker worker)
	{
		m_owner.log("ConfirmSelectUserWait exit");
		worker.StateMachine().setTimeoutTimer(-1);
		m_owner = null;
	}

	@Override
	public void exec(BaseWorker worker)
	{
		if( m_owner.noticeUsernamIsNew() )
		{
			m_owner.log("ConfirmSelectUserWait exec");
			UserInfo userInfo = ((InfoClerkManagerState)worker).NoticeUserInfo();
			m_owner.log("ConfirmSelectUserWait exec:"+userInfo.username());
			m_owner.noticeUsernameRead();
			VoiceDataInfo voiceResInfo = new VoiceDataInfo(m_owner.getNoticeUsername());
			m_owner.log("ConfirmSelectUserWait exec:"+voiceResInfo.voiceData);
			if( voiceResInfo.classId.equals(InfoClerkManagerImpl.Config().getString("VOICE_CONFIRM_YES")) &&
				voiceResInfo.score > InfoClerkManagerImpl.Config().getFloat("VOICE_AVAILABLE_SCORE"))
			{
				//通知先へ映像とメッセージを送信する
				//送信用クライアント生成
				m_chatClient.setIpaddress(userInfo.ipaddress());
				//メッセージ送信
				m_chatClient.send(Msg.get("0015"));
				
				m_owner.log(String.format("%s(%s)さんへ来客通知を行います", userInfo.username(),userInfo.ipaddress()));
//				m_owner.writeIPAddress(userInfo.ipaddress());
//				m_owner.writePort(InfoClerkManagerImpl.Config().getInt("CHAT_PORT"));
//				m_owner.writeMessage(Msg.get("0015"));
//				m_owner.writeChatsenderWakeupSignal(true);
				m_owner.speech(Msg.get("0020"));
				worker.StateMachine().changeState(UserResponseWaitState.Instance());
			}
			else if( voiceResInfo.score <= InfoClerkManagerImpl.Config().getFloat("VOICE_AVAILABLE_SCORE") )
			{
				m_owner.speech(Msg.get("0850"));
			}
			else
			{
				m_owner.speech(Msg.get("0025"));
				worker.StateMachine().changeState(UsernameCallbackWaitJuliusState.Instance());
			}			
		}
	}
	
	public void timeout(BaseWorker worker)
	{
		if( m_owner != null )
		{
			m_owner.log("ConfirmSelectUserWait timeout");
			worker.StateMachine().changeState(MotionDetectState.Instance());			
		}
	}

	@Override
	public boolean onMessage(BaseWorker owner, StateMessage msg)
	{
		return true;
	}
}
