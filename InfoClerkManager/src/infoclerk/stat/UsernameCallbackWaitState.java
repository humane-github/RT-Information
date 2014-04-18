package infoclerk.stat;

import infoclerk.InfoClerkManagerImpl;
import jp.co.humane.statemachine.BaseState;

public abstract class UsernameCallbackWaitState implements BaseState
{
	private static String m_currentEngineName = null;
	protected static BaseState m_self = null;
	protected InfoClerkManagerState m_owner = null;
	public static BaseState Instance()
	{
		String engine = InfoClerkManagerImpl.Config().getString("VOICE_RECOGNITION_ENGINE");
		if( m_self != null && engine.equals(m_currentEngineName) ){return m_self;}
		m_currentEngineName = engine;
		
		if( engine.equals("ANDROID"))
		{
			m_self = new UsernameCallbackWaitAndroidState();
		}
		else
		{
			m_self = new UsernameCallbackWaitJuliusState();			
		}
		return m_self;
	}
}
