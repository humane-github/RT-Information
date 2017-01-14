package jp.co.humane.rtc.motiondetector;

import jp.co.humane.rtc.common.starter.bean.ConfigBase;

/**
 * 動体検知処理の設定クラス。
 * @author terada.
 *
 */
public class MotionDetectorConfig extends ConfigBase {

    /** 画像の特徴点（コーナー）の最大検出数 */
    private Integer maxCornerSize = 80;

    /**
     * 最も優れた特徴点に対して足切りを行う品質の割合。
     * (最大が10でこの割合が0.1なら1未満の特徴点は使われない)
     */
    private Double qualityExcludeRatio = 0.05;

    /**
     * コーナー間の最小距離。
     * (コーナーが1個所で連続抽出されないようにするための距離)
     */
    private Integer cornersMargin = 5;

    /** 動いたと判定する2つのフレーム間での特徴点の移動距離 */
    private Integer cornerMoveLength = 20;

    /** 条件を満たす特徴点の数 */
    private Integer cornerMoveCount = 20;

    /** ビューアの有効状態 */
    private Boolean enableViewer = false;

    /**
     * maxCornerSizeを取得する。
     * @return maxCornerSize maxCornerSize。
     */
    public Integer getMaxCornerSize() {
        return maxCornerSize;
    }

    /**
     * maxCornerSizeを設定する。
     * @param maxCornerSize maxCornerSize.
     */
    public void setMaxCornerSize(Integer maxCornerSize) {
        this.maxCornerSize = maxCornerSize;
    }

    /**
     * qualityExcludeRatioを取得する。
     * @return qualityExcludeRatio qualityExcludeRatio。
     */
    public Double getQualityExcludeRatio() {
        return qualityExcludeRatio;
    }

    /**
     * qualityExcludeRatioを設定する。
     * @param qualityExcludeRatio qualityExcludeRatio.
     */
    public void setQualityExcludeRatio(Double qualityExcludeRatio) {
        this.qualityExcludeRatio = qualityExcludeRatio;
    }

    /**
     * cornersMarginを取得する。
     * @return cornersMargin cornersMargin。
     */
    public Integer getCornersMargin() {
        return cornersMargin;
    }

    /**
     * cornersMarginを設定する。
     * @param cornersMargin cornersMargin.
     */
    public void setCornersMargin(Integer cornersMargin) {
        this.cornersMargin = cornersMargin;
    }

    /**
     * cornerMoveLengthを取得する。
     * @return cornerMoveLength cornerMoveLength。
     */
    public Integer getCornerMoveLength() {
        return cornerMoveLength;
    }

    /**
     * cornerMoveLengthを設定する。
     * @param cornerMoveLength cornerMoveLength.
     */
    public void setCornerMoveLength(Integer cornerMoveLength) {
        this.cornerMoveLength = cornerMoveLength;
    }

    /**
     * cornerMoveCountを取得する。
     * @return cornerMoveCount cornerMoveCount。
     */
    public Integer getCornerMoveCount() {
        return cornerMoveCount;
    }

    /**
     * cornerMoveCountを設定する。
     * @param cornerMoveCount cornerMoveCount.
     */
    public void setCornerMoveCount(Integer cornerMoveCount) {
        this.cornerMoveCount = cornerMoveCount;
    }

    /**
     * enableViewerを取得する。
     * @return enableViewer enableViewer。
     */
    public Boolean getEnableViewer() {
        return enableViewer;
    }

    /**
     * enableViewerを設定する。
     * @param enableViewer enableViewer.
     */
    public void setEnableViewer(Boolean enableViewer) {
        this.enableViewer = enableViewer;
    }

}
