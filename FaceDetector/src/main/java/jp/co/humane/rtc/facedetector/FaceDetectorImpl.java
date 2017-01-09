package jp.co.humane.rtc.facedetector;

import RTC.CameraImage;
import RTC.ReturnCode_t;
import RTC.TimedLong;
import jp.co.humane.opencvlib.OpenCVLib;
import jp.co.humane.rtc.common.component.DataFlowStatefullComponent;
import jp.co.humane.rtc.common.port.RtcInPort;
import jp.co.humane.rtc.common.port.RtcOutPort;
import jp.co.humane.rtc.common.starter.RtcStarter;
import jp.co.humane.rtc.common.util.CorbaObj;
import jp.co.humane.rtc.facedetector.processor.DetectingProcesssor;
import jp.co.humane.rtc.facedetector.processor.WaitWakeupProcessor;
import jp.go.aist.rtm.RTC.Manager;

/**
 * カメラ映像を元に人の顔を検出し、検出数を出力する。
 * @author terada.
 */
public class FaceDetectorImpl extends DataFlowStatefullComponent<FaceDetectorConfig> {

    /** 状態を表すenum */
    public enum State {

        /** 待機中 */
        WAIT_WAKEUP,

        /** 検出中 */
        DETECTING;
    };

    /** カメラ映像の入力ポート */
    private RtcInPort<CameraImage> cameraImageIn = null;

    /** 顔検出の開始指示入力ポート */
    private RtcInPort<TimedLong> wakeupIn = null;

    /** 検出した顔の数を出力するポート */
    private RtcOutPort<TimedLong> faceCountOut = null;

    /** OpenCVの読み込み */
    static {
        OpenCVLib.LoadDLL();
    }

    /**
     * コンストラクタ。
     * @param manager RTCマネージャ。
     */
	public FaceDetectorImpl(Manager manager) {
        super(manager);
    }

    /**
     * 初期化処理。
     * 各ポートの追加を行う。
     *
     * @return リターンコード。
     */
    @Override
    protected ReturnCode_t onRtcInitialize() {

        // カメラの映像の入力ポートを追加
        cameraImageIn = new RtcInPort<CameraImage>("CameraImage", CorbaObj.newCameraImage(255));
        addInPort("CameraImage", cameraImageIn);

        // 顔検出の開始指示入力ポートを追加
        wakeupIn = new RtcInPort<TimedLong>("Wakeup", CorbaObj.newTimedLong());
        addInPort("Wakeup", wakeupIn);

        // 検出した顔の数を出力するポートを追加
        faceCountOut = new RtcOutPort<TimedLong>("FaceCount", CorbaObj.newTimedLong());
        addOutPort("FaceCount", faceCountOut);

        // 状態の関連情報を設定
        setStateRelation();

        return super.onRtcInitialize();
    }

    /**
     * 状態と処理、状態と処理結果と遷移先の関連を設定する。
     */
    private void setStateRelation() {

        // 状態とそれに対応する処理を登録
        stateProcMap.put(State.WAIT_WAKEUP, new WaitWakeupProcessor(wakeupIn));
        stateProcMap.put(State.DETECTING, new DetectingProcesssor(cameraImageIn, faceCountOut, config));

        // 現在状態、処理結果、遷移先状態の組み合わせを登録
        addStateMoveMap(State.WAIT_WAKEUP, WaitWakeupProcessor.Result.NOT_RECEIVE, State.WAIT_WAKEUP);
        addStateMoveMap(State.WAIT_WAKEUP, WaitWakeupProcessor.Result.RECEIVE,     State.DETECTING);
        addStateMoveMap(State.DETECTING,   DetectingProcesssor.Result.DETECT,       State.WAIT_WAKEUP);
        addStateMoveMap(State.DETECTING,   DetectingProcesssor.Result.NOT_DETECT,  State.DETECTING);
        addStateMoveMap(State.DETECTING,   DetectingProcesssor.Result.TIMEOUT,      State.WAIT_WAKEUP);

        // 初期状態を待機中に設定
        // TODO:後で戻す
        // this.state = State.WAIT_WAKEUP;
        this.state = State.DETECTING;

    }

    /**
     * メイン処理。
     * @param args 起動引数。
     */
    public static void main(String[] args) {

        RtcStarter.init(args)
                  .setConfig(new FaceDetectorConfig())
                  .start(FaceDetectorImpl.class);
    }
}

