package jp.co.humane.rtc.infoclerkmgr;

import RTC.ReturnCode_t;
import RTC.TimedBoolean;
import RTC.TimedLong;
import RTC.TimedString;
import RTC.TimedWString;
import jp.co.humane.rtc.common.component.DataFlowStatefullComponent;
import jp.co.humane.rtc.common.component.state.StateProcessor;
import jp.co.humane.rtc.common.port.RtcInPort;
import jp.co.humane.rtc.common.port.RtcOutPort;
import jp.co.humane.rtc.common.starter.RtcStarter;
import jp.co.humane.rtc.common.util.CorbaObj;
import jp.co.humane.rtc.infoclerkmgr.processor.CloseFaceDetectProc;
import jp.co.humane.rtc.infoclerkmgr.processor.FaceDetectProc;
import jp.co.humane.rtc.infoclerkmgr.processor.MotionDetectProc;
import jp.co.humane.rtc.infoclerkmgr.processor.VoiceRecognizeProc;
import jp.go.aist.rtm.RTC.Manager;

/**
 * 各コンポーネントからの入力を調整して案内システムを管理する。
 * @author terada.
 */
public class InfoClerkManagerImpl extends DataFlowStatefullComponent<InfoClerkManagerConfig> {


    /** 状態を表すenum */
    public enum State {

        /** 動体検知中 */
        MOTION_DETECT,

        /** 顔認識中 */
        FACE_DETECT,

        /** 音声認識中 */
        RECOGNIZE_VOICE,

        /** 画面クローズ用顔認識中 */
        CLOSE_FACE_DETECT;

    };

    /** 動体検知結果の入力ポート */
    private RtcInPort<TimedBoolean> motionResultIn = new RtcInPort<>("MotionDetectIn", CorbaObj.newTimedBoolean());

    /** 顔検知結果の入力ポート */
    private RtcInPort<TimedBoolean> faceResultIn = new RtcInPort<>("FaceDetecIn", CorbaObj.newTimedBoolean());

    /** 音声認識結果の入力ポート */
    private RtcInPort<TimedWString> voiceResulttIn = new RtcInPort<>("VoiceRecognitionIn", CorbaObj.newTimedWString());

    /** 動体検知開始通知用の出力ポート */
    private RtcOutPort<TimedLong> motionStartOut = new RtcOutPort<>("MotionDetectOut", CorbaObj.newTimedLong());

    /** 顔検知開始通知用の出力ポート */
    private RtcOutPort<TimedLong> faceStartOut = new RtcOutPort<>("FaceDetectOut", CorbaObj.newTimedLong());

    /** 音声合成用の出力ポート */
    private RtcOutPort<TimedString> voiceTextOut = new RtcOutPort<>("VoiceSpeech", CorbaObj.newTimedString());

    /**
     * コンストラクタ。
     * @param manager RTCマネージャ。
     */
    public InfoClerkManagerImpl(Manager manager) {
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

        // 状態の関連情報を設定
        setStateRelation();

        return super.onRtcInitialize();
    }

    /**
     * 状態と処理、状態と処理結果と遷移先の関連を設定する。
     */
    private void setStateRelation() {

        // 状態とそれに対応する処理を登録
        stateProcMap.put(State.MOTION_DETECT,     new MotionDetectProc(motionResultIn, motionStartOut, config));
        stateProcMap.put(State.FACE_DETECT,        new FaceDetectProc(faceResultIn, faceStartOut, config));
        stateProcMap.put(State.RECOGNIZE_VOICE,   new VoiceRecognizeProc(voiceResulttIn, voiceTextOut, config));
        stateProcMap.put(State.CLOSE_FACE_DETECT, new CloseFaceDetectProc(faceResultIn, faceStartOut, config));

        // 現在状態、処理結果、遷移先状態の組み合わせを登録
        addStateMoveMap(State.MOTION_DETECT,     MotionDetectProc.Result.NOT_DETECT,      State.MOTION_DETECT);
        addStateMoveMap(State.MOTION_DETECT,     MotionDetectProc.Result.TIMEOUT,         State.MOTION_DETECT);
        addStateMoveMap(State.MOTION_DETECT,     MotionDetectProc.Result.DETECT,          State.FACE_DETECT);
        addStateMoveMap(State.FACE_DETECT,       FaceDetectProc.Result.NOT_DETECT,        State.FACE_DETECT);
        addStateMoveMap(State.FACE_DETECT,       FaceDetectProc.Result.TIMEOUT,           State.MOTION_DETECT);
        addStateMoveMap(State.FACE_DETECT,       FaceDetectProc.Result.DETECT,            State.RECOGNIZE_VOICE);
        addStateMoveMap(State.RECOGNIZE_VOICE,   VoiceRecognizeProc.Result.NOT_RECOGNIZE, State.RECOGNIZE_VOICE);
        addStateMoveMap(State.RECOGNIZE_VOICE,   VoiceRecognizeProc.Result.RECOGNIZE,     State.CLOSE_FACE_DETECT);
        addStateMoveMap(State.RECOGNIZE_VOICE,   VoiceRecognizeProc.Result.TIMEOUT,       State.FACE_DETECT);
        addStateMoveMap(State.CLOSE_FACE_DETECT, CloseFaceDetectProc.Result.NOT_CLOSE,   State.CLOSE_FACE_DETECT);
        addStateMoveMap(State.CLOSE_FACE_DETECT, CloseFaceDetectProc.Result.CLOSE,        State.MOTION_DETECT);
        addStateMoveMap(State.CLOSE_FACE_DETECT, CloseFaceDetectProc.Result.TIMEOUT,      State.MOTION_DETECT);

        // 初期状態を動体検知中に設定
        this.state = State.MOTION_DETECT;
    }

    /**
     * アクティブ化処理。
     * @param ec_id ExecutionContext ID.
     * @return リターンコード。
     */
    @Override
    protected ReturnCode_t onRtcActivated(int ec_id) {

        // 処理開始を通知するために空の前情報を渡す
        StateProcessor currentProc = stateProcMap.get(this.state);
        currentProc.acceptPreResult(null);

        return super.onRtcActivated(ec_id);
    }

    /**
     * メイン処理。
     * @param args 起動引数。
     */
    public static void main(String[] args) {

        RtcStarter.init(args)
                  .setConfig(new InfoClerkManagerConfig())
                  .start(InfoClerkManagerImpl.class);
    }
}
