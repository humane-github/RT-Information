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
	//1�t���[���O�̉f���s��
	private Mat m_prevMat = null;
	//�N��or�ҋ@��ԃt���O
	private boolean m_sleep = false;
	//�v���r���[��ʕ\���t���O
	private PreviewDialog m_previewDialog = null;
	//RTCML��̓G���W��
	private RTCML m_rtcmlParser = null;
	
	private int m_featuresMaxCornersValue = 0;
	private double m_featuresQualityLevelValue = 0;
	private double m_featuresMinDistanceValue = 0;
	private double m_detectThresholdValue = 0;
	private int m_showPreviewDialogValue = 0;
	
	//RTC IN�|�[�g
    protected CameraImage m_cameraImage_val;
    protected DataRef<CameraImage> m_cameraImage;
    protected InPort<CameraImage> m_cameraImageIn;   
    protected TimedBoolean m_wakeup_val;
    protected DataRef<TimedBoolean> m_wakeup;
    protected InPort<TimedBoolean> m_wakeupIn;    
    protected TimedWString m_rtcml_val;
    protected DataRef<TimedWString> m_rtcml;
    protected InPort<TimedWString> m_rtcmlIn;

	//RTC OUT�|�[�g
    protected TimedBoolean m_result_val;
    protected DataRef<TimedBoolean> m_result;
    protected OutPort<TimedBoolean> m_resultOut;

    //RTC�R���t�B�M�����[�V����
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
    	//OpenCV��DLL���[�h
    	OpenCVLib.LoadDLL();
    	
    	//RTCML��̓G���W��������
    	m_rtcmlParser = new RTCML();
    	
    	//�R���t�B�O������
    	m_featuresMaxCornersValue = m_featuresMaxCorners.getValue();
    	m_featuresQualityLevelValue = m_featuresQualityLevel.getValue();
    	m_featuresMinDistanceValue = m_featuresMinDistance.getValue();
    	m_detectThresholdValue = m_detectThreshold.getValue();
    	m_showPreviewDialogValue = m_showPreviewDialog.getValue();

    	//�v���r���[��ʂ̏�����
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
    	//�N��or��~�t���O�̍X�V�����邩�m�F
    	if( m_wakeupIn.isNew() )
    	{
    		//�t���O�̍X�V���s��
    		m_wakeupIn.read();
    		m_sleep = !m_wakeup.v.data;    		
    	}
    	
    	//RTCML�̎�M�m�F
    	if( m_rtcmlIn.isNew() )
    	{
    		m_rtcmlIn.read();
    		String rtcml = m_rtcml.v.data;
    		System.out.println("rtcml="+rtcml);
    		parseRTCML(rtcml);
    	}
    	
    	//USB�J��������̉f�����擾����B�������A��~��Ԃ̂Ƃ��͏������Ȃ�
    	if( m_cameraImageIn.isNew() && !m_sleep )
    	{
    		//��ƗpMat
    		Mat grayMat = null;
    		//�����_�̍��W
			MatOfPoint currentCorners = new MatOfPoint();
			MatOfPoint2f currentCorners2f = new MatOfPoint2f();
			
    		//�J�����摜�ǂݍ���
    		m_cameraImageIn.read();
    		Mat cameraMat = MatFactory.create(m_cameraImage.v.width,
				    							m_cameraImage.v.height,
				    							m_cameraImage.v.bpp,
				    							m_cameraImage.v.pixels);
    		
//			//�O��̉摜��ێ�
//			if( grayMat != null )
//			{
//				m_prevMat = MatFactory.create(grayMat.width(), grayMat.height(), MatType.MONO_8BIT);
//				grayMat.copyTo(m_prevMat);
//			}
			//�O���[�X�P�[���̉摜�s��̈���m��
			grayMat = MatFactory.create(cameraMat.width(), cameraMat.height(), MatType.MONO_8BIT);
			//�摜���O���[�X�P�[���ɕϊ�
			Imgproc.cvtColor(cameraMat, grayMat, Imgproc.COLOR_BGR2GRAY);
			
			//�R�[�i�[���o
			Imgproc.goodFeaturesToTrack(grayMat, currentCorners,
										m_featuresMaxCornersValue,
										m_featuresQualityLevelValue,
										m_featuresMinDistanceValue);
			//MatOfPoint2���ɕϊ�
    		currentCorners.copyTo(currentCorners2f);
    		//calcOpticalFlowPyrlk��CV_32FC3�����󂯕t���Ȃ��̂ł����ŕϊ�
    		currentCorners2f.convertTo(currentCorners2f, CvType.CV_32FC3);
    		
			//�I�v�e�B�J���t���[���o
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
	    			//���o���s�����ꍇ�͏������Ȃ�
	    			if( tmpStatus[idx] != 1 ){continue;}	    			
	    			Point currentp = tmpCurrentCorners2f[idx];
	    			//�O��ƍ���̓����_�̋������Z�o
	    			double distance = Math.sqrt(Math.pow((p.x - currentp.x),2) + Math.pow((p.y - currentp.y),2));
	    			//������臒l�ȏ�Ȃ瓮�̌��m�Ƃ݂Ȃ�
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
			//���t���[���̉f����ێ�
			m_prevMat = MatFactory.create(grayMat.width(), grayMat.height(), MatType.MONO_8BIT);
			grayMat.copyTo(m_prevMat);
			//�v���r���[��ʕ\��
			if( m_showPreviewDialogValue == 1 ){m_previewDialog.showDialog(cameraMat);}		
    	}
    	
        return super.onExecute(ec_id);
    }
    
    /**
     * RTCML����͂��A�w�肳�ꂽ�p�����[�^���X�V����
     * 
     * @param	rtcml	RTCML������
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
