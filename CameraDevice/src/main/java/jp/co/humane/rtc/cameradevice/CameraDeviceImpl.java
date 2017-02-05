package jp.co.humane.rtc.cameradevice;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

import RTC.CameraImage;
import RTC.ReturnCode_t;
import jp.co.humane.opencvlib.MatViewer;
import jp.co.humane.opencvlib.OpenCVLib;
import jp.co.humane.rtc.common.component.DataFlowComponent;
import jp.co.humane.rtc.common.port.RtcOutPort;
import jp.co.humane.rtc.common.starter.RtcStarter;
import jp.co.humane.rtc.common.util.CorbaObj;
import jp.go.aist.rtm.RTC.Manager;

/**
 * USBカメラのキャプチャイメージデータを定期的に出力ポートに出力する。
 * @author terada
 *
 */
public class CameraDeviceImpl extends DataFlowComponent<CameraDeviceConfig> {

    /** デバイスID自動取得 */
    private static final int DEVICE_ID_AUTO_DETECT = -1;

    /** デバイスID最小値 */
    private static final int DEVICE_ID_MIN = 0;

    /** デバイスID最大値 */
    private static final int DEVICE_ID_MAX = 5;

    /** VideoCaptureインスタンス */
    private VideoCapture videoCapture = null;

    /** カメラ映像の送信ポート */
    protected RtcOutPort<CameraImage> cameraImageOut = new RtcOutPort<>("CameraImage", CorbaObj.newCameraImage());

    /** イメージ確認用ビューア */
    private MatViewer imageViewer = new MatViewer("CameraDevice");

    /** OpenCVの読み込み */
    static {
        OpenCVLib.LoadDLL();
    }

    /**
     * コンストラクタ。
     * @param manager RTCマネージャ。
     */
	public CameraDeviceImpl(Manager manager) {
        super(manager);
    }

    /**
     * アクティブ時の処理。
     * USBカメラの初期化を行う。
     *
     * @param ec_id ExecutionContext ID.
     * @return リターンコード。
     */
    @Override
    protected ReturnCode_t onRtcActivated(int ec_id) {

        // カメラをオープンする
        boolean isSuccess = initUsbCamera();
        if (!isSuccess) {
            logger.error("デバイスID:" + config.getDeviceid() + "に対するカメラの初期化に失敗しました。");
            return ReturnCode_t.RTC_ERROR;
        }

        return ReturnCode_t.RTC_OK;
    }

    /**
     * USBカメラをコンフィグのデバイスIDで初期化する。
     * デバイスIDが-1の場合はオープン可能なデバイスIDを特定の範囲から検出する。
     *
     * @return 処理結果。
     */
    private boolean initUsbCamera() {

        if (DEVICE_ID_AUTO_DETECT == config.getDeviceid()) {

            // デバイスIDが-1の場合はオープンできる最初のカメラを使用する
            for (int index = DEVICE_ID_MIN; index < DEVICE_ID_MAX; index++) {
                videoCapture = new VideoCapture(index);
                if (videoCapture.isOpened()) {
                    logger.debug("デバイスID=" + index + "でカメラをオープンしました。");
                    break;
                }
            }

        } else {
            videoCapture = new VideoCapture(config.getDeviceid());
        }

        // オープンできなかった場合はフィールドをnullに設定
        boolean isOpened = videoCapture.isOpened();
        if (!isOpened) {
            videoCapture = null;
        }

        // カメラのオープン状態を返す
        return isOpened;
    }

    /**
     * 非アクティブ化時の処理。
     * VideoCaptureを解放する。
     */
    @Override
    protected ReturnCode_t onRtcDeactivated(int ec_id) {

        // USBカメラを開放
        if (null != videoCapture) {
            videoCapture.release();
        }

        // ビューアを閉じる
        if (config.getEnableViewer()) {
            imageViewer.release();
        }
        return ReturnCode_t.RTC_OK;
    }

    /**
     * 周期的な処理。
     */
    @Override
    protected ReturnCode_t onRtcExecute(int ec_id) {

        // OpenCVでカメラの映像を取得
        Mat cameraMat = new Mat();
        videoCapture.read(cameraMat);

        // ビューアにイメージを表示
        if (config.getEnableViewer()) {
            imageViewer.updateImage(cameraMat);
        }

        // OpenCVのMatをRTCのCamerImage形式に変換して出力
        CameraImage imageData = cameraImageOut.geData();
        imageData.pixels = new byte[(int) (cameraMat.width() * cameraMat.height() * cameraMat.channels())];
        imageData.width = (short) cameraMat.width();
        imageData.height = (short) cameraMat.height();
        imageData.bpp = (short) cameraMat.type();
        cameraMat.get(0, 0, imageData.pixels);
        cameraImageOut.write(imageData);

        return ReturnCode_t.RTC_OK;
    }

    /**
     * メイン処理。
     * @param args 起動引数。
     */
    public static void main(String[] args) {

        RtcStarter.init(args)
                  .setConfig(new CameraDeviceConfig())
                  .start(CameraDeviceImpl.class);
    }

}
