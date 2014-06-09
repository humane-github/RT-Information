import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;


public class PreviewDialog
{
	private JDialog dialog = null;
	private JLabel imgLabel = null;
	
	public void init()
	{
		dialog = new JDialog();
		imgLabel = new JLabel();
		dialog.setLayout(new BorderLayout());
		dialog.add(imgLabel,BorderLayout.CENTER);
		dialog.setSize(640, 480);
		dialog.setVisible(true);
	}
	
	public void showDialog(Mat imgMat)
	{
		Image img = toBufferedImage(imgMat);
		ImageIcon icon = new ImageIcon(img);
		imgLabel.setIcon(icon);
		dialog.repaint();
	}
	
	public void hideDialog()
	{
		dialog.setVisible(false);
		dialog.dispose();
	}
	
    private Image toBufferedImage(Mat m)
    {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if ( m.channels() > 1 )
        {
            Mat m2 = new Mat();
            Imgproc.cvtColor(m,m2,Imgproc.COLOR_BGR2RGB);
            type = BufferedImage.TYPE_3BYTE_BGR;
            m = m2;
        }
        byte [] b = new byte[m.channels()*m.cols()*m.rows()];
        m.get(0,0,b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
        image.getRaster().setDataElements(0, 0, m.cols(),m.rows(), b);
        return image;
    }
}
