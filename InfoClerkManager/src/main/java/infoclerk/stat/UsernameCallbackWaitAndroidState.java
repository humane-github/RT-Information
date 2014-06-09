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
		//形態素解析エンジンの初期化
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
		//RTM on Androidのコンポーネントに音声認識開始信号を送信する
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
			//通知ユーザー情報を取得し解析する
			m_owner.androidVoiceRecognitionInRead();
			String voice = m_owner.getAndroidVoiceRecognitionIn();
			//UTF-16LEの文字コードを文字列に変換する
			voice = hex2String(voice);
			m_owner.log("UsernameCallbackWaitAndroidState conv="+voice);
			//形態素解析を行って人名を抜き出す
			String username = username(m_morphemeEngine.parse(voice));
			
			//ユーザー名からユーザー情報を取得
    		UserInfo[] userInfolist = m_owner.getUserInfo(username);
    		//未登録ユーザーなのでエラー
    		if( userInfolist == null || userInfolist.length < 1 || userInfolist[0] == null )
    		{
    			m_owner.log(String.format("user:%s not found",username));
    			m_owner.speech(Msg.get("0025"));
    		}
    		//ユーザー名をもう一度確認
    		else
    		{
    			//音声認識を停止する
    			//m_owner.writeSpeechRecogWakeupSignal("false");
    			//ユーザーに通知
				m_owner.log(String.format("%s(%s)さんへ来客通知を行います", userInfolist[0].username(),userInfolist[0].ipaddress()));
				m_owner.speech(Msg.get("0020"));
				worker.StateMachine().changeState(UserResponseWaitState.Instance());
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
	 * アンドロイドから取得したUTF-16LEの文字コードを文字列に変換する
	 * 
	 * @param	val	UTF-16LEの文字コード
	 * @return
	 * **/
	private String hex2String(String val)
	{
		ArrayList<String> recvdata = new ArrayList<String>(); 
		StringBuffer buff = new StringBuffer();
		//リトルエンディアンの文字コードをビッグエンディアンに変換
		for( int i=0; i<val.length();i+=4)
		{
			String token = val.substring(i,i+4);
			buff.append(token.charAt(2)).append(token.charAt(3)).append(token.charAt(0)).append(token.charAt(1));
			recvdata.add(buff.toString());
			buff.delete(0, buff.length());
		}
		//文字コードから文字列化
		String[] recvdataArray = new String[recvdata.size()];
		recvdataArray = (String[])recvdata.toArray(recvdataArray);    		
		String convdata = HCharEncoder.toString(recvdataArray);
		System.out.println("convert data:"+convdata);
		return convdata;
	}
	/**
	 * 形態素解析を行い文章中から名詞を抜き出す
	 * **/
	private String username(List<Morpheme> tokens)
	{
		String sei = "";
		String mei = "";
		for( Morpheme token : tokens )
		{
			String[] features = token.Features();
			if( features == null || features.length < 4 ){continue;}
			if( features[0].equals("名詞") && features[1].equals("固有名詞") && features[2].equals("人名"))
			{
				if( features[3].equals("姓") ){sei = token.Surface();}
				if( features[3].equals("名") ){mei = token.Surface();}
			}
		}
		return sei + mei;
	}
	
}
