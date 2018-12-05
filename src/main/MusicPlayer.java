import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class MusicPlayer {

  public static void random1(int base) {
    try {

      Sequencer sequencer = MidiSystem.getSequencer();
      sequencer.open();
      Sequence sequence = new Sequence(Sequence.PPQ,4);
      Track track = sequence.createTrack();

      MidiEvent event = null;
      track.add(new MidiEvent(new ShortMessage(192,1,0,0), 1));
      
      int time = 1;
      track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON,1,base,100), time));
      track.add( new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF,1,base,100), time+6));
      track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON,1,base + 7,100), time));
      track.add( new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF,1,base + 7,100), time+6));
      time+=6;
      
      track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON,1,base,100), time));
      track.add( new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF,1,base,100), time+2));
      time+=2;
      
      track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON,1,base+7,100), time));
      track.add( new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF,1,base+7,100), time+4));
      time+=4;
      
      track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON,1,base,100), time));
      track.add( new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF,1,base,100), time+2));
      time+=2;
      
      track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON,1,base+7,100), time));
      track.add( new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF,1,base+7,100), time+2));
      time+=2;
      
      int base2 = base + 2;
      track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON,1,base2,100), time));
      track.add( new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF,1,base2,100), time+4));
      track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON,1,base2+7,100), time));
      track.add( new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF,1,base2+7,100), time+4));
      time+=4;
      
      track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON,1,base2+4,100), time));
      track.add( new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF,1,base2+4,100), time+4));
      time+=4;
      
      track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON,1,base2,100), time));
      track.add( new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF,1,base2,100), time+4));
      time+=4;
      
      int base3 = base + 4;
      track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON,1,base3-5,100), time));
      track.add( new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF,1,base3-5,100), time+2));
      time+=2;
      
      track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON,1,base3,100), time));
      track.add( new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF,1,base3,100), time+16));
      track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON,1,base3+4,100), time));
      track.add( new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF,1,base3+4,100), time+16));
      time+=4;

      track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON,1,base3+7,100), time));
      track.add( new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF,1,base3+7,100), time+12));
      track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON,1,base3+12,100), time));
      track.add( new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF,1,base3+12,100), time+12));
      
      
      sequencer.setSequence(sequence);
      sequencer.setLoopCount(0);
      sequencer.start();
  } catch (Exception ex) { ex.printStackTrace(); }
  }
  
  public static void playNote(int finalNote, int finalInstrument) {
    try {
        Sequencer sequencer = MidiSystem.getSequencer();
        sequencer.open();
        Sequence sequence = new Sequence(Sequence.PPQ,4);
        Track track = sequence.createTrack();

        MidiEvent event = null;

        ShortMessage first = new ShortMessage();
        first.setMessage(192,1,finalInstrument,0);
        MidiEvent changeInstrument = new MidiEvent(first, 1);
        track.add(changeInstrument);
        
        track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON,1,finalNote,100), 0));
        track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF,1,finalNote,100), 10));
        sequencer.setSequence(sequence);
        sequencer.start();
    } catch (Exception ex) { ex.printStackTrace(); }

}
}
