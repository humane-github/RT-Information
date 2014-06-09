// -*- Java -*-
/*!
 * @file  MotionDetectorImpl.java
 * @brief MotionDetector
 * @date  $Date$
 *
 * $Id$
 */

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import jp.co.humane.opencvlib.MatFactory;
import jp.co.humane.opencvlib.MatFactory.MatType;
import jp.co.humane.opencvlib.OpenCVLib;
import jp.co.humane.xml.rtc.AttributeNotFoundException;
import jp.co.humane.xml.rtc.RTCML;
import jp.go.aist.rtm.RTC.DataFlowComponentBase;
import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.port.InPort;
import jp.go.aist.rtm.RTC.port.OutPort;
import jp.go.aist.rtm.RTC.util.DataRef;
import jp.go.aist.rtm.RTC.util.DoubleHolder;
import jp.go.aist.rtm.RTC.util.IntegerHolder;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;
import org.xml.sax.SAXException;

import RTC.CameraImage;
import RTC.ReturnCode_t;
import RTC.Time;
import RTC.TimedBoolean;
import RTC.TimedWString;

/*!
 * @class MotionDetectorImpl
 * @brief MotionDetector
 *
 */
public class MotionDetectorImpl extends DataFlowComponentBase
{
	//1フレーム前の映像行列
	private Mat m_prevMat = null;
	//起動or待機状態フラグ
	private boolean m_sleep = false;
	//プレビュー画面表示フラグ
	private PreviewDialog m_previewDialog = null;
	//RTCML解析エンジン
	private RTCML m_rtcmlParser = null;
	
	private int m_featuresMaxCornersValue = 0;
	private double m_featuresQualityLevelValue = 0;
	private double m_featuresMinDistanceValue = 0;
	private double m_detectThresholdValue = 0;
	private int m_showPreviewDialogValue = 0;
	
	//RTC INポート
    protected CameraImage m_cameraImage_val;
    protected DataRef<CameraImage> m_cameraImage;
    protected InPort<CameraImage> m_cameraImageIn;   
    protected TimedBoolean m_wakeup_val;
    protected DataRef<TimedBoolean> m_wakeup;
    protected InPort<TimedBoolean> m_wakeupIn;    
    protected TimedWString m_rtcml_val;
    protected DataRef<TimedWString> m_rtcml;
    protected InPort<TimedWString> m_rtcmlIn;

	//RTC OUTポート
    protected TimedBoolean m_result_val;
    protected DataRef<TimedBoolean> m_result;
    protected OutPort<TimedBoolean> m_resultOut;

    //RTCコンフィギュレーション
    protected IntegerHolder m_featuresMaxCorners = new IntegerHolder();
    protected DoubleHolder m_featuresQualityLevel = new DoubleHolder();
    protected DoubleHolder m_featuresMinDistance = new DoubleHolder();
    protected DoubleHolder m_detectThreshold = new DoubleHolder();
    protected IntegerHolder m_showPreviewDialog = new IntegerHolder();
	
  /*!
   * @brief constructor
   * @param manager Maneger Object
   */
	public MotionDetectorImpl(Manager manager) {  
        super(manager);
        // <rtc-template block="initializer">
        m_cameraImage_val = new CameraImage(new Time(0,0), (short)0, (short)0, (short)0, "", 0.0, new byte[255]);
        m_cameraImage = new DataRef<CameraImage>(m_cameraImage_val);
        m_cameraImageIn = new InPort<CameraImage>("cameraImage", m_cameraImage);
        m_wakeup_val = new TimedBoolean(new Time(0,0),false);
        m_wakeup = new DataRef<TimedBoolean>(m_wakeup_val);
        m_wakeupIn = new InPort<TimedBoolean>("wakeup", m_wakeup);
        m_rtcml_val = new TimedWString(new Time(0,0),"");
        m_rtcml = new DataRef<TimedWString>(m_rtcml_val);
        m_rtcmlIn = new InPort<TimedWString>("rtcml", m_rtcml);
        m_result_val = new TimedBoolean(new Time(0,0),false);
        m_result = new DataRef<TimedBoolean>(m_result_val);
        m_resultOut = new OutPort<TimedBoolean>("result", m_result);
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
        addInPort("cameraImage", m_cameraImageIn);
        addInPort("wakeup", m_wakeupIn);
        addInPort("rtcml",m_rtcmlIn);
        
        // Set OutPort buffer
        addOutPort("result", m_resultOut);
        // </rtc-template>
        
        bindParameter("FEATURES_MAX_CORNERS", m_featuresMaxCorners, "80");
        bindParameter("FEATURES_QUALITY_LEVEL", m_featuresQualityLevel, "0.01");
        bindParameter("FEATURES_MIN_DISTANCE", m_featuresMinDistance, "5");
        bindParameter("DETECT_THRESHOLD", m_detectThreshold, "10");
        bindParameter("ShowPreviewDialog", m_showPreviewDialog, "0");

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
    	//OpenCVのDLLロード
    	OpenCVLib.LoadDLL();
    	
    	//RTCML解析エンジン初期化
    	m_rtcmlParser = new RTCML();
    	
    	//コンフィグ初期化
    	m_featuresMaxCornersValue = m_featuresMaxCorners.getValue();
    	m_featuresQualityLevelValue = m_featuresQualityLevel.getValue();
    	m_featuresMinDistanceValue = m_featuresMinDistance.getValue();
    	m_detectThresholdValue = m_detectThreshold.getValue();
    	m_showPreviewDialogValue = m_showPreviewDialog.getValue();

    	//プレビュー画面の初期化
    	if( m_showPreviewDialogValue == 1 )
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
    protected ReturnCode_t onDeactivated(int ec_id)
    {
    	if( m_previewDialog != null ){m_previewDialog.hideDialog();}
    	//if( m_currentMat != null ){m_currentMat.release();}
    	if( m_prevMat != null ){m_prevMat.release();}
    	//if( m_currentCorners != null ){m_currentCorners.release();}
    	//m_currentMat = null;
    	m_prevMat = null;
    	//m_currentCorners = null;
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
    	//起動or停止フラグの更新があるか確認
    	if( m_wakeupIn.isNew() )
    	{
    		//フラグの更新を行う
    		m_wakeupIn.read();
    		m_sleep = !m_wakeup.v.data;    		
    	}
    	
    	//RTCMLの受信確認
    	if( m_rtcmlIn.isNew() )
    	{
    		m_rtcmlIn.read();
    		String rtcml = m_rtcml.v.data;
    		System.out.println("rtcml="+rtcml);
    		parseRTCML(rtcml);
    	}
    	
    	//USBカメラからの映像を取得する。ただし、停止状態のときは処理しない
    	if( m_cameraImageIn.isNew() && !m_sleep )
    	{
    		//作業用Mat
    		Mat grayMat = null;
    		//特徴点の座標
			MatOfPoint currentCorners = new MatOfPoint();
			MatOfPoint2f currentCorners2f = new MatOfPoint2f();
			
    		//カメラ画像読み込み
    		m_cameraImageIn.read();
    		Mat cameraMat = MatFactory.create(m_cameraImage.v.width,
				    							m_cameraImage.v.height,
				    							m_cameraImage.v.bpp,
				    							m_cameraImage.v.pixels);
    		
//			//前回の画像を保持
//			if( grayMat != null )
//			{
//				m_prevMat = MatFactory.create(grayMat.width(), grayMat.height(), MatType.MONO_8BIT);
//				grayMat.copyTo(m_prevMat);
//			}
			//グレースケールの画像行列領域を確保
			grayMat = MatFactory.create(cameraMat.width(), cameraMat.height(), MatType.MONO_8BIT);
			//画像をグレースケールに変換
			Imgproc.cvtColor(cameraMat, grayMat, Imgproc.COLOR_BGR2GRAY);
			
			//コーナー検出
			Imgproc.goodFeaturesToTrack(grayMat, currentCorners,
										m_featuresMaxCornersValue,
										m_featuresQualityLevelValue,
										m_featuresMinDistanceValue);
			//MatOfPoint2ｆに変換
    		currentCorners.copyTo(currentCorners2f);
    		//calcOpticalFlowPyrlkはCV_32FC3しか受け付けないのでここで変換
    		currentCorners2f.convertTo(currentCorners2f, CvType.CV_32FC3);
    		
			//オプティカルフロー検出
			if( m_prevMat != null )
			{
	    		MatOfByte status = new MatOfByte();
	    		MatOfPoint2f resultCorners2f = new MatOfPoint2f();
	    		currentCorners2f.copyTo(resultCorners2f);
	    		resultCorners2f.convertTo(resultCorners2f, CvType.CV_32FC3);
	    		MatOfFloat error = new MatOfFloat();
	    		Video.calcOpticalFlowPyrLK(m_prevMat, grayMat, currentCorners2f, resultCorners2f, status, error);

				int idx = 0;
				Point[] tmpCurrentCorners2f = currentCorners2f.toArray();
				Point[] tmpResultCorners2f = resultCorners2f.toArray();
				byte[] tmpStatus = status.toArray();
	    		for( Point p : tmpResultCorners2f )
	    		{
	    			//検出失敗した場合は処理しない
	    			if( tmpStatus[idx] != 1 ){continue;}	    			
	    			Point currentp = tmpCurrentCorners2f[idx];
	    			//前回と今回の特徴点の距離を算出
	    			double distance = Math.sqrt(Math.pow((p.x - currentp.x),2) + Math.pow((p.y - currentp.y),2));
	    			//距離が閾値以上なら動体検知とみなす
	    			if( distance > m_detectThresholdValue )
	    			{
	    				System.out.println("Detected!!!");
	    				m_result_val.data = true;
	    				m_resultOut.write();
	    				break;
	    			}
	    			idx++;
	    		}
			}
			//現フレームの映像を保持
			m_prevMat = MatFactory.create(grayMat.width(), grayMat.height(), MatType.MONO_8BIT);
			grayMat.copyTo(m_prevMat);
			//プレビュー画面表示
			if( m_showPreviewDialogValue == 1 ){m_previewDialog.showDialog(cameraMat);}		
    	}
    	
        return super.onExecute(ec_id);
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
	    	try
	    	{
	        	m_featuresMaxCornersValue = m_rtcmlParser.getInt("FEATURES_MAX_CORNERS");    		
	    	}
	    	catch( AttributeNotFoundException ex ){}
	    	try
	    	{
	    		m_featuresMinDistanceValue = m_rtcmlParser.getDouble("FEATURES_MIN_DISTANCE");    		
	    	}
	    	catch( AttributeNotFoundException ex ){}
	    	try
	    	{
	    		m_featuresQualityLevelValue = m_rtcmlParser.getDouble("FEATURES_QUALITY_LEVEL");    		
	    	}
	    	catch( AttributeNotFoundException ex ){}
	    	try
	    	{
	    		m_detectThresholdValue = m_rtcmlParser.getDouble("DETECT_THRESHOLD");    		
	    	}
	    	catch( AttributeNotFoundException ex ){}
	    	try
	    	{
	    		m_showPreviewDialogValue = m_rtcmlParser.getInt("ShowPreviewDialog");    		
	    	}
	    	catch( AttributeNotFoundException ex ){}
	    	if( m_showPreviewDialogValue == 1 )
	    	{
	    		m_previewDialog = new PreviewDialog();
	    		m_previewDialog.init();			    		
	    	}
	    	else
	    	{
	    		m_previewDialog.hideDialog();
	    	}
		}
		catch (ParserConfigurationException | SAXException | IOException e)
		{
			System.out.println("RTCML parse error");
			e.printStackTrace();
		}
    }
}
