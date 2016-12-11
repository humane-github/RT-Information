package infoclerk.stat;

import infoclerk.InfoClerkManagerImpl;
import jp.co.humane.msg.Msg;
import jp.co.humane.statemachine.BaseState;
import jp.co.humane.statemachine.BaseWorker;
import jp.co.humane.statemachine.StateMessage;

public class FaceDetectState implements BaseState
{
	private static BaseState m_self = null;
	public static BaseState Instance()
	{
		if( m_self == null ){m_self = new FaceDetectState();}
		return m_self;
	}
	
	private InfoClerkManagerState m_owner = null;
	
	@Override
	public void entry(BaseWorker worker)
	{
		m_owner = (InfoClerkManagerState)worker;
		m_owner.log("FaceDetect entry");
		m_owner.writeWakeupSignal(true);
		worker.StateMachine().setTimeoutTimer(InfoClerkManagerImpl.Config().getInt("CMN_FACEDETCT_TIMEOUT"));
	}

	@Override
	public void exit(BaseWorker worker)
	{
		m_owner.log("FaceDetect exit");
		m_owner.writeWakeupSignal(false);
		worker.StateMachine().setTimeoutTimer(-1);
		m_owner = null;
	}

	@Override
	public void exec(BaseWorker worker)
	{		
		if( m_owner.facesIsNew() )
		{
			m_owner.log("FaceDetect exec");
			m_owner.facesRead();
			int faces = m_owner.getFaces();
			if( faces > 0 )
			{
				//顔検出以前にマイクから拾っていた音声をクリアする
				m_owner.noticeUsernameClear();
				
				m_owner.log(String.format("%d人の人物を検出しました。", faces));
				m_owner.speech(Msg.get("0010"));
				//m_owner.writeCommandOut(InfoClerkCommands.USER_SELECT.toString());
				worker.StateMachine().changeState(UsernameCallbackWaitState.Instance());
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
			m_owner.log("FaceDetectState timeout");
			worker.StateMachine().changeState(MotionDetectState.Instance());			
		}		
	}

	@Override
	public boolean onMessage(BaseWorker owner, StateMessage msg)
	{
		return true;
	}
}
