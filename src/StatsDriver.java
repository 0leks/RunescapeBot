import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class StatsDriver {
  
  int MAX_HIT = 13;
  int[] counts = new int[MAX_HIT + 1];
  JPanel graph;
  public StatsDriver() {
    JFrame frame = new JFrame("Stats");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(530, 600);
    JPanel panel = new JPanel();
    
    JButton[] buttons = new JButton[counts.length];
    ActionListener buttonListener = (e) -> {
      int i = Integer.parseInt(e.getActionCommand());
      counts[i]++;
      frame.repaint();
    };
    for( int i = 0; i < buttons.length; i++ ) {
      buttons[i] = new JButton(i + "");
      buttons[i].setPreferredSize(new Dimension(60, 40));
      panel.add(buttons[i]);
      buttons[i].addActionListener(buttonListener);
    }
    graph = new JPanel() {
      @Override
      public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int maxCount = 5;
        for( int i = 0; i < counts.length; i++ ) {
          maxCount = Math.max(maxCount, counts[i]);
        }
        for( int i = 0; i < counts.length; i++ ) {
          g.setColor(Color.BLACK);
          g.drawString("" + i, i*getWidth()/counts.length + 2, 15);
          
          g.fillRect(i*getWidth()/counts.length, getHeight() - getHeight()*counts[i]/maxCount, getWidth()/counts.length - 5, getHeight()*counts[i]/maxCount - 5);
          g.setColor(Color.white);
          g.drawString("" + counts[i], i*getWidth()/counts.length + 2, getHeight() - 10);
        }
      }
    };
    graph.setBackground(new Color(150, 150, 150));
    graph.setPreferredSize(new Dimension(400, 400));
    panel.add(graph);
    JButton save = new JButton("Save");
    save.addActionListener((e) -> {
      String s = "";
      for( int i = 0; i < counts.length; i++ ) {
        s += counts[i];
        if( i != counts.length-1) {
          s += ", ";
        }
      }
      File saveFile = new File("stats.txt");
      try {
        PrintWriter writer = new PrintWriter(saveFile);
        writer.println(s);
        writer.close();
      } catch (FileNotFoundException e1) {
        e1.printStackTrace();
      }
    });
    save.setPreferredSize(new Dimension(200, 50));
    panel.add(save);
    frame.add(panel);
    frame.setVisible(true);
  }

  public static void main(String[] args) {
    new StatsDriver();
  }

}
