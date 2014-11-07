import javax.swing.UIManager;


public class Rendezvous 
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
		new GraphicUI("Test", 1200, 760);
	}
}
