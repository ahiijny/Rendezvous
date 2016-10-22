package ahiijny.rendezvous;

public class Planet 
{
	public double M ;
	public double R;
	
	public Planet()
	{
		this(5.972E24, 6.371E6);
	}

	public Planet(double M, double R) 
	{
		this.M = M;
		this.R = R;
	}

}
