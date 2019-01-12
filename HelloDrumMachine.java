import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class HelloDrumMachine {
    
    JPanel mainPanel;

    ArrayList <JCheckBox> checkBoxList;
    
    ArrayList <JPanel> panelList;
        
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame theFrame;
    JLabel tempoLabel;

    int tempoNumber;
    
    ArrayList<Integer> triggerList;
        
    String [] instrumentNames = {"KICK DRUM", "CLOSED HI-HAT    ", "OPEN HI-HAT", 
        "SNARE", "CRASH", "CLAP", "HIGH TOM", "HIGH BONGO", "MARACAS", 
        "WHISTLE", "LOW CONGA", "COWBELL", "SLAP", "MID TOM", 
        "HIGH AGOGO", "HIGH CONGA"};
    int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 
        47, 67, 63};
    
    public static void main (String[]args) {
        new HelloDrumMachine().buildGUI();
    }
    
    public void buildGUI() {
                
        theFrame = new JFrame("Hello DrumMachine");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);       
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        background.setBackground(Color.black);
        
        //checkBoxList = new ArrayList<JCheckBox>();
        
        
        Box buttonBox = new Box(BoxLayout.Y_AXIS);
        
        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);
        
        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);
        
        tempoNumber = 120;        
        tempoLabel = new JLabel(" " + Integer.toString(tempoNumber) + " BPM");
        tempoLabel.setFont(new Font("sanserif", Font.BOLD, 18));
        tempoLabel.setForeground(Color.white);
        
        buttonBox.add(tempoLabel);
        
        JButton upTempo = new JButton("+");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);
        
        JButton downTempo = new JButton("-");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        JButton clear = new JButton("Clear");
        clear.addActionListener(new MyClearListener());
        buttonBox.add(clear);

        Font bigFont = new Font("sanserif", Font.PLAIN, 18);        
        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 16; i++) {
            Label label = new Label(instrumentNames[i]);
            label.setFont(bigFont);
            nameBox.add((label));
        }
        nameBox.setForeground(Color.white);
        
        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);
        
        theFrame.getContentPane().add(background);
        
        GridLayout grid = new GridLayout(16, 16);
        grid.setVgap(3);
        grid.setHgap(3);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);
        
        panelList = new ArrayList <JPanel>();
        triggerList = new ArrayList <Integer>();
        
        for (int i = 0; i < 256; i++) {
            MyPanel myPanel = new MyPanel();
            myPanel.addMouseListener(new gridMouseListener());

            mainPanel.add(myPanel);
            panelList.add(myPanel);
            
            triggerList.add(0);
            
        }    
        
        setUpMidi();
        
        theFrame.setBounds(50,50,300,300);
        theFrame.pack();
        theFrame.setSize(800,700);
        theFrame.setVisible(true);
    }
    
    public void mouseClicked(MouseEvent e) {
        System.out.println("You clicked something");
    }
    
    
    public void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(tempoNumber);
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }
    
    public void buildTrackAndStart() {
        int [] trackList = null;
        
        sequence.deleteTrack(track);
        track = sequence.createTrack();
        
        for (int i = 0; i < 16; i++) {           
            trackList = new int[16];
            
            int key = instruments[i];
            
            for (int j = 0; j < 16; j++) {                
                //JCheckBox jc = (JCheckBox) checkBoxList.get(j + (16*i));                
                //if (jc.isSelected()) {
                //    trackList[j] = key;
                //}
                //else {
                //    trackList[j] = 0;
                //}
                
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
            
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }
    
    public class gridMouseListener implements MouseListener {
        public void mouseClicked (MouseEvent e) {
            System.out.println("You clicked a grid");
            
            MyPanel p;            
            p = (MyPanel)(e.getSource());
            System.out.println(p.getNumber());
            
            int panelNum = p.getNumber();
            Boolean a = p.getOffOrOn();
            
            if (!a) {
                p.repaint();
                p.setOffOrOn(true);
                //triggerList.set(p.getNumber(), true);
            }
            else {
                p.repaint();
                p.setOffOrOn(false);
                //triggerList.set(p.getNumber(), false);
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
    
    public class MyUpTempoListener implements ActionListener {
        public void actionPerformed (ActionEvent a) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor + 2));
            tempoNumber += 2;
            tempoLabel.setText(" " + Integer.toString(tempoNumber) + " BPM");

        }
    }
    
    
    public class MyDownTempoListener implements ActionListener {
        public void actionPerformed (ActionEvent a) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor - 2));
            tempoNumber -= 2;
            tempoLabel.setText(" " + Integer.toString(tempoNumber) + " BPM");
        }
    }

    public class MyClearListener implements ActionListener {
        public void actionPerformed (ActionEvent a) {            
            System.out.println("Cleared triggerlist");
            sequencer.stop();
            for (int y = 0; y < 256; y++) {
                MyPanel mp = (MyPanel) panelList.get(y);
                mp.setOffOrOn(false);
                mp.repaint();
                panelList.set(y, mp);
                triggerList.set(y, 0);
            }            
        }
    }
    
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
}