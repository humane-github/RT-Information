package infoclerk.stat;

import infoclerk.InfoClerkManagerImpl;
import infoclerk.user.UserInfo;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import jp.co.humane.hcharencoder.HCharEncoder;
import jp.co.humane.morpheme.Morpheme;
import jp.co.humane.morpheme.MorphemeEngine;
import jp.co.humane.morpheme.MorphemeEngineException;
import jp.co.humane.morpheme.MorphemeEngineFactory;
import jp.co.humane.msg.Msg;
import jp.co.humane.statemachine.BaseWorker;
import jp.co.humane.statemachine.StateMessage;

public class UsernameCallbackWaitAndroidState extends UsernameCallbackWaitState
{
	private MorphemeEngine m_morphemeEngine = null;
	private boolean m_halt = false;
	
	@Override
	public void entry(BaseWorker worker)
	{
		m_owner = ((InfoClerkManagerState)worker);
		m_owner.log("UsernameCallbackWaitAndroidState entry");
		worker.StateMachine().setTimeoutTimer(InfoClerkManagerImpl.Config().getInt("CMN_SELECTUSERWAIT_TIMEOUT"));
		//�`�ԑf��̓G���W���̏�����
		try
		{
			m_morphemeEngine = MorphemeEngineFactory.create(MorphemeEngineFactory.TYPE.IGO,InfoClerkManagerImpl.Config().getString("MORPHEME_IPADIC"));			
		}
		catch( FileNotFoundException e1 )
		{
			m_owner.log(e1.getMessage());
			m_halt = true;
		}
		catch( MorphemeEngineException e2 )
		{
			m_owner.log(e2.getMessage());
			m_halt = true;
		}
		//RTM on Android�̃R���|�[�l���g�ɉ����F���J�n�M���𑗐M����
		//m_owner.writeSpeechRecogWakeupSignal("true");
	}

	@Override
	public void exit(BaseWorker worker)
	{
		m_owner.log("UsernameCallbackWaitAndroidState exit");
		worker.StateMachine().setTimeoutTimer(-1);
		m_owner = null;
	}

	@Override
	public void exec(BaseWorker worker)
	{
		if( !m_halt && m_owner.androidVoiceRecognitionIsNew() )
		{
			m_owner.log("UsernameCallbackWaitAndroidState exec");
			//�ʒm���[�U�[�����擾����͂���
			m_owner.androidVoiceRecognitionInRead();
			String voice = m_owner.getAndroidVoiceRecognitionIn();
			//UTF-16LE�̕����R�[�h�𕶎���ɕϊ�����
			voice = hex2String(voice);
			m_owner.log("UsernameCallbackWaitAndroidState conv="+voice);
			//�`�ԑf��͂��s���Đl���𔲂��o��
			String username = username(m_morphemeEngine.parse(voice));
			
			//���[�U�[�����烆�[�U�[�����擾
    		UserInfo[] userInfolist = m_owner.getUserInfo(username);
    		//���o�^���[�U�[�Ȃ̂ŃG���[
    		if( userInfolist == null || userInfolist.length < 1 || userInfolist[0] == null )
    		{
    			m_owner.log(String.format("user:%s not found",username));
    			m_owner.speech(Msg.get("0025"));
    		}
    		//���[�U�[����������x�m�F
    		else
    		{
    			//�����F�����~����
    			//m_owner.writeSpeechRecogWakeupSignal("false");
    			//���[�U�[�ɒʒm
				m_owner.log(String.format("%s(%s)����֗��q�ʒm���s���܂�", userInfolist[0].username(),userInfolist[0].ipaddress()));
				m_owner.speech(Msg.get("0020"));
				worker.StateMachine().changeState(UserResponseWaitState.Instance());
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
			m_owner.log("UsernameCallbackWaitAndroidState timeout");
			worker.StateMachine().changeState(MotionDetectState.Instance());			
		}
	}

	@Override
	public boolean onMessage(BaseWorker owner, StateMessage msg)
	{
		return true;
	}
	
	/**
	 * �A���h���C�h����擾����UTF-16LE�̕����R�[�h�𕶎���ɕϊ�����
	 * 
	 * @param	val	UTF-16LE�̕����R�[�h
	 * @return
	 * **/
	private String hex2String(String val)
	{
		ArrayList<String> recvdata = new ArrayList<String>(); 
		StringBuffer buff = new StringBuffer();
		//���g���G���f�B�A���̕����R�[�h���r�b�O�G���f�B�A���ɕϊ�
		for( int i=0; i<val.length();i+=4)
		{
			String token = val.substring(i,i+4);
			buff.append(token.charAt(2)).append(token.charAt(3)).append(token.charAt(0)).append(token.charAt(1));
			recvdata.add(buff.toString());
			buff.delete(0, buff.length());
		}
		//�����R�[�h���當����
		String[] recvdataArray = new String[recvdata.size()];
		recvdataArray = (String[])recvdata.toArray(recvdataArray);    		
		String convdata = HCharEncoder.toString(recvdataArray);
		System.out.println("convert data:"+convdata);
		return convdata;
	}
	/**
	 * �`�ԑf��͂��s�����͒����疼���𔲂��o��
	 * **/
	private String username(List<Morpheme> tokens)
	{
		String sei = "";
		String mei = "";
		for( Morpheme token : tokens )
		{
			String[] features = token.Features();
			if( features == null || features.length < 4 ){continue;}
			if( features[0].equals("����") && features[1].equals("�ŗL����") && features[2].equals("�l��"))
			{
				if( features[3].equals("��") ){sei = token.Surface();}
				if( features[3].equals("��") ){mei = token.Surface();}
			}
		}
		return sei + mei;
	}
	
}
