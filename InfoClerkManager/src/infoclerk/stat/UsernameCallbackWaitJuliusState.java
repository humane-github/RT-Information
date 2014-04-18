package infoclerk.stat;

import infoclerk.InfoClerkManagerImpl;
import infoclerk.VoiceDataInfo;
import infoclerk.user.UserInfo;
import jp.co.humane.msg.Msg;
import jp.co.humane.statemachine.BaseWorker;
import jp.co.humane.statemachine.StateMessage;

public class UsernameCallbackWaitJuliusState extends UsernameCallbackWaitState
{
	
	@Override
	public void entry(BaseWorker worker)
	{
		m_owner = ((InfoClerkManagerState)worker);
		m_owner.log("UsernameCallbackWaitJuliusState entry");
		worker.StateMachine().setTimeoutTimer(InfoClerkManagerImpl.Config().getInt("CMN_SELECTUSERWAIT_TIMEOUT"));
	}

	@Override
	public void exit(BaseWorker worker)
	{
		m_owner.log("UsernameCallbackWaitJuliusState exit");
		worker.StateMachine().setTimeoutTimer(-1);
		m_owner = null;
	}

	@Override
	public void exec(BaseWorker worker)
	{
		if( m_owner.noticeUsernamIsNew() )
		{
			m_owner.log("UsernameCallbackWaitJuliusState exec");
			//通知ユーザー情報を取得し解析する
			m_owner.noticeUsernameRead();
    		VoiceDataInfo voiceInfo = new VoiceDataInfo(m_owner.getNoticeUsername());
    		//ユーザー名の通知か判定する
    		if( voiceInfo.classId.equals(InfoClerkManagerImpl.Config().getString("VOICE_NAME_CLASSID")) &&
    			voiceInfo.score > InfoClerkManagerImpl.Config().getFloat("VOICE_AVAILABLE_SCORE")	)
    		{
    			//ユーザー名からユーザー情報を取得
        		UserInfo[] userInfolist = m_owner.getUserInfo(voiceInfo.voiceData);
        		//未登録ユーザーなのでエラー
        		if( userInfolist == null || userInfolist.length < 1 || userInfolist[0] == null )
        		{
        			m_owner.log(String.format("user:%s is not already exists",m_owner.getNoticeUsername()));
        			m_owner.speech(Msg.get("0025"));
        		}
        		//ユーザー名をもう一度確認
        		else
        		{
        			m_owner.speech(Msg.get("0030", new Object[]{userInfolist[0].username()}));
        			((InfoClerkManagerState)worker).setNoticeUserInfo(userInfolist[0]);
        			worker.StateMachine().changeState(ConfirmSelectUserWaitState.Instance());
        		}    			
    		}
    		//ユーザー名の通知ではないので、再度入力待ち
    		else
    		{
    			m_owner.log(String.format("user:%s is not already exists",m_owner.getNoticeUsername()));
    			m_owner.speech(Msg.get("0025"));
    		}
		}
	}
	
 
	/**
	 * タイムアウト時の処理
	 * **/
	public void timeout(BaseWorker worker)
	{
		if( m_owner != null )
		{
			m_owner.log("UsernameCallbackWaitJuliusState timeout");
			worker.StateMachine().changeState(MotionDetectState.Instance());			
		}
	}

	@Override
	public boolean onMessage(BaseWorker owner, StateMessage msg)
	{
		return true;
	}
}
