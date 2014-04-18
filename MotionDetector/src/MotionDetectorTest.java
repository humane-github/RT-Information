import jp.co.humane.opencvlib.MatFactory;
import jp.co.humane.opencvlib.OpenCVLib;
import jp.co.humane.opencvlib.MatFactory.MatType;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;


public class MotionDetectorTest
{
	private static int MAX_CORNERS = 80;
	private static double QUALITY_LEVEL = 0.01f;
	private static double MIN_DISTANCE = 5;
	private static double DETECT_THRESHOLD = 10;
	
	private Mat m_currentMat = null;
	private MatOfPoint m_currentCorners = null;
	private Mat m_prevMat = null;
	private MatOfPoint m_prevCorners = null;
	
	private boolean m_sleep = false;
	private PreviewDialog m_previewDialog = null;
	
	public void test()
	{
		OpenCVLib.LoadDLL();
		VideoCapture camera = new VideoCapture(0);
    	if( !camera.isOpened() )
    	{
    		System.out.println("カメラ初期化失敗");
    	}
		//カメラ映像取得
		Mat cameraMat = new Mat(0, 0, CvType.CV_32S);
		
		m_previewDialog = new PreviewDialog();
		m_previewDialog.init();
		
		while(true)
		{
			
	    	cameraMat.release();
			camera.read(cameraMat);

			//前回の画像を保持
			if( m_currentMat != null )
			{
				m_prevMat = MatFactory.create(m_currentMat.width(), m_currentMat.height(), MatType.MONO_8BIT);
				m_currentMat.copyTo(m_prevMat);
			}
			//グレースケールの画像行列領域を確保
			m_currentMat = MatFactory.create(cameraMat.width(), cameraMat.height(), MatType.MONO_8BIT);
			//画像をグレースケールに変換
			Imgproc.cvtColor(cameraMat, m_currentMat, Imgproc.COLOR_BGR2GRAY);
			
			//コーナー検出
			if( m_currentCorners == null ){m_currentCorners = new MatOfPoint();}    		
			Imgproc.goodFeaturesToTrack(m_currentMat, m_currentCorners, MAX_CORNERS, QUALITY_LEVEL, MIN_DISTANCE);
			//MatOfPoint2ｆに変換
			MatOfPoint2f currentPoints = new MatOfPoint2f();
    		m_currentCorners.copyTo(currentPoints);
    		//calcOpticalFlowPyrlkはCV_32FC3しか受け付けないのでここで変換
    		currentPoints.convertTo(currentPoints, CvType.CV_32FC3);
    		
			//オプティカルフロー検出
			if( m_prevMat != null )
			{
	    		MatOfByte status = new MatOfByte();
	    		MatOfPoint2f resultPoints = new MatOfPoint2f();
	    		currentPoints.copyTo(resultPoints);
	    		MatOfFloat error = new MatOfFloat();
	    		Video.calcOpticalFlowPyrLK(m_prevMat, m_currentMat, currentPoints, resultPoints, status, error);

				int idx = 0;
				Point[] tmpCurrentPoints = currentPoints.toArray();
				Point[] tmpResultPoints = resultPoints.toArray();
				float[] tmpError = error.toArray();
				byte[] tmpStatus = status.toArray();
	    		for( Point p : tmpResultPoints )
	    		{
	    			
	    			if( tmpStatus[idx] != 1 ){continue;}	    			
	    			Point currentp = tmpCurrentPoints[idx];
	    			Core.line(cameraMat, p, currentp, new Scalar(80,80,255));
	    			double distance = Math.sqrt(Math.pow((p.x - currentp.x),2) + Math.pow((p.y - currentp.y),2));
	    			if( distance > DETECT_THRESHOLD )
	    			{
	    				System.out.println("Detected!!!");
	    			}
	    			idx++;
	    		}
				System.out.println("3");
			}
			m_previewDialog.showDialog(cameraMat);			
		}	
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new MotionDetectorTest().test();
	}

}
