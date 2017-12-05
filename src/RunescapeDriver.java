import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

public class RunescapeDriver {

  private static final int FRAME_WIDTH = 210;
  private static final int FRAME_HEIGHT = 170;
  
  private static final double EXTRA_SLEEP = 0.1;
  private static final long BASE_EXTRA_SLEEP = 10;
  
  private static final long IRON_ORE_TIME = 6000;
  private static final long COPPER_ORE_TIME = 2000;
  private static final int INVENTORY_COLOR = -12700375;

  private static final int[] Y_OFFSET = {0, -2};
  private static final int[] X_OFFSET = {-960, -1920};
  private static final int[] X_ITEM_OFFSET = {0, 1};
  private int offsetIndex;
  
  private List<BufferedImage> itemImages;
  private String[] itemNames;
  private Map<String, Integer> itemNameMap;
  private static final int EMPTY = -1;
  private static final int SOMETHING = -2;
  
  
  private JFrame recorderFrame;
  private JFrame mainFrame;
  private JPanel mainPanel;
  private JPanel otherPanel;
  private JPanel fishPanel;
  private JPanel panel;
  private Timer timer;
  private long launchTime;
  private final long SIX_HOURS = (long) ((5.9 + 0.1*Math.random()) * 3600000 * 2);
  private long lastSave = 0;
  private final long TEN_MINUTES = (long)(600000);
  
  private JComboBox<String> layoutChooser;
  private String[] offsetNames = { "-960", "-1920" };
  private JComboBox<String> offsetChooser;
  private List<JPanel> layouts;
  
  private JButton fightOne;
  private long startExp;
  private JButton mineOne;
  private boolean side;
  private int defaultLayout = 0;
  private volatile int itemsToDrop = 24;
  private volatile int numRocksMined;
  private volatile long startTime;
  private volatile long timeLastMined;
  private String FILE_NAME;
  
  private JButton chopOne;
  
  private JButton recordButton;
  
  private boolean recording;
  private List<Triple> actions;
  
  private Robot robot;
  
  private volatile Semaphore busy;
  
  private List<Thread> running;

  // iron varrock
  private Rectangle topClick = new Rectangle(1422, 474, 40, 37); // top side
  private Rectangle leftClick = new Rectangle(1365, 532, 45, 36); // left side

  // coal lumbridge
  private Rectangle topClickCoal;
  private Rectangle leftClickCoal;
  private Rectangle botClickCoal;
  private long topTime, leftTime, botTime;
  private JButton verify;
  
  
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
  
  public void setOffsetIndex(int index) {
    offsetIndex = index;
    topClickCoal = new Rectangle(X_OFFSET[offsetIndex] + 1415, Y_OFFSET[offsetIndex] + 470, 40, 10); // top side
    leftClickCoal = new Rectangle(X_OFFSET[offsetIndex] + 1350, Y_OFFSET[offsetIndex] + 520, 45, 36); // left side
    botClickCoal = new Rectangle(X_OFFSET[offsetIndex] + 1415, Y_OFFSET[offsetIndex] + 578, 40, 37); // bot side
    if( mainFrame != null ) {
      mainFrame.setLocation(3390 - 1920 + X_OFFSET[offsetIndex], 876);
    }
    if( offsetChooser != null ) {
      offsetChooser.setSelectedIndex(offsetIndex);
    }
    
  }
  public void setLayoutIndex(int index) {
    defaultLayout = index;
    if( mainPanel != null ) {
      mainPanel.removeAll();
      mainPanel.add(layouts.get(layoutChooser.getSelectedIndex()), BorderLayout.CENTER);
      mainPanel.validate();
      mainFrame.repaint();
      System.err.println("main:" + mainPanel.getSize().width + ", " + mainPanel.getSize().height);
      System.err.println("cur:" + layouts.get(layoutChooser.getSelectedIndex()).getSize().width + ", " + layouts.get(layoutChooser.getSelectedIndex()).getSize().height);
      
    }
  }
  public void loadSettings() {
    BufferedReader bufferedReader = null;
    try {
      FileReader fileReader = new FileReader("settings.txt");
      bufferedReader = new BufferedReader(fileReader);
      String line = bufferedReader.readLine();
      this.itemsToDrop = Integer.parseInt(line);
      line = bufferedReader.readLine();
      setLayoutIndex(Integer.parseInt(line));
      line = bufferedReader.readLine();
      setOffsetIndex(Integer.parseInt(line));
      System.err.println("loaded offsetIndex = " + offsetIndex);
      
    } catch( Exception e ) {
      e.printStackTrace();
    } finally {
      if( bufferedReader != null ) {
        try {
          bufferedReader.close();
        } catch (IOException e) {
        }  
      }
    }
  }
  public void saveSettings() {
    FileWriter fileWriter;
    try {
      fileWriter = new FileWriter("settings.txt");
      BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
      bufferedWriter.write(itemsToDrop + "");
      bufferedWriter.newLine();
      bufferedWriter.write(layoutChooser.getSelectedIndex() + "");
      bufferedWriter.newLine();
      bufferedWriter.write(offsetIndex + "");
      bufferedWriter.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  String[] layoutNames = { "Fishing", "Other" };

  private void loadItems() {
    File itemsFolder = new File("itemImages");
    File[] itemFiles = itemsFolder.listFiles();
    itemNames = new String[itemFiles.length];
    itemImages = new ArrayList<BufferedImage>();
    for( int i = 0; i < itemFiles.length; i++ ) {
      itemNames[i] = itemFiles[i].getName().substring(0, itemFiles[i].getName().length() - 4);
      try {
        itemImages.add(ImageIO.read(itemFiles[i]));
      } catch (IOException e) {
        System.err.println(itemNames[i] + " failed");
        e.printStackTrace();
      }
    }
    itemNameMap = new HashMap<String, Integer>();
    for( int i = 0; i < itemNames.length; i++ ) {
      itemNameMap.put(itemNames[i], i);
    }
    
  }
  public RunescapeDriver() {
    loadItems();
    timeLastMined = System.currentTimeMillis();
    startTime = System.currentTimeMillis();
    FILE_NAME = "file" + startTime/100000;
    loadItems();
    loadSettings();
//    try {
//      ironOre = ImageIO.read(new File("inv/iron_ore.png"));
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//    try {
//      sapphire = ImageIO.read(new File("inv/sapphire.png"));
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//    try {
//      emerald = ImageIO.read(new File("inv/emerald.png"));
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
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
    mainFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mainFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent arg0) {
        System.err.println("closing, saving settings");
        saveSettings();
      }
    });

    mainPanel = new JPanel() {
      
    };
    mainPanel.setLayout(new BorderLayout());
    fishPanel = new JPanel() {
    };
    JButton dropFishButton = new JButton("Drop -Raw Salmon");
    dropFishButton.setFocusable(false);
//    dropButton.setPreferredSize(new Dimension(100, 30));
    
    dropFishButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        stopAll();
        dropItemsThreaded(itemsToDrop,(itemid) -> {
          if( itemid == itemNameMap.get("raw_salmon") ) {
            return false;
          }
          return true;
        });
      }
    });
    fishPanel.add(dropFishButton);
    otherPanel = new JPanel() { 
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
        long curXP = ImageProcessor.getExperience(robot);
        g.drawString("" + numRocksMined + "=" + numRocksMined*35 + ", " + (timeLastMined-startTime)/60000 + ", D=" + (curXP - startExp) + ", exp=" + curXP, 5, otherPanel.getHeight() - 5);
      }
    };

    layouts = new ArrayList<JPanel>();
    
    layouts.add(fishPanel);
    layouts.add(otherPanel);
    
    layoutChooser = new JComboBox<String>(layoutNames);
    layoutChooser.setFont(new Font("Comic Sans", Font.PLAIN, 10));
//    layoutChooser.setPreferredSize(new Dimension(FRAME_WIDTH/2, 15));
    layoutChooser.setSelectedIndex(defaultLayout);
    mainPanel.add(layouts.get(defaultLayout), BorderLayout.CENTER);
    layoutChooser.addActionListener((e) -> {
      if( layoutChooser.getSelectedIndex() < layouts.size() ) {
        setLayoutIndex(layoutChooser.getSelectedIndex());
      }
    });
    
    offsetChooser = new JComboBox<String>(offsetNames);
    offsetChooser.setFont(new Font("Comic Sans", Font.PLAIN, 10));
//    offsetChooser.setPreferredSize(new Dimension(FRAME_WIDTH, 15));
    offsetChooser.setSelectedItem(offsetIndex);
    setOffsetIndex(offsetIndex);
    offsetChooser.addActionListener((e) -> {
      if( offsetChooser.getSelectedIndex() < X_OFFSET.length ) {
        setOffsetIndex(offsetChooser.getSelectedIndex());
      }
    });
  
    for(int i = 0; i < layouts.size(); i++ ) {
      JButton dropButton;
      JTextField amount;
      amount = new JTextField(itemsToDrop + "");
      amount.selectAll();
      amount.setBorder(null);
      amount.setBackground(otherPanel.getBackground().brighter());
      amount.setPreferredSize(new Dimension(25, 30));
      amount.getDocument().addDocumentListener(new DocumentListener() {
        public void changedUpdate(DocumentEvent e) {
          update(e);
        }
        public void removeUpdate(DocumentEvent e) {
          update(e);
        }
        public void insertUpdate(DocumentEvent e) {
          update(e);
        }
        public void update(DocumentEvent e) {
          try {
            String text = e.getDocument().getText(0, e.getDocument().getLength());
            try {
              int a = Integer.parseInt(text);
              itemsToDrop = a;
            } catch (NumberFormatException ex) {
              
            }
          } catch (BadLocationException e1) {
            e1.printStackTrace();
          }
        }
      });
      dropButton = new JButton("Drop");
      dropButton.setFocusable(false);
  //    dropButton.setPreferredSize(new Dimension(100, 30));
      dropButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          stopAll();
          dropItemsThreaded(itemsToDrop, (itemid) -> {
            return true;
          });
        }
      });
      JPanel dropPanel = new JPanel();
//      dropPanel.setBackground(new Color(0, 0, 0));
      dropPanel.add(dropButton);
      dropPanel.add(amount);
      layouts.get(i).add(dropPanel);

      layouts.get(i).addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          stopAll();
//          System.exit(0);
        }
      });
    }
    
    mineOne = new JButton("mine1");
    mineOne.addActionListener((e) -> {
      stopAll();
      mine1();
    });
    otherPanel.add(mineOne);
    fightOne = new JButton("fight1");
    fightOne.addActionListener((e) -> {
      stopAll();
      fight1();
    });
    otherPanel.add(fightOne);
    chopOne = new JButton("Chop");
    chopOne.addActionListener((e) -> {
      stopAll();
      chop3();
    });
    otherPanel.add(chopOne);
    recordButton = new JButton("Rec");
    recordButton.addActionListener((e) -> {
      stopAll();
      mainFrame.setVisible(false);
      mainFrame.dispose();
      recorderFrame.setVisible(true);
    });
    otherPanel.add(recordButton);
    verify = new JButton("Ver");
    verify.addActionListener((e) -> {
      verify();
    });
    otherPanel.add(verify);
    JPanel northPanel = new JPanel();
    northPanel.setPreferredSize(new Dimension(FRAME_WIDTH, 15));
    northPanel.setLayout(new BorderLayout());
    northPanel.add(layoutChooser, BorderLayout.WEST);
    northPanel.add(offsetChooser, BorderLayout.EAST);
    mainFrame.add(northPanel, BorderLayout.NORTH);
    mainFrame.add(mainPanel, BorderLayout.CENTER);
    mainFrame.setAlwaysOnTop(true);
    mainFrame.setVisible(true);
    launchTime = System.currentTimeMillis();
    timer = new Timer(1000, (e) -> {
      mainFrame.repaint();
      if( System.currentTimeMillis() - launchTime > SIX_HOURS) {
        try {
          shutDown();
        } catch (InterruptedException e1) {
          e1.printStackTrace();
        }
      }
    });
    timer.start();
  }
  public void saveData(long delay) {
    long curXP = ImageProcessor.getExperience(robot);
    long deltaXP = curXP - startExp; 
    long timeElapsed = (System.currentTimeMillis() - startTime)/1000;
    
    try {
      BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME + ".txt", true));
      String line = timeElapsed + ", " + deltaXP + ", " + delay;
//      System.err.println(line);
      bw.write(line);
      bw.newLine();
      bw.close();
    } catch (IOException e) {
    }
  }
  private void stopAll() {
    for( Thread t : running ) {
      if( !t.isInterrupted() && t.isAlive()) {
        System.err.println(t.getName() + " will be terminated" );
        t.interrupt();
      }
    }
    for(int i = 0; i < running.size(); i++ ) {
      Thread t = running.get(i);
      try {
        if( !t.isInterrupted() && t.isAlive() ) {
          System.err.println(t.getName() + " will be joined" );
          t.join();
        }
        running.remove(t);
        i--;
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    if( busy.availablePermits() == 0 ) {
      busy.release();
    }
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
    return robot.createScreenCapture(new Rectangle(X_OFFSET[offsetIndex] + X_ITEM_OFFSET[offsetIndex] + 1720 + x*42, Y_OFFSET[offsetIndex] + 750 + y*36, 30, 30));
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
    BufferedImage last = getItemImage(x, y);
    int amount = 0;
    int[] diff = new int[itemNames.length];
    for( int xx = 0; xx < last.getWidth(); xx+=3 ) {
      for( int yy = 0; yy < last.getHeight(); yy+=3 ) {
        for( int i = 0; i < itemImages.size(); i++ ) {
          if( itemImages.get(i).getRGB(xx, yy) != Color.white.getRGB() && last.getRGB(xx, yy) != itemImages.get(i).getRGB(xx, yy) ) {
            diff[i]++;
          }
        }
        if( isInv(new Color(last.getRGB(xx, yy) ))) {
          amount++;
        }
        boolean empty = amount >= 96;
        for( int i = 0; i < itemImages.size(); i++ ) {
          if( diff[i] < 2 ) {
            empty = false;
            break;
          }
        }
        if(empty) {
          return EMPTY;
        }
      }
    }
    for( int i = 0; i < diff.length; i++ ) {
//      System.err.println(x + "," + y + "=" + diff[i] + "=" + amount);
      if( diff[i] < 2 ) {
        return i;
      }
    }
    if( amount < 96 ) {
      return SOMETHING;
    }
    return EMPTY;
  }
  public boolean isItemThere(int x, int y) {
    BufferedImage last = robot.createScreenCapture(new Rectangle(X_OFFSET[offsetIndex] + 1720 + x*42, 750 + y*36, 30, 30));
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
        BufferedImage last = robot.createScreenCapture(new Rectangle(X_OFFSET[offsetIndex] + x, y, 30, 30));
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
    BufferedImage last = robot.createScreenCapture(new Rectangle(X_OFFSET[offsetIndex] + 1846, 965, 30, 30));
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
//    long start = System.currentTimeMillis();
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    // TODO need to fix screen width stuff
    BufferedImage image = robot.createScreenCapture(new Rectangle(960 + X_OFFSET[offsetIndex],0,X_OFFSET[offsetIndex] + size.width, size.height));
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
      if( p.getY() > 540 ) {
        yDist *= 1.2;
      }
      if( xDist + yDist < closest ) {
        closest = xDist + yDist;
        closestGoblin = p;
      }
    }
//    long end = System.currentTimeMillis();
//    System.err.println("Took " + (end - start) + "ms to find goblins long range search.");
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
    BufferedImage image = robot.createScreenCapture(new Rectangle(960 + X_OFFSET[offsetIndex],0,960, size.height));
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
        String name = "Unknown";
        if( item == EMPTY ) {
          name = "Empty";
        }
        else if( item >= 0  && item < itemNames.length ) {
          name = itemNames[item];
        }
        System.err.print(name + "     ");
        g.draw(getItemLocation(x, y));
      }
      System.err.println();
    }
    for( int x = 100; x < image.getWidth() - 200; x++ ) {
      for( int y = 200; y < image.getHeight() - 200; y++ ) {
        Color color = new Color(image.getRGB(x, y));
        if(isGoblin(color)) {
          image.setRGB(x, y, Color.WHITE.getRGB());
        }
        if( color.equals(Color.GREEN)) {
          image.setRGB(x, y, Color.BLUE.getRGB());
        }
        if( color.equals(Color.red)) {
          image.setRGB(x, y, Color.MAGENTA.getRGB());
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
          mouseClickMiss(new Rectangle(X_OFFSET[offsetIndex] + 1556, 32, 31, 9), 50, InputEvent.BUTTON1_MASK);
          sleep(8000);
          //deposit items
          mouseClickMiss(new Rectangle(X_OFFSET[offsetIndex] + 1483, 814, 29, 28), 50, InputEvent.BUTTON1_MASK);
          sleep(2000);
          // right click tuna
          mouseClickMiss(new Rectangle(X_OFFSET[offsetIndex] + 1135, 137, 17, 22), 50, InputEvent.BUTTON3_MASK);
          sleep(2000);
          Point mouse = MouseInfo.getPointerInfo().getLocation();
          // choose withdraw all
          mouseClickMiss(new Rectangle(X_OFFSET[offsetIndex] + mouse.x - 95, mouse.y + 95, 188, 10), 50, InputEvent.BUTTON1_MASK);
          sleep(2000);
          // move to stairs
          mouseClickMiss(new Rectangle(X_OFFSET[offsetIndex] + 1796, 161, 20, 15), 50, InputEvent.BUTTON1_MASK);
          sleep(8000);
          // click stairs 1
          mouseClickMiss(new Rectangle(X_OFFSET[offsetIndex] + 1405, 580, 54, 35), 50, InputEvent.BUTTON1_MASK);
          sleep(6000);
          // click stairs 2
          mouseClickMiss(new Rectangle(X_OFFSET[offsetIndex] + 1324, 537, 73, 62), 50, InputEvent.BUTTON1_MASK);
          sleep(3000);
          // choose go down
          mouseClickMiss(new Rectangle(X_OFFSET[offsetIndex] + 1000, 963, 450, 12), 50, InputEvent.BUTTON1_MASK);
          sleep(4000);
          // select tuna
          mouseClickMiss(new Rectangle(X_OFFSET[offsetIndex] + 1733, 765, 14, 15), 50, InputEvent.BUTTON1_MASK);
          sleep(2000);
          // click range
          mouseClickMiss(new Rectangle(X_OFFSET[offsetIndex] + 1755, 193, 22, 30), 50, InputEvent.BUTTON1_MASK);
          sleep(8000);
          // right click cook
          mouseClickMiss(new Rectangle(X_OFFSET[offsetIndex] + 1096, 949, 250, 37), 50, InputEvent.BUTTON3_MASK);
          mouse = MouseInfo.getPointerInfo().getLocation();
          sleep(2000);
          // select cook all
          mouseClickMiss(new Rectangle(X_OFFSET[offsetIndex] + mouse.x - 48, 1008, 95, 9), 50, InputEvent.BUTTON1_MASK);
          sleep(66000);
          // move to barrel
          mouseClickMiss(new Rectangle(X_OFFSET[offsetIndex] + 1016, 771, 29, 29), 50, InputEvent.BUTTON1_MASK);
          sleep(8000);
          //click stairs
          mouseClickMiss(new Rectangle(X_OFFSET[offsetIndex] + 1292, 713, 60, 64), 50, InputEvent.BUTTON1_MASK);
          sleep(6000);
          //click stairs
          mouseClickMiss(new Rectangle(X_OFFSET[offsetIndex] + 1364, 584, 71, 65), 50, InputEvent.BUTTON1_MASK);
          sleep(4000);
          //choose go up
          mouseClickMiss(new Rectangle(X_OFFSET[offsetIndex] + 1058, 933, 400, 11), 50, InputEvent.BUTTON1_MASK);
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
  public void mouseClickMissFast(Rectangle area, long delay, int button) throws InterruptedException {
    mouseMoveMiss(area);
    sleep(delay/5);
    robot.mousePress(button);
    sleep(delay/5);
    robot.mouseRelease(button);
    sleep(delay);
  }
  public void mouseClickMiss(Rectangle area, long delay, int button) throws InterruptedException {
    mouseMoveMiss(area);
    sleep(delay);
    robot.mousePress(button);
    sleep(delay);
    robot.mouseRelease(button);
    sleep(delay);
  }
  public String toString(int[] array) {
    String s = "";
    for( int i = 0; i < array.length; i++ ) {
      s += array[i];
      if( i != array.length - 1) {
        s += ", ";
      }
    }
    return s;
  }
  public int[] computeAverageColor(BufferedImage image) {
    int[] avg = new int[3];
    for( int x = 0; x < image.getWidth(); x++ ) {
      for( int y = 0; y < image.getHeight(); y++ ) {
        Color c1 = new Color(image.getRGB(x, y));
        avg[0] += c1.getRed();
        avg[1] += c1.getGreen();
        avg[2] += c1.getBlue();
      }
    }
    for( int i = 0; i < 3; i++ ) {
      avg[i] /= image.getWidth()*image.getHeight();
    }
    return avg;
  }

  public void chop3() {

  Rectangle treeRectangle = new Rectangle(573, 516, 37, 44);
  BufferedImage screen = robot.createScreenCapture(new Rectangle(0, 0,1920,1080));
  Graphics2D g = (Graphics2D)screen.getGraphics();
  g.draw(treeRectangle);
  try {
    ImageIO.write(screen, "png", new File("screen.png"));
  } catch (IOException e1) {
    // TODO Auto-generated catch block
    e1.printStackTrace();
  }
//  try {
////    BufferedImage image = robot.createScreenCapture(treeRectangle);
////    ImageIO.write(image, "png", new File("tree.png"));
//    
//    BufferedImage tree = ImageIO.read(new File("tree.png"));
//    BufferedImage notree = ImageIO.read(new File("notree.png"));
//    int[] avgTree = computeAverageColor(tree);
//    int[] avgNoTree = computeAverageColor(notree);
//    System.err.println(toString(avgTree));
//    System.err.println(toString(avgNoTree));
//  } catch (IOException e1) {
//    e1.printStackTrace();
    // }
    startTime = System.currentTimeMillis();
    Thread thread = new Thread(() -> {
      try {
        busy.acquire();
        mainFrame.repaint();
        long sixhour = 6 * 60 * 60 * 1000;
        while (System.currentTimeMillis() - startTime < sixhour) {
          numRocksMined++;
          timeLastMined = System.currentTimeMillis();
          while (!inventoryFull()) {
            boolean treeAvailable = false;
            while (!treeAvailable) {
              BufferedImage tree = robot.createScreenCapture(treeRectangle);
              int[] avg = computeAverageColor(tree);
              System.out.println(avg[1]);
              if (avg[1] > 85) {
                // is tree
                treeAvailable = true;
              } else {
                // no tree
                treeAvailable = false;
                sleep(500);
              }
            }
            mouseClickMiss(treeRectangle, 100, InputEvent.BUTTON1_MASK);
            sleep(10000);
          }
          // then bank
          mouseClickMiss(new Rectangle(832, 176, 1, 1), 100, InputEvent.BUTTON1_MASK);
          sleep(10000);
          mouseClickMiss(new Rectangle(841, 180, 1, 1), 100, InputEvent.BUTTON1_MASK);
          sleep(10000);
          mouseClickMiss(new Rectangle(815, 157, 1, 1), 100, InputEvent.BUTTON1_MASK);
          sleep(11000);
          mouseClickMiss(new Rectangle(816, 156, 1, 1), 100, InputEvent.BUTTON1_MASK);
          sleep(11000);

          mouseClickMiss(new Rectangle(447, 576, 44, 32), 100, InputEvent.BUTTON1_MASK);
          sleep(3000);
          dropItems(1, 20);
          sleep(1000);

          mouseClickMiss(new Rectangle(943, 82, 1, 1), 100, InputEvent.BUTTON1_MASK);
          sleep(11000);
          mouseClickMiss(new Rectangle(929, 62, 1, 1), 100, InputEvent.BUTTON1_MASK);
          sleep(11000);
          mouseClickMiss(new Rectangle(902, 45, 1, 1), 100, InputEvent.BUTTON1_MASK);
          sleep(10000);
          mouseClickMiss(new Rectangle(927, 70, 1, 1), 100, InputEvent.BUTTON1_MASK);
          sleep(10000);
        }
        shutDown();
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }, "CHOP3 THREAD");
    running.add(thread);
    thread.start();

  }
  public void shutDown() throws InterruptedException {
    mouseClickMiss(new Rectangle(25, 1061, 1, 1), 100, InputEvent.BUTTON1_MASK);
    sleep(5000);
    mouseClickMiss(new Rectangle(25, 1015, 1, 1), 100, InputEvent.BUTTON1_MASK);
    sleep(5000);
    mouseClickMiss(new Rectangle(25, 931, 1, 1), 100, InputEvent.BUTTON1_MASK);
    sleep(5000);
  }
  public void chop2() {
//    try {
//      BufferedImage image = robot.createScreenCapture(new Rectangle(359, 517, 60, 30));
//      ImageIO.write(image, "png", new File("tree.png"));
//      
////      BufferedImage tree = ImageIO.read(new File("doorclosed.png"));
////      BufferedImage notree = ImageIO.read(new File("dooropen.png"));
////      int[] avgTree = computeAverageColor(tree);
////      int[] avgNoTree = computeAverageColor(notree);
////      System.err.println(toString(avgTree));
////      System.err.println(toString(avgNoTree));
//    } catch (IOException e1) {
//      e1.printStackTrace();
//    }
//    try {
//      mouseClickMiss(new Rectangle(861, 70, 1, 1), 100, InputEvent.BUTTON1_MASK);
//    } catch (InterruptedException e1) {
//      // TODO Auto-generated catch block
//      e1.printStackTrace();
//    }
    
//    System.exit(0);
    startTime = System.currentTimeMillis();
    Thread thread = new Thread(() -> {
      try {
        busy.acquire();
        mainFrame.repaint();
        Rectangle treeRectangle = new Rectangle(359, 517, 60, 30);
        Rectangle doorRectangle = new Rectangle(443, 519, 9, 61);
//         ImageIO.write(rightTree, "png", new File("rightTree.png"));
        while(true) {
          while(!inventoryFull() ) {
            boolean treeAvailable = false;
            while(!treeAvailable) {
              BufferedImage tree = robot.createScreenCapture(treeRectangle);
              int[] avg = computeAverageColor(tree);
              if( avg[0] > 49 && avg[1] > 45 && avg[2] > 13 ) {
                // is tree
                treeAvailable = true;
                System.err.println("tree there");
              }
              else {
                //no tree
                System.err.println("no tree");
                
                treeAvailable = false;
                sleep(500);
              }
            }
            mouseClickMiss(new Rectangle(389, 517, 30, 10), 100, InputEvent.BUTTON1_MASK);
            sleep(10000);
          }
          // then bank
          mouseClickMiss(new Rectangle(888, 159, 1, 1), 100, InputEvent.BUTTON1_MASK);
          sleep(12000);
          mouseClickMiss(new Rectangle(508, 552, 1, 1), 100, InputEvent.BUTTON1_MASK);
          sleep(2000);
          mouseClickMiss(new Rectangle(889, 43, 1, 1), 100, InputEvent.BUTTON1_MASK);
          sleep(21000);
          mouseClickMiss(new Rectangle(531, 514, 31, 39), 100, InputEvent.BUTTON1_MASK);
          sleep(4000);
          dropItems(1, 20);
          sleep(2000);
          mouseClickMiss(new Rectangle(867, 187, 1, 1), 100, InputEvent.BUTTON1_MASK);
          sleep(20000);
          BufferedImage door = robot.createScreenCapture(doorRectangle);
          int[] avg = computeAverageColor(door);
          if( !(avg[0] < 77 || avg[0] > 79 || avg[1] < 46 || avg[1] > 48 || avg[2] < 18 || avg[2] > 20 )) {
            System.err.println("door closed");
            mouseClickMiss(doorRectangle, 100, InputEvent.BUTTON1_MASK);
            sleep(2000);
          }
          else {
            System.err.println("door open");
          }
          mouseClickMiss(new Rectangle(861, 71, 1, 1), 100, InputEvent.BUTTON1_MASK);
          sleep(13000);
        }
       
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    });
    running.add(thread);
    thread.start();
  
  }
  public void chop1() {
    startTime = System.currentTimeMillis();
    Thread thread = new Thread(() -> {
      try {
        busy.acquire();
        mainFrame.repaint();
        Rectangle leftTreeRectangle = new Rectangle(330, 455, 54, 68);
        Rectangle rightTreeRectangle = new Rectangle(560, 561, 94, 94);
//         ImageIO.write(rightTree, "png", new File("rightTree.png"));
        boolean lastChoppedLeft = false;
        while(true) {
          // go to tree
//          mouseClickMiss(new Rectangle( 894, 184, 0, 0), 100, InputEvent.BUTTON1_MASK);
//          sleep(25000);
//          mouseClickMiss(new Rectangle( 852, 183, 0, 0), 100, InputEvent.BUTTON1_MASK);
//          sleep(23000);
//          mouseClickMiss(new Rectangle( 877, 188, 0, 0), 100, InputEvent.BUTTON1_MASK);
//          sleep(19000);
//          mouseClickMiss(new Rectangle( 895, 186, 0, 0), 100, InputEvent.BUTTON1_MASK);
//          sleep(20000);
//          mouseClickMiss(new Rectangle( 884, 151, 0, 0), 100, InputEvent.BUTTON1_MASK);
//          sleep(13000);
          while(!inventoryFull() ) {
            BufferedImage leftTree = robot.createScreenCapture(leftTreeRectangle);
            BufferedImage rightTree = robot.createScreenCapture(rightTreeRectangle);
            int[] avg = computeAverageColor(leftTree);
            int[] avg2 = computeAverageColor(rightTree);
            boolean leftTreeAvailable = false;
            boolean rightTreeAvailable = false;
            if( avg[1] > 65 && avg[2] > 20) {
              leftTreeAvailable = true;
            }
            if( avg2[1] > 64 && avg2[2] > 16) {
              rightTreeAvailable = true;
            }
            System.err.println("left = " + leftTreeAvailable + ", right = " + rightTreeAvailable);
            if( (lastChoppedLeft && rightTreeAvailable) ||
                (!leftTreeAvailable && rightTreeAvailable) ) {
              mouseClickMiss(rightTreeRectangle, 100, InputEvent.BUTTON1_MASK);
              lastChoppedLeft = false;
              long time = 0;
              while( rightTreeAvailable && time < 10000 ) {
                sleep(1000);
                rightTree = robot.createScreenCapture(rightTreeRectangle);
                avg2 = computeAverageColor(rightTree);
                rightTreeAvailable = false;
                if( avg2[1] > 64 && avg2[2] > 16) {
                  rightTreeAvailable = true;
                }
                System.err.println("chopping riight left = " + leftTreeAvailable + ", right = " + rightTreeAvailable);
                
                time += 1000;
              }
            }
            else if( !lastChoppedLeft && leftTreeAvailable ||
                (leftTreeAvailable && !rightTreeAvailable) ) {
              mouseClickMiss(leftTreeRectangle, 100, InputEvent.BUTTON1_MASK);
              lastChoppedLeft = true;
              long time = 0;
              while( leftTreeAvailable && time <= 10000 ) {
                sleep(1000);
                leftTree = robot.createScreenCapture(leftTreeRectangle);
                avg = computeAverageColor(leftTree);
                leftTreeAvailable = false;
                if( avg[1] > 65 && avg[2] > 20) {
                  leftTreeAvailable = true;
                }
                System.err.println("choip left left = " + leftTreeAvailable + ", right = " + rightTreeAvailable);
                
                time += 1000;
              }
            }
          }
//          mouseClickMiss(new Rectangle( 881, 41, 0, 0), 100, InputEvent.BUTTON1_MASK);
//          sleep(18000);
//          mouseClickMiss(new Rectangle( 866, 42, 0, 0), 100, InputEvent.BUTTON1_MASK);
//          sleep(23000);
//          mouseClickMiss(new Rectangle( 866, 42, 0, 0), 100, InputEvent.BUTTON1_MASK);
//          sleep(23000);
//          mouseClickMiss(new Rectangle( 866, 42, 0, 0), 100, InputEvent.BUTTON1_MASK);
//          sleep(23000);
//          mouseClickMiss(new Rectangle( 705, 238, 0, 0), 100, InputEvent.BUTTON1_MASK);
//          sleep(13000);
          // then bank
          dropItems(1, 20);
          sleep(2000);
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
                  if( itemThere == itemNameMap.get("iron_ore") || itemThere == itemNameMap.get("sapphire") || itemThere == itemNameMap.get("emerald") ) {
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
              dropItems(0, 0, itemNameMap.get("iron_ore"));
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
  
  public List<Integer> checkHealth() {
    Rectangle capture = new Rectangle(960 + X_OFFSET[offsetIndex] + 360, 400, 240, 240);
    BufferedImage image = robot.createScreenCapture(capture);
//    BufferedImage image2 = robot.createScreenCapture(capture);
    LinkedList<Integer> bars = new LinkedList<Integer>();
    for( int y = 0; y < image.getHeight(); y+=3 ) {
      for( int x = 0; x < image.getWidth(); x+=10 ) {
        Color color = new Color(image.getRGB(x,  y));
        if( color.equals(Color.green) || color.equals(Color.red) ) {
          int leftX = x;
          while( (color.equals(Color.green) || color.equals(Color.red)) && leftX > 0 ) {
            leftX--;
            color = new Color(image.getRGB(leftX,  y));
            image.setRGB(leftX, y, Color.YELLOW.getRGB());
          }
          leftX++;
          color = new Color(image.getRGB(leftX,  y));
          int topY = y;
          while( (color.equals(Color.GREEN) || color.equals(Color.RED)) && topY > 0 ) {
            topY--;
            color = new Color(image.getRGB(leftX,  topY));
            image.setRGB(leftX, topY, Color.ORANGE.getRGB());
          }
          topY++;
          int firstRed = 30;
          for( int i = 0; i < 30 && leftX < image.getWidth() - 30; i++ ) {
            color = new Color(image.getRGB(leftX + i, topY));
            if( color.equals(Color.RED) ) {
              firstRed = Math.min(firstRed, i);
              image.setRGB(leftX + i, topY, Color.BLUE.getRGB());
            }
            else if( color.equals(Color.GREEN) ) {
              image.setRGB(leftX + i, topY, Color.MAGENTA.getRGB());
            }
            else {
              firstRed = -1;
              break;
            }
          }
          if( firstRed != -1 && leftX < image.getWidth() - 30) {
            bars.add(firstRed);
          }
        }
      }
    }
    String hps = "_";
    for( Integer hp : bars ) {
      hps += hp + ", ";
    }
    hps += "_";
    Collections.sort(bars, Collections.reverseOrder());
    hps += "after sort=";
    for( Integer hp : bars ) {
      hps += hp + ", ";
    }
    hps += "_";
//    System.err.println(hps);
    
//    for( int x = 0; x < image.getWidth(); x++ ) {
//      for( int y = 0; y < image.getHeight(); y++ ) {
//        Color color = new Color(image2.getRGB(x, y));
//        if( color.equals(Color.GREEN)) {
//          image2.setRGB(x, y, Color.BLUE.getRGB());
//        }
//        if( color.equals(Color.red)) {
//          image2.setRGB(x, y, Color.MAGENTA.getRGB());
//        }
//      }
//    }
//    try {
//      ImageIO.write(image, "png", new File(hps + System.currentTimeMillis() + ".png"));
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
    return bars;
  }
  public void fight1() {
    startTime = System.currentTimeMillis();
    startExp = ImageProcessor.getExperience(robot);
    lastSave = startTime;
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          busy.acquire();
          mainFrame.repaint();
          long timeElapsed = System.currentTimeMillis() - startTime;
          boolean reenable = true;
          while(true) {
            if( timeElapsed > SIX_HOURS/2 && reenable ) {
              reenable = false;
              mouseClickMissFast(new Rectangle(812, 173, 1, 1), 100, InputEvent.BUTTON1_MASK);
            }
            Point goblin = closestGoblin();
            while( goblin == null ) {
              sleep(500);
              goblin = closestGoblin();
            }
            mouseClickMissFast(new Rectangle(goblin.x, goblin.y, 1, 1), 100, InputEvent.BUTTON1_MASK);
            sleep(2000);
            timeElapsed = System.currentTimeMillis() - startTime;
            
            long delay = 1000;
            for( int i = 0; i < 10; i++ ) {
              sleep(delay);
              List<Integer> bars = checkHealth();
              if( bars.size() == 0 ) {
                System.err.print("Found zero health bars");
                break;
              } else if( bars.size() == 1 ) {
                if( bars.get(0) == 0 ) {
                  System.err.print("DEAD");
                  break;
                }
                else {
                  System.err.print(bars.get(0) + " ");
                }
              }
              else {
                if( bars.get(1) == 0 ) {
                  System.err.print("DEAD");
                  break;
                }
                else {
                  System.err.print(bars.get(1) + " ");
                }
              }
            }
            System.err.println();
            sleep(200);
            numRocksMined++;
            timeLastMined = System.currentTimeMillis();
            if( timeLastMined - lastSave > TEN_MINUTES/2 ) {
              saveData(delay);
              lastSave = timeLastMined;
            }
            mainFrame.repaint();
            
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
    mouseMoveMiss(new Rectangle(X_OFFSET[offsetIndex] + 1693, 869, 7, 127));
    sleep(20);
    robot.mousePress(InputEvent.BUTTON1_MASK);
    sleep(20);
    robot.mouseRelease(InputEvent.BUTTON1_MASK);
    sleep(200);
  }
  public void dropItems(int skip, int delay) throws InterruptedException {
    try {
      robot.keyPress(KeyEvent.VK_SHIFT);
      sleep(100 + delay);
      int amount = 0;
      for (int y = 0; y < 7; y++) {
        boolean dropped = false;
        for (int x = 0; x < 4; x++) {
          if (amount >= skip && itemThere(x, y) != EMPTY ) {
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
  public void dropItemsThreaded(int skip, ItemLogic logic) {
    dropItemsThreaded(skip, 0, logic);
  }
  public Rectangle getItemLocation(int x, int y) {
    return new Rectangle(X_OFFSET[offsetIndex] + 1723 + x*43, Y_OFFSET[offsetIndex] + 755 + y*36, 20, 20);
  }
  public void dropItemsThreaded(int skip, int delay, ItemLogic logic) {
    System.err.println("Drop items");
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          busy.acquire();
          mainFrame.repaint();
          selectScreen();
          robot.keyPress(KeyEvent.VK_SHIFT);
          sleep(50 + delay);
          int amount = 0;
          for( int y = 0; y < 7; y ++ ) {
            boolean dropped = false;
            for( int x = 0; x < 4; x++ ) {
              int itemid = itemThere(x, y);
              if( amount >= skip && itemid != EMPTY && logic.shouldDrop(itemid)) {
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
    }, "DROP ITEMS THREAD");
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
