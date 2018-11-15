package main;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import map.Location;
import map.MapManager;

public class AgilityTask implements Runnable {
  private MapManager mapManager;
  private Robot robot;
  private Color markColor = new Color(186, 163, 21);
  public AgilityTask(Robot robot) {
    this.robot = robot;
    mapManager = new MapManager();
  }
  public void detectAndTakeMarks() throws InterruptedException {
    Rectangle gameScreenRect = new Rectangle(99, 204, 850, 660);
    BufferedImage gameScreen = robot.createScreenCapture(gameScreenRect);
    Graphics g = gameScreen.getGraphics();
    g.setColor(Color.black);
    g.fillRect(717 - 99, 698 - 204, 500, 500);
    g.dispose();
    int markRGB = markColor.getRGB();
    int avgx = 0;
    int avgy = 0;
    int count = 0;
    for( int y = 0; y < gameScreen.getHeight(); y++ ) {
      for( int x = 0; x < gameScreen.getWidth(); x++ ) {
        int rgb = gameScreen.getRGB(x, y);
        Color c = new Color(rgb);
        int deltaRed = Math.abs(((markRGB & 0x00FF0000) >> 16) - ((rgb & 0x00FF0000) >> 16));
        int deltaGreen = Math.abs(((markRGB & 0x0000FF00) >> 8) - ((rgb & 0x0000FF00) >> 8));
        int deltaBlue = Math.abs((markRGB & 0x000000FF) - (rgb & 0x000000FF));
        if( deltaRed + deltaGreen + deltaBlue < 20 ) {
          gameScreen.setRGB(x, y, Color.MAGENTA.getRGB());
          count++;
          avgx += x;
          avgy += y;
        }
      }
    }
    if( count > 5 ) {
      int x = avgx / count;
      int y = avgy / count;
      System.err.println("Grabbing mark at " + x + ", " + y);
      mouseClick(x + gameScreenRect.x, y + gameScreenRect.y, 100, InputEvent.BUTTON1_MASK);
      sleep(3000);
    }
//    try {
//      ImageIO.write(gameScreen, "png", new File("screen" + System.currentTimeMillis() + ".png"));
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
  }
  public Location getCurrentLocation() {
    BufferedImage currentMap = ImageProcessor.cropMap(ImageProcessor.removeObjects(ImageProcessor.removePlayer(robot.createScreenCapture(ImageProcessor.MAP_RECT_SMALL))));
    return mapManager.getLocation(currentMap);
  }
  public boolean moveTo(int x, int y) throws InterruptedException {
    int attempts = 5;
    int maxWait = 5000;
    int increments = 10;
    detectAndTakeMarks();
    for( int attempt = 0; attempt < attempts; attempt++ ) {
      Location l = getCurrentLocation();
      move(x - l.getX(), y - l.getY());
      for( int count = 0; count < increments; count++ ) {
        sleep(maxWait / increments);
        long startTime = System.currentTimeMillis();
        l = getCurrentLocation();
        long endTime = System.currentTimeMillis();
//        System.err.println("time to get location:" + (endTime-startTime));
        if( l.getX() == x && l.getY() == y ) {
          return true;
        }
      }
    }
    return false;
  }
  public class Stage {
    public int targetX;
    public int targetY;
    public Rectangle click;
    public Stage nextStage;
    public String name;
    public long time;
    public int mapLabel;
    public Stage(int x, int y, Rectangle rect, long time, int mapLabel) {
      targetX = x;
      targetY = y;
      click = rect;
      this.time = time;
      this.mapLabel = mapLabel;
    }
    public Stage(int x, int y, Rectangle rect, long time, int mapLabel, String name) {
      this(x, y, rect, time, mapLabel);
      this.name = name;
    }
    public boolean isStage(Location l) {
      return isCoord(l) && l.getMap().getLabel() == mapLabel;
    };
    public boolean isCoord(Location l) {
      return true;
    }
  }
  public void doStage(Stage s) throws InterruptedException {
    boolean success = moveTo(s.targetX, s.targetY);
    if( s.click != null ) {
      mouseClickMiss(s.click, 100, InputEvent.BUTTON1_MASK);
    }
    mapManager.moveMapToFront(s.nextStage.mapLabel);
    sleep(s.time);
  }
  @Override
  public void run() {
    try {
      detectAndTakeMarks();
      System.err.println("Starting canifis agility");
      mapManager.loadCanifisAgility();
      Location l = getCurrentLocation();
      System.err.println(l);
      boolean success = true;
      sleep(500);
      Stage stage0a = new Stage(28, 14, new Rectangle(474, 445, 10, 20), 5000, 0, "0a") {
        @Override
        public boolean isCoord(Location l) {
          return l.getX() >= 13;
        }
      };
      Stage stage0b = new Stage(13, 11, null, 1000, 0, "0b") {
        @Override
        public boolean isCoord(Location l) {
          return l.getX() < 13;
        }
      };
      Stage stage1 = new Stage(0, 0, new Rectangle(453, 473, 30, 20), 3000, 1, "1");
      Stage stage2a = new Stage(11, 1, new Rectangle(373, 544, 30, 40), 3000, 2, "2a") {
        @Override
        public boolean isCoord(Location l) {
          return l.getX() > 6;
        }
      };
      Stage stage2b = new Stage(0, 6, new Rectangle(360, 551, 30, 30), 3500, 2, "2b") {
        @Override
        public boolean isCoord(Location l) {
          return l.getX() <= 6;
        }
      };
      Stage stage3 = new Stage(3, 6, new Rectangle(465, 581, 30, 30), 3000, 3, "3");
      Stage stage4 = new Stage(3, 3, new Rectangle(457, 587, 40, 30), 6000, 4, "ZIPLINE");
      Stage stage5 = new Stage(13, 2, new Rectangle(546, 508, 40, 40), 3000, 5, "5");
      Stage stage6 = new Stage(1, 0, new Rectangle(461, 458, 20, 20), 3000, 6, "6");
      List<Stage> stageList = new LinkedList<Stage>();
      
      stageList.add(stage0a);
      stageList.add(stage0b);
      stageList.add(stage1);
      stageList.add(stage2a);
      stageList.add(stage2b);
      stageList.add(stage3);
      stageList.add(stage4);
      stageList.add(stage5);
      stageList.add(stage6);
      stage0a.nextStage = stage1;
      stage0b.nextStage = stage0a;
      stage1.nextStage = stage2a;
      stage2a.nextStage = stage2b;
      stage2b.nextStage = stage3;
      stage3.nextStage = stage4;
      stage4.nextStage = stage5;
      stage5.nextStage = stage6;
      stage6.nextStage = stage0a;
   
      while(true) {
        long startTime = System.currentTimeMillis();
        Location current = getCurrentLocation();
        long endTime = System.currentTimeMillis();
//        System.err.println("time to get location:" + (endTime-startTime));
        
        if( current != null ) {
          for( Stage s : stageList ) {
            if( s.isStage(current) ) {
              System.err.println("Doing stage " + s.name);
              doStage(s);
            }
          }
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  public void move(int deltax, int deltay) throws InterruptedException {
    if( deltax == 0 && deltay == 0 ) {
      return;
    }
    int mouseX = ImageProcessor.MAP_RECT_CROPPED.x + 45 + deltax*4;
    int mouseY = ImageProcessor.MAP_RECT_CROPPED.y + 45 + deltay*4;
    mouseClick(mouseX, mouseY, 100, InputEvent.BUTTON1_MASK);
  }
  
  public void mouseClick(int x, int y, long delay, int button) throws InterruptedException {
    mouseMove(x, y);
    sleep(delay);
    robot.mousePress(button);
    sleep(delay);
    robot.mouseRelease(button);
    sleep(delay);
  }
  private void mouseClickMiss(Rectangle rect, long delay, int button) throws InterruptedException {
    int targetX = rect.x + (int)(RunescapeDriver.getRandomGaussian(3) * rect.width);
    int targetY = rect.y + (int)(RunescapeDriver.getRandomGaussian(3) * rect.height);
    mouseClick(targetX, targetY, delay, button);
  }

  private void sleep(long time) throws InterruptedException {
    Thread.sleep(time);
  }

  private void mouseMove(int x, int y) throws InterruptedException {
    int targetX = x;
    int targetY = y;
    Point target = new Point(targetX, targetY);
    Point cur;
    int counter = 0;
    do {
      cur = MouseInfo.getPointerInfo().getLocation();
      targetX = (target.x > cur.x) ? 1 : (target.x < cur.x) ? -1 : 0;
      targetY = (target.y > cur.y) ? 1 : (target.y < cur.y) ? -1 : 0;
      robot.mouseMove(cur.x + targetX, cur.y + targetY);
      // Thread.sleep(1);
    } while (!cur.equals(target) && counter++ < 1280);
  }
}
