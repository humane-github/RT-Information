package jp.co.humane.rtc.common.component;

import java.util.concurrent.TimeUnit;

import RTC.ReturnCode_t;
import jp.co.humane.rtc.common.logger.RtcLogger;
import jp.co.humane.rtc.common.starter.bean.ConfigBase;
import jp.co.humane.rtc.common.util.ConfigUtils;
import jp.co.humane.rtc.common.util.ElapsedTimer;
import jp.go.aist.rtm.RTC.DataFlowComponentBase;
import jp.go.aist.rtm.RTC.Manager;

/**
 * 設定をBeanで扱うRTCのスーパークラス。
 * @author terada.
 *
 * @param <T> 設定情報が記載されたBean。
 */
public abstract class DataFlowComponent<T extends ConfigBase> extends DataFlowComponentBase {

    /** ロガー */
    protected RtcLogger logger = null;

    /** 設定情報 */
    protected T config = null;

    /** タイマー */
    protected ElapsedTimer onExecuteTimer = null;

    /**
     * コンストラクタ。
     * @param manager RTCマネージャ。
     */
    public DataFlowComponent(Manager manager) {
        super(manager);
    }

    /**
     * コンポーネント名を設定する。
     * @param componentName コンポーネント名。
     */
    public void setComponentName(String componentName) {
        // ロガーを作成する
        logger = new RtcLogger(componentName);
    }

    /**
     * 設定情報のBeanを設定する。
     * @param bean 設定情報のBean。
     */
    @SuppressWarnings("unchecked")
    public void setConfigBean(ConfigBase bean) {
        this.config = (T) bean;
    }

    // 元の処理にエラーハンドリングを追加
    // 全部のメソッドに対して対処が必要だが、大変なのでよく使うメソッドのみ対応

    /**
     * 初期化処理に例外処理を追加。
     */
    @Override
    protected final ReturnCode_t onInitialize() {

        try {
            // 設定情報のバインド処理を行う
            ConfigUtils.bindParameters(this, config);

            // 初期化処理を継承クラスに委譲
            return onRtcInitialize();

        } catch (Throwable th) {
            logger.error("初期化処理に失敗しました。", th);
            throw th;
        }
    }

    /**
     * 初期化処理。
     * @return リターンコード。
     */
    protected ReturnCode_t onRtcInitialize() {
        return ReturnCode_t.RTC_OK;
    }

    /**
     * アクティブ化処理に例外処理を追加。
     */
    @Override
    protected final ReturnCode_t onActivated(int ec_id) {

        try {
            // 設定情報を取得
            ConfigUtils.updateConfig(m_configsets, config);

            // ログレベルを設定
            logger.setLevel(config.getLogLevel());

            // タイマーが有効な場合はタイマーを作成
            if (0 < config.getInterval()) {
                onExecuteTimer = new ElapsedTimer();
            }

            // アクティブ化処理を継承クラスに委譲
            return onRtcActivated(ec_id);

        } catch (Throwable th) {
            logger.error("アクティブ化処理に失敗しました。", th);
            throw th;
        }
    }

    /**
     * アクティブ化処理。
     * @param ec_id ExecutionContext ID.
     * @return リターンコード。
     */
    protected ReturnCode_t onRtcActivated(int ec_id) {
        return ReturnCode_t.RTC_OK;
    }

    /**
     * 非アクティブ化処理に例外処理を追加。
     */
    @Override
    protected final ReturnCode_t onDeactivated(int ec_id) {

        try {
            // 非アクティブ化処理を継承クラスに委譲
            return onRtcDeactivated(ec_id);

        } catch (Throwable th) {
            logger.error("非アクティブ化処理に失敗しました。", th);
            throw th;
        }
    }

    /**
     * 非アクティブ化処理。
     * @param ec_id ExecutionContext ID.
     * @return リターンコード。
     */
    protected ReturnCode_t onRtcDeactivated(int ec_id) {
        return ReturnCode_t.RTC_OK;
    }

    /**
     * 定期実行処理に例外処理を追加。
     */
    @Override
    protected final ReturnCode_t onExecute(int ec_id) {

        try {
            // exec_cxt.periodic.rateが効かないので自前でインターバルを設定する
            // ベースタイムから指定時間が経過するまで待機する
            if (null != onExecuteTimer) {
                onExecuteTimer.wait(TimeUnit.MICROSECONDS, config.getInterval());
            }

            // 非アクティブ化処理を継承クラスに委譲
            ReturnCode_t returnCode = onRtcExecute(ec_id);

            // タイマーのベースタイムを設定
            if (null != onExecuteTimer) {
                onExecuteTimer.setBaseTime();
            }

            return returnCode;

        } catch (Throwable th) {
            logger.error("定期実行処理に失敗しました。", th);
            throw th;
        }
    }

    /**
     * 定期実行処理。
     * @param ec_id ExecutionContext ID.
     * @return リターンコード。
     */
    protected ReturnCode_t onRtcExecute(int ec_id) {
        return ReturnCode_t.RTC_OK;
    }

    /**
     * リセット時処理に例外処理を追加。
     */
    @Override
    protected ReturnCode_t onReset(int ec_id) {

        try {
            // リセット時処理を継承クラスに委譲
            return onRtcReset(ec_id);

        } catch (Throwable th) {
            logger.error("リセット時処理に失敗しました。", th);
            throw th;
        }
    }

    /**
     * リセット時処理。
     * @param ec_id ExecutionContext ID.
     * @return リターンコード。
     */
    protected ReturnCode_t onRtcReset(int ec_id) {
        return ReturnCode_t.RTC_OK;
    }

}
