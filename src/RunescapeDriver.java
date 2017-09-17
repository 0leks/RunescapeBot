import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RunescapeDriver {
  
  private static final double EXTRA_SLEEP = 0.1;
  private static final long BASE_EXTRA_SLEEP = 10;
  
  private static final long IRON_ORE_TIME = 6000;
  private static final long COPPER_ORE_TIME = 2000;
  private static final int INVENTORY_COLOR = -12700375;
  
  private static final int X_OFFSET = -960;
  private static final int EMPTY = 0;
  private static final int SOMETHING = 1;
  private static final int IRON_ORE = 2;
  private static final int SAPPHIRE = 3;
  private static final int EMERALD = 4;
  private JFrame recorderFrame;
  private JFrame mainFrame;
  private JPanel mainPanel;
  private JPanel panel;
  
  private JButton dropButton;
  private JTextField amount;
  
  private JButton fightOne;
  private JButton mineOne;
  private boolean side;
  private volatile int itemsToDrop;
  private volatile int numRocksMined;
  private volatile long startTime;
  private volatile long timeLastMined;
  
  private JButton cookOne;
  
  private JButton recordButton;
  
  private boolean recording;
  private List<Triple> actions;
  
  private Robot robot;
  
  private Semaphore busy;
  
  private List<Thread> running;

  // iron varrock
  private Rectangle topClick = new Rectangle(1422, 474, 40, 37); // top side
  private Rectangle leftClick = new Rectangle(1365, 532, 45, 36); // left side

  // coal lumbridge
  private Rectangle topClickCoal = new Rectangle(X_OFFSET + 1415, 470, 40, 10); // top side
  private Rectangle leftClickCoal = new Rectangle(X_OFFSET + 1350, 520, 45, 36); // left side
  private Rectangle botClickCoal = new Rectangle(X_OFFSET + 1415, 578, 40, 37); // bot side
  private long topTime, leftTime, botTime;
  private JButton verify;
  
  public BufferedImage ironOre;
  public BufferedImage sapphire;
  public BufferedImage emerald;
  
  public double getRandomGaussian(int num) {
    if( num == 2 ) {
      return (Math.random() + Math.random())/2.0;
    }
    if( num == 3 ) {
      return (Math.random() + Math.random() + Math.random())/3;
    }
    if( num == 4 ) {
      return (Math.random() + Math.random() + Math.random() + Math.random())/4;
    }
    if( num == 5 ) {
      return (Math.random() + Math.random() + Math.random() + Math.random() + Math.random())/5;
    }
    return Math.random();
  }
  public class Triple {
    int x, y, z;
    public Triple(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }
  }
  public RunescapeDriver() {
    try {
      ironOre = ImageIO.read(new File("inv/iron_ore.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      sapphire = ImageIO.read(new File("inv/sapphire.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      emerald = ImageIO.read(new File("inv/emerald.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    running = new LinkedList<Thread>();
    busy = new Semaphore(1);
    try {
      robot = new Robot();
    } catch (AWTException e1) {
      e1.printStackTrace();
    }
    actions = new LinkedList<Triple>();
    recorderFrame = new JFrame("Runescape Bot");
    recorderFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());;
    recorderFrame.setUndecorated(true);
    recorderFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    panel = new JPanel();
    panel.addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent e) {
        
      }
      @Override
      public void mousePressed(MouseEvent e) {
        System.out.println("Press = " + e.getX() + ", " + e.getY() + ", " + e.getButton());
      }
      @Override
      public void mouseReleased(MouseEvent e) {
        System.out.println("Release = " + e.getX() + ", " + e.getY() + ", " + e.getButton());
        if( recording ) {Triple t = new Triple(e.getX(), e.getY(), e.getButton());
          actions.add(t);
        }
      }
      @Override
      public void mouseEntered(MouseEvent e) {
        
      }
      @Override
      public void mouseExited(MouseEvent e) {
        
      }
    });
    recorderFrame.addKeyListener(new KeyListener() {
      @Override
      public void keyTyped(KeyEvent e) {
        
      }
      @Override
      public void keyPressed(KeyEvent e) {
        
      }
      @Override
      public void keyReleased(KeyEvent e) {
        if( e.getKeyCode() == KeyEvent.VK_R ) {
          recording = !recording;
        }
        if( e.getKeyCode() == KeyEvent.VK_S ) {
          save();
        }
      }
    });
    recorderFrame.add(panel);
    recorderFrame.setOpacity(0.01f);
    
    mainFrame = new JFrame("Runescape Bot");
    mainFrame.setSize(210, 170);
    mainFrame.setLocation(3390 - 1920 + X_OFFSET, 876);
    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    dropButton = new JButton("Drop");
    dropButton.setFocusable(false);
//    dropButton.setPreferredSize(new Dimension(100, 30));
    dropButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        getDropAmount();
        stopAll();
        dropItemsThreaded(itemsToDrop);
      }
    });
    mainPanel = new JPanel() { 
      @Override
      public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(side) {
          g.setColor(Color.green);
        } else {
          g.setColor(Color.red);
        }
        g.fillRect(0, 0, 10, 10);
        if( busy.availablePermits() == 1 ) {
          g.setColor(Color.green);
        } else if( busy.availablePermits() == 0 ){
          g.setColor(Color.red);
        } else {
          g.setColor(Color.orange);
        }
        g.fillRect(20, 0, 10, 10);
        g.setColor(Color.red);
        g.drawString("" + numRocksMined + "=" + numRocksMined*35 + ", " + (timeLastMined-startTime)/60000, 5, mainPanel.getHeight() - 5);
      }
    };
    amount = new JTextField("24");
    amount.selectAll();
    amount.setBorder(null);
    amount.setBackground(mainPanel.getBackground());
    amount.setPreferredSize(new Dimension(25, 30));
    JPanel dropPanel = new JPanel();
    dropPanel.add(dropButton);
    dropPanel.add(amount);
    mainPanel.add(dropPanel);
    mainPanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
//        stopAll();
        System.exit(0);
      }
    });
    mineOne = new JButton("mine1");
    mineOne.addActionListener((e) -> {
      getDropAmount();
      stopAll();
      mine1();
    });
    mainPanel.add(mineOne);
    fightOne = new JButton("fight1");
    fightOne.addActionListener((e) -> {
      stopAll();
      fight1();
    });
    mainPanel.add(fightOne);
    cookOne = new JButton("Cook");
    cookOne.addActionListener((e) -> {
      stopAll();
      cook1();
    });
    mainPanel.add(cookOne);
    recordButton = new JButton("Rec");
    recordButton.addActionListener((e) -> {
      stopAll();
      mainFrame.setVisible(false);
      mainFrame.dispose();
      recorderFrame.setVisible(true);
    });
    mainPanel.add(recordButton);
    verify = new JButton("Ver");
    verify.addActionListener((e) -> {
      verify();
    });
    mainPanel.add(verify);
    mainFrame.add(mainPanel);
    mainFrame.setAlwaysOnTop(true);
    mainFrame.setVisible(true);
  }
  private void getDropAmount() {
    String am = amount.getText();
    int a = 16;
    try {
      a = Integer.parseInt(am);
    } catch (NumberFormatException ex) {
      
    }
    itemsToDrop = a;
  }
  private void stopAll() {
    Thread stopThread = new Thread(() -> {
      for( Thread t : running ) {
        t.interrupt();
      }
      for( Thread t : running ) {
        try {
          t.join();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
    stopThread.start();
  }
  private void sleep(long time) throws InterruptedException {
    Thread.sleep((long)((Math.random()*BASE_EXTRA_SLEEP + time)*(1 + Math.random()*EXTRA_SLEEP)));
  }
  private void mouseMoveMiss(int x, int y, int missX, int missY) {
    robot.mouseMove(x + (int)((getRandomGaussian(3) * 2 - 1) * missX), y + (int)((getRandomGaussian(3) * 2 - 1) * missY));
  }
  private void mouseMoveMiss(Rectangle rect) throws InterruptedException {
    int targetX = rect.x + (int)(getRandomGaussian(3) * rect.width);
    int targetY = rect.y + (int)(getRandomGaussian(3) * rect.height);
    Point target = new Point(targetX, targetY);
    Point cur;
    int counter = 0;
    do {
      cur = MouseInfo.getPointerInfo().getLocation();
      targetX = (target.x > cur.x)?1:(target.x < cur.x)?-1:0;
      targetY = (target.y > cur.y)?1:(target.y < cur.y)?-1:0;
      robot.mouseMove(cur.x + targetX, cur.y + targetY);
//      Thread.sleep(1);
    }
    while( !cur.equals(target) && counter++ < 1280 );
  }
  public BufferedImage getItemImage(int x, int y) {
    return robot.createScreenCapture(new Rectangle(X_OFFSET + 1720 + x*42, 750 + y*36, 30, 30));
  }
  public void verifyItemPositionIndep() {
    BufferedImage[][] inventory = new BufferedImage[4][7];
    for( int x = 0; x < 4; x++ ) {
      for( int y = 0; y < 7; y++ ) {
        inventory[x][y] = getItemImage(x, y);
      }
    }
    BufferedImage compare = inventory[1][1];
    for( int x = 0; x < 4; x++ ) {
      for( int y = 0; y < 7; y++ ) {
        compare = inventory[x][y];
        System.err.println("Inventory position " + x + ", " + y);
        System.err.println("{");
        for( int i = 0; i < compare.getWidth(); i++ ) {
          for( int j = 0; j < compare.getHeight(); j++ ) {
            Color c = new Color(compare.getRGB(i, j));
            if( c.getRed() <= 64 && c.getRed() >= 59 
                && c.getGreen() <= 54 && c.getGreen() >= 50 
                && c.getBlue() <= 44 && c.getBlue() >= 38 ) {
              compare.setRGB(i, j, Color.white.getRGB());
            }
            else {
              System.err.println("RGB=" + c.getRed() + "," + c.getGreen() + "," + c.getBlue());
            }
          }
        }
        System.err.println("}");
      }
    }
    File folder = new File("inv");
    if( !folder.exists() ) {
      folder.mkdir();
    }
    for( int x = 0; x < 4; x++ ) {
      for( int y = 0; y < 7; y++ ) {
        try {
          ImageIO.write(inventory[x][y], "png", new File("inv/" + x + "_" + y + ".png"));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
  
  public int itemThere(int x, int y) {
    BufferedImage last = robot.createScreenCapture(new Rectangle(X_OFFSET + 1720 + x*42, 750 + y*36, 30, 30));
    int amount = 0;
    int diffIron = 0;
    int diffSapphire = 0;
    int diffEmerald = 0;
    for( int xx = 0; xx < last.getWidth(); xx+=3 ) {
      for( int yy = 0; yy < last.getHeight(); yy+=3 ) {
        if( ironOre.getRGB(xx, yy) != Color.white.getRGB() && last.getRGB(xx, yy) != ironOre.getRGB(xx, yy) ) {
          diffIron++;
        }
        if( sapphire.getRGB(xx, yy) != Color.white.getRGB() && last.getRGB(xx, yy) != sapphire.getRGB(xx, yy) ) {
          diffSapphire++;
        }
        if( emerald.getRGB(xx, yy) != Color.white.getRGB() && last.getRGB(xx, yy) != emerald.getRGB(xx, yy) ) {
          diffEmerald++;
        }
        if( isInv(new Color(last.getRGB(xx, yy) ))) {
          amount++;
        }
        if( diffIron >= 2 && diffSapphire >= 2 && diffEmerald >= 2 && amount >= 96 ) {
          return EMPTY;
        }
      }
    }
//    System.err.println(x + "," + y + "=" + diff + "=" + amount);
    if( diffIron < 2 ) {
      return IRON_ORE;
    }
    if( diffSapphire < 2 ) {
      return SAPPHIRE;
    }
    if( diffEmerald < 2 ) {
      return EMERALD;
    }
    if( amount < 96 ) {
      return SOMETHING;
    }
    return EMPTY;
  }
  public boolean isItemThere(int x, int y) {
    BufferedImage last = robot.createScreenCapture(new Rectangle(X_OFFSET + 1720 + x*42, 750 + y*36, 30, 30));
    int amount = 0;
    for( int xx = 0; xx < last.getWidth(); xx++ ) {
      for( int yy = 0; yy < last.getHeight(); yy++ ) {
        if( last.getRGB(xx, yy) == INVENTORY_COLOR ) {
          amount++;
        }
      }
    }
    if( amount < 750 ) {
      return true;
    }
    return false;
  }
  public int numItems() {
    int items = 0;
    for( int x = 1720; x <= 1846; x += 42 ) {
      for( int y = 750; y <= 966; y += 36 ) {
        BufferedImage last = robot.createScreenCapture(new Rectangle(X_OFFSET + x, y, 30, 30));
        int amount = 0;
        for( int xx = 0; xx < last.getWidth(); xx++ ) {
          for( int yy = 0; yy < last.getHeight(); yy++ ) {
            if( last.getRGB(xx, yy) == INVENTORY_COLOR ) {
              amount++;
            }
          }
        }
        if( amount < 750 ) {
          items++;
        }
      }
    }
    return items;
  }
  public boolean inventoryFull() {
    BufferedImage last = robot.createScreenCapture(new Rectangle(X_OFFSET + 1846, 965, 30, 30));
    int amount = 0;
    for( int x = 0; x < last.getWidth(); x++ ) {
      for( int y = 0; y < last.getHeight(); y++ ) {
        if( last.getRGB(x, y) == INVENTORY_COLOR ) {
          amount++;
        }
      }
    }
//    System.err.println(amount);
    return (amount < 800);
  }
  private List<BufferedImage> loadAll(String prefix) {
    List<BufferedImage> list = new LinkedList<BufferedImage>();
    int count = 1;
    File file = new File(prefix + count + ".png");
    while(file.exists()) {
      try {
        BufferedImage image = ImageIO.read(file);
        list.add(image);
      } catch (IOException e) {
        e.printStackTrace();
      }
      count++;
      file = new File(prefix + count + ".png");
    }
    return list;
  }
  private boolean isGray(Color c) {
    if( Math.abs(c.getRed() - c.getGreen()) < 10 && Math.abs(c.getRed() - c.getBlue()) < 10 ) {
      return true;
    }
    return false;
  }
  private boolean isInv(Color c) {
    if( c.getRed() <= 64 && c.getRed() >= 59 
        && c.getGreen() <= 54 && c.getGreen() >= 50 
        && c.getBlue() <= 44 && c.getBlue() >= 38 ) {
      return true;
    }
    return false;
  }
  private boolean isIron(Color c) {
    if( c.getRed() > c.getGreen() && c.getGreen() > c.getBlue()
        && c.getRed() < 120 && c.getGreen() < 100 && c.getBlue() < 50
        ) {
      return true;
    }
    return false;
  }
  public boolean dispColor(Rectangle rect) {
    BufferedImage image = robot.createScreenCapture(rect);
    int ironCount = 0;
    for( int xx = 0; xx < image.getWidth(); xx++ ) {
      for( int yy = 0; yy < image.getHeight(); yy++ ) {
        Color c = new Color(image.getRGB(xx, yy));
       if( isIron(c) ) {
//        if( c.getRed() > 60 && c.getRed() < 90 
//            && c.getGreen() > 30 && c.getGreen() < 60
//            && c.getBlue() > 20 && c.getBlue() < 40 ) {
          ironCount++;
          image.setRGB(xx, yy, Color.white.getRGB());
        }
//        System.err.println("(RGB)=(" + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue() + ")");
      }
    }
    try {
      ImageIO.write(image, "png", new File("iron rock" + rect.x + "_" + rect.y + ".png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    double ratio = ironCount * 1.0 / image.getWidth() / image.getHeight();
    if( ratio > 0.4 ) { 
      return true;
    }
    return false;
  }
  private boolean isRockAvailable(Rectangle rect) {
    BufferedImage image = robot.createScreenCapture(rect);
    int numGray = 0;
    int numIron = 0;
    for( int x = 0; x < image.getWidth(); x++ ) {
      for( int y = 0; y < image.getHeight(); y++ ) {
        Color col = new Color(image.getRGB(x, y));
        if( isGray(col) ) {
          numGray++;
        }
        if( isIron(col) ) {
//          image.setRGB(x, y, Color.red.getRGB());
          numIron++;
        }
      }
    }
    double ratioGray = 1.0 * numGray / (image.getWidth()*image.getHeight());
    double ratioIron = numIron * 1.0 / image.getWidth() / image.getHeight();
//    try {
//      ImageIO.write(image, "png", new File("iron" + rect.x + "_" + rect.y + ".png"));
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
    if( ratioIron > 0.4 ) { 
      return true;
    }
    return false;
//    if( ratioGray > 0.5 ) {
//      return false;
//    }
//    return true;
  }
  public Point closestGoblin() {
    long start = System.currentTimeMillis();
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    BufferedImage image = robot.createScreenCapture(new Rectangle(960 + X_OFFSET,0,X_OFFSET + size.width, size.height));
    List<Point> goblins = new LinkedList<Point>();
    for( int x = 100; x < image.getWidth() - 100; x+=10 ) {
      for( int y = 200; y < image.getHeight() - 200; y+=10 ) {
        if( x < image.getWidth()-250 || y < image.getHeight()-380) {
          if( isGoblin(new Color(image.getRGB(x, y)))) {
            goblins.add(new Point(x, y));
          }
        }
      }
    }
    int closest = Integer.MAX_VALUE;
    Point closestGoblin = null;
    for( Point p : goblins ) {
      int xDist = (int) Math.abs(p.getX() - 480);
      int yDist = (int) Math.abs(p.getY() - 540);
      if( xDist < 100 && yDist < 100 ) {
        xDist = 400;
        yDist = 400;
      }
      if( xDist + yDist < closest ) {
        closest = xDist + yDist;
        closestGoblin = p;
      }
    }
    long end = System.currentTimeMillis();
    System.err.println("Took " + (end - start) + "ms to find goblins long range search.");
    return closestGoblin;
  }
  public boolean isGoblin(Color c) {
    double ratio1 = (double)(c.getGreen()) / c.getRed();
    double ratio2 = (double)(c.getGreen()) / c.getBlue();
    return (ratio1 > 1 && ratio1 < 1.06 && ratio2 > 2.6 && ratio2 < 2.9)
        || (ratio1 > 2.1 && ratio1 < 2.3 && ratio2 > 1.5 && ratio2 < 1.7);
  }
  public void verify() {
    verifyItemPositionIndep();
    System.err.println(mainFrame.getX() + "," + mainFrame.getY());
    System.err.println("~~~~~");
    System.err.println("top coal = " + isRockAvailable(topClickCoal));
    System.err.println("left coal = " + isRockAvailable(leftClickCoal));
    System.err.println("bot coal = " + isRockAvailable(botClickCoal));
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    BufferedImage image = robot.createScreenCapture(new Rectangle(960 + X_OFFSET,0,X_OFFSET + size.width, size.height));
    Graphics2D g = (Graphics2D)image.getGraphics();
    g.setColor(Color.red);
//    g.draw(topClick);
//    g.draw(leftClick);
    g.draw(topClickCoal);
    g.draw(leftClickCoal);
    g.draw(botClickCoal);
    for( int y = 0; y < 7; y++) {
      for( int x = 0; x < 4; x++ ) {
        int item = itemThere(x, y);
        System.err.println("Item = " + item);
        g.draw(getItemLocation(x, y));
      }
    }
    for( int x = 100; x < image.getWidth() - 200; x++ ) {
      for( int y = 200; y < image.getHeight() - 200; y++ ) {
        Color color = new Color(image.getRGB(x, y));
        if(isGoblin(color)) {
          image.setRGB(x, y, Color.WHITE.getRGB());
        }
      }
    }
    g.drawRect(440, 500, 80, 80);
    g.drawRect(100, 200, image.getWidth()-200, image.getHeight()-400);
    g.drawRect(image.getWidth()-250, image.getHeight()-380, 150, 250);
    try {
      ImageIO.write(image, "png", new File("verify.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    g.dispose();
  }
  int fishLeft = 1335;
  public void cook1() {
    Thread thread = new Thread(() -> {
      try {
        busy.acquire();
        mainFrame.repaint();
        while( fishLeft > 0 ) {
          // click bank
          mouseClickMiss(new Rectangle(X_OFFSET + 1556, 32, 31, 9), 50, InputEvent.BUTTON1_MASK);
          sleep(8000);
          //deposit items
          mouseClickMiss(new Rectangle(X_OFFSET + 1483, 814, 29, 28), 50, InputEvent.BUTTON1_MASK);
          sleep(2000);
          // right click tuna
          mouseClickMiss(new Rectangle(X_OFFSET + 1135, 137, 17, 22), 50, InputEvent.BUTTON3_MASK);
          sleep(2000);
          Point mouse = MouseInfo.getPointerInfo().getLocation();
          // choose withdraw all
          mouseClickMiss(new Rectangle(X_OFFSET + mouse.x - 95, mouse.y + 95, 188, 10), 50, InputEvent.BUTTON1_MASK);
          sleep(2000);
          // move to stairs
          mouseClickMiss(new Rectangle(X_OFFSET + 1796, 161, 20, 15), 50, InputEvent.BUTTON1_MASK);
          sleep(8000);
          // click stairs 1
          mouseClickMiss(new Rectangle(X_OFFSET + 1405, 580, 54, 35), 50, InputEvent.BUTTON1_MASK);
          sleep(6000);
          // click stairs 2
          mouseClickMiss(new Rectangle(X_OFFSET + 1324, 537, 73, 62), 50, InputEvent.BUTTON1_MASK);
          sleep(3000);
          // choose go down
          mouseClickMiss(new Rectangle(X_OFFSET + 1000, 963, 450, 12), 50, InputEvent.BUTTON1_MASK);
          sleep(4000);
          // select tuna
          mouseClickMiss(new Rectangle(X_OFFSET + 1733, 765, 14, 15), 50, InputEvent.BUTTON1_MASK);
          sleep(2000);
          // click range
          mouseClickMiss(new Rectangle(X_OFFSET + 1755, 193, 22, 30), 50, InputEvent.BUTTON1_MASK);
          sleep(8000);
          // right click cook
          mouseClickMiss(new Rectangle(X_OFFSET + 1096, 949, 250, 37), 50, InputEvent.BUTTON3_MASK);
          mouse = MouseInfo.getPointerInfo().getLocation();
          sleep(2000);
          // select cook all
          mouseClickMiss(new Rectangle(X_OFFSET + mouse.x - 48, 1008, 95, 9), 50, InputEvent.BUTTON1_MASK);
          sleep(66000);
          // move to barrel
          mouseClickMiss(new Rectangle(X_OFFSET + 1016, 771, 29, 29), 50, InputEvent.BUTTON1_MASK);
          sleep(8000);
          //click stairs
          mouseClickMiss(new Rectangle(X_OFFSET + 1292, 713, 60, 64), 50, InputEvent.BUTTON1_MASK);
          sleep(6000);
          //click stairs
          mouseClickMiss(new Rectangle(X_OFFSET + 1364, 584, 71, 65), 50, InputEvent.BUTTON1_MASK);
          sleep(4000);
          //choose go up
          mouseClickMiss(new Rectangle(X_OFFSET + 1058, 933, 400, 11), 50, InputEvent.BUTTON1_MASK);
          sleep(4000);
          fishLeft -= 28;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      finally {
        if( busy.availablePermits() == 0 ) {
          busy.release();
          mainFrame.repaint();
        }
      }
    });
    running.add(thread);
    thread.start();
  }
  public void mouseClickMiss(Rectangle area, long delay, int button) throws InterruptedException {
    mouseMoveMiss(area);
    sleep(delay);
    robot.mousePress(button);
    sleep(delay);
    robot.mouseRelease(button);
    sleep(delay);
  }
  public void chop1() {
    startTime = System.currentTimeMillis();
    Thread thread = new Thread(() -> {
      try {
        busy.acquire();
        mainFrame.repaint();
//        selectScreen();

        while(true) {
          mouseClickMiss(new Rectangle(521, 511, 36, 26), 50, InputEvent.BUTTON1_MASK);
          sleep(20000);
          dropItems(1, 20, SOMETHING);
        }
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    });
    running.add(thread);
    thread.start();
  }
  volatile Rectangle selected;
  volatile Rectangle second;
  volatile Rectangle third;
  volatile Rectangle fourth;
  public void mine1() { // iron ore at al kharid
    startTime = System.currentTimeMillis();
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          busy.acquire();
          mainFrame.repaint();
          selectScreen();
//          if( isRockAvailable(topClickCoal) ) {
//            selected = topClickCoal;
//          }
//          if( isRockAvailable(leftClickCoal) ) {
//            selected = leftClickCoal;
//          }
//          if( isRockAvailable(botClickCoal) ) {
//            selected = botClickCoal;
//          }
//          System.exit(0);
//          for( int i = 0; i < 50; i++ ) {
//            mouseMoveMiss(topClickCoal); // top side
//            sleep(100);
//            robot.mousePress(InputEvent.BUTTON1_MASK);
//            sleep(20);
//            robot.mouseRelease(InputEvent.BUTTON1_MASK);
//          }
//          System.exit(0);
          while(true) {
            selected = null;
            boolean shiftDown = false;
            for( int j = 0; j < 7 && selected == null; j++ ) {
              boolean dropped = false;
              for( int i = 0; i < 4 && selected == null; i++ ) {
                if( isRockAvailable(topClickCoal) ) {
                  selected = topClickCoal;
                }
                else if( isRockAvailable(leftClickCoal) ) {
                  selected = leftClickCoal;
                }
                else if( isRockAvailable(botClickCoal) ) {
                  selected = botClickCoal;
                }
                if( selected == null ) {
                  int itemThere = itemThere(i, j);
                  if( itemThere == IRON_ORE || itemThere == SAPPHIRE || itemThere == EMERALD ) {
                    if( !shiftDown ) {
                      shiftDown = true;
                      robot.keyPress(KeyEvent.VK_SHIFT);
                      sleep(50);
                    }
                    mouseMoveMiss(getItemLocation(i, j));
                    sleep(20);
                    robot.mousePress(InputEvent.BUTTON1_MASK);
                    sleep(20);
                    robot.mouseRelease(InputEvent.BUTTON1_MASK);
                    numRocksMined++;
                    timeLastMined = System.currentTimeMillis();
                    mainFrame.repaint();
                    sleep(20);
                    dropped = true;
                  }
                }
              }
              if( dropped ) {
                sleep(200);
              }
            }
            while( selected == null ) {
              if( isRockAvailable(topClickCoal) ) {
                selected = topClickCoal;
              }
              else if( isRockAvailable(leftClickCoal) ) {
                selected = leftClickCoal;
              }
              else if( isRockAvailable(botClickCoal) ) {
                selected = botClickCoal;
              }
            }
            mouseMoveMiss(selected);
            if( shiftDown ) {
              shiftDown = false;
              robot.keyRelease(KeyEvent.VK_SHIFT);
            }
            sleep(50);
            robot.mousePress(InputEvent.BUTTON1_MASK);
            sleep(50);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            int count = 0;
            do {
              sleep(100);
              count++;
            } while ( isRockAvailable(selected) && count < 50);
            long curTime = System.currentTimeMillis();
            if( selected == topClickCoal ) {
              topTime = curTime;
            } else if( selected == leftClickCoal ) {
              leftTime = curTime;
            } else if( selected == botClickCoal ) {
              botTime = curTime;
            }
            if( inventoryFull() ) {
              dropItems(0, 0, IRON_ORE);
              continue;
            }
            Rectangle preMove = null;
            if( topTime < leftTime && topTime < botTime && curTime - topTime < 5000 ) {
              preMove = topClickCoal;
            }
            if( leftTime < topTime && leftTime < botTime && curTime - leftTime < 5000 ) {
              preMove = leftClickCoal;
            }
            if( botTime < leftTime && botTime < topTime && curTime - botTime < 5000 ) {
              preMove = botClickCoal;
            }
            if(preMove != null) {
              mouseMoveMiss(preMove);
              sleep(20);
              robot.mousePress(InputEvent.BUTTON1_MASK);
              sleep(10);
              robot.mouseRelease(InputEvent.BUTTON1_MASK);
              sleep(10);
            }
            
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        finally {
          robot.keyRelease(KeyEvent.VK_SHIFT);
          if( busy.availablePermits() == 0 ) {
            busy.release();
            mainFrame.repaint();
          }
        }
      }
    });
    running.add(thread);
    thread.start();
    
  }
  
  public boolean checkHealth() {
    BufferedImage image = robot.createScreenCapture(new Rectangle(960 + X_OFFSET + 380, 440, 200, 200));

    for( int y = 0; y < image.getHeight(); y++ ) {
      for( int x = 0; x < image.getWidth(); x++ ) {
        if( image.getRGB(x, y) == Color.GREEN.getRGB() ) {
          y++;
          x = -1;
          continue;
//          while( x < image.getWidth() ) {
//            if( image.getRGB(x, y) != Color.RED.getRGB() 
//                &&  image.getRGB(x, y) != Color.GREEN.getRGB() ) {
//
//              image.setRGB(x, y, Color.YELLOW.getRGB());
//              break;
//            }
//            image.setRGB(x, y, Color.BLUE.getRGB());
//            x++;
//          }
        }
        else if( image.getRGB(x, y) == Color.RED.getRGB() ) {
          for( int z = x; z < image.getWidth(); z++ ) {
            if( image.getRGB(z, y) != Color.RED.getRGB() ) {
              if( z > 20 ) {
                System.err.println("Long red = " + z);
                return true;
              }
              else {
                System.err.println("Short red = " + z);
              }
            }
          }
          image.setRGB(x, y, Color.MAGENTA.getRGB());
//          try {
//            ImageIO.write(image, "png", new File(System.currentTimeMillis() + "hp.png"));
//          } catch (IOException e) {
//            e.printStackTrace();
//          }
        }
      }
    }
//    try {
//      ImageIO.write(image, "png", new File(System.currentTimeMillis() + "hp.png"));
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
    return false;
  }
  public void fight1() { // iron ore at al kharid
    startTime = System.currentTimeMillis();
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          busy.acquire();
          mainFrame.repaint();
          
          while(true) {
            Point goblin = closestGoblin();
            while( goblin == null ) {
              sleep(2000);
              goblin = closestGoblin();
            }
            mouseClickMiss(new Rectangle(goblin.x, goblin.y, 1, 1), 100, InputEvent.BUTTON1_MASK);
            sleep(2000);
            for( int i = 0; i < 20; i++ ) {
              if( checkHealth() ) {
                System.err.println("DEAD");
                break;
              }
              sleep(500);
            }
            sleep(1000);
          } 
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        finally {
          if( busy.availablePermits() == 0 ) {
            busy.release();
            mainFrame.repaint();
          }
        }
      }
    });
    running.add(thread);
    thread.start();
    
  }

  public void selectScreen() throws InterruptedException {
    mouseMoveMiss(new Rectangle(X_OFFSET + 1693, 869, 7, 127));
    sleep(20);
    robot.mousePress(InputEvent.BUTTON1_MASK);
    sleep(20);
    robot.mouseRelease(InputEvent.BUTTON1_MASK);
    sleep(200);
  }
  public void dropItems(int skip, int delay, int itemID) throws InterruptedException {
    try {
      robot.keyPress(KeyEvent.VK_SHIFT);
      sleep(100 + delay);
      int amount = 0;
      for (int y = 0; y < 7; y++) {
        boolean dropped = false;
        for (int x = 0; x < 4; x++) {
          if (amount >= skip && itemThere(x, y) == itemID ) {
            mouseMoveMiss(getItemLocation(x, y));
            sleep(12 + delay);
            robot.mousePress(InputEvent.BUTTON1_MASK);
            sleep(12 + delay);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            sleep(12 + delay);
            dropped = true;
          }
          amount++;
        }
        if (dropped) {
          sleep(delay);
        }
      }
    } finally {
      robot.keyRelease(KeyEvent.VK_SHIFT);
    }
  }
  public void dropItemsThreaded(int skip) {
    dropItemsThreaded(skip, 0);
  }
  public Rectangle getItemLocation(int x, int y) {
    return new Rectangle(X_OFFSET + 1723 + x*43, 755 + y*36, 20, 20);
  }
  public void dropItemsThreaded(int skip, int delay) {
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          busy.acquire();
          mainFrame.repaint();
          selectScreen();
          robot.keyPress(KeyEvent.VK_SHIFT);
          sleep(100 + delay);
          int amount = 0;
          for( int y = 0; y < 7; y ++ ) {
            boolean dropped = false;
            for( int x = 0; x < 4; x++ ) {
              if( amount >= skip && isItemThere(x, y)) {
                mouseMoveMiss(getItemLocation(x, y));
                sleep(12 + delay);
                robot.mousePress(InputEvent.BUTTON1_MASK);
                sleep(12 + delay);
                robot.mouseRelease(InputEvent.BUTTON1_MASK);
                sleep(12 + delay);
                dropped = true;
              }
              amount++;
            }
            if( dropped ) {
              sleep(delay);
            }
          }
          robot.keyRelease(KeyEvent.VK_SHIFT);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        finally {
          if( busy.availablePermits() == 0 ) {
            busy.release();
            mainFrame.repaint();
          }
        }
      }
    });
    running.add(thread);
    thread.start();
  }
  
  public void save() {
    File file = new File("asdf.txt");
    try {
      BufferedWriter p = new BufferedWriter(new PrintWriter(file));
      for( Triple t : actions ) {
        String str = t.x + ", " + t.y + ", " + t.z;
        p.write(str);
        p.newLine();
      }
      p.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    new RunescapeDriver();
  }

}
