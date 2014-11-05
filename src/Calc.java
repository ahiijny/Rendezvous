
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
		double c[] = new double[3];
		c[0] = a[0] * scalar;
		c[1] = a[1] * scalar;
		c[2] = a[2] * scalar;
		return a;
	}
	
	public static double[] add(double[] a, double[] b)
	{
		double c[] = new double[3];
		c[0] = a[0] + b[0];
		c[1] = a[1] + b[1];
		c[2] = a[2] + b[2];
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

}
