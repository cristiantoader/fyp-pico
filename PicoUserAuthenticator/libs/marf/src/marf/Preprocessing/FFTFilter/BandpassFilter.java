package marf.Preprocessing.FFTFilter;

import marf.*;
import marf.util.*;
import marf.Storage.*;
import marf.Preprocessing.*;

import java.io.*;

/**
 * <p>Class BandpassFilter</p>
 * <p>Bandpass Filter Implementation based on the FFTFilter</p>
 * <p>$Header: /cvsroot/marf/marf/src/marf/Preprocessing/FFTFilter/BandpassFilter.java,v 1.8 2003/01/26 06:10:48 mokhov Exp $</p>
 * @since 0.2.0
 */
public class BandpassFilter extends FFTFilter
{
	/**
	 * BandpassFilter Constructor
	 * @param poSample incoming sample
	 */
	public BandpassFilter(Sample poSample)
	{
		super(poSample);

		try
		{
			double[] response = new double[128];

			// Note: Frequencies kept: ~= 1000Hz - 2853Hz
			for(int i = 0; i < response.length; i++)
				if(i >= 25 && i <= 70)
					response[i] = 1;

			setFrequencyResponse(response);
		}
		catch(Exception e)
		{
			System.err.println(e.toString());
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Stub implementation of removeNoise()
	 * @return <code>false</code>
	 * @exception PreprocessingException
	 */
	public final boolean removeNoise() throws PreprocessingException
	{
		MARF.debug("BandpassFilter.removeNoise()");
		return false;
	}

	/**
	 * Stub implementation of removeSilence()
	 * @return <code>false</code>
	 * @exception PreprocessingException
	 */
	public final boolean removeSilence() throws PreprocessingException
	{
		MARF.debug("BandpassFilter.removeSilence()");
		return false;
	}

	/**
	 * Stub implementation of cropAudio()
	 * @return <code>false</code>
	 * @exception PreprocessingException
	 */
	public final boolean cropAudio(double pdStartingFrequency, double pdEndFrequency) throws PreprocessingException
	{
		MARF.debug("BandpassFilter.cropAudio()");
		return false;
	}
}

// EOF
