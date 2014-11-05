import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


public class MFD extends JPanel 
{
	public GraphicUI parent;
	public JTextPane data = new JTextPane ();
	public Satellite sat;
	
	private Font font = new Font ("Courier", Font.PLAIN, 12);
	private SimpleAttributeSet keyWord = new SimpleAttributeSet();
	private Color bgColor = Color.black;
	private Color fgColor = Color.white;
	
	public MFD(GraphicUI parent) 
	{
		this(parent, null);
	}
	
	public MFD(GraphicUI parent, Satellite sat)
	{
		this.parent = parent;
		this.sat = sat;
		data.setFont(font);		
		data.setEditable (false);
		data.setBackground (bgColor);
		data.setForeground(fgColor);
		data.setCaretColor (fgColor);		
		add(data);
	}
	
	public void setPaneBackground (Color bg)
	{
		bgColor = bg;
		data.setBackground (bgColor);
	}
	
	public void setPaneForeground (Color fg)
	{
		fgColor = fg;
		data.setForeground (fgColor);
	}
	
	public void setTextColor (Color color)
	{
		if (color == null)
			color = fgColor;
		StyleConstants.setForeground(keyWord, color);
	}
	
	public void setTextBackground (Color color)
	{		
		if (color == null)
			color = bgColor;
		StyleConstants.setBackground(keyWord, color);
	}
	
	public void setSize (int size)
	{
		StyleConstants.setFontSize(keyWord, size);
	}
	
	/** Sets the given String as the text on this MDF.
	 * 
	 * @param str	the String to be put on the MFD
	 */
	public void setText (String str)
	{
		StyledDocument doc = data.getStyledDocument ();
		try {		
			doc.remove(0,doc.getLength());
			doc.insertString(0, str, keyWord);
		} catch(Exception e) {	 
			System.out.println(e); 
		}		
	}
	
	public void refresh()
	{
		setText(sat.toString());
	}
}
