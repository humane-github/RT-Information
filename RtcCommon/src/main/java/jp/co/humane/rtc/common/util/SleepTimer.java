package jp.co.humane.rtc.common.util;

import java.util.concurrent.TimeUnit;

/**
 * InterruptExceptionを発生させないタイマー。
 * @author terada
 *
 */
public class SleepTimer {

    /**
     * 指定時間だけSlepする。
     * @param time 時間。
     * @param unit 時間の単位。
     */
    public static void Sleep(long time, TimeUnit unit) {

        try {
            unit.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 指定時間(ミリ秒)だけSlepする。
     * @param time 時間。
     */
    public static void Sleep(long time) {
        SleepTimer.Sleep(time, TimeUnit.MILLISECONDS);
    }
}
