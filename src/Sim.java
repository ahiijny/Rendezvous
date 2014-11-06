import java.util.ArrayList;

public class Sim implements Runnable
{
	public static final double G = 6.67384E-11;
	public GraphicUI parent;
	public double mu = 0;
	public double t = 0;
	public double dt = 0.0001;
	public double warp = 100;
	public double refreshRate = 1;
	public double MFDt = 0;
	public ArrayList<Satellite> satellites;
	public Planet planet;
		
	private long nextEvent;
	private long nextRefresh;
	private Thread thread;
	private boolean running = false;

	public Sim(GraphicUI parent) 
	{
		this.parent = parent;
		satellites = new ArrayList<Satellite>();
		planet = new Planet();
		mu = planet.M * G;
	}
	
	public void add(Satellite sat)
	{
		satellites.add(sat);
		sat.sim = this;
	}
	
	public boolean rm(Satellite sat)
	{
		return satellites.remove(sat);
	}
	
	public Satellite get(int index)
	{
		if (index >= 0 && index < satellites.size())
			return satellites.get(index);
		else
			return null;
	}
	
	public void start()
	{
		if (!running)
		{			
			running = true;
			nextEvent = System.currentTimeMillis();
			nextRefresh = System.currentTimeMillis();
			thread = new Thread(this);	
			thread.start();
		}
	}
	
	public void stop()
	{
		running = false;
	}
	
	@Override
	public void run()
	{
		while (running)
		{
			// Update simulation
			
			if (t < MFDt)
			{
				while (t < MFDt)
				{
					for (int i = 0; i < satellites.size(); i++)
					{
						Satellite sat = satellites.get(i);
						double[] a = Calc.scale(sat.r, -mu/(sat.Rad*sat.Rad*sat.Rad));
						a = Calc.scale(a, dt);
						sat.v = Calc.add(a, sat.v);						
						sat.r = Calc.add(sat.r, Calc.scale(sat.v, dt));
					}
					t += dt;
				}
			}
						
			// Update refresh
			
			long remaining = nextRefresh - System.currentTimeMillis();
			
			if (remaining <= 0)
			{
				for (int i = 0; i < satellites.size(); i++)
				{
					satellites.get(i).stateToOrbital();
					satellites.get(i).updateSecondaries();
				}
				parent.refreshInFields();
				parent.refreshMFDs();
				parent.repaint();
				nextRefresh += (long)(1000 * refreshRate);
				MFDt += refreshRate * warp;
				remaining = nextRefresh - System.currentTimeMillis();
			}
			
			// Sleep, if necessary			
			
			if (remaining > 0)
			{
				try
				{
					Thread.sleep(remaining);
				}
				catch (InterruptedException e)
				{					
					System.out.println("Interrupt");
				}
			}
		}
	}
}
