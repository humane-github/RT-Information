package infoclerk;
// -*- Java -*-
/*!
 * @file  InfoClerkManagerImpl.java
 * @brief InfoClerkManager
 * @date  $Date$
 *
 * $Id$
 */

import infoclerk.chat.InfoClerkTcpServer;
import infoclerk.stat.InfoClerkManagerState;
import infoclerk.stat.MotionDetectState;
import infoclerk.user.UserMaster;
import infoclerk.user.UserMasterArgs;
import infoclerk.user.UserMasterFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.ParserConfigurationException;

import jp.co.humane.configlib.ConfigFile;
import jp.co.humane.logger.Logger;
import jp.co.humane.opencvlib.OpenCVLib;
import jp.co.humane.xml.rtc.RTCML;
import jp.go.aist.rtm.RTC.DataFlowComponentBase;
import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.port.InPort;
import jp.go.aist.rtm.RTC.port.OutPort;
import jp.go.aist.rtm.RTC.util.DataRef;
import jp.go.aist.rtm.RTC.util.StringHolder;

import org.xml.sax.SAXException;

import RTC.CameraImage;
import RTC.ReturnCode_t;
import RTC.Time;
import RTC.TimedBoolean;
import RTC.TimedLong;
import RTC.TimedString;
import RTC.TimedWString;

/*!
 * @class InfoClerkManagerImpl
 * @brief InfoClerkManager
 *
 */
public class InfoClerkManagerImpl extends DataFlowComponentBase
{
	private static final String COMPONENT_NAME = "InfoClerkManager";
	private Timer m_resetTimer = null;
	private Logger m_logger = null;
	private static UserMaster m_userMaster = null;
	public static UserMaster UserMaster(){return m_userMaster;}
	private static ConfigFile m_config = null;
	public static ConfigFile Config(){return m_config;}
	private InfoClerkManagerState m_stateWorker = null;
	private Thread m_threadCtrlSender = null;
	private InfoClerkTcpServer m_tcpImgServer = null;
	private InfoClerkTcpServer m_tcpCmdServer = null;
	
	//コンフィグレーション取得要求関連
	//キーはコンポーネント名
	private HashMap<String,RTCML> m_componentRtcml = new HashMap<String,RTCML>();
	public HashMap<String,RTCML> getConfigrations(){return m_componentRtcml;}
	
	//Inport
    public TimedLong m_Faces_val;
    public DataRef<TimedLong> m_Faces;
    public InPort<TimedLong> m_FacesIn;
    public CameraImage m_cameraImage_val;
    public DataRef<CameraImage> m_cameraImage;
    public InPort<CameraImage> m_cameraImageIn;   
    public TimedBoolean m_detectMotion_val;
    public DataRef<TimedBoolean> m_detectMotion;
    public InPort<TimedBoolean> m_detectMotionIn;
    public TimedWString m_juliusVoiceRecognition_val;
    public DataRef<TimedWString> m_juliusVoiceRecognition;
    public InPort<TimedWString> m_juliusVoiceRecognitionIn;
    public TimedString m_androidVoiceRecognition_val;
    public DataRef<TimedString> m_androidVoiceRecognition;
    public InPort<TimedString> m_androidVoiceRecognitionIn;
    public TimedWString m_rtcmlIn_val;
    public DataRef<TimedWString> m_rtcmlIn;
    public InPort<TimedWString> m_rtcmlInIn;
    
    //Outport
    public TimedWString m_speechOut_val;
    public DataRef<TimedWString> m_speechOut;
    public OutPort<TimedWString> m_speechOutOut;
    public TimedBoolean m_faceDetectWakeupSignal_val;
    public DataRef<TimedBoolean> m_faceDetectWakeupSignal;
    public OutPort<TimedBoolean> m_faceDetectWakeupSignalOut;
    public TimedBoolean m_motionDetectWakeupSignal_val;
    public DataRef<TimedBoolean> m_motionDetectWakeupSignal;
    public OutPort<TimedBoolean> m_motionDetectWakeupSignalOut;
    public TimedWString m_rtcmlOut_val;
    public DataRef<TimedWString> m_rtcmlOut;
    public OutPort<TimedWString> m_rtcmlOutOut;
    
    //コンフィグ
    public StringHolder m_configpath = new StringHolder();
    
  /*!
   * @brief constructor
   * @param manager Maneger Object
   */
	public InfoClerkManagerImpl(Manager manager) {  
        super(manager);
        // <rtc-template block="initializer">
        m_Faces_val = new TimedLong(new Time(0,0),0);
        m_Faces = new DataRef<TimedLong>(m_Faces_val);
        m_FacesIn = new InPort<TimedLong>("Faces", m_Faces);
        m_detectMotion_val = new TimedBoolean(new Time(0,0),false);
        m_detectMotion = new DataRef<TimedBoolean>(m_detectMotion_val);
        m_detectMotionIn = new InPort<TimedBoolean>("detectMotion", m_detectMotion);
        m_juliusVoiceRecognition_val = new TimedWString(new Time(0,0),"");
        m_juliusVoiceRecognition = new DataRef<TimedWString>(m_juliusVoiceRecognition_val);
        m_juliusVoiceRecognitionIn = new InPort<TimedWString>("juliusVoiceRecognition", m_juliusVoiceRecognition);
        m_androidVoiceRecognition_val = new TimedString();
        m_androidVoiceRecognition = new DataRef<TimedString>(m_androidVoiceRecognition_val);
        m_androidVoiceRecognitionIn = new InPort<TimedString>("androidVoiceRecognition", m_androidVoiceRecognition);
        m_cameraImage_val = new CameraImage(new Time(0,0), (short)0, (short)0, (short)0, "", 0.0, new byte[255]);
        m_cameraImage = new DataRef<CameraImage>(m_cameraImage_val);
        m_cameraImageIn = new InPort<CameraImage>("cameraImage", m_cameraImage);
        m_rtcmlIn_val = new TimedWString(new Time(0,0),"");
        m_rtcmlIn = new DataRef<TimedWString>(m_rtcmlIn_val);
        m_rtcmlInIn = new InPort<TimedWString>("rtcmlIn", m_rtcmlIn);
        
        m_speechOut_val = new TimedWString(new Time(0,0),"");
        m_speechOut = new DataRef<TimedWString>(m_speechOut_val);
        m_speechOutOut = new OutPort<TimedWString>("speechOut", m_speechOut);
        m_faceDetectWakeupSignal_val = new TimedBoolean(new Time(0,0),false);
        m_faceDetectWakeupSignal = new DataRef<TimedBoolean>(m_faceDetectWakeupSignal_val);
        m_faceDetectWakeupSignalOut = new OutPort<TimedBoolean>("faceDetectWakeupSignal", m_faceDetectWakeupSignal);
        m_motionDetectWakeupSignal_val = new TimedBoolean(new Time(0,0),false);
        m_motionDetectWakeupSignal = new DataRef<TimedBoolean>(m_motionDetectWakeupSignal_val);
        m_motionDetectWakeupSignalOut = new OutPort<TimedBoolean>("motionDetectWakeupSignal", m_motionDetectWakeupSignal);
        m_rtcmlOut_val = new TimedWString(new Time(0,0),"");
        m_rtcmlOut = new DataRef<TimedWString>(m_rtcmlOut_val);
        m_rtcmlOutOut = new OutPort<TimedWString>("rtcmlOut", m_rtcmlOut);
        // </rtc-template>
    }
		
    /**
     *
     * The initialize action (on CREATED->ALIVE transition)
     * formaer rtc_init_entry() 
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
    @Override
    protected ReturnCode_t onInitialize() {
        // Registration: InPort/OutPort/Service
        // <rtc-template block="registration">
        // Set InPort buffers
        addInPort("Faces", m_FacesIn);
        addInPort("detectMotion", m_detectMotionIn);
        addInPort("juliusVoiceRecognition", m_juliusVoiceRecognitionIn);
        addInPort("androidVoiceRecognition", m_androidVoiceRecognitionIn);
        addInPort("cameraImage",m_cameraImageIn);
        addInPort("rtcmlIn",m_rtcmlInIn);
        
        // Set OutPort buffer
        addOutPort("speechOut", m_speechOutOut);
        addOutPort("faceDetectWakeupSignal", m_faceDetectWakeupSignalOut);
        addOutPort("motionDetectWakeupSignal", m_motionDetectWakeupSignalOut);
        addOutPort("rtcmlOut", m_rtcmlOutOut);
        // </rtc-template>
        bindParameter("configpath", m_configpath, "./InfoClerk.properties");
        return super.onInitialize();
    }

    /***
     *
     * The activated action (Active state entry action)
     * former rtc_active_entry()
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
    @Override
    protected ReturnCode_t onActivated(int ec_id)
    {
    	//OpenCVライブラリのロード
    	OpenCVLib.LoadDLL();
    	//INポートのバッファクリア
    	portClear(m_juliusVoiceRecognitionIn);
    	portClear(m_FacesIn);
    	portClear(m_detectMotionIn);
    	portClear(m_androidVoiceRecognitionIn);
    	
    	//設定ファイル読み込み
    	m_config = new ConfigFile();
    	try
    	{
			m_config.load(m_configpath.value);
			//コントロールパネルからの要求のため、事前に設定情報をRTCML化しておく
			String xml = m_config.serialize(new InfoClerkConfigSerializer());
			RTCML rtcml = new RTCML();
			rtcml.parse(xml);
			m_componentRtcml.put(rtcml.getTargetComponent(), rtcml);
		}
    	catch (IOException e)
    	{    		
    		System.out.println(m_configpath.value+" not found.");
			e.printStackTrace();
			return super.onActivated(ec_id);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//ログ初期化
    	m_logger = Logger.create(m_config.getString("LOG_CONFIGPATH"));
    	m_logger.trace("InfoClerkManager onActivated");
    	
    	//ユーザー情報を読み込む
    	m_userMaster = UserMasterFactory.create(
    						UserMasterFactory.TYPE.valueOf(m_config.getString("USERMASTER_TYPE")),
    						new UserMasterArgs(m_config.getString("USERMASTER_PATH"),
			    								m_config.getString("USERMASTER_DBHOSTNAME"),
			    								m_config.getString("USERMASTER_DBNAME"),
			    								m_config.getString("USERMASTER_DBUSERNAME"),
			    								m_config.getString("USERMASTER_DBPASSWORD")));    	
    	int res = m_userMaster.initialize();
    	if( res < 0 )
    	{
    		m_logger.trace(String.format("UserMasterの初期化に失敗しました（エラーコード:%d)", res));
    		return super.onActivated(ec_id);
    	}

    	//温泉認識エンジンの種類をログに出力する
    	m_logger.trace("VOICE_RECOGNITION_ENGINE="+m_config.getString("VOICE_RECOGNITION_ENGINE"));
    	   	   	
    	//映像送信用TCPサーバーの初期化
    	m_tcpImgServer = new InfoClerkTcpServer(this,m_config.getInt("CTRL_IMGPORT"),
    											m_config.getInt("CTRL_PACKETSIZE"),
    											m_config.getString("CTRL_ENCODING"));
    	//コマンド送受信用TCPサーバーの初期化
    	m_tcpCmdServer = new InfoClerkTcpServer(this,m_config.getInt("CTRL_CMDPORT"),
													m_config.getInt("CTRL_PACKETSIZE"),
													m_config.getString("CTRL_ENCODING"));
    	
    	m_tcpImgServer.start();
    	m_tcpCmdServer.start();
    	
    	//ステータスを待機状態に設定
    	m_stateWorker = new InfoClerkManagerState(this);
    	m_stateWorker.StateMachine().changeState(MotionDetectState.Instance());

        return super.onActivated(ec_id);
    }

    /***
     *
     * The deactivated action (Active state exit action)
     * former rtc_active_exit()
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
    @Override
    protected ReturnCode_t onDeactivated(int ec_id)
    {
    	m_logger.trace("InfoClerkManager onDeactivated");
    	m_stateWorker.StateMachine().changeState(null);
    	m_stateWorker = null;
    	m_tcpImgServer.stop();
    	m_tcpCmdServer.stop();
        return super.onDeactivated(ec_id);
    }

    /***
     *
     * The execution action that is invoked periodically
     * former rtc_active_do()
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
    @Override
    protected ReturnCode_t onExecute(int ec_id)
    {	
    	//カメラからの映像をコントロールパネルへ送信する
    	if( m_cameraImageIn.isNew() )
    	{
    		m_cameraImageIn.read();
    		m_tcpImgServer.setCameraImage(m_cameraImage.v.width,
					m_cameraImage.v.height,
					m_cameraImage.v.bpp,
					m_cameraImage.v.pixels);
    	}
    	    	
    	//コンフィグレーション取得要求に対する応答を受ける
    	if( m_rtcmlInIn.isNew() )
    	{
    		m_rtcmlInIn.read();
    		String rtcmlString = m_rtcmlIn.v.data;
    		RTCML rtcml = new RTCML();
    		try
    		{
				rtcml.parse(rtcmlString);
				if( rtcml.isResponse() )
				{
					m_componentRtcml.put(rtcml.getTargetComponent(),rtcml);
				}
			}
    		catch (ParserConfigurationException | SAXException | IOException e)
    		{
				e.printStackTrace();
			}
    	}
    	
    	//コンフィグレーション更新要求を他コンポーネントに送信する
    	if( m_tcpImgServer.getUpdateConfigRtcml() != null )
    	{
    		System.out.println("RTCMLOut1");
    	}
    	if( m_tcpCmdServer.getUpdateConfigRtcml() != null )
    	{
    		RTCML rtcml = new RTCML();
    		try
    		{
				rtcml.parse(m_tcpCmdServer.getUpdateConfigRtcml());
	    		rtcml.setCommand(RTCML.CMD_SEND_PROPERTY);
	    		m_rtcmlOut_val.data = m_tcpCmdServer.getUpdateConfigRtcml();
	    		System.out.println("RTCMLOut2");
	    		m_rtcmlOutOut.write();
			}
    		catch (ParserConfigurationException | SAXException | IOException e)
    		{
				e.printStackTrace();
			}
    		m_tcpCmdServer.resetUpdateConfigRtcml();
    	}
    	
    	if( m_stateWorker != null )
    	{
    		m_stateWorker.StateMachine().exec();
    	}    	
        return super.onExecute(ec_id);
    }  

    /**
     * 音声合成コンポーネントへ発声する文字列を送信する
     * 
     * @param	msg	発声文字列
     * **/
    public void speech(String msg)
    {
    	m_logger.trace("Speech >>"+msg);
		m_speechOut.v.data = msg;
		m_speechOutOut.write();
    }
    
    /**
     * 状態遷移を初期化する
     * **/
    public void reset()
    {
    	try
    	{
        	TimerTask task = new TimerTask()
        	{
    			@Override
    			public void run()
    			{    				
    				m_stateWorker.StateMachine().changeState(MotionDetectState.Instance());
    				m_resetTimer.cancel();
    			}
    		};
    		m_resetTimer = new Timer();	    		
    		m_resetTimer.schedule(task, m_config.getInt("CMN_RESETTIME"));
    	}
    	catch( Exception ex )
    	{
    		ex.printStackTrace();
    	}

    }
    
	/**
	 * INポートのバッファをクリアする
	 * 
	 * @param	inport	初期化するポート
	 * **/
	public void portClear(InPort inport)
	{
		while(!inport.isEmpty()){inport.read();}
	}
	
	/**
	 * ログ出力を行う
	 * **/
	public void log(String s)
	{
		m_logger.trace(s);
	}
}
