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
			//�ʒm���[�U�[�����擾����͂���
			m_owner.noticeUsernameRead();
    		VoiceDataInfo voiceInfo = new VoiceDataInfo(m_owner.getNoticeUsername());
    		//���[�U�[���̒ʒm�����肷��
    		if( voiceInfo.classId.equals(InfoClerkManagerImpl.Config().getString("VOICE_NAME_CLASSID")) &&
    			voiceInfo.score > InfoClerkManagerImpl.Config().getFloat("VOICE_AVAILABLE_SCORE")	)
    		{
    			//���[�U�[�����烆�[�U�[�����擾
        		UserInfo[] userInfolist = m_owner.getUserInfo(voiceInfo.voiceData);
        		//���o�^���[�U�[�Ȃ̂ŃG���[
        		if( userInfolist == null || userInfolist.length < 1 || userInfolist[0] == null )
        		{
        			m_owner.log(String.format("user:%s is not already exists",m_owner.getNoticeUsername()));
        			m_owner.speech(Msg.get("0025"));
        		}
        		//���[�U�[����������x�m�F
        		else
        		{
        			m_owner.speech(Msg.get("0030", new Object[]{userInfolist[0].username()}));
        			((InfoClerkManagerState)worker).setNoticeUserInfo(userInfolist[0]);
        			worker.StateMachine().changeState(ConfirmSelectUserWaitState.Instance());
        		}    			
    		}
    		//���[�U�[���̒ʒm�ł͂Ȃ��̂ŁA�ēx���͑҂�
    		else
    		{
    			m_owner.log(String.format("user:%s is not already exists",m_owner.getNoticeUsername()));
    			m_owner.speech(Msg.get("0025"));
    		}
		}
	}
	
 
	/**
	 * �^�C���A�E�g���̏���
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
