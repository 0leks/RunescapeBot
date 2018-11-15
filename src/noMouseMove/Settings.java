package noMouseMove;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Settings {

  private int previousWindowXPosition;
  private int previousWindowYPosition;

  private HashMap<String, String> valueMap;

  public Settings() {
    valueMap = new HashMap<>();
  }

  public Settings loadFromFile(File file) {
    try (FileReader fileReader = new FileReader(file); BufferedReader bufferedReader = new BufferedReader(fileReader)) {

    } catch (IOException e) {
      System.err.println("Error loading settings from file " + file.getName());
      e.printStackTrace();
    }
    return this;
  }
}
