package piratemap.utils;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Util
{
    public static void showImage(BufferedImage im)
    {
        JFrame jf = new JFrame();
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.getContentPane().add(new JLabel(new ImageIcon(im)));
        jf.getContentPane().setBackground(Color.black);;
        jf.pack();
        jf.setVisible(true);
    }
    
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
