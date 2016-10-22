package ahiijny.rendezvous;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class Logger 
{
	public StringBuffer csv;
	public Sim sim;
	public double loggedTime = -1;
	public double logStep = 5;
	public BufferedWriter out;
	
	public Logger(Sim sim) 
	{
		this.sim = sim;
		csv = new StringBuffer(0);
		csv.append("(s),(m),(m)\n");
		csv.append("Time,V-bar,R-bar\n");
	}
	
	public void log()
	{
		if (sim.t >= loggedTime + logStep)
		{
			csv.append(Double.toString(sim.t) + "," 
					+ Double.toString(sim.vbar) + ","
					+ Double.toString(sim.rbar) + "\n");
			
			loggedTime = sim.t;
		}
	}
	
	public void write()
	{	
		try 
		{
			BufferedWriter out = new BufferedWriter(new FileWriter("data.csv"));
			out.write(csv.toString());
			out.flush();
			out.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

}
