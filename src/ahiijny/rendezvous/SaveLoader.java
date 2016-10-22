package ahiijny.rendezvous;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;

import javax.swing.JOptionPane;


public class SaveLoader 
{
	public Sim sim;
	public File path = new File("init.scn");

	public SaveLoader(Sim sim, String path) 
	{
		this.sim = sim;
		this.path = new File(path);
	}

	public String getString(Satellite sat)
	{
		String str = "";
		str += "=== " + sim.satellites.indexOf(sat) + " " + sat.name + " ===\n";
		str += "emptyMass = " + sat.emptyMass + "\n";
		str += "fuelMass = " + sat.fuelMass + "\n";
		str += "nEngines = ";
		for (int i = 0; i < sat.nEngines.length; i++)
			str += sat.nEngines[i] + " ";
		str += "\n";
		str += "flowRates = ";
		for (int i = 0; i < sat.flowRates.length; i++)
			str += sat.flowRates[i] + " ";
		str += "\n";
		str += "thrusts = ";
		for (int i = 0; i < sat.thrusts.length; i++)
			str += sat.thrusts[i] + " ";
		str += "\n";
		str += "SMa = " + sat.SMa + "\n";
		str += "Ecc = " + sat.Ecc + "\n";
		str += "Inc = " + sat.Inc + "\n";
		str += "LAN = " + sat.LAN + "\n";
		str += "AgP = " + sat.AgP + "\n";
		str += "TrA = " + sat.TrA + "\n";
		return str;
	}

	public void writeSave()
	{
		BufferedWriter out = null;

		try // try writing to the file
		{
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(path), "utf-8"));
			for (int i = 0; i < sim.satellites.size(); i++)
			{
				out.write(getString(sim.satellites.get(i)));
				out.write("\n");
			}
		} 
		catch (Exception e) // show error dialog
		{
			String message = "Error. Could not write file.";
			int type = JOptionPane.INFORMATION_MESSAGE;
			JOptionPane.showMessageDialog(null, message, "Save", type);
		} 
		finally 
		{
			try 
			{
				out.close();
			} 
			catch (Exception ex) {}
		}
	}
	
	public static int atoi(String str)
	{
		return Integer.parseInt(str);
	}
	
	public static double atof(String str)
	{
		return Double.parseDouble(str);
	}

	public void loadSave() throws Exception
	{
		String save = readSave();
		String[] parameters = save.split("\n");
		boolean sameShip = true;
		int line = 0; 

		// Iterate through save String

		try
		{
			for (line = 0 ; line < parameters.length; line++)
			{                               
				String text = parameters[line]; 
				if (!text.startsWith (";")) // Line starting with ";" are comments
				{
					String[] params = text.split(" ");
					if (params[0].equals("==="))
					{
						int index = atoi(params[1]);
						if (index > sim.satellites.size())
							sim.add(new Satellite(params[2]));
						Satellite sat = sim.get(index);
						sameShip = true;
						
						params = parameters[++line].split(" ");
						sameShip = !params[0].equals("===");
												
						while (sameShip)
						{
							if (params[0].equals("emptyMass"))
								sat.emptyMass = atof(params[2]);
							else if (params[0].equals("fuelMass"))
								sat.fuelMass = atof(params[2]);
							else if (params[0].equals("SMa"))
								sat.SMa = atof(params[2]);
							else if (params[0].equals("Ecc"))
								sat.Ecc = atof(params[2]);
							else if (params[0].equals("Inc"))
								sat.Inc = Math.toRadians(atof(params[2]));
							else if (params[0].equals("LAN"))
								sat.LAN = Math.toRadians(atof(params[2]));
							else if (params[0].equals("AgP"))
								sat.AgP = Math.toRadians(atof(params[2]));
							else if (params[0].equals("TrA"))
								sat.TrA = Math.toRadians(atof(params[2]));														
							else if (params[0].equals("nEngines"))
							{
								sat.nEngines = new int[params.length - 2];
								for (int j = 0; j < sat.nEngines.length; j++)
									sat.nEngines[j] = atoi(params[2+j]);
							}
							else if (params[0].equals("flowRates"))
							{
								sat.flowRates = new double[params.length - 2];
								
								for (int j = 0; j < sat.flowRates.length; j++)
								{
									sat.flowRates[j] = atof(params[2+j]);
								}
							}
							else if (params[0].equals("thrusts"))
							{
								sat.thrusts = new double[params.length - 2];
								for (int j = 0; j < sat.thrusts.length; j++)
									sat.thrusts[j] = atof(params[2+j]);
							}
							line++;
							sameShip = line < parameters.length;
							if (sameShip)
							{
								params = parameters[line].split(" ");
								sameShip = !params[0].equals("===");
							}
						}
						sat.mass = sat.emptyMass + sat.fuelMass;
						sat.orbitalToState();
						sat.updateSecondaries();
						sat.update();
						line--;						
					}
				}
			}       
		}
		catch (Exception e)
		{
			String exMessage = (line + 1) + " :\n" + e.getMessage();
			int type = JOptionPane.INFORMATION_MESSAGE;
            String message = "Error: Corrupt save file.\n";
            message += "Could not parse file at line " + exMessage;                                        
            JOptionPane.showMessageDialog(null, message, "Error", type);
            throw e;
		}
	}

	/** Reads the file at the indicated path and returns
	 * a String representation of the contents of the file.
	 * 
	 * @param path  the file to be read
	 * @return the file's contents in String format
	 */
	private String readSave()
	{
		// Declaration of Variables
		String save = "";
		BufferedReader in = null;
		boolean reading = true;

		try     // try reading the file
		{
			in = new BufferedReader (new FileReader (path));

			while (reading)
			{               
				String line = in.readLine();                                
				if (line != null)
					save += line + "\n";
				else
					reading = false;
			}

			in.close();
		}
		catch (Exception e) // show error dialog
		{
			String message = "Error. Could not read file:" + path;
			int type = JOptionPane.INFORMATION_MESSAGE;
			JOptionPane.showMessageDialog(null, message, "Load", type);
		}
		return save;
	}

}
