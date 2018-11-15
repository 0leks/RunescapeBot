import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class RuneMining {

  JFrame frame;
  JPanel panel;
  String[] worlds = new String[] { "1", "8", "16", "26", "35", "82", "83", "84", "93", "94" };
  JPanel[] panels = new JPanel[worlds.length];
  JButton[] buttons = new JButton[worlds.length];
  
  Timer timer;
  
  public RuneMining() {
    frame = new JFrame("Rune Mining");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setAlwaysOnTop(true);
    
    panel = new JPanel();
    panel.setLayout(new GridLayout(worlds.length, 2));
    
    for( int i = 0; i < worlds.length; i++ ) {
      JButton button = new JButton(worlds[i]);
      button.setPreferredSize(new Dimension(50, 30));
      panel.add(button);
      
      JPanel worldPanel = new WorldPanel(button);
      worldPanel.setPreferredSize(new Dimension(100, 30));
//      worldPanel.setBackground(new Color((int)(255*Math.random())));
      panel.add(worldPanel);
      panels[i] = worldPanel;
    }
    
    frame.add(panel);
    frame.setSize(500, 500);
    frame.pack();
    frame.setVisible(true);
    timer = new Timer(1000, (e) -> {
      frame.repaint();
    });
    timer.start();
  }
  public class WorldPanel extends JPanel {
    public static final long RESPAWN_TIME = 1000*60*12;
    public static final long FIVE_MINUTES = 1000*60*5;
    long timeUntilRune;
    boolean disabled;
    public WorldPanel(JButton button) {
      disabled = true;
      button.addActionListener((e) -> {
        timeUntilRune = System.currentTimeMillis() + RESPAWN_TIME;
        disabled = false;
        frame.repaint();
      });
      setFont(new Font("Times", Font.PLAIN, 20));
      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
          disabled = !disabled;
          timeUntilRune = System.currentTimeMillis();
          frame.repaint();
        }
      });
    }
    @Override
    public void paintComponent(Graphics g) {
      long timeLeft =  timeUntilRune - System.currentTimeMillis();
      if( timeLeft > 0 && timeLeft < RESPAWN_TIME ) {
        double ratio = 1.0*timeLeft/RESPAWN_TIME;
        Color color = new Color(0, 0, (int)(255*(1-ratio)));
        g.setColor(color);
        g.fillRect(0, 5, (int) ((getWidth()) * (1-ratio)), getHeight()-10);
        
        g.setColor(Color.BLACK);
        g.drawString("" + timeLeft/1000, 60, getHeight()/2 + 10);
        g.setColor(Color.white);
        g.drawString("" + timeLeft/1000, 10, getHeight()/2 + 10);
      }
      if(timeLeft < 0) {
        double ratio = -1.0*timeLeft/FIVE_MINUTES;
        g.setColor(new Color(0, 150, 255));
        g.fillRect(0, 5, getWidth(), getHeight()-10);
        g.setColor(Color.BLACK);
        g.fillRect(0, getHeight()/2-2, (int) (getWidth()*ratio), 5);
        
      }
      if( disabled ) {
        g.setColor(Color.black);
        g.fillRect(0, 5, getWidth(), getHeight()-10);
      }
    }
  }
  
  public static void main(String[] args) {
    new RuneMining();
  }

}
