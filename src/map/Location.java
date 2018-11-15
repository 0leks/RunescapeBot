package map;

import java.awt.Point;

public class Location {
  private Map map;
  private Point point;
  
  public Location(Map map, Point point) {
    this.map = map;
    this.point = point;
  }
  public Map getMap() {
    return map;
  }
  public int getX() {
    return point.x/4;
  }
  public int getY() {
    return point.y/4;
  }
  
  @Override
  public String toString() {
    return map + "->px(" + point.x + ", " + point.y + ")xy(" + point.x/4 + ", " + point.y/4 + ")";
  }
}
