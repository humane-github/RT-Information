package jp.co.humane.rtc.juliusclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXB;

import RTC.TimedOctetSeq;
import RTC.TimedWString;
import jp.co.humane.rtc.common.logger.RtcLogger;
import jp.co.humane.rtc.common.port.RtcOutPort;
import jp.co.humane.rtc.common.util.CorbaObj;
import jp.co.humane.rtc.common.util.SleepTimer;
import jp.co.humane.rtc.juliusclient.dto.RecogOutDto;
import jp.co.humane.rtc.juliusclient.dto.ShypoDto;
import jp.co.humane.rtc.juliusclient.dto.WhypoDto;
import jp.go.aist.rtm.RTC.port.ConnectorBase.ConnectorInfo;
import jp.go.aist.rtm.RTC.port.ConnectorDataListenerT;

/**
 * Juliusとの通信を行うクラス。
 * @author terada.
 *
 */
public class JuliusCommunicator extends ConnectorDataListenerT<TimedOctetSeq> {

    /** ロガー */
    private static final RtcLogger logger = new RtcLogger("JuliusClient");

    /** テキストデータ監視周期 */
    private static final long TEXT_WATCH_INTERVAL = 200;

    /** 属性内の開始タグパターン */
    private static final Pattern TAG_START_PATTERN = java.util.regex.Pattern.compile("=\"<([^>]+)>\"");

    /** 属性内の終了タグパターン */
    private static final Pattern TAG_END_PATTERN = java.util.regex.Pattern.compile("=\"</([^>]+)>\"");

    /** PHONE除外リスト(無声音を表す予約語) */
    private static final List<String> EXCLUDE_PHONE_LIST = Arrays.asList("silB", "silE", "sp");

    /** テキスト情報を出力するポート */
    private RtcOutPort<TimedWString> resultOut = null;

    /** 設定情報 */
    private JuliusClientConfig config = null;

    /** 音声データ送信用ソケット */
    private Socket voiceSocket = new Socket();

    /** テキストデータ受信用ソケット */
    private Socket textSocket = new Socket();

    /** Juliusのテキストポート監視状態 */
    private boolean isListenJuliusPort = false;

    /**
     * コンストラクタ。
     *
     * @param config 設定情報。
     */
    public JuliusCommunicator(JuliusClientConfig config, RtcOutPort<TimedWString> resultOut) {
        super(TimedOctetSeq.class);
        this.config = config;
        this.resultOut = resultOut;
    }

    /**
     * 通信を開始する。
     */
    public void start() {

        // 前の通信が閉じられていない場合はエラーとする
        if (isConnected(voiceSocket) || isConnected(textSocket)) {
            String msg = "前回のJuliusとの通信が正常に閉じられていないため通信を開始できませんでした。";
            logger.error(msg);
            throw new RuntimeException(msg);
        }

        // ソケットをオープン
        try {
            textSocket.connect(new InetSocketAddress(config.getJuliusHostname(), config.getJuliusModulePort()));
            voiceSocket.connect(new InetSocketAddress(config.getJuliusHostname(), config.getJuliusAudioPort()));
        } catch (IOException e) {
            String msg = "Juliusとの通信に失敗しました。";
            logger.error(msg);
            throw new RuntimeException(msg);
        }

        // テキスト受信ソケットを監視
        listenTextSocket();

    }

    /**
     * テキストデータを監視する。
     */
    private void listenTextSocket() {

        isListenJuliusPort = true;
        logger.info("Juliusの監視を開始しました。");

        // 別スレッドでソケットのデータを監視
        new Thread(new Runnable() {

            // テキストの読み込みを行う
            @Override
            public void run() {
                readTextSocket();
            }
        }).start();

    }

    /**
     * ソケットからの受信テキストの読み込み処理を行う。
     */
    private void readTextSocket() {

        try {
            // ソケットからReaderを取得
            BufferedReader br = new BufferedReader(new InputStreamReader(textSocket.getInputStream()));

            // テキスト保持用バッファ
            StringBuilder sb = new StringBuilder();

            // ソケットがオープン中は読み込み処理を続ける
            while(!textSocket.isClosed()) {

                // バッファにデータがたまっていない場合は待機
                if (!br.ready()) {
                    SleepTimer.Sleep(TEXT_WATCH_INTERVAL);
                    continue;
                }

                String line = null;
                while ((line = br.readLine()) != null) {

                    // ピリオドまでがひとまとまりのテキストデータ
                    if (line.endsWith(".") ) {
                        onReceiveTextData(sb.toString());
                        sb.setLength(0);
                        break;
                    }

                    // 不正な文字をエンコードしてバッファに追加
                    line = encodeXmlValue(line);
                    sb.append(line + "\n");
                }
            }

            logger.info("Juliusの監視を終了しました。");

        } catch (IOException ex) {
            logger.warn("ソケットの読み取りに失敗しました。テキスト受信処理を終了します。");
            return;
        }
    }

    /**
     * 属性内に含まれるタグをエンコードして無害化する。
     * <tagName attr="<xxx>"> ⇒ <tagname attr="&lt;xxx&gt;">
     * <tagName attr="</xxx>"> ⇒ <tagname attr="&lt;/xxx&gt;">
     *
     * @param xml xmlのテキスト情報。
     * @return エンコードされたxmlテキスト。
     */
    private String encodeXmlValue(String xml) {

        // 終了タグを変換 </XXX> -> &lt;/XXX&gt;
        Matcher m = TAG_END_PATTERN.matcher(xml);
        if (m.find()) {
            xml = m.replaceFirst("=\"&lt;/" + m.group(1) + "&gt;\"");
        }

        // 開始タグを変換 <XXX> -> &lt;XXX&gt;
        m = TAG_START_PATTERN.matcher(xml);
        if (m.find()) {
            xml = m.replaceFirst("=\"&lt;" + m.group(1) + "&gt;\"");
        }

        return xml;
    }

    /**
     * テキストデータ受信時の処理。
     * @param text 受信したテキストデータ。
     */
    private void onReceiveTextData(String text) {

        logger.info("Juliusからの受信データ：\n" + text);

        // 受信内容が解析データではない場合は処理を抜ける
        if (!text.startsWith("<RECOGOUT>")) {
            return;
        }

        // XMLをBeanにデシリアライズ
        RecogOutDto recogOut = JAXB.unmarshal(new StringReader(text), RecogOutDto.class);

        // 認識結果からテキストデータを取得する
        List<WhypoDto> whypoList = new ArrayList<>();
        for (ShypoDto shypoDto : recogOut.getShypoList()) {
            for (WhypoDto whypoDto : shypoDto.getWhypoList()) {
                if (!EXCLUDE_PHONE_LIST.contains(whypoDto.getPhone())) {
                    whypoList.add(whypoDto);
                }
            }
        }

        // テキストデータが見つからない場合は処理を抜ける
        if (0 == whypoList.size()) {
            return;
        }

        // 認識されたテキスト情報を出力ポートに書き込む
        StringBuilder sb = new StringBuilder();
        for (WhypoDto whypo : whypoList) {
            sb.append(whypo.getWord());
        }
        String voiceText = sb.toString();
        resultOut.write(CorbaObj.newTimedWString(voiceText));
        logger.info("[" + voiceText + "]を出力ポートに書き込みました。");
    }

    /**
     * 音声データ受信時の処理。
     * @param info ローカル側の接続情報。
     * @param data 受信データ。
     */
    @Override
    public void operator(ConnectorInfo info, TimedOctetSeq data) {

        // 音声データ送信用ソケットが開いていない場合は何もしない
        if (!isConnected(voiceSocket)) {
            return;
        }

        // 入力ポートから受信したデータをJuliusに送信する
        try {
            OutputStream out = voiceSocket.getOutputStream();
            byte[] octets = data.data;

            out.write(int2octet(octets.length));
            out.write(octets);
            out.flush();

        } catch (IOException ex) {
            String msg = "Juliusへの音声データ送信に失敗しました。";
            logger.error(msg);
            throw new RuntimeException(msg);
        }
    }

    /**
     * 通信を終了する。
     */
    public void stop() {

        // ソケットをクローズさせる
        try {
            voiceSocket.close();
            textSocket.close();
        } catch (IOException ex) {
            throw new RuntimeException("Juliusサーバとのソケットをクローズできませんでした。", ex);
        }
    }


    /**
     * データ長をバイト配列に変換する。
     * @param length 長さ。
     * @return バイト配列。
     */
    private byte[] int2octet(int length) {
        byte[] data = new byte[4];
        for (int i = 0; i < 4; i++) {
            data[i] = (byte)(length & 0xFF);
            length = length >> 8;
        }
        return data;
    }

    /**
     * ソケットの接続状態を返す。
     * @param socket ソケット。
     * @return 接続の有無。
     */
    private boolean isConnected(Socket socket) {

        if (socket.isClosed()) {
            return false;
        }

        if (!socket.isBound() || !socket.isConnected() || socket.isInputShutdown() || socket.isOutputShutdown()) {
            return false;
        }

        return true;

    }

}
