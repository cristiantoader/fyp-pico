package marf.Preprocessing.FFTFilter;

import marf.*;
import marf.Storage.*;
import marf.Preprocessing.*;

/**
 * LowPassFilter Class
 * <p>Implements low pass filtering the FFT Filter</p>
 *
 * <p>$Header: /cvsroot/marf/marf/src/marf/Preprocessing/FFTFilter/LowPassFilter.java,v 1.3 2003/01/26 06:10:48 mokhov Exp $</p>
 */
public class LowPassFilter extends FFTFilter
{
	/**
	 * LowPassFilter Constructor
	 * @param poSample incoming sample
	 */
	public LowPassFilter(Sample poSample)
	{
		super(poSample);

		double[] response = new double[128];

		/*
		 * create a response that drops all frequencies above 2853 Hz
		 * XXX -- Note: 2853Hz = 70 * 128 / 8000Hz
		 */
		for(int i = 0; i < response.length; i++)
		{
			if(i > 70)
				response[i] = 0;
			else
				response[i] = 1;
		}

		setFrequencyResponse(response);
	}

	/**
	 * Stub implementation of removeNoise()
	 * @return <code>false</code>
	 * @exception PreprocessingException
	 */
	public final boolean removeNoise() throws PreprocessingException
	{
		MARF.debug("LowPassFilter.removeNoise()");
		return false;
	}

	/**
	 * Stub implementation of removeSilence()
	 * @return <code>false</code>
	 * @exception PreprocessingException
	 */
	public final boolean removeSilence() throws PreprocessingException
	{
		MARF.debug("LowPassFilter.removeSilence()");
		return false;
	}

	/**
	 * Stub implementation of cropAudio()
	 * @return <code>false</code>
	 * @exception PreprocessingException
	 */
	public final boolean cropAudio(double pdStartingFrequency, double pdEndFrequency) throws PreprocessingException
	{
		MARF.debug("LowPassFilter.cropAudio()");
		return false;
	}
}

// EOF
