import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import javafx.util.Pair;

public class ImageProcessor {

  public enum Digit {
    COMMA(0x00000044, -1), ZERO(0x06864686, 0), ONE(0x0023aa11, 1), TWO(0x03544553, 2), THREE(0x002533a7, 3), FOUR(
        0x08816611,
        4), FIVE(0x00563475, 5), SIX(0x06863652, 6), SEVEN(0x00355542, 7), EIGHT(0x03773773, 8), NINE(0x035322a9, 9);
    private long code;
    private int value;

    Digit(long code, int value) {
      this.code = code;
      this.value = value;
    }
  }

  private static Digit[] digits = Digit.values();
  private JFrame frame;
  private JPanel imagePanel;
  private BufferedImage debugImage;
  private Rectangle selected;
  private Rectangle defaultSelected;
  private Point mousePress;
  private Point mouseCurrent;
  private Robot robot;

  private static List<BufferedImage> removal;

  static {
    removal = new LinkedList<BufferedImage>();
    try {
      BufferedImage redDot = ImageIO.read(new File("mapImages/dotRed.png"));
      BufferedImage yellowDot = ImageIO.read(new File("mapImages/dotYellow.png"));
      BufferedImage whiteDot = ImageIO.read(new File("mapImages/dotWhite.png"));
      BufferedImage redFlag = ImageIO.read(new File("mapImages/dotRedFlag.png"));
      removal.add(redDot);
      removal.add(yellowDot);
      removal.add(whiteDot);
      removal.add(redFlag);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public enum Mode {
    NORMAL, WHITE_THRESHOLD, GOBLINS, MAP
  };

  private Mode mode = Mode.MAP;

  public ImageProcessor(Rectangle selected) {
    try {
      robot = new Robot();
    } catch (AWTException e1) {
      e1.printStackTrace();
      System.exit(1);
    }
    this.defaultSelected = new Rectangle(selected);
    this.selected = new Rectangle(selected);
    frame = new JFrame("ImageProcessor");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    imagePanel = new JPanel() {
      @Override
      public void paintComponent(Graphics gg) {
        if (debugImage == null) {
          return;
        }
        Graphics2D g = (Graphics2D) gg;
        if (mode == Mode.NORMAL) {
          List<BufferedImage> goblinT = goblinColors(debugImage);
          g.drawImage(debugImage, 0, 0, getWidth(), getHeight(), null);
          g.setColor(Color.red);
          if (mousePress != null) {
            if (mouseCurrent != null) {
              g.draw(computeSelection());
            } else {
              g.drawRect(mousePress.x, mousePress.y, 1, 1);
            }
          }
        } else if (mode == Mode.GOBLINS) {
          List<BufferedImage> goblinT = goblinColors(debugImage);
          g.drawImage(debugImage, 0, 0, getWidth() / 2, getHeight() / 2, null);
          g.drawImage(goblinT.get(0), getWidth() / 2, 0, getWidth() / 2, getHeight() / 2, null);
          g.drawImage(goblinT.get(1), getWidth() / 2, getHeight() / 2, getWidth() / 2, getHeight() / 2, null);
        } else if (mode == Mode.WHITE_THRESHOLD) {
          g.drawImage(debugImage, 0, 0, getWidth() / 2, getHeight() / 2, null);
          BufferedImage whiteT = whiteThreshold(debugImage, 255);
          g.drawImage(whiteT, getWidth() / 2, 0, getWidth() / 2, getHeight() / 2, null);
          LinkedList<int[][]> digits = separateDigits(whiteT);
          long totalValue = 0;
          for (int[][] digit : digits) {
            // System.err.println("~~~~~~~~~~~");
            // ImageProcessor.print(digit);
            long code = reduceDigit(digit);
            int value = getValue(code);
            if (value == -1) {
              // System.err.print(",");
            } else {
              totalValue *= 10;
              totalValue += value;
              // System.err.print(value);
            }
          }
          // System.err.println();
          // System.err.println(totalValue);
        } else if (mode == Mode.MAP) {
          g.drawImage(debugImage, 0, 0, getWidth() / 2, getHeight() / 2, null);
          g.setColor(Color.black);
          g.fillRect(getWidth() / 2, 0, getWidth() / 2, getHeight() / 2);
          BufferedImage noPlayer = removePlayer(debugImage);
          g.drawImage(noPlayer, getWidth() / 2, 0, getWidth() / 2, getHeight() / 2, null);

        }
      }
    };
    frame.addKeyListener(new KeyListener() {
      @Override
      public void keyPressed(KeyEvent arg0) {

      }

      @Override
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
          updateImage();
        }
        if (e.getKeyCode() == KeyEvent.VK_Q) {
          mode = Mode.NORMAL;
        }
        if (e.getKeyCode() == KeyEvent.VK_W) {
          mode = Mode.WHITE_THRESHOLD;
        }
        if (e.getKeyCode() == KeyEvent.VK_E) {
          mode = Mode.GOBLINS;
        }
        if (e.getKeyCode() == KeyEvent.VK_F) {
          mode = Mode.MAP;
        }
        frame.repaint();
      }

      @Override
      public void keyTyped(KeyEvent arg0) {

      }
    });
    imagePanel.addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseDragged(MouseEvent e) {
        mouseCurrent = e.getPoint();
        frame.repaint();
      }
    });
    imagePanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          if (mode == Mode.NORMAL) {
            mousePress = e.getPoint();
            frame.repaint();
          }
        }
        if (e.getButton() == MouseEvent.BUTTON2) {
          updateImage();
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
          if (mode == Mode.NORMAL) {
            ImageProcessor.this.selected = new Rectangle(defaultSelected);
            // updateImage();
          }
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          if (mode == Mode.NORMAL) {
            if (mouseCurrent != null) {
              updateSelection(computeSelection());
              // updateImage();
            }
            mousePress = null;
            mouseCurrent = null;
            frame.repaint();
          }
        }
      }
    });
    frame.setSize(640, 480);
    frame.setBounds(new Rectangle(960, 0, 480, 540));
    frame.add(imagePanel);
    frame.setVisible(true);
    // updateImage();
  }

  public List<BufferedImage> goblinColors(BufferedImage image) {
    BufferedImage input = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
    Graphics gg = input.getGraphics();
    gg.drawImage(image, 0, 0, null);
    gg.setColor(Color.black);
    gg.fillRect(718, 704 - GOBLIN_RECT.y, 300, 200);
    gg.fillRect(746, 32 - GOBLIN_RECT.y, 300, 190);
    gg.fillRect(567, 33 - GOBLIN_RECT.y, 500, 38);
    gg.dispose();
    boolean[][] goblins = new boolean[image.getWidth()][image.getHeight()];
    BufferedImage orig = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
    BufferedImage debug = new BufferedImage(FULL.width, FULL.height, image.getType());
    BufferedImage ret = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
    List<BufferedImage> images = new LinkedList<BufferedImage>();
    boolean[] table = new boolean[256 * 256 * 256];
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        Color col = new Color(image.getRGB(x, y));
        int rgb = col.getRGB() & 0x00FFFFFF;
        // System.err.println(String.format("0x%h -> 0x%h", col.getRGB(), rgb));
        if (isGoblin(col)) {
          orig.setRGB(x, y, Color.black.getRGB());
          ret.setRGB(x, y, Color.white.getRGB());
          goblins[x][y] = true;
        } else {
          table[rgb] = true;
          orig.setRGB(x, y, col.getRGB());
          ret.setRGB(x, y, Color.BLACK.getRGB());
        }
      }
    }
    dilate(goblins);
    dilate(goblins);
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        if (goblins[x][y]) {
          ret.setRGB(x, y, Color.white.getRGB());
        }
      }
    }
    int index = 0;
    Graphics g = debug.getGraphics();
    int wid = 10;
    int x = 0;
    int y = 0;
    for (int counter = 0; counter < table.length; counter++) {
      if (table[counter]) {
        int rgb = counter | 0xFF000000;
        g.setColor(new Color(rgb));
        g.fillRect(x, y, wid, wid);
        x += wid;
        if (x >= image.getWidth()) {
          x = 0;
          y += wid;
        }
        // ret.setRGB(index%image.getWidth(), index/image.getWidth(), new
        // Color(rgb).getRGB());
        // ret.setRGB((index+1)%image.getWidth(), index/image.getWidth(), new
        // Color(rgb).getRGB());
        // ret.setRGB((index+1)%image.getWidth(), index/image.getWidth()+1, new
        // Color(rgb).getRGB());
        // ret.setRGB(index%image.getWidth(), index/image.getWidth()+1, new
        // Color(rgb).getRGB());
        index += wid;
      }
    }
    g.dispose();
    images.add(ret);
    images.add(debug);
    return images;
  }

  public boolean[][] dilate(boolean[][] input) {
    boolean[][] output = new boolean[input.length][input[0].length];
    for (int x = 0; x < input.length; x++) {
      for (int y = 0; y < input[0].length; y++) {
        if ((x > 0 && input[x - 1][y]) || (x < input.length - 1 && input[x + 1][y]) || (y > 0 && input[x][y - 1])
            || (y < input[0].length - 1 && input[x][y + 1])) {
          output[x][y] = true;
        }
      }
    }
    return output;
  }

  public boolean isGoblin(Color c) {
    double ratio1 = (double) (c.getGreen()) / c.getRed();
    double ratio2 = (double) (c.getGreen()) / c.getBlue();
    return (ratio1 > 1 && ratio1 < 1.06 && ratio2 > 2.6 && ratio2 < 2.9)
        || (ratio1 > 2.1 && ratio1 < 2.3 && ratio2 > 1.5 && ratio2 < 1.7);
  }

  public static void print(int[][] array) {
    for (int y = 0; y < array[0].length; y++) {
      String line = "";
      for (int x = 0; x < array.length; x++) {
        if (array[x][y] == 1) {
          line += "#";
        } else {
          line += " ";
        }
      }
      System.err.println(line);
    }
  }

  public static long getExperience(Robot robot) {
    BufferedImage expImage = robot.createScreenCapture(EXP_RECT);
    BufferedImage whiteT = whiteThreshold(expImage, 255);
    LinkedList<int[][]> digits = separateDigits(whiteT);
    long totalValue = 0;
    for (int[][] digit : digits) {
      long code = reduceDigit(digit);
      int value = getValue(code);
      if (value == -1) {
      } else if (value == -2) {
        print(digit);
        // System.err.println(code);
        // System.err.println(value);
        totalValue = -1;
        return totalValue;
      } else {
        totalValue *= 10;
        totalValue += value;
      }
    }
    return totalValue;
  }

  public static int getValue(long reducedDigit) {
    for (Digit d : digits) {
      if (d.code == reducedDigit) {
        return d.value;
      }
    }
    return -2;
  }

  public static long reduceDigit(int[][] digit) {
    int[] vertSum = new int[digit.length];
    String vert = "";
    for (int x = 0; x < digit.length; x++) {
      for (int y = 0; y < digit[x].length; y++) {
        vertSum[x] += digit[x][y];
      }
      vert += vertSum[x] + ", ";

    }
    // System.err.println(vert);
    long ret = 0;
    for (int x = 0; x < vertSum.length; x++) {
      ret += vertSum[x];
      if (x != vertSum.length - 1) {
        ret = ret * 16;
      }
    }
    // System.err.println(String.format("0x%08x", ret));
    return ret;
  }

  public static LinkedList<int[][]> separateDigits(BufferedImage image) {
    LinkedList<int[][]> ret = new LinkedList<int[][]>();
    boolean[] max = verticalMax(image);
    boolean active = false;
    int activeIndex = 0;
    for (int x = 0; x < max.length; x++) {
      if (!active) {
        if (max[x]) {
          active = true;
          activeIndex = x;
        }
      } else {
        if (!max[x]) {
          int[][] digit = new int[x - activeIndex][image.getHeight()];
          for (int i = 0; i < digit.length; i++) {
            for (int j = 0; j < digit[i].length; j++) {
              digit[i][j] = (image.getRGB(activeIndex + i, j) == Color.white.getRGB()) ? 1 : 0;
            }
          }
          ret.add(digit);
          active = false;
        }
      }
    }
    if (active) {
      int[][] digit = new int[max.length - activeIndex][image.getHeight()];
      for (int i = 0; i < digit.length; i++) {
        for (int j = 0; j < digit[i].length; j++) {
          digit[i][j] = (image.getRGB(activeIndex + i, j) == Color.white.getRGB()) ? 1 : 0;
        }
      }
      ret.add(digit);
      active = false;
    }
    return ret;
  }

  public static boolean[] verticalMax(BufferedImage image) {
    boolean[] ret = new boolean[image.getWidth()];
    for (int x = 0; x < image.getWidth(); x++) {
      int maxRed = 0;
      int maxGreen = 0;
      int maxBlue = 0;
      for (int y = 0; y < image.getHeight(); y++) {
        Color col = new Color(image.getRGB(x, y));
        maxRed = Math.max(col.getRed(), maxRed);
        maxGreen = Math.max(col.getGreen(), maxGreen);
        maxBlue = Math.max(col.getBlue(), maxBlue);
      }
      Color col = new Color(maxRed, maxGreen, maxBlue);
      ret[x] = col.equals(Color.WHITE);
    }
    return ret;
  }

  public static BufferedImage whiteThreshold(BufferedImage image, int value) {
    BufferedImage ret = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        Color col = new Color(image.getRGB(x, y));
        if (col.getRed() >= value && col.getGreen() >= value && col.getBlue() >= value) {
          ret.setRGB(x, y, Color.white.getRGB());
          // System.err.println(x + ", " + y);
        } else {
          ret.setRGB(x, y, Color.BLACK.getRGB());
        }
      }
    }
    return ret;
  }

  public void updateSelection(Rectangle newSelection) {
    selected = new Rectangle(selected.x + selected.width * newSelection.x / imagePanel.getWidth(),
        selected.y + selected.height * newSelection.y / imagePanel.getHeight(),
        selected.width * newSelection.width / imagePanel.getWidth(),
        selected.height * newSelection.height / imagePanel.getHeight());
    System.err
        .println("new selection = " + selected.x + ", " + selected.y + ", " + selected.width + ", " + selected.height);
  }

  public Rectangle computeSelection() {
    return new Rectangle(Math.min(mousePress.x, mouseCurrent.x), Math.min(mousePress.y, mouseCurrent.y),
        Math.abs(mousePress.x - mouseCurrent.x), Math.abs(mousePress.y - mouseCurrent.y));
  }

  public void updateImage() {
    debugImage = robot.createScreenCapture(selected);
    frame.repaint();
    try {
      ImageIO.write(debugImage, "png", new File("screenshot.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static BufferedImage loadMap() {
    try {
      return ImageIO.read(new File("map.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static BufferedImage cropMap(BufferedImage image) {
    BufferedImage cropped = new BufferedImage(image.getWidth() - MAP_CROP * 2, image.getHeight() - MAP_CROP * 2,
        BufferedImage.TYPE_4BYTE_ABGR);
    Graphics g = cropped.getGraphics();
    g.drawImage(image, -MAP_CROP, -MAP_CROP, null);
    g.dispose();
    return cropped;
  }

  public static BufferedImage removePlayer(BufferedImage image) {
    BufferedImage noPlayer = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
    Graphics g = noPlayer.getGraphics();
    g.drawImage(image, 0, 0, null);
    g.dispose();
    for (int x = -1; x < 2; x++) {
      for (int y = -1; y < 2; y++) {
        noPlayer.setRGB(MAP_RECT_SMALL.width / 2 + x, MAP_RECT_SMALL.height / 2 + y, new Color(0, 0, 0, 0).getRGB());
      }
    }
    return noPlayer;
  }

  public static BufferedImage removeObjects(BufferedImage image) {
    BufferedImage noPlayer = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
    Graphics g = noPlayer.getGraphics();
    g.drawImage(image, 0, 0, null);
    g.dispose();
    for (int iteration = 0; iteration < 10; iteration++) {
      boolean removedOne = false;
      for (int index = 0; index < removal.size(); index++) {
        BufferedImage remove = removal.get(index);
        for (int y = 0; y < image.getHeight() - remove.getHeight() + 1; y++) {
          for (int x = 0; x < image.getWidth() - remove.getWidth() + 1; x++) {
            int diff = 0;
            int skip = 0;
            for (int j = 0; j < remove.getHeight() && diff == 0; j++) {
              for (int i = 0; i < remove.getWidth() && diff == 0; i++) {
                int alpha = image.getRGB(x + i, y + j) >>> 24;
                int alpha2 = remove.getRGB(i, j) >>> 24;
                if (alpha == 0 || alpha2 == 0) {
                  skip++;
                }
                if (alpha != 0 && alpha2 != 0 && image.getRGB(x + i, y + j) != remove.getRGB(i, j)) {
                  diff++;
                }
              }
            }
            if (diff == 0 && skip < remove.getHeight() * remove.getWidth()) {
              removedOne = true;
              // System.err.println(index + " Found remove at " + x + "," + y + ", skipped=" +
              // skip);
              for (int j = 0; j < remove.getHeight(); j++) {
                for (int i = 0; i < remove.getWidth(); i++) {
                  int alpha2 = remove.getRGB(i, j) >>> 24;
                  if (alpha2 != 0) {
                    noPlayer.setRGB(x + i, y + j, 0);
                    image.setRGB(x + i, y + j, 0);
                  }
                }
              }
            }
          }
        }
      }
      if (!removedOne) {
        break;
      }
    }
    return noPlayer;
  }

  public static BufferedImage smallMap;

  public static Point previousBest;
  public static BufferedImage findBestAlignment(BufferedImage small, BufferedImage large) {
//    System.err.println("Beginning alignment computation with map size " + large.getWidth() + "," + large.getHeight());
    int bestAlign = Integer.MAX_VALUE;
    int bestSkipped = 0;
    Point maxPos = null;
    int OFFSET_LIMIT = 4 * 8;
    // int comparisons = 0;
    int MAX_DIFF = 1;
    smallMap = small;
    // for( int y = 0-small.getHeight(); y < large.getHeight(); y++ ) {
    // for( int x = 0-small.getWidth(); x < large.getWidth(); x++ ) {
    
    if( previousBest == null ) {
      previousBest = new Point(0, 0);
    }
    for( int attempt = 0; attempt < 3 && bestAlign > 0; attempt++ ) {
      int yLimit = large.getHeight() - OFFSET_LIMIT;
      int xLimit = large.getWidth() - OFFSET_LIMIT;
      int xStart = 0 - OFFSET_LIMIT;
      int yStart = 0 - OFFSET_LIMIT;
      if( attempt == 2 ) {
        System.err.println("went to attempt 2");
      }
      else if(attempt == 1){
        xStart = previousBest.x - prevDelta*prevDelta;
        yStart = previousBest.y - prevDelta*prevDelta;
        xLimit = previousBest.x + prevDelta*prevDelta;
        yLimit = previousBest.y + prevDelta*prevDelta;
      }
      else if(attempt == 0){
        xStart = previousBest.x - prevDelta;
        yStart = previousBest.y - prevDelta;
        xLimit = previousBest.x + prevDelta;
        yLimit = previousBest.y + prevDelta;
      }
      for (int y = yStart; y < yLimit && bestAlign > 0; y++) {
        for (int x = xStart; x < xLimit && bestAlign > 0; x++) {
          int diff = 0;
          int skipped = 0;
          for (int j = 0; j < small.getHeight() && diff < MAX_DIFF; j++) {
            for (int i = 0; i < small.getWidth() && diff < MAX_DIFF; i++) {
              if (x + i >= 0 && y + j >= 0 && x + i < large.getWidth() && y + j < large.getHeight()) {
                int largeRGB = large.getRGB(x + i, y + j);
                int alpha = largeRGB >>> 24;
                int smallRGB = small.getRGB(i, j);
                int alpha2 = smallRGB >> 24;
                // comparisons++;
                if (alpha != 0 && alpha2 != 0 && largeRGB != small.getRGB(i, j)) {
                  int deltaRed = Math.abs(((largeRGB & 0x00FF0000) >> 16) - ((smallRGB & 0x00FF0000) >> 16));
                  int deltaGreen = Math.abs(((largeRGB & 0x0000FF00) >> 8) - ((smallRGB & 0x0000FF00) >> 8));
                  int deltaBlue = Math.abs((largeRGB & 0x000000FF) - (smallRGB & 0x000000FF));
                  if( deltaRed + deltaGreen + deltaBlue > 40 ) {
                    diff++;
                  }
                }
                if (alpha == 0 && alpha2 != 0) {
                  skipped++;
                }
              } else {
                skipped++;
              }
            }
          }
          if (diff < MAX_DIFF && skipped < small.getWidth() * small.getHeight() / 4 && diff < bestAlign) {
            maxPos = new Point(x, y);
            bestAlign = diff;
            bestSkipped = skipped;
          }
        }
      }
    }
    // System.err.println("# comparisons = " + comparisons);
    if (bestAlign > 300) {
      System.err.println("failed alignment " + bestAlign);
      return large;
    }
    BufferedImage bestImage = new BufferedImage(small.getWidth(), small.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
    Graphics g = bestImage.getGraphics();
    g.drawImage(small, 0, 0, null);
    g.dispose();
    Color blankColor = new Color(123, 123, 123, 0);
    for (int j = 0; j < small.getHeight(); j++) {
      for (int i = 0; i < small.getWidth(); i++) {
        if (maxPos.x + i >= 0 && maxPos.y + j >= 0 && maxPos.x + i < large.getWidth()
            && maxPos.y + j < large.getHeight()) {
          int largeRGB = large.getRGB(maxPos.x + i, maxPos.y + j);
          int alpha2 = largeRGB >>> 24;
          int alpha = small.getRGB(i, j) >>> 24;
          if (alpha == 255 && largeRGB != small.getRGB(i, j) && alpha2 != 0) {
            bestImage.setRGB(i, j, blankColor.getRGB());
          }
          if (largeRGB != small.getRGB(i, j)) {
            int smallRGB = small.getRGB(i, j);
            int deltaRed = Math.abs(((largeRGB & 0x00FF0000) >> 16) - ((smallRGB & 0x00FF0000) >> 16));
            int deltaGreen = Math.abs(((largeRGB & 0x0000FF00) >> 8) - ((smallRGB & 0x0000FF00) >> 8));
            int deltaBlue = Math.abs((largeRGB & 0x000000FF) - (smallRGB & 0x000000FF));
            if( deltaRed + deltaGreen + deltaBlue > 40 ) {
              small.setRGB(i, j, Color.cyan.getRGB());
            }
          }
        }
      }
    }
    int lx = 0;
    int ly = 0;
    int sx = 0;
    int sy = 0;
    int neww = 0;
    int newh = 0;
    if (maxPos.x < 0) {
      lx = -maxPos.x;
      neww = large.getWidth() - maxPos.x;
    } else {
      sx = maxPos.x;
      neww = Math.max(large.getWidth(), small.getWidth() + sx);
    }
    if (maxPos.y < 0) {
      ly = -maxPos.y;
      newh = large.getHeight() - maxPos.y;
    } else {
      sy = maxPos.y;
      newh = Math.max(large.getHeight(), small.getHeight() + sy);
    }
    BufferedImage newImage = large;
    Graphics2D g2;
    if( neww != large.getWidth() || newh != large.getHeight() ) {
      newImage = new BufferedImage(neww, newh, BufferedImage.TYPE_4BYTE_ABGR);
      g2 = (Graphics2D) newImage.getGraphics();
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
      g2.fillRect(0, 0, newImage.getWidth(), newImage.getHeight());
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
      g2.drawImage(large, lx, ly, null);
    }
    else {
      g2 = (Graphics2D) newImage.getGraphics();
    }
    g2.drawImage(bestImage, sx, sy, null);
    g2.dispose();
//    System.err.println("best alignment = " + maxPos.x + ", " + maxPos.y + " = " + bestAlign + ", skip " + bestSkipped);
//    System.err.println("map size " + newImage.getWidth() + "," + newImage.getHeight());
    // try {
    // ImageIO.write(bestImage, "png", new File("mapBestImage.png"));
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // try {
    // ImageIO.write(newImage, "png", new File("mapIntersect" +
    // (System.currentTimeMillis()/200)%1000 + ".png") );
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
//    try {
//      smallMap = small;
//      ImageIO.write(small, "png", new File("mapSmall" + counter + ".png"));
//      ImageIO.write(large, "png", new File("mapSLarge" + counter + ".png"));
//    } catch (IOException e) {
//      e.printStackTrace();
    maxDiffPoint = Math.max(maxDiffPoint, Math.max(Math.abs(previousBest.x - maxPos.x), Math.abs(previousBest.y - maxPos.y)));
    previousBest = maxPos;
    return newImage;
    // return new Pair<Point, Integer>(maxPos, bestAlign);
  }
  static int prevDelta = 10;
  static int maxDiffPoint;
  static int counter = 0;

  public static String hex(int val) {
    return String.format("%h", val);
  }

  public static final Rectangle EXP_RECT = new Rectangle(581, 41, 122, 10);
  public static final Rectangle FULL = new Rectangle(0, 0, 960, 1080);
  public static final Rectangle GOBLIN_RECT = new Rectangle(0, 31, 960, 843);
  public static final Rectangle GOBLIN_INV_RECT = new Rectangle(718, 704, 250, 180);
  public static final Rectangle MAP_RECT = new Rectangle(799, 33, 156, 162);
  public static final int MAP_CROP = 16;
  public static final Rectangle MAP_RECT_SMALL = new Rectangle(830 - MAP_CROP, 68 - MAP_CROP, 94 + MAP_CROP * 2,
      94 + MAP_CROP * 2);
  // public static void main(String[] args) {
  // ImageProcessor imgP = new ImageProcessor(MAP_RECT_SMALL);
  // }
}
