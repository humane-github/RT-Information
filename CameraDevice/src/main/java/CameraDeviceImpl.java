// -*- Java -*-
/*!
 * @file  CameraDeviceImpl.java
 * @brief CameraDevice
 * @date  $Date$
 *
 * $Id$
 */

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import jp.co.humane.msg.Msg;
import jp.co.humane.opencvlib.OpenCVLib;
import jp.co.humane.xml.rtc.AttributeNotFoundException;
import jp.co.humane.xml.rtc.RTCML;
import jp.go.aist.rtm.RTC.DataFlowComponentBase;
import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.port.InPort;
import jp.go.aist.rtm.RTC.port.OutPort;
import jp.go.aist.rtm.RTC.util.DataRef;
import jp.go.aist.rtm.RTC.util.IntegerHolder;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;
import org.xml.sax.SAXException;

import RTC.CameraImage;
import RTC.ReturnCode_t;
import RTC.Time;
import RTC.TimedWString;

/*!
 * @class CameraDeviceImpl
 * @brief CameraDevice
 *
 */
public class CameraDeviceImpl extends DataFlowComponentBase
{

	private VideoCapture m_camera = null;
    private Mat m_cameraMat = null;
    private RTCML m_rtcmlParser = null;
    private int m_waittimeValue = 0;
    
    protected CameraImage m_CameraImage_val;
    protected DataRef<CameraImage> m_CameraImage;
    protected OutPort<CameraImage> m_CameraImageOut;
    
    protected TimedWString m_rtcml_val;
    protected DataRef<TimedWString> m_rtcml;
    protected InPort<TimedWString> m_rtcmlIn;
    
    protected IntegerHolder m_deviceid;
    protected IntegerHolder m_waittime;
	
  /*!
   * @brief constructor
   * @param manager Maneger Object
   */
	public CameraDeviceImpl(Manager manager)
	{
        super(manager);
        m_CameraImage_val = new CameraImage(new Time(0,0), (short)0, (short)0, (short)0, "", 0.0, new byte[255]);
        m_CameraImage = new DataRef<CameraImage>(m_CameraImage_val);
        m_CameraImageOut = new OutPort<CameraImage>("CameraImage", m_CameraImage);        
        m_rtcml_val = new TimedWString(new Time(0,0),"");
        m_rtcml = new DataRef<TimedWString>(m_rtcml_val);
        m_rtcmlIn = new InPort<TimedWString>("rtcml",m_rtcml);
        m_deviceid = new IntegerHolder();
        m_waittime = new IntegerHolder();
        
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
    protected ReturnCode_t onInitialize()
    {
    	addInPort("rtcml",m_rtcmlIn);
        addOutPort("CameraImage", m_CameraImageOut);
        bindParameter("deviceid", m_deviceid, "1");
        bindParameter("waittime", m_waittime, "100");
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
    	//コンフィグ読み込み
    	m_waittimeValue = m_waittime.getValue();
    	//USBカメラ初期化
    	m_camera = new VideoCapture(m_deviceid.getValue());
    	if( !m_camera.isOpened() )
    	{
    		System.out.println(Msg.get("0901"));
    	}
		System.out.println(Msg.get("0900"));
		
		m_CameraImage.v.width = 0;
		m_CameraImage.v.height = 0;

		//カメラ映像の行列
		m_cameraMat = new Mat(0, 0, CvType.CV_32S);
		
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
    	m_camera.release();
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
    	if( m_rtcmlIn.isNew() )
    	{
    		m_rtcmlIn.read();
    		String rtcml = m_rtcml.v.data;
    		System.out.println("rtcml="+rtcml);
    		parseRTCML(rtcml);
    	}
    	m_cameraMat.release();
		//mat = new Mat(0, 0, CvType.CV_32S);
		m_camera.read(m_cameraMat);
		try
		{
			m_CameraImage.v.pixels = new byte[(int)(m_cameraMat.width()*m_cameraMat.height()*m_cameraMat.channels())];
			m_CameraImage.v.width = (short)m_cameraMat.width();
			m_CameraImage.v.height = (short)m_cameraMat.height();
			m_cameraMat.get(0, 0, m_CameraImage.v.pixels);
			m_CameraImageOut.write();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			Thread.sleep(m_waittimeValue);
			//Thread.sleep(m_config.getInt("CAMERA_WAIT"));
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	        	m_waittimeValue = m_rtcmlParser.getInt("waittime");    		
	    	}
	    	catch( AttributeNotFoundException ex ){}
		}
		catch (ParserConfigurationException | SAXException | IOException e)
		{
			System.out.println("RTCML parse error");
			e.printStackTrace();
		}
    }
}
