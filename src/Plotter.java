import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

public class Plotter extends JPanel 
{
	public static final double fullTurn = 2*Math.PI;
	public GraphicUI parent;	
	public Color surface = Color.gray;
	public Color shuttle = new Color(0, 200, 0);
	public Color iss = new Color(180,180,0);
	public double downscale = 35000;
	public double plotStep = 0.001;
	public Dimension size = new Dimension(450,550);
	public Point mid = new Point(0,0);
	
	public Plotter(GraphicUI parent) 
	{
		this.parent = parent;
		addComponentListener(new ResizeListener());
	}

	@Override
	public void paintComponent(Graphics g)
	{		
		Graphics2D g2d = (Graphics2D)g;
		AffineTransform at = AffineTransform.getScaleInstance(1, -1);
		at.preConcatenate(AffineTransform.getTranslateInstance(0, size.height));
		g2d.transform(at);
		
		g2d.setColor(Color.black);
		g2d.fillRect(0, 0, size.width, size.height);
		plotSurface(g2d);
		plotOrbit(g2d, parent.sim.get(0),shuttle);
		plotOrbit(g2d, parent.sim.get(1),iss);
	}
	
	public void plotSurface(Graphics2D g)
	{				
		double r = parent.sim.planet.R / downscale;
		g.setColor(surface);
		int x, y;
		
		for (double nu = 0; nu < fullTurn; nu += plotStep)
		{
			x = (int)(mid.x + r * Math.cos(nu));
			y = (int)(mid.y + r * Math.sin(nu));
			g.fillRect(x, y, 1, 1);		
		}
	}
	
	public void plotOrbit(Graphics2D g, Satellite sat, Color col)
	{
		g.setColor(col);
		double r;
		int x, y;
		
		for (double nu = 0; nu < fullTurn; nu += plotStep)
		{			
			r = sat.dist(nu);			
			x = (int)(mid.x + (r * sat.rx(nu)/ downscale));
			y = (int)(mid.y + (r * sat.ry(nu)/ downscale));
			g.fillRect(x, y, 1, 1);		
		}
		plotApses(g, sat);
		plotNodes(g, sat);
		plotLocation(g, sat);
	}
	
	public void plotApses(Graphics2D g, Satellite sat)
	{
		double nu = 0;
		double r = sat.dist(nu);			
		int x = (int)(mid.x - 3 + (r * sat.rx(nu)/ downscale));
		int y = (int)(mid.y - 3 + (r * sat.ry(nu)/ downscale));
		g.fillOval(x, y, 7, 7);	
		
		nu = Math.PI;
		r = sat.dist(nu);
		x = (int)(mid.x - 3 + (r * sat.rx(nu)/ downscale));
		y = (int)(mid.y - 3 + (r * sat.ry(nu)/ downscale));
		g.drawOval(x, y, 7, 7);	
	}
	
	public void plotNodes(Graphics2D g, Satellite sat)
	{
		double nu = - sat.AgP;
		double r = sat.dist(nu);			
		int x = (int)(mid.x - 3 + (r * sat.rx(nu)/ downscale));
		int y = (int)(mid.y - 3 + (r * sat.ry(nu)/ downscale));
		g.fillRect(x, y, 7, 7);	
		
		nu += Math.PI;
		r = sat.dist(nu);
		x = (int)(mid.x - 3 + (r * sat.rx(nu)/ downscale));
		y = (int)(mid.y - 3 + (r * sat.ry(nu)/ downscale));
		g.drawRect(x, y, 7, 7);	
	}
	
	public void plotLocation(Graphics2D g, Satellite sat)
	{
		int x = (int)(mid.x + sat.r[0] / downscale);
		int y = (int)(mid.y + sat.r[1] / downscale);
		
		g.drawLine(mid.x, mid.y, x, y);
	}
	
	private class ResizeListener extends ComponentAdapter
	{
		 public void componentResized(ComponentEvent e) 
		 {
             getSize(size);
             mid.x = size.width/2;
             mid.y = size.height/2;
         }
	}

}
