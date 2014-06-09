// -*- Java -*-
/*!
 * @file  FaceDetectorImpl.java
 * @brief ${rtcParam.description}
 * @date  $Date$
 *
 * $Id$
 */

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.ParserConfigurationException;

import jp.co.humane.opencvlib.CascadeFaceDetector;
import jp.co.humane.opencvlib.MatFactory;
import jp.co.humane.opencvlib.MatFactory.MatType;
import jp.co.humane.opencvlib.OpenCVLib;
import jp.co.humane.opencvlib.exception.CascadeClassifierException;
import jp.co.humane.xml.rtc.AttributeNotFoundException;
import jp.co.humane.xml.rtc.RTCML;
import jp.go.aist.rtm.RTC.DataFlowComponentBase;
import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.port.InPort;
import jp.go.aist.rtm.RTC.port.OutPort;
import jp.go.aist.rtm.RTC.util.DataRef;
import jp.go.aist.rtm.RTC.util.IntegerHolder;
import jp.go.aist.rtm.RTC.util.StringHolder;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.xml.sax.SAXException;

import RTC.CameraImage;
import RTC.ReturnCode_t;
import RTC.Time;
import RTC.TimedBoolean;
import RTC.TimedLong;
import RTC.TimedWString;

/*!
 * @class FaceDetectorImpl
 * @brief ${rtcParam.description}
 *
 */
public class FaceDetectorImpl extends DataFlowComponentBase
{
    private final static String CASCADE_PATH1 = "CascadePath1";
    private final static String CASCADE_PATH2 = "CascadePath2";
    private final static String CASCADE_PATH3 = "CascadePath3";
    private final static String SHOW_PREVIEW_DIALOG = "ShowPreviewDialog";
    private final static String CASCADE_SCALE = "CascadeScale";
    private final static String DETECT_THRESHOLD = "DetectThreshold";
    private final static String WAIT_TIME = "waitTime";
    private final static String RESET_TIME = "ResetTime";
    
	private boolean m_failedInitialize = false;
	private boolean m_sleep = false;
	private PreviewDialog m_previewDialog = null;
	private CascadeFaceDetector m_detector = null;
	private int m_faceDetectCount = 0;
	private Timer m_resetTimer = null;
	//RTCML解析エンジン
	private RTCML m_rtcmlParser = null;
	private RTCML m_configRtcmlParser = null;
	//コンフィギュレーション
	/*
	private String m_cascadePath1Value = null;
	private String m_cascadePath2Value = null;
	private String m_cascadePath3Value = null;
    private int m_showPreviewDialogValue = 0;
    private int m_scaleValue = 0;
    private int m_detectThresholdValue = 0;
    private int m_waitTimeValue = 0;
    private int m_resetTimeValue = 0;
    */
	private HashMap<String,String> m_configHash = new HashMap<String,String>();
    
    //RTC INポート
    protected CameraImage m_CameraImage_val;
    protected DataRef<CameraImage> m_CameraImage;
    protected InPort<CameraImage> m_CameraImageIn;
    protected TimedBoolean m_wakeup_val;
    protected DataRef<TimedBoolean> m_wakeup;
    protected InPort<TimedBoolean> m_wakeupIn;    
    protected TimedWString m_rtcmlIn_val;
    protected DataRef<TimedWString> m_rtcmlIn;
    protected InPort<TimedWString> m_rtcmlInIn;
    

    //RTC OUTポート
    protected TimedLong m_Faces_val;
    protected DataRef<TimedLong> m_Faces;
    protected OutPort<TimedLong> m_FacesOut;
    protected TimedWString m_rtcmlOut_val;
    protected DataRef<TimedWString> m_rtcmlOut;
    protected OutPort<TimedWString> m_rtcmlOutOut;
    
    //RTCコンフィギュレーション
    protected StringHolder m_cascadePath1;
    protected StringHolder m_cascadePath2;
    protected StringHolder m_cascadePath3;
    protected IntegerHolder m_showPreviewDialog;
    protected IntegerHolder m_scale;
    protected IntegerHolder m_detectThreshold;
    protected IntegerHolder m_waitTime;
    protected IntegerHolder m_resetTime;    
	
  /*!
   * @brief constructor
   * @param manager Maneger Object
   */
	public FaceDetectorImpl(Manager manager) {  
        super(manager);
        // <rtc-template block="initializer">
        m_CameraImage_val = new CameraImage(new Time(0,0), (short)0, (short)0, (short)0, "", 0.0, new byte[255]);
        m_CameraImage = new DataRef<CameraImage>(m_CameraImage_val);
        m_CameraImageIn = new InPort<CameraImage>("CameraImage", m_CameraImage);
        m_wakeup_val = new TimedBoolean(new Time(0,0),false);
        m_wakeup = new DataRef<TimedBoolean>(m_wakeup_val);
        m_wakeupIn = new InPort<TimedBoolean>("wakeup", m_wakeup);
        m_rtcmlIn_val = new TimedWString(new Time(0,0),"");
        m_rtcmlIn = new DataRef<TimedWString>(m_rtcmlIn_val);
        m_rtcmlInIn = new InPort<TimedWString>("rtcmlIn", m_rtcmlIn);
        m_Faces_val = new TimedLong(new Time(0,0),0);
        m_Faces = new DataRef<TimedLong>(m_Faces_val);
        m_FacesOut = new OutPort<TimedLong>("Faces", m_Faces);
        m_rtcmlOut_val = new TimedWString(new Time(0,0),"");
        m_rtcmlOut = new DataRef<TimedWString>(m_rtcmlOut_val);
        m_rtcmlOutOut = new OutPort<TimedWString>("rtcmlOut", m_rtcmlOut);
        // </rtc-template>

        m_cascadePath1 = new StringHolder();
        m_cascadePath2 = new StringHolder();
        m_cascadePath3 = new StringHolder();
        m_showPreviewDialog= new IntegerHolder();
        m_scale = new IntegerHolder();
        m_detectThreshold = new IntegerHolder();
        m_waitTime = new IntegerHolder();
        m_resetTime = new IntegerHolder();
        
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
        addInPort("CameraImage", m_CameraImageIn);
        addInPort("wakeup", m_wakeupIn);
        addInPort("rtcmlIn",m_rtcmlInIn);
        // Set OutPort buffer
        addOutPort("Faces", m_FacesOut);
        addOutPort("rtcmlOut",m_rtcmlOutOut);
        // </rtc-template>
        
        bindParameter("CascadePath1", m_cascadePath1, "");
        bindParameter("CascadePath2", m_cascadePath2, "");
        bindParameter("CascadePath3", m_cascadePath3, "");
        bindParameter("ShowPreviewDialog", m_showPreviewDialog, "0");
        bindParameter("CascadeScale", m_scale, "4");
        bindParameter("DetectThreshold", m_detectThreshold, "5");
        bindParameter("waitTime", m_waitTime, "500");
        bindParameter("ResetTime", m_resetTime, "4000");
        return super.onInitialize();
    }

    /***
     *
     * The finalize action (on ALIVE->END transition)
     * formaer rtc_exiting_entry()
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
//    @Override
//    protected ReturnCode_t onFinalize() {
//        return super.onFinalize();
//    }

    /***
     *
     * The startup action when ExecutionContext startup
     * former rtc_starting_entry()
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
//    @Override
//    protected ReturnCode_t onStartup(int ec_id) {
//        return super.onStartup(ec_id);
//    }

    /***
     *
     * The shutdown action when ExecutionContext stop
     * former rtc_stopping_entry()
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
//    @Override
//    protected ReturnCode_t onShutdown(int ec_id) {
//        return super.onShutdown(ec_id);
//    }

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
    	//OpenCVのDLLをロードする
    	OpenCVLib.LoadDLL();
    	//コンフィグの読み込み
//    	m_cascadePath1Value = m_cascadePath1.value;
//    	m_cascadePath2Value = m_cascadePath2.value;
//    	m_cascadePath3Value = m_cascadePath3.value;
//    	m_showPreviewDialogValue = m_showPreviewDialog.value;
//    	m_scaleValue = m_scale.value;
//    	m_detectThresholdValue = m_detectThreshold.value;
//    	m_waitTimeValue = m_waitTime.value;
//    	m_resetTimeValue = m_resetTime.value;
    	
        m_configHash.put(CASCADE_PATH1, m_cascadePath1.value);
        m_configHash.put(CASCADE_PATH2, m_cascadePath2.value);
        m_configHash.put(CASCADE_PATH3, m_cascadePath3.value);
        m_configHash.put(SHOW_PREVIEW_DIALOG, m_showPreviewDialog.value.toString());
        m_configHash.put(CASCADE_SCALE, m_scale.value.toString());
        m_configHash.put(DETECT_THRESHOLD, m_detectThreshold.value.toString());
        m_configHash.put(WAIT_TIME, m_waitTime.value.toString());
        m_configHash.put(RESET_TIME, m_resetTime.value.toString());
                
    	//Cascadeファイルのロード
    	try
    	{
        	m_detector = new CascadeFaceDetector();
    		m_detector.addCascade("cascade1", m_configHash.get(CASCADE_PATH1));
    		m_detector.addCascade("cascade2", m_configHash.get(CASCADE_PATH2));
    		m_detector.addCascade("cascade3", m_configHash.get(CASCADE_PATH3));
    	}
    	catch(CascadeClassifierException ex2)
    	{
    		System.out.println(ex2.getMessage());
    		m_failedInitialize = true;
    		return super.onActivated(ec_id);    		
    	}
    	
    	//RTCML解析エンジン初期化
    	m_rtcmlParser = new RTCML();
    	m_configRtcmlParser = new RTCML();
    	refleshConfigRTCML();
    	
		if( m_configHash.get(SHOW_PREVIEW_DIALOG).equals("1") )
		{
			m_previewDialog = new PreviewDialog();
			m_previewDialog.init();			
		}		
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
    protected ReturnCode_t onDeactivated(int ec_id) {
    	m_sleep = false;
    	m_failedInitialize = false;
    	if( m_previewDialog != null )
    	{
    		m_previewDialog.hideDialog();
    		m_previewDialog = null;
    	}
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
		int waitTime = Integer.parseInt(m_configHash.get(WAIT_TIME));
		
    	if( m_failedInitialize ){return super.onExecute(ec_id);}   	
    	if( m_wakeupIn.isNew() )
    	{
    		m_wakeupIn.read();
    		boolean wakeup = m_wakeup.v.data;
    		if( wakeup ){m_sleep = false;}
    		else{m_sleep = true;}
    	}
    	
    	//RTCMLの受信確認
    	if( m_rtcmlInIn.isNew() )
    	{
    		m_rtcmlInIn.read();
    		String rtcml = m_rtcmlIn.v.data;
    		System.out.println("rtcml="+rtcml);
    		parseRTCML(rtcml);
    	}
    	
    	/**
    	 * コンフィグ情報を送信
    	 * **/
    	if( !m_sleep )
    	{
    		m_rtcmlOut_val.data = m_configRtcmlParser.serialize();
    		m_rtcmlOutOut.write();
    	}
    	
    	/**
    	 * 画像を受信したか判定する
    	 * m_sleepフラグは他コンポーネントからwakeupInポートより更新される
    	 * **/
    	if( m_CameraImageIn.isNew() && !m_sleep )
    	{
			int scaleValue = Integer.parseInt(m_configHash.get(CASCADE_SCALE));
			int detectThresholdValue = Integer.parseInt(m_configHash.get(DETECT_THRESHOLD));
    		try
    		{
    			//画像を読み込む
        		m_CameraImageIn.read();
        		//画像データを行列に変換する
        		Mat cameraMat = MatFactory.create(m_CameraImage.v.width,
        											m_CameraImage.v.height,
        											m_CameraImage.v.bpp,
        											m_CameraImage.v.pixels);

        		//グレースケールの画像行列領域を確保
        		Mat grayImg = MatFactory.create(cameraMat.width(), cameraMat.height(), MatType.MONO_8BIT);
        		//縮小画像の行列領域を確保
        		Mat smallImg = MatFactory.create(cameraMat.width()/scaleValue, cameraMat.height()/scaleValue, MatType.MONO_8BIT);
        		
        		//顔判定処理の高速化のため、画像をグレースケール&縮小する
        		//画像をグレースケールに変換
        		Imgproc.cvtColor(cameraMat, grayImg, Imgproc.COLOR_BGR2GRAY);
        		//画像を指定サイズの縮小
        		Imgproc.resize(grayImg, smallImg, smallImg.size(),0,0,Imgproc.INTER_LINEAR);
        		
        		//画像内の顔数を取得
        		int faces = m_detector.detect(smallImg);
        		//1以上の顔を検出した
        		if( faces > 0 )
        		{
        			//初回の顔検出の場合は、タイムアウトタイマーを開始する
        			if( m_faceDetectCount == 0 ){reset(true);}
        			//顔検出数を増やす
        			//顔検出数が指定数以上となったとき、顔を検出したものとする
        			m_faceDetectCount++;
        			//顔検出数が指定数以上となったか判定
        			if( m_faceDetectCount > detectThresholdValue )
        			{
        				//顔検出数をOutポートより他コンポーネントへ出力する
            			System.out.println("** Face Detected! **");
            			reset(false);
        				m_faceDetectCount = 0;
            			m_sleep = false;
            			m_Faces_val.data = faces;
            			m_FacesOut.write();
        			}
        			else
        			{
            			System.out.println(String.format("Detecting...[%s/%s]", m_faceDetectCount,detectThresholdValue));        				
        			}
        		}
        		//プレビュー画面の表示
        		if(m_configHash.get(SHOW_PREVIEW_DIALOG).equals("1")){m_previewDialog.showDialog(cameraMat);}
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    	}
    	
		//負荷軽減のためメインスレッドを一定時間停止する
		try {
			Thread.sleep(waitTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return super.onExecute(ec_id);
    }

    public void reset(boolean onoff)
    {
    	if( !onoff ){m_resetTimer.cancel();}
    	else
    	{
        	try
        	{
        		int resetTime = Integer.parseInt(m_configHash.get(RESET_TIME));
            	TimerTask task = new TimerTask()
            	{
        			@Override
        			public void run()
        			{
        				m_faceDetectCount = 0;   				
        			}
        		};
        		m_resetTimer = new Timer();
        		m_resetTimer.schedule(task, resetTime);
        	}
        	catch( Exception ex )
        	{
        		ex.printStackTrace();
        	}    		
    	}
    }
    
    private void refleshConfigRTCML()
    {
    	m_configRtcmlParser = new RTCML();
    	try
    	{
			m_configRtcmlParser.parse("FaceDetect", RTCML.CMD_RESPONSE, m_configHash);
		}
    	catch (ParserConfigurationException | SAXException | IOException e)
    	{
			e.printStackTrace();
		}

    }
    /**
     * RTCMLを解析し、指定されたパラメータを更新する
     * 
     * @param	rtcml	RTCML文字列
     * **/
    private void parseRTCML(String rtcml)
    {
		try
		{
			m_rtcmlParser.parse(rtcml);
			if( m_rtcmlParser.isSetProperty() && m_rtcmlParser.getTargetComponent().equals("FaceDetect"))
			{
		    	try
		    	{
		        	m_configHash.put(CASCADE_PATH1,m_rtcmlParser.getString("CascadePath1"));    		
		    	}
		    	catch( AttributeNotFoundException ex ){}
		    	try
		    	{
		    		m_configHash.put(CASCADE_PATH2,m_rtcmlParser.getString("CascadePath2"));    		
		    	}
		    	catch( AttributeNotFoundException ex ){}
		    	try
		    	{
		    		m_configHash.put(CASCADE_PATH3,m_rtcmlParser.getString("CascadePath3"));    		
		    	}
		    	catch( AttributeNotFoundException ex ){}
		    	try
		    	{
		    		m_configHash.put(CASCADE_SCALE,String.valueOf(m_rtcmlParser.getInt("CascadeScale")));    		
		    	}
		    	catch( AttributeNotFoundException ex ){}
		    	try
		    	{
		    		m_configHash.put(DETECT_THRESHOLD,String.valueOf(m_rtcmlParser.getInt("DetectThreshold")));    		
		    	}
		    	catch( AttributeNotFoundException ex ){}
		    	try
		    	{
		    		m_configHash.put(RESET_TIME,String.valueOf(m_rtcmlParser.getInt("ResetTime")));    		
		    	}
		    	catch( AttributeNotFoundException ex ){}
		    	try
		    	{
		    		m_configHash.put(WAIT_TIME,String.valueOf(m_rtcmlParser.getInt("WaitTime")));
		    	}
		    	catch( AttributeNotFoundException ex ){}	    	
		    	try
		    	{
		    		m_configHash.put(SHOW_PREVIEW_DIALOG,String.valueOf(m_rtcmlParser.getInt("ShowPreviewDialog")));
			    	if( m_configHash.get(SHOW_PREVIEW_DIALOG).equals("1") )
			    	{
			    		m_previewDialog = new PreviewDialog();
			    		m_previewDialog.init();
			    	}
			    	else
			    	{
			    		m_previewDialog.hideDialog();
			    	}
		    	}
		    	catch( AttributeNotFoundException ex ){}
		    	//
		    	refleshConfigRTCML();
			}
		}
		catch (ParserConfigurationException | SAXException | IOException e)
		{
			System.out.println("RTCML parse error");
			e.printStackTrace();
		}
    }
}

