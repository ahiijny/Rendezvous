
public class Calc 
{			
	public static double[] cross(double[] a, double[] b)
	{
		double[] c = new double[3];
		c[0] = a[1]*b[2] - a[2]*b[1];
		c[1] = -(a[0]*b[2] - a[2]*b[0]);
		c[2] = a[0]*b[1] - a[1]*b[0];
		return c;
	}
	
	public static double[] scale(double[] a, double scalar)
	{
		double c[] = new double[a.length];
		for (int i = 0; i < a.length; i++)
			c[i] = a[i] * scalar;
		return c;
	}
	
	public static double[] add(double[] a, double[] b)
	{
		double c[] = new double[a.length];
		for (int i = 0; i < a.length; i++)
			c[i] = a[i] + b[i];
		return c;
	}
	
	public static double dot(double[] a, double[] b)
	{
		return a[0]*b[0] + a[1]*b[1] + a[2]*b[2];
	}
	
	public static double mag(double[] a)
	{
		double m = a[0]*a[0] + a[1]*a[1] + a[2]*a[2];
		m = Math.sqrt(m);
		return m;
	}
	
	public static double acosh(double a)
	{
		return Math.log(a + Math.sqrt(a+1) * Math.sqrt(a-1));
	}
	
	public static double[] unit(double[] a)
	{
		double mag = mag(a);
		if (mag != 0)
			return scale(a, 1/mag);
		else
			return copy(a);
	}
	
	public static double[] copy(double[] a)
	{
		double[] c = new double[a.length];
		for (int i = 0; i < a.length; i++)
			c[i] = a[i];
		return c;		
	}
	
	public static double[] empty(double[] a)
	{
		for (int i = 0; i < a.length; i++)
			a[i] = 0;
		return a;
	}
	
	/** Ref usually ISS at center of rotating level horizon
	 * reference frame.
	 */
	public static double[] unit_v_bar(Satellite ref)
	{
		return scale(ref.v, 1 / mag(ref.v));
	}
	
	public static double[] unit_r_bar(Satellite ref)
	{
		return scale(ref.r, -1 / mag(ref.r));
	}
	
	public static double[] v_r_bar(Satellite ref, Satellite shuttle)
	{
		double[] result = new double[2];
		double[] v_bar = unit_v_bar(ref);
		double[] r_bar = unit_r_bar(ref);		
		double[] dr = add(shuttle.r, scale(ref.r, -1));
		
		result[0] = dot(v_bar, dr);		
		result[1] = dot(r_bar, dr);		
		
		return result;
	}
}
