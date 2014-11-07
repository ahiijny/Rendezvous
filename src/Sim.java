import java.util.ArrayList;

/** http://www.myphysicslab.com/runge_kutta.html
 * http://en.wikipedia.org/wiki/Runge%E2%80%93Kutta_methods
 * http://en.wikipedia.org/wiki/List_of_Runge%E2%80%93Kutta_methods
 */
public class Sim implements Runnable
{
	public static final double G = 6.67384E-11;
	
	public static final int R_X = 0;
	public static final int R_Y = 1;
	public static final int R_Z = 2;	
	public static final int V_X = 3;
	public static final int V_Y = 4;
	public static final int V_Z = 5;
	public static final int MASS = 6;	
	public static final int THRUST = 7;
	public static final int FLOW = 8;
	public static final int DIRECTION = 9;
	
	public static final int RK1 = 0;
	public static final int RK2 = 1;
	public static final int RK4 = 2;
	
	public static final double[][][] butcherTableau = 
	{
		{
			{0, 0},
			{0, 1}
		}
		,
		{
			{0,     0,     0},
			{1/2.0, 1/2.0, 0},
			{0,     0,     1}
		}
		,
		{
			{0,     0,     0,     0,     0},
			{1/2.0, 1/2.0, 0,     0,     0},
			{1/2.0, 0,     1/2.0, 0,     0},
			{1,     0,     0,     1,     0},
			{0,     1/6.0, 1/3.0, 1/3.0, 1/6.0}
		}
	}
	;	
	
	public GraphicUI parent;
	public double mu = 0;
	public double t = 0;
	public double dt = 0.01;
	public double warp = 1;
	public double refreshStep = 1;
	public double MFDt = 0;
	public int integrationMethod = RK4;
	public ArrayList<Satellite> satellites;
	public Planet planet;
	
	public Thread thread;
	private long nextEvent;
	private long nextRefresh;	
	public boolean running = false;
	
	public double rbar = 0, vbar = 0; // For the shuttle wrt the ISS

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
						double[] vars = getVars(sat);
						vars = rkn(t, dt, vars);						
						updateVars(sat, vars);
						sat.update();
					}	
					double[] pair = Calc.v_r_bar(satellites.get(1), satellites.get(0));
					vbar = pair[0];
					rbar = pair[1];
					parent.logger.log();
					t += dt;
				}
			}
						
			// Update refresh
			
			long remaining = nextRefresh - System.currentTimeMillis();
			
			if (remaining <= 0)
			{		
				refresh();
				nextRefresh += (long)(1000 * Math.min(Math.max(refreshStep/warp, 1/60), 1));
				MFDt += refreshStep * warp;
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
	
	public void refresh()
	{
		for (int i = 0; i < satellites.size(); i++)
		{
			satellites.get(i).stateToOrbital();
			satellites.get(i).updateSecondaries();
		}
		parent.refreshInFields();
		parent.refreshMFDs();
		parent.repaint();		
	}
	
	public double[] getVars(Satellite sat)
	{
		double[] vars = new double[10];
		vars[R_X] = sat.r[0];
		vars[R_Y] = sat.r[1];
		vars[R_Z] = sat.r[2];
		vars[V_X] = sat.v[0];
		vars[V_Y] = sat.v[1];
		vars[V_Z] = sat.v[2];
		vars[MASS] = sat.mass;
		vars[THRUST] = sat.thrust;
		vars[FLOW] = sat.flowRate;
		vars[DIRECTION] = sat.direction;
		return vars;
	}
	
	public void updateVars(Satellite sat, double[] vars)
	{
		sat.r[0] = vars[R_X];
		sat.r[1] = vars[R_Y];
		sat.r[2] = vars[R_Z];
		sat.v[0] = vars[V_X];
		sat.v[1] = vars[V_Y];
		sat.v[2] = vars[V_Z];
		sat.mass = vars[MASS];
	}
	
	public double[] rk1(double t, double dt, double[] vars)
	{
		double[] diff = diff_vars(t, vars);
		for (int i = 0; i < vars.length; i++)
			vars[i] += dt * diff[i];
		return vars;
	}
	
	/** http://en.wikipedia.org/wiki/List_of_Runge%E2%80%93Kutta_methods
	 * Uses the Runge-Kutta method of the nth order, with n set
	 * by the <code>integrationMethod</code> variable.
	 */
	public double[] rkn(double t, double dt, double[] vars)
	{
		int s = butcherTableau[integrationMethod].length - 1;
		double[] temp = new double[vars.length];		
		double[][] K = new double[s][vars.length];
		
		// Determine K values
		
		for (int i = 0; i < s; i++)
		{
			double c = butcherTableau[integrationMethod][i][0];
			double t_n = t + c*dt;
			temp = Calc.copy(vars);
			
			for (int j = 1; j < i+1; j++)
			{
				double a = butcherTableau[integrationMethod][i][j];
				if (a != 0)				
					temp = Calc.add(temp, Calc.scale(K[j-1], dt*a));				
			}
			K[i] = diff_vars(t_n, temp);				
		}

		// Sum all of the K's accordingly
		
		double[] diff = new double[vars.length];
		diff = Calc.empty(diff);
		
		for (int i = 0; i < K.length; i++)
		{
			double b = butcherTableau[integrationMethod][s][i+1];
			diff = Calc.add(diff, Calc.scale(K[i],b));
		}
		
		// Scale by dt
		
		diff = Calc.scale(diff, dt);
		
		// Apply deltas to the input vars
		
		vars = Calc.add(vars, diff);
		return vars;
	}
	
	public double[] rk2(double t, double dt, double[] vars)
	{
		return vars;
	}
	
	public double[] rk4(double t, double dt, double[] vars)
	{
		return vars;
	}
	
	public double[] rk5(double t, double dt, double[] vars)
	{
		return vars;
	}
	
	public double[] rk6(double t, double dt, double[] vars)
	{
		return vars;
	}
	
	public double[] rk7(double t, double dt, double[] vars)
	{
		return vars;
	}
	
	public double[] rk8(double t, double dt, double[] vars)
	{
		return vars;
	}
	
	public double[] diff_vars(double t, double[] vars)
	{
		double[] diff = new double[vars.length];
		double[] dr = diff_r(t, vars);
		double[] dv = diff_v(t, vars);
		double dm = diff_mass(t, vars);
		
		diff[R_X] = dr[0];
		diff[R_Y] = dr[1];
		diff[R_Z] = dr[2];		
		diff[V_X] = dv[0];
		diff[V_Y] = dv[1];
		diff[V_Z] = dv[2];		
		diff[MASS] = dm;
		diff[THRUST] = 0;
		diff[FLOW] = 0;
		diff[DIRECTION] = 0;
		
		return diff;
	}
	
	public double[] diff_r(double t, double[] vars)
	{
		double[] v = {vars[V_X],vars[V_Y],vars[V_Z]};
		return v;
	}
	
	public double[] diff_v(double t, double[] vars)
	{
		double[] a = new double[3];
		double[] r = {vars[R_X],vars[R_Y],vars[R_Z]};
		double[] v = diff_r(t, vars);
		double[] u = Satellite.getThrustDirection(r, v, (int)vars[DIRECTION]);
		double thrust = vars[THRUST];
		double mass = vars[MASS];
		double r_mag = Calc.mag(r);
		
		double[] a_g = Calc.scale(r, -mu /(r_mag*r_mag*r_mag));
		double[] a_t = Calc.scale(u, thrust/mass);
		a = Calc.add(a_g, a_t);
		return a;
	}
	
	public double diff_mass(double t, double[] vars)
	{
		return -vars[FLOW];
	}
	
	public double diff_thrust(double t, double[] vars)
	{
		return 0;
	}
	
	public double diff_flow(double t, double[] vars)
	{
		return 0;
	}
	
	public double diff_direction(double t, double[] vars)
	{
		return 0;
	}
}
