package ahiijny.rendezvous;

import java.text.DecimalFormat;

/** http://ccar.colorado.edu/asen5070/primers/kep2cart_2000/kep2cart.htm
 * http://www.scribd.com/doc/128255987/M002-Cartesian-State-Vectors-to-Keplerian-Orbit-Elements
 * http://www.bogan.ca/orbits/kepler/orbteqtn.html
 * http://www.braeunig.us/space/orbmech.htm
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
	
	public Sim sim;
	public String name;
	public static DecimalFormat large = new DecimalFormat("0.000E0");
	public static DecimalFormat small = new DecimalFormat("0.00");
	public static DecimalFormat precise = new DecimalFormat("0.############");
	
	public static final int PROGRADE = 0;
	public static final int RETROGRADE = 1;
	public static final int NORMAL_PLUS = 2;
	public static final int NORMAL_MINUS = 3;
	
	public static final int OMS = 0;
	public static final int RCS = 1;
	
	// Satellite Parameters	
	public double emptyMass = 78000;
	public double fuelMass = 30000;
	public double mass = emptyMass + fuelMass;
	public int direction = PROGRADE;
	public int engine = OMS;
	public double thrust = 0;
	public double flowRate = 0;
	public int[] nEngines = {2, 4};
	public double[] flowRates = {8.71, 1.41};	
	public double[] thrusts = {2727, 395};
	public boolean burnScheduled = false;
	public double burnStart = 0;
	public double burnEnd = 0;
	
	// State Vectors
	public double[] r = new double[3];
	public double[] v = new double[3];
	
	// Orbital elements
	public double SMa = 6.670E6;
	public double Ecc = 0;
	public double Inc = Math.toRadians(3.50);
	public double LAN = Math.toRadians(0);
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
	public double TrL = Math.toRadians(233.88);
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
		double e[] = Calc.scale(Calc.cross(v, h), 1/sim.mu);
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
		if (Double.isNaN(LAN))
			LAN = 0;
		AgP = Math.acos(Calc.dot(n, e)/(n_mag * Ecc));
		if (e[2] < 0)
			AgP = 2*Math.PI - AgP;
		if (Double.isNaN(AgP))
			AgP = Math.atan2(e[1], e[0]);
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
		Vel = Calc.mag(v);
		Rad = Calc.mag(r);
		Alt = Rad - sim.planet.R;
		TrL = (TrA + AgP + LAN) % (2 * Math.PI);
		
		if (Ecc < 1)
		{
			double E = 2 * Math.atan2(tan(TrA/2), Math.sqrt((1+Ecc)/(1-Ecc)));
			T = Math.sqrt(4*Math.PI*Math.PI/sim.mu * SMa*SMa*SMa);
			MnA = E - Ecc * sin(E);
			if (MnA < 0)
				MnA += 2*Math.PI;
			PeT = T - MnA / (2*Math.PI/T);
			ApT = PeT - T/2;
			if (ApT < 0)
				ApT += T;
		}
		else
		{
			double E = Calc.acosh((Ecc + cos(TrA))/(1 + Ecc * cos(TrA)));
			if (TrA < 0)
				E *= -1;
			MnA = Ecc * Math.sinh(E) - E;
			PeT = -Math.sqrt((-SMa*SMa*SMa)/sim.mu) * MnA;
			ApT = Double.NaN;
			T = Double.NaN;
		}
	}
	
	public void update()
	{
		fuelMass = mass - emptyMass;
		if (fuelMass <= 0)
		{
			fuelMass = 0;
			thrust = 0;
			flowRate = 0;
			burnScheduled = false;
		}		
		else if (burnScheduled)
		{
			if (burnStart - sim.t < 0)
			{
				if (burnEnd - sim.t < 0)
				{
					burnScheduled = false;
					setThrust(engine, 0);
				}
				else if (thrust == 0)
				{
					setThrust(engine, 1);
				}
			}			
		}				
	}
	
	public void setThrust(int engine, double fraction)
	{
		double scalar = fraction * nEngines[engine];
		scalar = scalar < 0 ? 0 : scalar;
		thrust = scalar * thrusts[engine];
		flowRate = scalar * flowRates[engine];
	}
	
	public static String format(String label, double num)
	{
		return format(label,num,"\n");
	}
	
	public static String format(String label, double num, String endl)
	{
		if (Math.abs(num) < 1000)
			return label + small.format(num) + endl;
		else
			return label + large.format(num) + endl;
	}
	
	@Override
	public String toString()
	{
		String str = "---" + name + "---" + "\n";
		str += format("SMa ", SMa);
		str += format("SMi ", SMi);
		if (!sim.parent.alt)
		{
			str += format("PeD ", PeD);
			str += format("ApD ", ApD);
			str += format("Rad ", Rad);
		}
		else
		{
			str += format("PeA ", PeA/1000, "k\n");
			str += format("ApA ", ApA/1000, "k\n");
			str += format("Alt ", Alt/1000, "k\n");
		}
		str += format("Ecc ", Ecc);
		str += format("T   ", T);
		str += format("PeT ", PeT);
		str += format("ApT ", ApT);
		str += format("Vel ", Vel);
		
		str += format("Inc ", Math.toDegrees(Inc), "°\n");
		str += format("LAN ", Math.toDegrees(LAN), "°\n");
		str += format("AgP ", Math.toDegrees(AgP), "°\n");
		str += format("TrA ", Math.toDegrees(TrA), "°\n");
		str += format("TrL ", Math.toDegrees(TrL), "°\n");
		str += format("MnA ", Math.toDegrees(MnA), "°\n");
		
		return str;
	}
	
	public static double[] getThrustDirection(double[] r, double[] v, int direction)
	{
		if (direction == PROGRADE)
			return Calc.unit(v);
		else if (direction == RETROGRADE)
			return Calc.scale(Calc.unit(v), -1);
		else if (direction == NORMAL_PLUS)
			return Calc.unit(Calc.cross(r, v));
		else if (direction == NORMAL_MINUS)
			return Calc.scale(Calc.unit(Calc.cross(r, v)), -1);
		else
			return null;
	}
	
	public String name()
	{
		return name;
	}
}
