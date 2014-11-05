import javax.swing.UIManager;


public class Astrophysics 
{
	public static void main(String[] args) 
	{
		try 
		{            
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{			
		}
		new GraphicUI("Test", 1000, 750);
	}
}
