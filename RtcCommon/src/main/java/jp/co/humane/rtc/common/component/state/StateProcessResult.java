package jp.co.humane.rtc.common.component.state;

/**
 * 特定の状態に対する処理結果を表すBean。
 * @author terada.
 *
 */
public class StateProcessResult {

    /** 結果コード */
    private Enum<?> resultEnum = null;

    /** 次のプロセッサに渡す情報 */
    private Object resultData = null;

    /**
     * コンストラクタ。
     * @param resultCode 結果コード。
     */
    public StateProcessResult(Enum<?> resultEnum) {
        this.resultEnum = resultEnum;
    }

    /**
     * コンストラクタ。
     * @param resultCode  結果コード。
     * @param resultData  次のプロセッサに渡す情報。
     */
    public StateProcessResult(Enum<?> resultEnum, Object resultData) {
        this.resultEnum = resultEnum;
        this.resultData = resultData;
    }

    /**
     * resultEnumを取得する。
     * @return resultEnum resultEnum。
     */
    public Enum<?> getResultEnum() {
        return resultEnum;
    }

    /**
     * resultEnumを設定する。
     * @param resultEnum resultEnum.
     */
    public void setResultEnum(Enum<?> resultEnum) {
        this.resultEnum = resultEnum;
    }

    /**
     * resultDataを取得する。
     * @return resultData resultData。
     */
    public Object getResultData() {
        return resultData;
    }

    /**
     * resultDataを設定する。
     * @param resultData resultData.
     */
    public void setPassedData(Object resultData) {
        this.resultData = resultData;
    }

}
