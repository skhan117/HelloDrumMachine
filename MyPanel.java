import java.awt.*;
import javax.swing.*;

class MyPanel extends JPanel {

static int panelNumber;
int number;
Boolean offOrOn;

    public MyPanel() {
        panelNumber++;
        offOrOn = false;
        number = panelNumber;        
    }
    
    public void paintComponent(Graphics g) {
        
        if (!offOrOn) {
            g.setColor(Color.black);        
            g.fillRect(0,0,this.getWidth(),this.getHeight());
        }
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