package map;

import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.ImageProcessor;

public class Map {
  private BufferedImage mapImage;
  private BufferedImage travelImage;
  private int label;
  private Robot robot;
  public Map(BufferedImage mapImage, int label) {
    this.mapImage = mapImage;
    this.label = label;
  }
  
//  public void createTravelMap(Robot robot) throws InterruptedException {
//    this.robot = robot;
//    BufferedImage travelImage = new BufferedImage(mapImage.getWidth(), mapImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
//    BufferedImage currentMap = ImageProcessor.cropMap(ImageProcessor.removeObjects(ImageProcessor.removePlayer(robot.createScreenCapture(ImageProcessor.MAP_RECT_SMALL))));
//    Point previousPoint = null;
//    for( int x = 0; x < mapImage.getWidth()-1; x++ ) {
//      for( int yy = 0; yy < mapImage.getHeight()-1; yy++ ) {
//        int y = yy;
//        if( x %2 == 1 ) {
//          y = mapImage.getHeight() - yy - 2;
//        }
//        System.err.println("Position " + x + ", " + y);
//        currentMap = ImageProcessor.cropMap(ImageProcessor.removeObjects(ImageProcessor.removePlayer(robot.createScreenCapture(ImageProcessor.MAP_RECT_SMALL))));
//        Point currentPosition = getIntersection(currentMap);
//        if( currentPosition != null ) {
//          System.err.println("Intersected=" + currentPosition.x + ", " + currentPosition.y);
//        }
//        if( currentPosition != null 
//            && x >= currentPosition.x 
//            && y >= currentPosition.y 
//            && x < currentPosition.x + ImageProcessor.MAP_RECT_CROPPED.getWidth()
//            && y < currentPosition.y + ImageProcessor.MAP_RECT_CROPPED.getHeight() ) {
//          int mouseX = ImageProcessor.MAP_RECT_CROPPED.x - currentPosition.x + x + (int)ImageProcessor.MAP_RECT_CROPPED.getWidth()/2;
//          int mouseY = ImageProcessor.MAP_RECT_CROPPED.y - currentPosition.y + y + (int)ImageProcessor.MAP_RECT_CROPPED.getHeight()/2;
//
////          System.err.println("moving to position " + x + ", " + y + " clicking " + mouseX + ", " + mouseY);
//          mouseClick(mouseX, mouseY,
//              100, 
//              InputEvent.BUTTON1_MASK);
//          Point cur = MouseInfo.getPointerInfo().getLocation();
////          System.err.println("mouse at " + cur.x + ", " + cur.y);
//          for( int wait = 0; wait < 10; wait++ ) {
//            sleep(1000);
//            BufferedImage newMap = ImageProcessor.cropMap(ImageProcessor.removeObjects(ImageProcessor.removePlayer(robot.createScreenCapture(ImageProcessor.MAP_RECT_SMALL))));
//            Point newPoint = getIntersection(newMap);
//            if( newPoint != null && newPoint.x/4 == x/4 && newPoint.y/4 == y/4) {
//              travelImage.setRGB(x, y, Color.white.getRGB());
//              System.err.println("successfully moved to position " + x + ", " + y);
//              break;
//            }
//            if( previousPoint != null && newPoint != null 
//                && previousPoint.x == newPoint.x && previousPoint.y == newPoint.y ) {
//              System.err.println("FAIL");
//              break;
//            }
//            previousPoint = newPoint;
//          }
//        }
//      }
//      try {
//        System.err.println("saving travel image");
//        ImageIO.write(travelImage, "png", new File("travel" + x + ".png"));
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//    }
//  }
//  public void mouseClick(int x, int y, long delay, int button) throws InterruptedException {
//    mouseMove(x, y);
//    sleep(delay);
//    robot.mousePress(button);
//    sleep(delay);
//    robot.mouseRelease(button);
//    sleep(delay);
//  }
//  private void sleep(long time) throws InterruptedException {
//    Thread.sleep(time);
//  }
//  private void mouseMove(int x, int y) throws InterruptedException {
//    int targetX = x;
//    int targetY = y;
//    Point target = new Point(targetX, targetY);
//    Point cur;
//    int counter = 0;
//    do {
//      cur = MouseInfo.getPointerInfo().getLocation();
//      targetX = (target.x > cur.x)?1:(target.x < cur.x)?-1:0;
//      targetY = (target.y > cur.y)?1:(target.y < cur.y)?-1:0;
//      robot.mouseMove(cur.x + targetX, cur.y + targetY);
////      Thread.sleep(1);
//    }
//    while( !cur.equals(target) && counter++ < 1280 );
//  }
  public Point getIntersection(BufferedImage image) {
//    System.err.println("intersecting with map " + label);
    int bestAlign = Integer.MAX_VALUE;
    Point maxPos = null;
    int bestSkipped = 0;
    int OFFSET_LIMIT = 4 * 8;
    int yLimit = mapImage.getHeight() - image.getHeight() + OFFSET_LIMIT;
    int xLimit = mapImage.getWidth() - image.getWidth() + OFFSET_LIMIT;
    int xStart = 0 - OFFSET_LIMIT;
    int yStart = 0 - OFFSET_LIMIT;
    int MAX_DIFF = 1;
    for (int y = yStart; y < yLimit && bestAlign > 0; y++) {
      for (int x = xStart; x < xLimit && bestAlign > 0; x++) {
        int diff = 0;
        int skipped = 0;
        for (int j = 0; j < image.getHeight() && diff < MAX_DIFF; j++) {
          for (int i = 0; i < image.getWidth() && diff < MAX_DIFF; i++) {
            if (x + i >= 0 && y + j >= 0 && x + i < mapImage.getWidth() && y + j < mapImage.getHeight()) {
              int largeRGB = mapImage.getRGB(x + i, y + j);
              int alpha = largeRGB >>> 24;
              int smallRGB = image.getRGB(i, j);
              int alpha2 = smallRGB >> 24;
              // comparisons++;
              if (alpha != 0 && alpha2 != 0 && largeRGB != image.getRGB(i, j)) {
                int deltaRed = Math.abs(((largeRGB & 0x00FF0000) >> 16) - ((smallRGB & 0x00FF0000) >> 16));
                int deltaGreen = Math.abs(((largeRGB & 0x0000FF00) >> 8) - ((smallRGB & 0x0000FF00) >> 8));
                int deltaBlue = Math.abs((largeRGB & 0x000000FF) - (smallRGB & 0x000000FF));
                if( deltaRed + deltaGreen + deltaBlue > 60 ) {
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
        if (diff < MAX_DIFF && skipped < image.getWidth() * image.getHeight() / 4 && diff < bestAlign) {
          maxPos = new Point(x, y);
          bestAlign = diff;
          bestSkipped = skipped;
        }
      }
    }
    if (bestAlign > 300) {
//      System.err.println("failed alignment " + bestAlign);
      return null;
    }
    return maxPos;
  }
  public int getLabel() {
    return label;
  }
  @Override
  public String toString() {
    return "map " + label;
  }
}
