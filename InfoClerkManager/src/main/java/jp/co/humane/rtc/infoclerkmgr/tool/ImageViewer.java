package jp.co.humane.rtc.infoclerkmgr.tool;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Panel;

/**
 * 画像ビューア。
 * @author terada.
 *
 */
public class ImageViewer {

    /** フレーム */
    private Frame frame = null;

    /** ラベル */
    private Panel panel = null;

    /**
     * コンストラクタ。
     * @param title タイトル。
     */
    public ImageViewer(String title) {
        frame = new Frame(title);
        panel = new Panel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        frame.add(panel);
    }

    /**
     * イメージを設定する。
     * @param path 画像ファイルのパス。
     */
    public void setImage(String path) {
        ImageComponent img = new ImageComponent(path);
        panel.removeAll();
        panel.add(img);
        frame.setSize(img.getPreferredSize());
        frame.setVisible(true);
    }

    /**
     * 画面を隠す。
     */
    public void hide() {
        frame.setVisible(false);

    }
}
