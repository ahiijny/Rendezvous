package ahiijny.rendezvous;

import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class TimeDisplay extends JTextPane 
{
	public Sim sim;
	public Date date;
	public SimpleDateFormat sdf;
	private Font font = new Font("Courier", Font.PLAIN, 16);
	private SimpleAttributeSet keyWord = new SimpleAttributeSet();
	private StyledDocument doc;
	private Color bgColor = Color.black;
	private Color fgColor = Color.red;
	
	public TimeDisplay(Sim sim) 
	{
		this.sim = sim;
		setFont(font);		
		setEditable (false);
		setBackground(bgColor);
		setForeground(fgColor);
		setCaretColor(fgColor);
		StyleConstants.setAlignment(keyWord, StyleConstants.ALIGN_CENTER);
		doc = getStyledDocument ();
		doc.setParagraphAttributes(0, doc.getLength(), keyWord, false);
		sdf = new SimpleDateFormat("\'t = Day\' DDD, HH:mm:ss.SSS");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		date = new Date();	
	}
	
	public void setText(String str)
	{
		doc = getStyledDocument();
		try {		
			doc.remove(0,doc.getLength());
			doc.insertString(0, str, keyWord);
		} catch(Exception e) {	 
			System.out.println(e); 
		}		
	}
	
	public void update()
	{
		date.setTime((long)(sim.t*1000));
		setText(sdf.format(date));
	}
}
