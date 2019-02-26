/*
    The MyPanel class represents an individual panel in the GUI grid. Each panel can be
    clicked on or off, which will indicate when an individual sound should be played.
*/

import java.awt.*;
import javax.swing.*;

class MyPanel extends JPanel {

// Each panel will have a number in the grid. Information about each panel
// can be retrieved via this number.
static int panelNumber;
int number;
Boolean offOrOn;

    public MyPanel() {
        panelNumber++;
        // Each panel is "off" by default
        offOrOn = false;
        number = panelNumber;        
    }
    
    public void paintComponent(Graphics g) {
        
        // if a panel is "off", paint it black
        if (!offOrOn) {
            g.setColor(Color.black);        
            g.fillRect(0,0,this.getWidth(),this.getHeight());
        }
        // if a panel is "on", paint it red
        if (offOrOn) {
            g.setColor(new Color(204, 20, 55));
            g.fillRect(0,0,this.getWidth(),this.getHeight());
        }
    }   
    
    public int getNumber() {
        return number;
    }
    
    public void setNumber(int n) {
        number = n;
    }
    
    public void setOffOrOn(Boolean b) {
        offOrOn = b;
    }
    
    public Boolean getOffOrOn() {
        return offOrOn;
    }   
}
