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
import jp.co.humane.rtc.infoclerkmgr.processor.FaceDetectProcessor;
import jp.co.humane.rtc.infoclerkmgr.processor.MotionDetectProcessor;
import jp.co.humane.rtc.infoclerkmgr.processor.VoiceRecognizeProcessor;
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
        RECOGNIZE_VOICE;
    };

    /** 動体検知結果の入力ポート */
    private RtcInPort<TimedBoolean> detectMotionResultIn = null;

    /** 顔検知結果の入力ポート */
    private RtcInPort<TimedBoolean> detectFaceResultIn = null;

    /** 音声認識結果の入力ポート */
    private RtcInPort<TimedWString> voiceTexResulttIn = null;

    /** 動体検知開始通知用の出力ポート */
    private RtcOutPort<TimedLong> detectMotionStartOut = null;

    /** 顔検知開始通知用の出力ポート */
    private RtcOutPort<TimedLong> detectFaceStartOut = null;

    /** 音声合成用の出力ポート */
    private RtcOutPort<TimedString> voiceTextOut = null;

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

        // 動体検知結果の入力ポートを追加
        detectMotionResultIn = new RtcInPort<TimedBoolean>("MotionDetectIn", CorbaObj.newTimedBoolean());
        addInPort("MotionDetectIn", detectMotionResultIn);

        // 顔検知結果の入力ポートを追加
        detectFaceResultIn = new RtcInPort<TimedBoolean>("FaceDetecIn", CorbaObj.newTimedBoolean());
        addInPort("FaceDetectIn", detectFaceResultIn);

        // 音声認識結果の入力ポートを追加
        voiceTexResulttIn = new RtcInPort<TimedWString>("VoiceRecognitionIn", CorbaObj.newTimedWString());
        addInPort("VoiceRecognitionIn", voiceTexResulttIn);

        // 動体検知開始通知用の出力ポートを追加
        detectMotionStartOut = new RtcOutPort<TimedLong>("MotionDetectOut", CorbaObj.newTimedLong());
        addOutPort("MotionDetectOut", detectMotionStartOut);

        // 顔検知開始通知用の出力ポートを追加
        detectFaceStartOut = new RtcOutPort<TimedLong>("FaceDetectOut", CorbaObj.newTimedLong());
        addOutPort("FaceDetectOut", detectFaceStartOut);

        // 音声合成用の出力ポートを追加
        voiceTextOut = new RtcOutPort<TimedString>("VoiceSpeech", CorbaObj.newTimedString());
        addOutPort("VoiceSpeech", voiceTextOut);

        // 状態の関連情報を設定
        setStateRelation();

        return ReturnCode_t.RTC_OK;
    }

    /**
     * 状態と処理、状態と処理結果と遷移先の関連を設定する。
     */
    private void setStateRelation() {

        // 状態とそれに対応する処理を登録
        stateProcMap.put(State.MOTION_DETECT, new MotionDetectProcessor(detectMotionResultIn, detectMotionStartOut, config));
        stateProcMap.put(State.FACE_DETECT, new FaceDetectProcessor(detectFaceResultIn, detectFaceStartOut, config));
        stateProcMap.put(State.RECOGNIZE_VOICE, new VoiceRecognizeProcessor(voiceTexResulttIn, voiceTextOut, config));

        // 現在状態、処理結果、遷移先状態の組み合わせを登録
        addStateMoveMap(State.MOTION_DETECT,   MotionDetectProcessor.Result.NOT_DETECT,      State.MOTION_DETECT);
        addStateMoveMap(State.MOTION_DETECT,   MotionDetectProcessor.Result.TIMEOUT,         State.MOTION_DETECT);
        addStateMoveMap(State.MOTION_DETECT,   MotionDetectProcessor.Result.DETECT,          State.FACE_DETECT);
        addStateMoveMap(State.FACE_DETECT,     FaceDetectProcessor.Result.NOT_DETECT,        State.FACE_DETECT);
        addStateMoveMap(State.FACE_DETECT,     FaceDetectProcessor.Result.TIMEOUT,            State.MOTION_DETECT);
        addStateMoveMap(State.FACE_DETECT,     FaceDetectProcessor.Result.DETECT,             State.RECOGNIZE_VOICE);
        addStateMoveMap(State.RECOGNIZE_VOICE, VoiceRecognizeProcessor.Result.NOT_RECOGNIZE, State.RECOGNIZE_VOICE);
        addStateMoveMap(State.RECOGNIZE_VOICE, VoiceRecognizeProcessor.Result.RECOGNIZE,     State.MOTION_DETECT);
        addStateMoveMap(State.RECOGNIZE_VOICE, VoiceRecognizeProcessor.Result.TIMEOUT,       State.MOTION_DETECT);

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

        return ReturnCode_t.RTC_OK;
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
