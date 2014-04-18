package infoclerk.stat;

import jp.co.humane.statemachine.BaseState;
import jp.co.humane.statemachine.BaseWorker;
import jp.co.humane.statemachine.StateMessage;

public class MotionDetectState implements BaseState
{
	private static BaseState m_self = null;
	public static BaseState Instance()
	{
		if( m_self == null ){m_self = new MotionDetectState();}
		return m_self;
	}
	
	private InfoClerkManagerState m_owner = null;
	
	@Override
	public void entry(BaseWorker worker)
	{
		m_owner = (InfoClerkManagerState)worker;
		m_owner.log("MotionDetect entry");
		m_owner.writeMotionDetectWakeupSignal(true);
	}

	@Override
	public void exit(BaseWorker worker)
	{
		m_owner.log("MotionDetect exit");
		m_owner.writeMotionDetectWakeupSignal(false);
		m_owner = null;
	}

	@Override
	public void exec(BaseWorker worker)
	{
		if( m_owner.detectMotionIsNew() )
		{
			m_owner.log("MotionDetect exec");
			m_owner.detectMotionRead();
			//動体検知
			boolean detected = m_owner.getDetectMotion();
			if( detected )
			{
				m_owner.log(String.format("動体検知しました"));
				worker.StateMachine().changeState(FaceDetectState.Instance());
			}
		}
	}
	
	/**
	 * タイムアウト時の処理
	 * **/
	public void timeout(BaseWorker worker)
	{
		m_owner.log("MotionDetect timeout");
	}

	@Override
	public boolean onMessage(BaseWorker owner, StateMessage msg)
	{
		return true;
	}
}
