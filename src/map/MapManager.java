package map;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

public class MapManager {
  private List<Map> mapList;
  public MapManager() {
  }
  public void loadCanifisAgility() {
    mapList = new LinkedList<Map>();
    try {
      for( int index = 0; index <= 6; index++ ) {
        BufferedImage image = ImageIO.read(new File("maps/canifis/canifis" + index + ".png"));
        Map map = new Map(image, index);
        mapList.add(map);
      }
      System.err.println("Successfully loaded all 7 canifis maps.");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public Location getLocation(BufferedImage image) {
    Map found = null;
    Point intersection = null;
    for( Map map : mapList ) {
      Point i = map.getIntersection(image);
      if( i != null ) {
        found = map;
        intersection = i;
        break;
      }
    }
    if( found != null ) {
      if( mapList.remove(found) ) {
        mapList.add(0, found);
      }
      return new Location(found, intersection);
    }
    return null;
  }
  public void moveMapToFront(int mapLabel) {
    Map map = null;
    for( Map m : mapList ) {
      if( m.getLabel() == mapLabel ) {
        map = m;
        break;
      }
    }
    if( map != null ) {
      if( mapList.remove(map) ) {
        System.err.println("Moving " + mapLabel + " to front");
        mapList.add(0, map);
      }
    }
  }
}
