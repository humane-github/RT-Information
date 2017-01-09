package jp.co.humane.rtc.common.util;

import java.util.concurrent.TimeUnit;

/**
 * 経過時間を用いたタイマー。
 * <p>
 * 以下の用途で使用する。<br>
 * ・ベース時間から指定時間が経過するまでSleepする。<br>
 * ・ベース時間からの経過時間を取得する。<br>
 * </p>
 *
 * @author terada.
 */
public class ElapsedTimer {

    /** ベース時間 */
    private long baseTime = 0;

    /**
     * ベース時間を設定する。
     */
    public void setBaseTime() {
        this.baseTime = System.nanoTime();
    }

    /**
     * ベース時間からの経過時間を取得する。
     * 指定単位で四捨五入した値を返す。
     *
     * @param unit 時間の単位。
     * @return 経過時間。
     */
    public long getElapsedTime(TimeUnit unit) {

        // ベース時間が設定されていない場合は0を返す
        if (0 == baseTime) {
            return 0;
        }

        // ベース時間と現在時刻の差をナノ秒で取得
        long elapsedNanoTime = System.nanoTime() - baseTime;

        // ナノ秒を指定の時間単位に変換した値を返す
        return convertTimeUnit(unit, elapsedNanoTime);

    }

    /**
     * ナノ秒を指定の時間単位に変換する。
     * 小数点以下があれば四捨五入する。
     *
     * @param unit    時間の単位。
     * @param nanoSec ナノ秒。
     * @return 変換後の時間。
     */
    private long convertTimeUnit(TimeUnit unit, long nanoSec) {

        double convertTime = 0;
        switch(unit) {
        case DAYS:
            convertTime = nanoSec / 1000 / 1000 / 1000 / 60 / 60 / 24;
            break;

        case HOURS:
            convertTime = nanoSec / 1000 / 1000 / 1000 / 60 / 60;
            break;

        case MINUTES:
            convertTime = nanoSec / 1000 / 1000 / 1000 / 60;
            break;

        case SECONDS:
            convertTime = nanoSec / 1000 / 1000 / 1000;
            break;

        case MILLISECONDS:
            convertTime = nanoSec / 1000 / 1000;
            break;

        case MICROSECONDS:
            convertTime = nanoSec / 1000;
            break;

        case NANOSECONDS:
            convertTime = nanoSec;
            break;
        }

        // 四捨五入した値を返す
        return Math.round(convertTime);

    }

    /**
     * ベース時間から指定時間が経過するまでSleepする。
     * @param unit  時間の単位。
     * @param span  時間。
     */
    public void wait(TimeUnit unit, long span) {

        // 現在時刻を取得
        long currentTime = System.nanoTime();

        // ベース時間が設定されていない場合は待機しない
        if (0 == baseTime) {
            return;
        }

        // 終了時間をナノ秒で取得
        long endTime = getEndTime(unit, span);

        // 終了時間になるまで待機
        try {
            if (currentTime < endTime) {
                TimeUnit.NANOSECONDS.sleep(endTime - currentTime);
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

    }

    /**
     * 待機が終了する時刻をナノ秒で取得する。
     *
     * @param unit 時間の単位。
     * @param span 時間。
     * @return 待機が終了する時刻。
     */
    private long getEndTime(TimeUnit unit, long span) {

        long endTime = 0;
        switch (unit) {
        case DAYS:
            endTime = baseTime + span * 1000 * 1000 * 1000 * 60 * 60 * 24;
            break;

        case HOURS:
            endTime = baseTime + span * 1000 * 1000 * 1000 * 60 * 60;
            break;

        case MINUTES:
            endTime = baseTime + span * 1000 * 1000 * 1000 * 60;
            break;

        case SECONDS:
            endTime = baseTime + span * 1000 * 1000 * 1000;
            break;

        case MILLISECONDS:
            endTime = baseTime + span * 1000 * 1000;
            break;

        case MICROSECONDS:
            endTime = baseTime + span * 1000;
            break;

        case NANOSECONDS:
            endTime = baseTime + span;
            break;
        }

        return endTime;
    }


}
