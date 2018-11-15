package main;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Category {
  
  public Category() {
    List<BufferedReader> readers = new LinkedList<BufferedReader>();
    List<String> fileNames = new LinkedList<String>();
    for( int i = 1; i < 8; i++ ) {
      fileNames.add("predictions_Category" + i + ".txt");
    }
    String header = "";
    fileNames.add("predictions_Categorypart.txt");
    fileNames.add("predictions_Categorypart2.txt");
    try {
      for( String name : fileNames ) {
        File file = new File(name);
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        header = bufferedReader.readLine();
        readers.add(bufferedReader);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter("predictions.txt"));
      writer.write(header);
      writer.newLine();
      while(true) {
        int[] votes = new int[10];
        String lineStart = null;
        boolean first = true;
        for(BufferedReader reader : readers) {
          String line = reader.readLine();
          if( line == null ) {
            if( first ) {
              System.err.println("returning");
              return;
            }
            else {
              continue;
            }
          }
          first = false;
          if( lineStart == null ) {
            lineStart = line.split(",")[0];
          }
          line = line.split(",")[1];
          int vote = Integer.parseInt(line);
          votes[vote]++;
        }
        int maxVote = 0;
        int max = votes[maxVote];
        for( int i = 0; i < votes.length; i++ ) {
          if( votes[i] > max ) {
            maxVote = i;
            max = votes[maxVote];
          }
        }
        writer.write(lineStart + "," + maxVote);
        writer.newLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      System.err.println("done");
      try {
        writer.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) {
    new Category();
  }

}
