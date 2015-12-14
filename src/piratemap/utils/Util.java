package piratemap.utils;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Util
{
    /**
     * Show an image in a popup window. Closing the window will stop the process.
     * @param im
     */
    public static void showImage(BufferedImage im)
    {
        JFrame jf = new JFrame();
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.getContentPane().add(new JLabel(new ImageIcon(im)));
        jf.getContentPane().setBackground(Color.black);;
        jf.pack();
        jf.setVisible(true);
    }
    
    /**
     * Process will exit, thereby closing any windows, after x seconds.
     * This means you can keep running the process to see the output without
     * having to close windows individually. 
     * @param secs
     */
    public static void exitAfter(final int secs)
    {
        Thread t = new Thread() {
            
            @Override
            public void run() {
                try
                {
                    Thread.sleep(secs * 1000);
                } catch (InterruptedException e)
                {        }
                
                System.exit(0);
            }
        };
        t.start();
        
    }
}
