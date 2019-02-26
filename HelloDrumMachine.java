/*
    The HelloDrumMachine class sets up a drum machine, deploys its GUI, adds Listeners,
    and responds to user's ActionEvents by changing up the drum pattern. Can also change
    the drum pattern's tempo, or save or load it to a file.
*/

import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;
import java.awt.event.MouseEvent;
import java.io.*;

public class HelloDrumMachine implements Serializable {
    
    JPanel mainPanel;    
    ArrayList <JPanel> panelList;
    ArrayList<Integer> triggerList;
        
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame theFrame;
    JLabel tempoLabel;
    int tempoNumber;
        
    // List of instruments
    String [] instrumentNames = {"KICK DRUM", "CLOSED HI-HAT  ", "OPEN HI-HAT", 
        "SNARE", "CRASH CYMBAL", "HIGH TOM", "MID TOM"};
    int[] instruments = {35, 42, 46, 38, 49, 50, 47};
    
    public static void main (String[]args) {
        new HelloDrumMachine().buildGUI();
    }
    
    // Setup GUI
    public void buildGUI() {
          
        tempoNumber = 120;
        
        // Add buttons, setup layout, etc.
        theFrame = new JFrame("Hello DrumMachine");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);       
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        background.setBackground(Color.black);
                
        Box buttonBox = new Box(BoxLayout.Y_AXIS);
        
        JButton start = new JButton("Play");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);
        
        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);
        
        tempoLabel = new JLabel(" " + Integer.toString(tempoNumber) + " BPM ");
        tempoLabel.setFont(new Font("sanserif", Font.BOLD, 18));
        tempoLabel.setForeground(Color.white);
        
        buttonBox.add(new JLabel("    "));
        buttonBox.add(tempoLabel);
        
        JButton upTempo = new JButton("+");
        upTempo.addActionListener(new MyIncreaseTempoListener());
        buttonBox.add(upTempo);
        
        JButton downTempo = new JButton("-");
        downTempo.addActionListener(new MyDecreaseTempoListener());
        buttonBox.add(downTempo);

        buttonBox.add(new JLabel("    "));

        JButton clear = new JButton("Clear");
        clear.addActionListener(new MyClearListener());
        buttonBox.add(clear);

        buttonBox.add(new JLabel("    "));
        
        JButton saveButton = new JButton("Save");
        buttonBox.add(saveButton);

        JButton loadButton = new JButton("Load");
        buttonBox.add(loadButton);
        
        Font bigFont = new Font("sanserif", Font.PLAIN, 18);        
        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 7; i++) {
            Label label = new Label(instrumentNames[i]);
            label.setFont(bigFont);
            nameBox.add((label));
        }
        nameBox.setForeground(Color.white);
        
        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);
        
        theFrame.getContentPane().add(background);
        
        // The GUI will have a main grid component for entering beats.
        // If more instruments will be added later, then this part will need modification.
        GridLayout grid = new GridLayout(7, 16);
        grid.setVgap(3);
        grid.setHgap(3);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);
        
        panelList = new ArrayList <JPanel>();
        triggerList = new ArrayList <Integer>();
        
        // Add a listener to each panel in the grid.
        for (int i = 0; i < 112; i++) {
            MyPanel myPanel = new MyPanel();
            myPanel.addMouseListener(new gridMouseListener());

            mainPanel.add(myPanel);
            panelList.add(myPanel);         
            triggerList.add(0);
        }    
        
        setUpMidi();
        
        // Finalize GUI dimensions.
        // May need modification later if more instruments are added.
        theFrame.setBounds(0,0,300,300);
        theFrame.pack();
        theFrame.setSize(1200,450);
        theFrame.setVisible(true);
    }
           
    public void setUpMidi() {
        try {
            // Open a sequencer and set its tempo.
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }
    
    public void buildTrackAndStart() {
        int [] trackList = null;
        
        sequence.deleteTrack(track);
        track = sequence.createTrack();
        
        // Iterate over all the panels in the grid, and trigger
        // the appopriate instrument at the appropriate beat.
        for (int i = 0; i < 7; i++) {           
            trackList = new int[16];
            
            int key = instruments[i];
            
            for (int j = 0; j < 16; j++) {                

                int assess = triggerList.get(j + (16*i));
                if (assess == 1) {
                    trackList[j] = key;
                }
                else {
                    trackList[j] = 0;
                }
            }            
            makeTracks(trackList);
            track.add(makeEvent(176,1,127,0,16));
        }
        
        track.add(makeEvent(192,9,1,0,15));
        
        try {            
            // Sequencer will loop continuously by default.
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(tempoNumber);
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }
    
    // Implement mouse listeners that can change drum patterns,
    // increase or decrease tempo, and/or save the beat.
    public class gridMouseListener implements MouseListener {
        public void mouseClicked (MouseEvent e) {
            //System.out.println("You clicked a grid");
            
            MyPanel p;            
            p = (MyPanel)(e.getSource());
            //System.out.println(p.getNumber());
            
            int panelNum = p.getNumber();
            Boolean a = p.getOffOrOn();
            
            if (!a) {
                p.repaint();
                p.setOffOrOn(true);
            }
            else {
                p.repaint();
                p.setOffOrOn(false);
            }
            
            if (triggerList.get(panelNum) == 0) {
                triggerList.set(panelNum, 1);
            }
            else if (triggerList.get(panelNum) == 1) {
                triggerList.set(panelNum, 0);
            }            
        }
        
        public void mouseExited (MouseEvent e) {}
        public void mouseEntered (MouseEvent e) {}
        public void mouseReleased (MouseEvent e) {}
        public void mousePressed (MouseEvent e) {}
    }
    
    public class MyStartListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            buildTrackAndStart();
        }        
    }    
    
    public class MyStopListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            sequencer.stop();
        }
    }
    
    public class MyIncreaseTempoListener implements ActionListener {
        public void actionPerformed (ActionEvent a) {
            tempoNumber = (int)Math.round(sequencer.getTempoInBPM());
            tempoNumber += 2;
            sequencer.stop();
            sequencer.setTempoInBPM(tempoNumber);
            tempoLabel.setText(" " + Integer.toString(tempoNumber) + " BPM ");
            buildTrackAndStart();
        }
    }    
    
    public class MyDecreaseTempoListener implements ActionListener {
        public void actionPerformed (ActionEvent a) {
            tempoNumber = (int)Math.round(sequencer.getTempoInBPM());
            tempoNumber -= 2;
            sequencer.stop();
            sequencer.setTempoInBPM(tempoNumber);
            tempoLabel.setText(" " + Integer.toString(tempoNumber) + " BPM ");
            sequencer.start();
        }
    }

    public class MyClearListener implements ActionListener {
        public void actionPerformed (ActionEvent a) {            
            //System.out.println("Cleared triggerlist");
            sequencer.stop();
            for (int y = 0; y < 112; y++) {
                MyPanel mp = (MyPanel) panelList.get(y);
                mp.setOffOrOn(false);
                panelList.set(y, mp);
                triggerList.set(y, 0);
                mp.repaint();
            }            
        }
    }
    
    // makeTracks will iterate over each of the 7 instruments
    // and trigger the key on the appropriate beat.
    public void makeTracks(int[]list) {
        
        for (int i = 0; i < 16; i++) {
            int key = list[i];
            
            if (key != 0) {
                track.add(makeEvent(144, 9, key, 100, i));
                track.add(makeEvent(128, 9, key, 100, i));
            }
        }
    }
    
    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            event = new MidiEvent(a, tick);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }
    
    // SaveListener will save or load a drum pattern.
    public class MySaveListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            
            boolean[] patternState = new boolean[112];
            
            for (int i = 0; i < 112; i++) {
                int assess = triggerList.get(i);
                if (assess == 1) {
                    patternState[i] = true;
                }
            }
            
            try {
                FileOutputStream fileOut = new FileOutputStream(new File("patternState.ser"));
                ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
                objOut.writeObject(patternState);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public class MyLoadListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            boolean [] inBooleanArray = null;
            
            try {
                FileInputStream fileIn = new FileInputStream(new File("patternState.ser"));
                ObjectInputStream objIn = new ObjectInputStream(fileIn);
                inBooleanArray = (boolean[]) objIn.readObject();
            }
            catch (Exception ex) {
                System.out.println("File not found");
                ex.printStackTrace();
            }
            
            for (int i = 0; i < 112; i++) {
                if (inBooleanArray[i] == false) {
                    triggerList.set(i, 0);
                } 
                else if (inBooleanArray[i] == true) {
                    triggerList.set(i, 1);
                }
            }
            sequencer.stop();
            buildTrackAndStart();
        }
    }
}
