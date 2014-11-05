import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
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
	public Plotter plotter;

	String[] labels = {"rx","ry","rz","vx","vy","vz","SMa","e  ","Inc","LAN","AgP","TrA"};
	public JTextField[] states;
	public JTextField scale, step;
	public JComboBox<String> shipSel;
	public JButton altButton;

	public boolean alt = false;

	public int width, height;
	public Sim sim;

	private KeyboardFocusManager manager;
	private MyDispatcher keyDispatcher;

	public GraphicUI(String title, int width, int height)
	{
		super(title);

		this.width = width;
		this.height = height;
		sim = new Sim(this);
		sim.add(new Satellite("Shuttle", 6.57E6, 0.01, 0, 0, 0, 0));
		sim.add(new Satellite("ISS", 6.67E6, 0, 51.6, 122, 2.49, 0));

		setContentPane(createContent());	
		setJMenuBar(createMenuBar());

		manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		keyDispatcher = new MyDispatcher ();
		manager.addKeyEventDispatcher(keyDispatcher);

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
		JMenu game, help;
		JMenuItem button;
		int menuKeyMask = KeyEvent.CTRL_MASK;

		// Attempt to make MenuShortcutKeyMask valid for multiple platforms.
		try {
			menuKeyMask = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		} catch (Exception e) {			
		}

		// "File" Menu        
		game = new JMenu ("File");
		game.setMnemonic('g');

		button = new JMenuItem ("Exit"); // exit button
		button.setMnemonic('x');
		button.setAccelerator(KeyStroke.getKeyStroke (
				KeyEvent.VK_X, menuKeyMask));
		button.addActionListener (new MenuListener ());
		game.add(button);

		// "Help" Menu
		help = new JMenu ("Help");
		help.setMnemonic('h');	

		button = new JMenuItem ("About"); // about button
		button.setMnemonic('a');
		button.addActionListener (this.new MenuListener ());
		help.add(button);


		// Add All Menus        
		menuBar.add (game);
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

		// MFD panel

		Satellite sat;
		JPanel MFDs = new JPanel();
		dataPanes = new MFD[2];
		for (int i = 0; i < 2; i++)
		{
			sat = sim.get(i);
			sat.orbitalToState();
			sat.updateSecondaries();
			dataPanes[i] = new MFD(this, sat);			
			MFDs.add(dataPanes[i]);
		}
		dataPanes[0].setTextColor(Color.green);
		dataPanes[1].setTextColor(new Color(241,241,0));
		refreshMFDs();
		content.add(MFDs, BorderLayout.EAST);

		// Buttons panel

		JPanel buttons = new JPanel();

		JButton button = new JButton("Refresh");
		button.addActionListener(new MyListener());
		buttons.add(button);

		altButton = new JButton("Altitude");
		altButton.addActionListener(new MyListener());
		buttons.add(altButton);

		content.add(buttons,BorderLayout.NORTH);

		// Input panel

		JPanel inpane = new JPanel();
		inpane.setLayout(new BoxLayout(inpane, BoxLayout.Y_AXIS));

		shipSel = new JComboBox<String>();
		shipSel.setMaximumSize(new Dimension(300, 10));
		shipSel.addItem(sim.get(0).name());
		shipSel.addItem(sim.get(1).name());
		shipSel.addActionListener(new MyListener());
		
		inpane.add(new JLabel("Select ship:"));
		inpane.add(shipSel);
		inpane.add(new JLabel("    "));

		button = new JButton("State to Orbital");
		button.addActionListener(new MyListener());
		inpane.add(button);		

		states = new JTextField[12];				

		for (int i = 0; i < 12; i++)
		{				
			states[i] = new JTextField();
			states[i].setColumns(15);

			JPanel row = new JPanel();
			JLabel label = new JLabel(labels[i]);
			label.setFont(new Font("Courier New", Font.PLAIN, 12));
			row.add(label);
			row.add(states[i]);
			inpane.add(row);		
			
			if (i == 5)
				inpane.add(new JSeparator());
		}						
		button = new JButton("Orbital to State");
		button.addActionListener(new MyListener());
		inpane.add(button);

		content.add(inpane,BorderLayout.WEST);

		// Bottom Panel

		JPanel bottom = new JPanel(new BorderLayout());
		JPanel row3 = new JPanel();

		JLabel label = new JLabel("Downscale:");
		scale = new JTextField();
		scale.setColumns(6);
		button = new JButton("Set Scale");
		button.addActionListener(new MyListener());		
		row3.add(label);
		row3.add(scale);
		row3.add(button);

		label = new JLabel("Plot step:");
		step = new JTextField();
		step.setColumns(6);
		button = new JButton("Set Step");
		button.addActionListener(new MyListener());
		row3.add(label);
		row3.add(step);
		row3.add(button);		

		bottom.add(row3, BorderLayout.SOUTH);
		content.add(bottom,BorderLayout.SOUTH);

		// Middle Panel

		plotter = new Plotter(this);
		content.add(plotter,BorderLayout.CENTER);

		return content;
	}	

	public void refreshMFDs()
	{
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
	}

	public void refreshOtherFields()
	{
		scale.setText(Double.toString(plotter.downscale));
		step.setText(Double.toString(plotter.plotStep));
	}

	public void refresh()
	{
		refreshMFDs();					
		refreshInFields();		
		refreshOtherFields();
		repaint();
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

	public void setStep()
	{
		try
		{
			double newStep = Double.parseDouble(step.getText());
			plotter.plotStep = newStep;
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
			refreshInFields();
			refreshMFDs();
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
			refreshInFields();
			refreshMFDs();
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

	private class MyListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			Object parent = e.getSource();

			if (parent instanceof JButton)
			{
				JButton button = (JButton)parent;				
				if (button.getText() == "Refresh")
				{					
					refresh();
				}
				else if (button.getText() == "Set Scale")
				{
					setScale();
				}
				else if (button.getText() == "Set Step")
				{
					setStep();
				}
				else if (button.getText() == "State to Orbital")
				{
					stateToOrbital();			
				}
				else if (button.getText() == "Orbital to State")
				{
					orbitalToState();		
				}
				else if (button.getText() == "Altitude" || button.getText() == "Radius")
				{
					toggleAlt();
				}
			}
			repaint();
		}

	}
	public void close ()
	{
		dispose ();	
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
					String message = "Version: 2014.11.05\n";					
					message += "Program by: Jiayin Huang\n";
					message += "Work in progress.\n";
					JOptionPane.showMessageDialog(GraphicUI.this, message, "About", JOptionPane.PLAIN_MESSAGE);			
				}
			}
		}		
	}

	private class MyDispatcher implements KeyEventDispatcher 
	{
		@Override
		public boolean dispatchKeyEvent(KeyEvent e) 
		{			
			boolean consumed = false;

			if (e.getID() == KeyEvent.KEY_PRESSED) 
			{
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_TAB)
				{
					int n = shipSel.getSelectedIndex();
					if (n < shipSel.getItemCount() - 2)
						shipSel.setSelectedIndex(n+1);
					else
						shipSel.setSelectedIndex(0);
				}
				else if (key == KeyEvent.VK_F5)
				{
					refresh();
				}	
				else if (key == KeyEvent.VK_A)
				{
					toggleAlt();
				}	
			} 
			else if (e.getID() == KeyEvent.KEY_RELEASED) 
			{
			}  
			return consumed;
		}
	}
}
