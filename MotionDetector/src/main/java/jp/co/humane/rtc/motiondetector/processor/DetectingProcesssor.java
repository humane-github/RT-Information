package jp.co.humane.rtc.motiondetector.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import RTC.CameraImage;
import RTC.TimedBoolean;
import jp.co.humane.opencvlib.MatViewer;
import jp.co.humane.rtc.common.collection.Pair;
import jp.co.humane.rtc.common.component.state.StateProcessResult;
import jp.co.humane.rtc.common.component.state.StateProcessor;
import jp.co.humane.rtc.common.port.RtcInPort;
import jp.co.humane.rtc.common.port.RtcOutPort;
import jp.co.humane.rtc.common.util.CorbaObj;
import jp.co.humane.rtc.common.util.ElapsedTimer;
import jp.co.humane.rtc.motiondetector.MotionDetectorConfig;

/**
 * 検出中ステータスでの処理クラス。
 * @author terada.
 *
 */
public class DetectingProcesssor extends StateProcessor {

    /**
     * 処理結果を表すENUM。
     * @author terada.
     */
    public enum Result {
        DETECT,
        NOT_DETECT,
        TIMEOUT
    }

    /** カメラ映像の入力ポート */
    private RtcInPort<CameraImage> cameraImageIn = null;

    /** 動体検知を通知する出力ポート */
    private RtcOutPort<TimedBoolean> detectResultOut = null;

    /** 設定情報 */
    private MotionDetectorConfig config = null;

    /** 1フレーム前の画像情報 */
    private Mat prevImage = null;

    /** 1フレーム前のコーナー情報 */
    private MatOfPoint prevCorners = null;

    /** イメージ確認用ビューア */
    private MatViewer matViewer = new MatViewer("MotionDetector");

    /** 検出継続時間(sec) */
    private int detectLimitSec = 0;

    /** 経過時間タイマー */
    private ElapsedTimer timer = new ElapsedTimer();

    /**
     * コンストラクタ。
     * @param cameraImageIn カメラ映像の入力ポート。
     * @param faceCountOut  動体検知を通知する出力ポート。
     * @param config        設定情報。
     */
    public DetectingProcesssor(RtcInPort<CameraImage> cameraImageIn,
                               RtcOutPort<TimedBoolean> detectResultOut,
                               MotionDetectorConfig config) {
        this.cameraImageIn = cameraImageIn;
        this.detectResultOut = detectResultOut;
        this.config = config;
    }

    /**
     * 前の処理から引き渡される情報を受け取る。
     * @param 検出継続時間。
     */
    @Override
    public void acceptPreResult(StateProcessResult result) {
        detectLimitSec = (Integer)result.getResultData();
        timer.setBaseTime();
    }

    /**
     * 検出中ステータスの処理。
     * 動体検知できればDETECT、それ以外はNOT_DETECTを返す。
     * @param ec_id ExecutionContext ID.
     * @return  処理結果。
     */
    @Override
    public StateProcessResult onExecute(int ec_id) {

        // 検出継続時間を過ぎた場合はTIMEOUTを返す
        if (detectLimitSec < timer.getElapsedTime(TimeUnit.SECONDS)) {
            logger.debug("指定時間" + detectLimitSec + "秒を経過しても検出できませんでした。");;
            return new StateProcessResult(Result.TIMEOUT);
        }

        // 画像が入力ポートにない場合はNOT_DETECTを返す
        if (!cameraImageIn.isNew() || cameraImageIn.isEmpty()) {
            return new StateProcessResult(Result.NOT_DETECT);
        }

        // 画像データがnullの場合はNOT_DETECTを返す
        CameraImage image = cameraImageIn.readData();
        if (null == image) {
            return new StateProcessResult(Result.NOT_DETECT);
        }

        // 顔の検出処理を行い検出しなかった場合はNOT_DETECTを返す
        boolean isDetected = doDetectMotion(image);
        if (!isDetected) {
            return new StateProcessResult(Result.NOT_DETECT);
        }

        // 検出したことを出力ポートに書き込みDETECTを返す
        detectResultOut.write(CorbaObj.newTimedBoolean(true));
        logger.info("動体検知に成功し、出力ポートに検出結果を書き込みました。");
        return new StateProcessResult(Result.DETECT);
   }

    /**
     * 画像情報をもとに動体検知処理を行う
     * @param image 画像情報。
     * @return 検出結果。true:検出、false:非検出
     */
    private boolean doDetectMotion(CameraImage image) {

        // 画像を格納するMatを作成
        // CV_8UC1：8ビット + unsigned(0～255)、チャネル数1(RGBは3チャネル) ⇒ 白黒表示
        Mat cameraMat = new Mat(image.height, image.width, image.bpp);
        Mat grayMat   = new Mat(image.height, image.width, CvType.CV_8UC1);

        // カメラの映像をMatに格納後、グレースケールに変換
        cameraMat.put(0, 0, image.pixels);
        Imgproc.cvtColor(cameraMat, grayMat, Imgproc.COLOR_BGR2GRAY);

        // 初回は特徴点（コーナー）を検出して終了
        if (null == prevCorners) {

            // 画像情報を保持
            prevImage = grayMat;

            // 特徴点を検出
            prevCorners = new MatOfPoint();
            Imgproc.goodFeaturesToTrack(grayMat, prevCorners,
                                        config.getMaxCornerSize(),
                                        config.getQualityExcludeRatio(),
                                        config.getCornersMargin());
            return false;
        }

        // 移動後の特徴点（コーナー）をオプティカルフローで検出
        // 「元画像、現画像、元コーナー」をインプットとして「元コーナーに対応する現コーナー」を予測する
        Point[] prevCornerPoints = prevCorners.toArray();
        MatOfPoint2f predictConers = new MatOfPoint2f();
        MatOfByte status = new MatOfByte();
        Video.calcOpticalFlowPyrLK(prevImage, grayMat,
                                     new MatOfPoint2f(prevCornerPoints), predictConers,
                                     status, new MatOfFloat());

        // 閾値となるコーナー間の距離の2乗を計算
        double threshold2 = Math.pow(config.getCornerMoveLength(), 2);

        // statusが1のインデックス位置にあるコーナーが検出できたコーナーなのでその移動距離を求める
        List<Pair<Point, Point>> moveLineList = new ArrayList<>();
        Point[] predictCornerPoints = predictConers.toArray();
        byte[] statusArray = status.toArray();
        for (int index = 0; index < statusArray.length; index++) {

            // 対応する移動後の特徴点（コーナー）が検出できない場合は対象外
            if (1 != statusArray[index]) {
                continue;
            }

            // 2点間の距離の2乗を計算
            Point prevCorner = prevCornerPoints[index];
            Point currentCorner = predictCornerPoints[index];
            double distance2 = Math.pow((prevCorner.x - currentCorner.x), 2)
                               + Math.pow((prevCorner.y - currentCorner.y), 2);

            // 距離が閾値を超えている場合は該当のポイントを格納
            if (threshold2 < distance2) {
                moveLineList.add(new Pair<Point, Point>(prevCorner, currentCorner));
            }
        }

        // ビューアを表示する
        if (config.getEnableViewer()) {
            updateViewer(cameraMat, moveLineList);
        }

        // 移動量が閾値を超えているコーナーの数が指定数を満たさない場合は非検出とする
        if (moveLineList.size() < config.getCornerMoveCount()) {
            return false;
        }

        return true;

    }

    /**
     * 検出結果をビューアに表示する。
     * @param image        画像データ。
     * @param moveLineList 移動線のリスト。
     */
    private void updateViewer(Mat image, List<Pair<Point, Point>> moveLineList) {

        Scalar scalar = new Scalar(255, 0, 0);
        for (Pair<Point, Point> points : moveLineList) {
            Core.line(image, points.getKey(), points.getValue(), scalar);
        }

        matViewer.updateImage(image);
    }
}
