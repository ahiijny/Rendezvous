import java.text.DecimalFormat;

/** http://ccar.colorado.edu/asen5070/primers/kep2cart_2000/kep2cart.htm
 * http://www.scribd.com/doc/128255987/M002-Cartesian-State-Vectors-to-Keplerian-Orbit-Elements
 */
public class Satellite 
{	
	public static double sin(double theta)
	{
		return Math.sin(theta);
	}
	
	public static double cos(double theta)
	{
		return Math.cos(theta);
	}
	
	public static double tan(double theta)
	{
		return Math.tan(theta);
	}	
	
	// Simulation	
	
	Sim sim;
	String name;
	DecimalFormat large = new DecimalFormat("0.000E0");
	DecimalFormat angle = new DecimalFormat("0.00");
	
	// Satellite Parameters	
	public double emptyMass = 78000;
	public double fuelMass = 30000;
	public double propFlow = 8.71;
	public double nEngines = 2;
	public double thrustPerEngine = 2727;
	
	// State Vectors
	public double[] r = new double[3];
	public double[] v = new double[3];
	
	// Orbital elements
	public double SMa = 6.670E6;
	public double Ecc = 0;
	public double Inc = Math.toRadians(3.50);
	public double LAN = Math.toRadians(359.96);
	public double AgP = Math.toRadians(230.35);
	public double TrA = Math.toRadians(3.53);
	
	// Secondary Elements
	public double SMi = 6.670E6;
	public double PeD = 6.668E6;
	public double ApD = 6.672E6;
	public double Rad = 6.668E6;
	public double PeA = 300;
	public double ApA = 400;
	public double Alt = 350;
	public double T = 5.421E3;
	public double PeT = 5.368E3;
	public double ApT = 2.658E3;
	public double Vel = 7.733E3;			
	public double MnA = Math.toRadians(3.53);
	
	public Satellite(String name)
	{
		this.name = name;
	}
	
	public Satellite(String name,double SMa, double Ecc, double Inc, double LAN, double AgP, double TrA)
	{
		this(name);
		this.SMa = SMa;
		this.Ecc = Ecc;
		this.Inc = Math.toRadians(Inc);
		this.LAN = Math.toRadians(LAN);
		this.AgP = Math.toRadians(AgP);
		this.TrA = Math.toRadians(TrA);
	}
	
	/** Computes and stores the state vectors corresponding to
	 * the current orbital element values. This method uses:
	 * Semi-major axis, eccentricity, inclination, longitude 
	 * of ascending node, argument of periapsis, and true anomaly.
	 */
	public void orbitalToState()
	{
		double mu = sim.mu;
		double p = SMa * (1 - Ecc * Ecc);
		double rad = p / (1 + Ecc * cos(TrA));		
		double h = Math.sqrt(mu * SMa * (1 - Ecc * Ecc));
		double presum = sin(TrA) * h * Ecc / (rad * p); 
		
		r[0] = rad * (cos(LAN)*cos(AgP+TrA)-sin(LAN)*sin(AgP+TrA)*cos(Inc));;
		r[1] = rad * (sin(LAN)*cos(AgP+TrA)+cos(LAN)*sin(AgP+TrA)*cos(Inc));
		r[2] = rad * (sin(Inc)*sin(AgP+TrA));
		
		v[0] = r[0]*presum - h/rad*(cos(LAN)*sin(AgP+TrA)+sin(LAN)*cos(AgP+TrA)*cos(Inc));
		v[1] = r[1]*presum - h/rad*(sin(LAN)*sin(AgP+TrA)-cos(LAN)*cos(AgP+TrA)*cos(Inc));
		v[2] = r[2]*presum + h/rad*(sin(Inc)*cos(AgP+TrA));
	}
	
	public double rx(double nu)
	{
		return cos(LAN)*cos(AgP+nu)-sin(LAN)*sin(AgP+nu)*cos(Inc);
	}
	
	public double ry(double nu)
	{
		return sin(LAN)*cos(AgP+nu)+cos(LAN)*sin(AgP+nu)*cos(Inc);
	}
	
	public double rz(double nu)
	{
		return sin(Inc)*sin(AgP+nu);
	}
	
	public double dist(double nu)
	{
		return SMa * (1 - Ecc * Ecc) / (1 + Ecc * cos(nu));	
	}
	
	/** Uses the current state vectors and computes the
	 * corresponding orbital elements.
	 */
	public void stateToOrbital()
	{
		double rad = Calc.mag(r);
		double h[] = Calc.cross(r, v);
		double e[] = Calc.scale(Calc.cross(r, h), 1/sim.mu);
		double r_hat[] = Calc.scale(r, -1/rad);
		e = Calc.add(e, r_hat);
		double n[] = {-h[1], h[0], 0};
		double n_mag = Calc.mag(n);
		double v_mag = Calc.mag(v);
		
		Ecc = Calc.mag(e);		
		TrA = Math.acos(Calc.dot(e,r)/(rad*Ecc));
		if (Calc.dot(r, v) < 0)
			TrA = 2*Math.PI - TrA;
		Inc = Math.acos(h[2]/Calc.mag(h));
		LAN = Math.acos(n[0]/n_mag);
		if (n[1] < 0)
			LAN = 2*Math.PI - LAN;
		AgP = Math.acos(Calc.dot(n, e)/(n_mag * Ecc));
		if (e[2] < 0)
			AgP = 2*Math.PI - AgP;
		SMa = 1 / (2/rad - v_mag*v_mag / sim.mu);
	}
	
	/** Uses the current orbital elements to determine
	 * the values of all of the secondary parameters.
	 */
	public void updateSecondaries()
	{
		SMi = Math.sqrt(SMa * SMa * (1 - Ecc * Ecc));		
		PeD = SMa * (1 - Ecc);
		ApD = SMa * (1 + Ecc);
		PeA = PeD - sim.planet.R;
		ApA = ApD - sim.planet.R;
		T = 4*Math.PI*Math.PI/sim.mu * SMa*SMa*SMa;
		Vel = Calc.mag(v);
		Rad = Calc.mag(r);
		Alt = Rad - sim.planet.R;

		double E = 2 * Math.atan2(tan(TrA/2), Math.sqrt((1+Ecc)/(1-Ecc)));
		MnA = E - Ecc * sin(E);
		PeT = T - MnA / (2*Math.PI/T);
		ApT = PeT - T/2;
		if (ApT < 0)
			ApT += T;
	}
	
	@Override
	public String toString()
	{
		String str = "---" + name + "---" + "\n";
		str += "SMa " + large.format(SMa) + "\n";
		str += "SMi " + large.format(SMi) + "\n";
		if (!sim.parent.alt)
		{
			str += "PeD " + large.format(PeD) + "\n";
			str += "ApD " + large.format(ApD) + "\n";
			str += "Rad " + large.format(Rad) + "\n";
		}
		else
		{
			str += "PeA " + angle.format(PeA/1000) + "k\n";
			str += "ApA " + angle.format(ApA/1000) + "k\n";
			str += "Alt " + angle.format(Alt/1000) + "k\n";
		}
		str += "Ecc " + angle.format(Ecc) + "\n";
		str += "T   " + large.format(T) + "\n";
		str += "PeT " + large.format(PeT) + "\n";
		str += "ApT " + large.format(ApT) + "\n";
		str += "Vel " + large.format(Vel) + "\n";
		str += "Inc " + angle.format(Math.toDegrees(Inc)) + "°\n";
		str += "LAN " + angle.format(Math.toDegrees(LAN)) + "°\n";
		str += "AgP " + angle.format(Math.toDegrees(AgP)) + "°\n";
		str += "TrA " + angle.format(Math.toDegrees(TrA)) + "°\n";
		str += "MnA " + angle.format(Math.toDegrees(MnA)) + "°\n";
		return str;
	}
	
	public String name()
	{
		return name;
	}

}
