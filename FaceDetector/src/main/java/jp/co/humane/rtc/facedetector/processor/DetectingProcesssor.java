package jp.co.humane.rtc.facedetector.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import RTC.CameraImage;
import RTC.TimedBoolean;
import jp.co.humane.opencvlib.MatViewer;
import jp.co.humane.rtc.common.component.state.StateProcessResult;
import jp.co.humane.rtc.common.component.state.StateProcessor;
import jp.co.humane.rtc.common.port.RtcInPort;
import jp.co.humane.rtc.common.port.RtcOutPort;
import jp.co.humane.rtc.common.util.CorbaObj;
import jp.co.humane.rtc.common.util.ElapsedTimer;
import jp.co.humane.rtc.facedetector.FaceDetectorConfig;

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

    /** 検出した顔の数を出力するポート */
    private RtcOutPort<TimedBoolean> detectResultOut = null;

    /** 設定情報 */
    private FaceDetectorConfig config = null;

    /** カスケード分類器リスト */
    private List<CascadeClassifier> cascadeClassifierList = new ArrayList<>();

    /** イメージ確認用ビューア */
    private MatViewer matViewer = new MatViewer("FaceDetector");

    /** 検出継続時間(sec) */
    private int detectLimitSec = 0;

    /** 経過時間タイマー */
    private ElapsedTimer timer = new ElapsedTimer();

    /**
     * コンストラクタ。
     * @param cameraImageIn カメラ映像の入力ポート。
     * @param faceCountOut  顔検出の結果を出力するポート。
     * @param config        設定情報。
     */
    public DetectingProcesssor(RtcInPort<CameraImage> cameraImageIn,
                               RtcOutPort<TimedBoolean> faceCountOut,
                               FaceDetectorConfig config) {
        this.cameraImageIn = cameraImageIn;
        this.detectResultOut = faceCountOut;
        this.config = config;
    }

    /**
     * アクティブ化時の処理。
     * @param ec_id  ExecutionContext ID.
     * @return  処理結果。true:正常終了, false:異常終了。
     */
    @Override
    public boolean onActivated(int ec_id) {

        // Cascadeファイルのロード
        boolean isSuccess = loadCascadeConfigFile();
        if (!isSuccess) {
            logger.error("カスケードファイルの読み込みに失敗しました。");
            return false;
        }
        return true;
    }


    /**
     * 顔の特徴量を定義したカスケードファイルを読み込む。
     * @return 処理結果。
     */
    private boolean loadCascadeConfigFile() {

        // 各ファイルに対して読み込み処理を行う
        List<String> paths = Arrays.asList(config.getCascadePath1(), config.getCascadePath2(), config.getCascadePath3());
        for (String path : paths) {

            // パスが指定されていない場合は次のファイルに移る
            if (StringUtils.isEmpty(path)) {
                continue;
            }

            // 読み込みに失敗した場合は次のファイルに移る
            CascadeClassifier cc = new CascadeClassifier();
            boolean canLoaded = cc.load(path);
            if (!canLoaded) {
                logger.warn("指定されたカスケードファイルの読み込みに失敗しました。パス=[" + path + "]");
                continue;
            }

            // 読み込んだ情報をリストに格納
            cascadeClassifierList.add(cc);
        }

        // 読み込めているかどうかを返却する
        return (0 < cascadeClassifierList.size());
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
     * 顔が検出できればDETECT、それ以外はNOT_DETECTを返す。
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
        boolean isDetected = doDetectFace(image);
        if (!isDetected) {
            return new StateProcessResult(Result.NOT_DETECT);
        }

        // 検出した場合は出力ポートにtrue送信後DETECT
        detectResultOut.write(CorbaObj.newTimedBoolean(true));
        logger.info("顔を検出し、出力ポートに検出結果を書き込みました。");
        return new StateProcessResult(Result.DETECT);

    }

    /**
     * 画像情報をもとに顔の検出処理を行う
     * @param image 画像情報。
     * @return 検出結果。true:検出、false:非検出
     */
    private boolean doDetectFace(CameraImage image) {

//        // 縮小画像のサイズを取得
//        int smallWidth = (int) (image.width / config.getZoomOutRatio());
//        int smallHeight = (int) (image.height / config.getZoomOutRatio());
//
        // 画像を格納するMatを作成
        // CV_8UC1：8ビット + unsigned(0～255)、チャネル数1(RGBは3チャネル) ⇒ 白黒表示
        Mat cameraMat = new Mat(image.height, image.width, image.bpp);
        Mat grayMat   = new Mat(image.height, image.width, CvType.CV_8UC1);
//        Mat smallMat  = new Mat(smallHeight, smallWidth, CvType.CV_8UC1);

        // カメラの映像をMatに格納後、グレースケールに変換
        cameraMat.put(0, 0, image.pixels);
        Imgproc.cvtColor(cameraMat, grayMat, Imgproc.COLOR_BGR2GRAY);

//        // 画像を縮小化する
//        Imgproc.resize(grayMat, smallMat, smallMat.size(), 0, 0, Imgproc.INTER_LINEAR);
//
//        // 各特徴量を使って顔の検出処理を行う
//        int detectedCount = 0;
//        List<MatOfRect> facesList = new ArrayList<>();
//        for (CascadeClassifier cc : cascadeClassifierList) {
//
//            // 検出処理を実行
//            MatOfRect faces = new MatOfRect();
//            cc.detectMultiScale(smallMat, faces, 1.1, 2, Objdetect.CASCADE_SCALE_IMAGE , new Size(30,30), new Size());
//
//            // 検出できた場合は検出数を増やし、検出範囲をリストに格納
//            if (0 < faces.toArray().length) {
//                detectedCount++;
//                facesList.add(faces);
//            }
//        }
//
//        // ビューアに画像を表示する
//        if (config.getEnableViewer()) {
//            updateViewer(smallMat, facesList);
//        }
//
//        // 検出数が指定回数未満の場合は未検出とする
//        if (detectedCount < config.getDetectThreshold()) {
//            return false;
//        }


        // 顔検出の特徴量は1つめのCascade
        CascadeClassifier cc =cascadeClassifierList.get(0);

        // 検出処理を実行
        MatOfRect faces = new MatOfRect();
        cc.detectMultiScale(grayMat, faces);

         // 検出されなかったらfalse
        if (0 < faces.toArray().length){
            return false;
        }

        // 検出した顔より面積が大きいものを抽出
        int faceAreaMax=0;

        for (Rect rect :faces.toArray()){
            int faceArea = rect.width * rect.height;

            if (faceAreaMax < faceArea){
                faceAreaMax = faceArea;
            }
        }

        // 一番面積の大きい顔が画像全体の?%をしめていれば検出とする
        // 今のところは50%にしているがconfigのgetDetectThresholdで設定?
        int imageArea = grayMat.width() * grayMat.height();
        if (faceAreaMax < imageArea * 0.5){
          return false;
        }

        return true;
    }

    /**
     * 検出結果をビューアに表示する。
     * @param smallMat  画像データ。
     * @param facesList 検出範囲データ。
     */
    private void updateViewer(Mat smallMat, List<MatOfRect> facesList) {

        // 暫定的に最大8個まで表示
        Iterator<Scalar> it =
                Arrays.asList(new Scalar(255,   0,   0), new Scalar(  0, 255,   0), new Scalar(  0,   0, 255),
                               new Scalar(255, 255,   0), new Scalar(255,   0, 255), new Scalar(  0, 255, 255),
                               new Scalar(  0,   0,   0), new Scalar(255, 255, 255)).iterator();

        if (8 < facesList.size()) {
            facesList = facesList.subList(0, 7);
        }

        for (MatOfRect faces : facesList) {
            Scalar scalar = it.next();
            for (Rect face : faces.toList()) {

                Core.rectangle(
                        smallMat,
                        new Point(face.x, face.y),
                        new Point(face.x + face.width, face.y + face.height),
                        scalar);
            }
        }

        matViewer.updateImage(smallMat);
    }
}
