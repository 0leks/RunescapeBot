package noMouseMove;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Optional;
import java.util.StringTokenizer;

public class Settings {

  private Setting<Integer> previousWindowXPosition = new Setting<>("PREVIOUS_X_POS", 0);
  private Setting<Integer> previousWindowYPosition = new Setting<>("PREVIOUS_Y_POS", 0);
  

  private HashMap<String, String> valueMap;

  public Settings() {
    valueMap = new HashMap<>();
  }
  
  public void saveToFile(File file) {
    
    valueMap.put(previousWindowXPosition.key, previousWindowXPosition.getStringValue());
    valueMap.put(previousWindowYPosition.key, previousWindowYPosition.getStringValue());
    
    try ( FileWriter fileWriter = new FileWriter(file);
          PrintWriter printWriter = new PrintWriter(fileWriter)) {
      valueMap.forEach((String key, String value) -> {
        printWriter.println(key + " " + value);
      });
    } catch (IOException e) {
      
      e.printStackTrace();
    }
  }
  
  public int getPreviousWindowXPosition() {
    return previousWindowXPosition.value;
  }
  public int getPreviousWindowYPosition() {
    return previousWindowYPosition.value;
  }
  public void setWindowPosition(int x, int y) {
    previousWindowXPosition.value = x;
    previousWindowYPosition.value = y;
  }
  

  public static Settings loadFromFile(File file) {
    Settings settings = new Settings();
    settings.valueMap.put(settings.previousWindowXPosition.key, settings.previousWindowXPosition.getStringValue());
    
    try ( FileReader fileReader = new FileReader(file); 
          BufferedReader bufferedReader = new BufferedReader(fileReader)) {
      String line;
      while((line = bufferedReader.readLine()) != null) {
        StringTokenizer st = new StringTokenizer(line);
        if(st.hasMoreTokens()) {
          String key = st.nextToken();
          String value = "";
          while(st.hasMoreTokens()) {
            value += st.nextToken();
          }
          settings.valueMap.put(key, value);
          System.out.println("Loaded setting: " + key + "=" + value);
        }
      }
    } catch (IOException e) {
      System.err.println("Error loading settings from file " + file.getName());
      e.printStackTrace();
    }
    
    String stringValue;
    
    stringValue = settings.valueMap.get(settings.previousWindowXPosition.key);
    settings.previousWindowXPosition.value = parseInt(stringValue).orElse(settings.previousWindowXPosition.value);
    
    stringValue = settings.valueMap.get(settings.previousWindowYPosition.key);
    settings.previousWindowYPosition.value = parseInt(stringValue).orElse(settings.previousWindowYPosition.value);
    
    return settings;
  }
  
  private static Optional<Integer> parseInt(String string) {
    try {
      return Optional.of(Integer.parseInt(string));
    }
    catch(NumberFormatException e) {
      System.out.println("Failed to parse int: " + string);
      return Optional.empty();
    }
  }
  
  private class Setting<T> {
    String key;
    T value;
    public Setting(String key, T defaultValue) {
      this.key = key;
      value = defaultValue;
    }
    public String getStringValue() {
      return value.toString();
    }
  }
}
