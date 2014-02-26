package marf.gui;

import marf.*;
import marf.util.*;
import marf.Storage.*;

import java.util.*;
import java.io.*;

/**
 * Class WaveGrapher
 * <p>$Header: /cvsroot/marf/marf/src/marf/gui/WaveGrapher.java,v 1.4 2003/02/08 04:05:00 mokhov Exp $</p>
 * @author Steven Sinclair
 */
public class WaveGrapher implements StorageManager
{
	/**
	 * Filename to dump graph to
	 */
	private String strFilename;

	/**
	 * Data to graph
	 */
	private double[] DataArray = null;

	/**
	 * Range of data for the X axis -- Minimum
	 */
	private double dXmin = 0;

	/**
	 * Range of data for the X axis -- Maximum
	 */
	private double dXmax = 0;

	/**
	 * Constructor
	 */
	public WaveGrapher(double[] data, double min, double max, String name, String descriptor)
	{
		DataArray = data;

		this.dXmin = min;
		this.dXmax = max;

		this.strFilename = new String(name + "." + MARF.getPreprocessingMethod() + "." + descriptor);
	}

	/**
	 * Dumps graph of wave in the CSV format
	 * @exception IOException
	 */
	public final void dump() throws java.io.IOException
	{
		try
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter(this.strFilename + ".txt"));

			bw.write(this.strFilename);
			bw.newLine();

			double range = dXmax - dXmin;

			for(int i = 0; i < DataArray.length; i++)
			{
				bw.write((dXmin + (double)i * range / (double)DataArray.length) + "\t" + DataArray[i]);
				bw.newLine();
			}

			bw.close();

			MARF.debug("WaveGrapher dumped to " + this.strFilename + ".txt");
		}
		catch(IOException e)
		{
			MARF.debug
			(
				"WaveGrapher couldn't generate graph (" + this.strFilename + "): " +
				e.getMessage()
			);
		}
	}

	/**
	 * Not implemented
	 */
	public final void restore() throws java.io.IOException
	{
		throw new NotImplementedException("WaveGrapher.restore()");
	}
}

// EOF
