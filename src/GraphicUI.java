import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class GraphicUI extends JFrame 
{
	public MFD[] dataPanes;
	public TimeDisplay timer;
	public Plotter plotter;

	String[] elemLabels = {"rx","ry","rz","vx","vy","vz","SMa","e  ","Inc","LAN","AgP","TrA"};
	String[] propagatorLabels = {"Runge-Kutta, 1st order (RK1)", 
								"Runge-Kutta, 2nd order (RK2)",								
								"Runge-Kutta, 4th order (RK4)"};
	public JTextField[] states;
	public JTextField[] shipSpecs;
	public JTextField scale, step;
	public JComboBox<String> shipSel;
	public JComboBox<String> rkSel;
	public JLabel shipLabel;
	public JButton altButton, playButton;
	public JButton scaleBut, renderBut, simStepBut, refreshBut, warpBut;
	public JTextField simStep, refreshStep, warp;

	public boolean alt = false;

	public int width, height;
	public Sim sim;

	public GraphicUI(String title, int width, int height)
	{
		super(title);

		this.width = width;
		this.height = height;
		sim = new Sim(this);
		timer = new TimeDisplay(sim);
		sim.add(new Satellite("Shuttle", 6.57E6, 0.01, 0, 0, 0, 0));
		sim.add(new Satellite("ISS", 6.67E6, 0, 51.6, 122, 2.49, 0));

		setContentPane(createContent());	
		setJMenuBar(createMenuBar());

		refreshMFDs();
		refreshInFields();
		refreshOtherFields();

		setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);	
		setSize(width, height);	
		setVisible(true);	
	}

	/** Creates the menu bar for the GUI.
	 * 
	 * @return the JMenuBar with everything added to it
	 */
	private JMenuBar createMenuBar()
	{		
		JMenuBar menuBar = new JMenuBar();
		JMenu file, simulation, help;
		JMenuItem button;
		int menuKeyMask = InputEvent.CTRL_MASK;

		// Attempt to make MenuShortcutKeyMask valid for multiple platforms.
		try {
			menuKeyMask = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		} catch (Exception e) {			
		}

		// "File" Menu        
		file = new JMenu ("File");
		file.setMnemonic('g');

		button = new JMenuItem ("Exit"); // exit button
		button.setMnemonic('x');
		button.setAccelerator(KeyStroke.getKeyStroke (
				KeyEvent.VK_E, menuKeyMask));
		button.addActionListener (new MenuListener ());
		file.add(button);
		
		// "Simulation" Menu
		simulation = new JMenu ("Simulation");
		simulation.setMnemonic('s');		
		
		button = new JMenuItem ("Resume/Pause");
		button.setMnemonic('p');
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		button.addActionListener (new MenuListener());
		simulation.add(button);
		
		button = new JMenuItem ("Sim step default");
		button.addActionListener (new MenuListener());
		simulation.add(button);
		
		button = new JMenuItem ("Refresh step default");
		button.addActionListener (new MenuListener());
		simulation.add(button);
		
		button = new JMenuItem ("Warp++");
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, menuKeyMask));
		button.addActionListener (new MenuListener());
		simulation.add(button);
		
		button = new JMenuItem ("Warp--");
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, menuKeyMask));
		button.addActionListener (new MenuListener());
		simulation.add(button);
		
		button = new JMenuItem ("Warp default");
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, menuKeyMask));
		button.addActionListener (new MenuListener());
		simulation.add(button);
						
		
		simulation.add(new JSeparator());
				
		button = new JMenuItem ("State to Orbital");
		button.setMnemonic('s');
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		button.addActionListener (new MenuListener ());
		simulation.add(button);
		
		button = new JMenuItem ("Orbital to State");
		button.setMnemonic('o');
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
		button.addActionListener (new MenuListener ());
		simulation.add(button);
		
		button = new JMenuItem ("Switch Ship Focus");
		button.setMnemonic('w');
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		button.addActionListener (new MenuListener ());
		simulation.add(button);
		
		button = new JMenuItem ("Toggle Altitude/Radius");
		button.setMnemonic('a');
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
		button.addActionListener (new MenuListener ());
		simulation.add(button);
		
		button = new JMenuItem ("Refresh");
		button.setMnemonic('r');
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		button.addActionListener (new MenuListener ());
		simulation.add(button);				

		// "Help" Menu
		help = new JMenu ("Help");
		help.setMnemonic('h');	

		button = new JMenuItem ("About"); // about button
		button.setMnemonic('a');
		button.addActionListener (new MenuListener ());
		help.add(button);


		// Add All Menus        
		menuBar.add (file);
		menuBar.add (simulation);
		menuBar.add (help);

		// Return        
		return menuBar;
	}

	/** Creates the content to go inside the JFrame. 
	 * 
	 * @return the JPanel with everything added to it
	 */
	private JPanel createContent ()
	{
		JPanel content = new JPanel (new BorderLayout());
				
		content.add(createMiddlePanel(),BorderLayout.CENTER);
		content.add(createLeftPanel(),BorderLayout.WEST);
		content.add(createRightPanel(),BorderLayout.EAST);
		content.add(createTopPanel(),BorderLayout.NORTH);
		content.add(createBottomPanel(),BorderLayout.SOUTH);

		return content;
	}	
	
	private Plotter createMiddlePanel()
	{
		plotter = new Plotter(this);
		return plotter;
	}
	
	private JPanel createRightPanel()
	{
		// MFD panel

		Satellite sat;
		JPanel control = new JPanel(new BorderLayout());
		JPanel MFDs = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(1,2,1,2);
		c.gridy = 0;
		
		dataPanes = new MFD[2];
		for (int i = 0; i < 2; i++)
		{
			c.gridx = i;
			sat = sim.get(i);
			sat.orbitalToState();
			sat.updateSecondaries();
			dataPanes[i] = new MFD(this, sat);
			MFDs.add(dataPanes[i], c);
		}
		dataPanes[0].setTextColor(Color.green);
		dataPanes[1].setTextColor(new Color(241,241,0));
		c.ipadx = 0;
		refreshMFDs();
		control.add(MFDs, BorderLayout.NORTH);
		
		// Spacecraft panel
		
		shipSpecs = new JTextField[9];
		
		JPanel crafts = new JPanel(new BorderLayout());
		JPanel specs = new JPanel(new GridBagLayout());
				
		c.gridx = 0;
		c.gridy = 0;
		
		c.gridwidth = 2;		
		c.anchor = GridBagConstraints.CENTER;
		shipLabel = new JLabel("Shuttle");
		shipLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.gridwidth = 1;
		specs.add(shipLabel, c);		
		c.gridy += 2;
		
		JLabel label = new JLabel(" Empty mass: ");
		c.gridx = 0;
		shipSpecs[0] = new JTextField(15);
		specs.add(label, c);
		c.gridx = 1;
		specs.add(shipSpecs[0], c);
		c.gridy++;
		
		label = new JLabel(" Fuel mass: ");
		shipSpecs[1] = new JTextField(15);
		c.gridx = 0;
		specs.add(label, c);
		c.gridx = 1;
		specs.add(shipSpecs[1], c);
		c.gridy++;
		
		label = new JLabel(" SHIP MASS: ");
		shipSpecs[2] = new JTextField(15);
		c.gridx = 0;
		specs.add(label, c);
		c.gridx = 1;
		specs.add(shipSpecs[2], c);
		c.gridy++;
		
		c.gridwidth = 2;
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;	
		specs.add(new JSeparator(), c);
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.gridy++;
		
		label = new JLabel("OMS Engines:");
		c.gridx = 0;
		specs.add(label, c);
		c.gridy++;
		
		int offset = 0;
		
		for (int i = 0; i < 2; i++)
		{		
			label = new JLabel(" Number: ");
			shipSpecs[offset+3] = new JTextField(15);
			c.gridx = 0;
			specs.add(label, c);
			c.gridx = 1;
			specs.add(shipSpecs[offset+3], c);
			c.gridy++;
			
			label = new JLabel(" Flow Rate (kg/s): ");
			shipSpecs[offset+4] = new JTextField(15);
			c.gridx = 0;
			specs.add(label, c);
			c.gridx = 1;
			specs.add(shipSpecs[offset+4], c);
			c.gridy++;
			
			label = new JLabel(" Thrust per Engine (kN): ");
			shipSpecs[offset+5] = new JTextField(15);
			c.gridx = 0;
			specs.add(label, c);
			c.gridx = 1;
			specs.add(shipSpecs[offset+5], c);
			c.gridy++;
			
			if (i == 0)
			{
				c.gridwidth = 2;
				c.gridx = 0;
				c.fill = GridBagConstraints.HORIZONTAL;	
				specs.add(new JSeparator(), c);
				c.fill = GridBagConstraints.NONE;
				c.gridwidth = 1;
				c.gridy++;
				
				label = new JLabel(" RCS +X Thrusters:");
				c.gridx = 0;
				specs.add(label, c);
				c.gridy++;
				offset += 3;
			}
		}
		crafts.add(specs, BorderLayout.NORTH);
		control.add(crafts, BorderLayout.CENTER);
		return control;
	}
	
	private JPanel createLeftPanel()
	{
		JPanel leftpane = new JPanel(new BorderLayout());
		JPanel inpane = new JPanel(new GridBagLayout());
		JButton button; 
		JLabel label;
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2,4,2,4);
		
		// Input data
		
		shipSel = new JComboBox<String>();
		shipSel.setPreferredSize(new Dimension(200, 20));
		shipSel.addItem(sim.get(0).name());
		shipSel.addItem(sim.get(1).name());
		shipSel.addActionListener(new MyListener());
				
		c.gridx = 0;
		c.gridy = 0;
		inpane.add(new JLabel("Select ship:"), c);
				
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		inpane.add(shipSel, c);

		c.gridx = 0;
		c.gridy = 2;		
		button = new JButton("State to Orbital");
		button.addActionListener(new MyListener());
		inpane.add(button, c);
		
		c.gridwidth = 1;
		c.gridy = 3;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		states = new JTextField[12];				

		for (int i = 0; i < 12; i++)
		{				
			states[i] = new JTextField();
			states[i].setColumns(15);
			
			label = new JLabel(elemLabels[i]);
			label.setFont(new Font("Courier New", Font.PLAIN, 12));
			
			c.gridx = 0;
			inpane.add(label, c);
			c.gridx = 1;
			inpane.add(states[i], c);
			c.gridy++;
			
			if (i == 5)
			{
				c.gridwidth = 2;
				c.gridx = 0;
				JSeparator sep = new JSeparator();
				//sep.setPreferredSize(new Dimension(100, 20));
				c.fill = GridBagConstraints.HORIZONTAL;
				inpane.add(sep, c);
				c.fill = GridBagConstraints.NONE;
				c.gridwidth = 1;
				c.gridy++;
			}
		}						
		button = new JButton("Orbital to State");
		button.addActionListener(new MyListener());
		c.anchor = GridBagConstraints.CENTER;
		c.gridwidth = 2;
		c.gridx = 0;
		inpane.add(button, c);
		leftpane.add(inpane, BorderLayout.NORTH);
		
		// Burn data
		
		JPanel burnpane = new JPanel(new GridBagLayout());
		label = new JLabel("Burns");
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		burnpane.add(label, c);
		
		leftpane.add(burnpane, BorderLayout.SOUTH);
		
		return leftpane;
	}
	
	private JPanel createTopPanel()
	{
		JPanel top = new JPanel(new BorderLayout());
		
		// Sim Buttons Panel
		
		JPanel simButtons = new JPanel();
		playButton = new JButton("Resume");
		playButton.addActionListener(new MyListener());
		simButtons.add(playButton);
				
		// Buttons panel		
		
		JPanel buttons = new JPanel();
		JButton button = new JButton("Refresh");
		button.addActionListener(new MyListener());
		buttons.add(button);

		altButton = new JButton("Altitude");
		altButton.addActionListener(new MyListener());
		buttons.add(altButton);

		top.add(timer, BorderLayout.NORTH);
		top.add(simButtons, BorderLayout.CENTER);
		top.add(buttons,BorderLayout.SOUTH);
		
		return top;
	}
	
	private JPanel createBottomPanel()
	{
		JPanel bottom = new JPanel(new BorderLayout());					
		
		// Simulation
		
		JPanel row2 = new JPanel();
		
		JLabel label = new JLabel("Propagator: ");		
		
		rkSel = new JComboBox<String>();
		rkSel.setPreferredSize(new Dimension(170, 20));
		for (int i = 0; i < propagatorLabels.length; i++)			
			rkSel.addItem(propagatorLabels[i]);		
		rkSel.setSelectedIndex(sim.RK4);
		rkSel.addActionListener(new MyListener());
		
		row2.add(label);
		row2.add(rkSel);
		
		label = new JLabel("Warp (x):");
		warp = new JTextField();
		warp.setColumns(5);		
		warpBut = new JButton("Set");
		warpBut.addActionListener(new MyListener());
		
		row2.add(label);
		row2.add(warp);
		row2.add(warpBut);
		
		label = new JLabel("Sim step (s):");		
		simStep = new JTextField();
		simStep.setColumns(5);		
		simStepBut = new JButton("Set");
		simStepBut.addActionListener(new MyListener());
		
		row2.add(label);
		row2.add(simStep);
		row2.add(simStepBut);
		
		label = new JLabel("Refresh step (s):");		
		refreshStep = new JTextField();
		refreshStep.setColumns(5);		
		refreshBut = new JButton("Set");
		refreshBut.addActionListener(new MyListener());
		
		row2.add(label);
		row2.add(refreshStep);
		row2.add(refreshBut);
		
		// Display
		label = new JLabel("Downscale:");
		scale = new JTextField();
		scale.setColumns(6);
		scaleBut = new JButton("Set");
		scaleBut.addActionListener(new MyListener());		
		row2.add(label);
		row2.add(scale);
		row2.add(scaleBut);

		label = new JLabel("Plot step (°):");
		step = new JTextField();
		step.setColumns(5);
		renderBut = new JButton("Set");
		renderBut.addActionListener(new MyListener());
		row2.add(label);
		row2.add(step);
		row2.add(renderBut);
		 
		bottom.add(row2, BorderLayout.CENTER);
		
		return bottom;
	}

	public void refreshMFDs()
	{
		timer.update();
		for (int i = 0; i < 2; i++)
			dataPanes[i].refresh();
	}

	public void refreshInFields()
	{
		Satellite sat = sim.get(shipSel.getSelectedIndex());
		for (int i = 0; i < 3; i++)
			states[i].setText(Double.toString(sat.r[i]));
		for (int i = 0; i < 3; i++)
			states[3+i].setText(Double.toString(sat.v[i]));
		states[6].setText(Double.toString(sat.SMa));
		states[7].setText(Double.toString(sat.Ecc));
		states[8].setText(Double.toString(Math.toDegrees(sat.Inc)));
		states[9].setText(Double.toString(Math.toDegrees(sat.LAN)));
		states[10].setText(Double.toString(Math.toDegrees(sat.AgP)));
		states[11].setText(Double.toString(Math.toDegrees(sat.TrA)));
		shipLabel.setText(sat.name());
		shipSpecs[0].setText(Double.toString(sat.emptyMass));
		shipSpecs[1].setText(Double.toString(sat.fuelMass));
		shipSpecs[2].setText(Double.toString(sat.emptyMass + sat.emptyMass));
		shipSpecs[3].setText(Integer.toString(sat.nEngines[0]));
		shipSpecs[4].setText(Double.toString(sat.flowRates[0]));
		shipSpecs[5].setText(Double.toString(sat.thrusts[0]));
		shipSpecs[6].setText(Integer.toString(sat.nEngines[1]));
		shipSpecs[7].setText(Double.toString(sat.flowRates[1]));
		shipSpecs[8].setText(Double.toString(sat.thrusts[1]));
	}

	public void refreshOtherFields()
	{
		scale.setText(Double.toString(plotter.downscale));
		step.setText(Double.toString(plotter.plotStep));
		warp.setText(Double.toString(sim.warp));
		simStep.setText(Double.toString(sim.dt));
		refreshStep.setText(Double.toString(sim.refreshStep));
	}

	public void refresh()
	{
		refreshOtherFields();
		sim.refresh();
	}

	public void setScale()
	{
		try
		{
			double newScale = Double.parseDouble(scale.getText());
			plotter.downscale = newScale;
		}
		catch(Exception ex)
		{
			refreshOtherFields();
		}
	}

	public void setPlotStep()
	{
		try
		{
			double newStep = Double.parseDouble(step.getText());
			plotter.plotStep = Math.toRadians(newStep);
		}
		catch(Exception ex)
		{
			refreshOtherFields();
		}
	}
	
	public void setSimStep()
	{
		try
		{
			double newDt = Double.parseDouble(simStep.getText());
			sim.dt = newDt;
		}
		catch(Exception ex)
		{
			refreshOtherFields();
		}
	}
	
	public void setRefreshStep()
	{
		try
		{
			double newStep = Double.parseDouble(refreshStep.getText());
			sim.refreshStep = newStep;
		}
		catch(Exception ex)
		{
			refreshOtherFields();
		}
	}
	
	public void setWarp()
	{
		try
		{
			double newWarp = Double.parseDouble(warp.getText());
			sim.warp = newWarp;
		}
		catch(Exception ex)
		{
			refreshOtherFields();
		}
	}

	public void stateToOrbital()
	{
		Satellite sat = sim.get(shipSel.getSelectedIndex());
		double rx, ry, rz, vx, vy, vz;
		try
		{
			rx = Double.parseDouble(states[0].getText());
			ry = Double.parseDouble(states[1].getText());
			rz = Double.parseDouble(states[2].getText());
			vx = Double.parseDouble(states[3].getText());
			vy = Double.parseDouble(states[4].getText());
			vz = Double.parseDouble(states[5].getText());

			sat.r[0] = rx;
			sat.r[1] = ry;
			sat.r[2] = rz;
			sat.v[0] = vx;
			sat.v[1] = vy;
			sat.v[2] = vz;
			sat.stateToOrbital();
			sat.updateSecondaries();
			refresh();
		}
		catch (Exception nfe)
		{
			refreshInFields();
		}		
	}

	public void orbitalToState()
	{
		Satellite sat = sim.get(shipSel.getSelectedIndex());
		double a, E, i, O, w, v;
		try
		{
			a = Double.parseDouble(states[6].getText());
			E = Double.parseDouble(states[7].getText());
			i = Math.toRadians(Double.parseDouble(states[8].getText()));
			O = Math.toRadians(Double.parseDouble(states[9].getText()));
			w = Math.toRadians(Double.parseDouble(states[10].getText()));
			v = Math.toRadians(Double.parseDouble(states[11].getText()));

			sat.SMa = a;
			sat.Ecc = E;
			sat.Inc = i;
			sat.LAN = O;
			sat.AgP = w;
			sat.TrA = v;
			sat.orbitalToState();
			sat.updateSecondaries();
			refresh();
		}
		catch (Exception nfe)
		{
			refreshInFields();
		}			
	}

	public void toggleAlt()
	{
		if (altButton.getText() == "Altitude")
		{
			alt = true;
			altButton.setText("Radius");			
		}
		else
		{		
			alt = false;
			altButton.setText("Altitude");
		}
		refreshMFDs();
	}
	
	public void toggleRunning()
	{
		if (playButton.getText().equals("Resume"))
		{
			sim.start();
			playButton.setText("Pause");
		}
		else
		{
			sim.stop();
			playButton.setText("Resume");
		}
	}	
	
	public void setPropagator(int rk)
	{
		sim.stop();
		while (sim.thread.isAlive())
		{
			try
			{
				Thread.sleep(1);
			}
			catch (InterruptedException e)
			{				
			}
		}
		sim.integrationMethod = rk;
		sim.start();
	}
	
	private class MyListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			Object parent = e.getSource();

			if (parent instanceof JButton)
			{
				JButton button = (JButton)parent;			
				String text = button.getText();
				if (text.equals("Refresh"))
				{		
					refresh();
				}
				else if (text.equals("Set"))
				{
					if (button.equals(scaleBut))
						setScale();
					else if (button.equals(renderBut))
						setPlotStep();
					else if (button.equals(warpBut))
						setWarp();
					else if (button.equals(simStepBut))
						setSimStep();
					else if (button.equals(refreshBut))
						setRefreshStep();
				}
				else if (text.equals("State to Orbital"))
				{
					stateToOrbital();			
				}
				else if (text.equals("Orbital to State"))
				{
					orbitalToState();		
				}
				else if (text.equals("Altitude") || text.equals("Radius"))
				{
					toggleAlt();
				}
				else if (text.equals("Resume") || text.equals("Pause"))
				{
					toggleRunning();
				}
			}
			else if (parent instanceof JComboBox)
			{
				JComboBox<String> cb = (JComboBox<String>)parent;
				if (cb.equals(rkSel))
					setPropagator(rkSel.getSelectedIndex());
				refresh();				
			}	
			repaint();
		}
	}
	public void close ()
	{
		dispose ();	
	}
	
	public void switchFocus()
	{
		int n = shipSel.getSelectedIndex();
		if (n < shipSel.getItemCount() - 1)
			shipSel.setSelectedIndex(n+1);
		else
			shipSel.setSelectedIndex(0);
		refresh();
	}

	/** Listener for menu buttons	 
	 */
	private class MenuListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			Object parent = e.getSource();

			if (parent instanceof JMenuItem)
			{
				JMenuItem button = (JMenuItem) parent;
				String name = button.getText ();
				if (name.equals("Exit"))
				{					
					close();
				}
				else if (name.equals("About"))
				{
					String message = "Version: 2014.11.06\n";					
					message += "Program by: Jiayin Huang\n";
					message += "Orbits.\n";
					JOptionPane.showMessageDialog(GraphicUI.this, message, "About", JOptionPane.PLAIN_MESSAGE);			
				}
				else if (name.equals("Switch Ship Focus"))
				{
					switchFocus();
				}
				else if (name.equals("Refresh"))
				{
					refresh();
				}	
				else if (name.equals("Toggle Altitude/Radius"))
				{
					toggleAlt();
				}	
				else if (name.equals("State to Orbital"))
				{
					stateToOrbital();
				}	
				else if (name.equals("Orbital to State"))
				{
					orbitalToState();
				}	
				else if (name.equals("Resume/Pause"))
				{
					toggleRunning();
				}	
				else if (name.equals("Warp++"))
				{
					sim.warp *= 2;
					refreshOtherFields();
				}
				else if (name.equals("Warp--"))
				{
					sim.warp /= 2;
					refreshOtherFields();
				}
				else if (name.equals("Warp default"))
				{
					sim.warp = 1;
					refreshOtherFields();
				}
				else if (name.equals("Sim step default"))
				{
					sim.dt = 0.01;
					refreshOtherFields();
				}
				else if (name.equals("Refesh step default"))
				{
					sim.refreshStep = 1;
					refreshOtherFields();
				}
			}
		}		
	}
}
