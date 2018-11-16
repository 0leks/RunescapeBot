package noMouseMove;

import java.io.File;

public class Driver {

  public Driver() {
    Settings s = Settings.loadFromFile(new File("settingsin.txt"));
    System.out.println("x " + s.getPreviousWindowXPosition() + " y " + s.getPreviousWindowYPosition());
    s.setWindowPosition(20, 30);
    s.saveToFile(new File("settingsout.txt"));
  }

  public static void main(String[] args) {
    new Driver();
  }

}
