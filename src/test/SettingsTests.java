package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import noMouseMove.Settings;

public class SettingsTests {
  
  private static final String NONEXIST = "testResources/nonexistantFile.txt";
  private static final String NORMAL = "testResources/normalValues.txt";
  private static final String UNPARSABLE = "testResources/unparsableValues.txt";
  private static final String OUTPUT = "testResources/testOutput.txt";

  @Test
  @DisplayName("Test default values after loading nonexistant file")
  void testDefaultValues() {
    Settings blankSettings = Settings.loadFromFile(new File(NONEXIST));
    assertEquals(blankSettings.getPreviousWindowXPosition(), 0);
    assertEquals(blankSettings.getPreviousWindowYPosition(), 0);
  }

  @Test
  @DisplayName("Test default values after loading unparsable values")
  void testDefaultValues2() {
    Settings unparsableSettings = Settings.loadFromFile(new File(UNPARSABLE));
    assertEquals(unparsableSettings.getPreviousWindowXPosition(), 0);
    assertEquals(unparsableSettings.getPreviousWindowYPosition(), 0);
  }

  @Test
  @DisplayName("Test loading normal values")
  void testLoad() {
    Settings normalSettings = Settings.loadFromFile(new File(NORMAL));
    assertEquals(normalSettings.getPreviousWindowXPosition(), 2);
    assertEquals(normalSettings.getPreviousWindowYPosition(), 1);
  }

  @Test
  @DisplayName("Test saving")
  void testSave() {
    Settings settings = Settings.loadFromFile(new File(NONEXIST));
    settings.setWindowPosition(3, 4);
    settings.saveToFile(new File(OUTPUT));

    Settings outputtedSettings = Settings.loadFromFile(new File(OUTPUT));
    assertEquals(outputtedSettings.getPreviousWindowXPosition(), 3);
    assertEquals(outputtedSettings.getPreviousWindowYPosition(), 4);
  }
}
