import java.util.ArrayList;

public class Sim 
{
	public static final double G = 6.67384E-11;
	public GraphicUI parent;
	public double mu = 0;
	public ArrayList<Satellite> satellites;
	public Planet planet;

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
}
