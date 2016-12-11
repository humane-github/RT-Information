package infoclerk.stat;

import infoclerk.InfoClerkManagerImpl;
import infoclerk.chat.ChatReciver;
import infoclerk.chat.ChatSender;
import infoclerk.user.UserInfo;
import jp.co.humane.statemachine.BaseState;
import jp.co.humane.statemachine.BaseWorker;
import jp.co.humane.statemachine.StateMessage;

public class UserResponseWaitState implements BaseState
{
	private ChatSender m_chatSender = null;
	private ChatReciver m_chatRecver = null;
	private static BaseState m_self = null;
	public static BaseState Instance()
	{
		if( m_self == null ){m_self = new UserResponseWaitState();}
		return m_self;
	}
	
	private InfoClerkManagerState m_owner = null;
	
	@Override
	public void entry(BaseWorker worker)
	{
		m_owner = (InfoClerkManagerState)worker;
		//送信用クライアント生成
		m_chatSender = new ChatSender("",
				InfoClerkManagerImpl.Config().getInt("CHAT_SENDIMAGEPORT"),
				InfoClerkManagerImpl.Config().getString("CHAT_ENCODING"),
				InfoClerkManagerImpl.Config().getInt("CHAT_PACKETSIZE"));
		//受信用クライアント生成
		m_chatRecver = new ChatReciver(InfoClerkManagerImpl.Config().getInt("CHAT_RECVPORT"),
										InfoClerkManagerImpl.Config().getString("CHAT_ENCODING"),
										InfoClerkManagerImpl.Config().getInt("CHAT_PACKETSIZE"));
		m_owner.log("UserResponseWait entry");
	}

	@Override
	public void exit(BaseWorker worker)
	{
		m_owner.log("UserResponseWait exit");
		m_chatRecver.close();
		m_owner = null;
	}

	@Override
	public void exec(BaseWorker worker)
	{
		if( m_owner == null ){return;}	
		String recvData = null;
		if( (recvData = m_chatRecver.recv()) != null )
		{
			m_owner.log("\t>>"+recvData);
			m_owner.speech(recvData);
			m_owner.reset();
			m_chatRecver.close();
		}
	}
	
	public void timeout(BaseWorker worker)
	{		
	}

	@Override
	public boolean onMessage(BaseWorker owner, StateMessage msg)
	{
		return true;
	}
}
