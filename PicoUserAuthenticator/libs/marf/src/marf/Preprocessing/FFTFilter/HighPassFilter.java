package marf.Preprocessing.FFTFilter;

import marf.*;
import marf.Storage.*;
import marf.Preprocessing.*;

/**
 * HighPassFilter Class
 * <p>Implements high pass filtering the FFT Filter</p>
 *
 * <p>$Header: /cvsroot/marf/marf/src/marf/Preprocessing/FFTFilter/HighPassFilter.java,v 1.3 2003/01/26 06:10:48 mokhov Exp $</p>
 */
public class HighPassFilter extends FFTFilter
{
	/**
	 * HighPassFilter Constructor
	 * @param poSample incoming sample
	 */
	public HighPassFilter(Sample poSample)
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
				response[i] = 1;
			else
				response[i] = 0;
		}

		// [SM]: XXX - Would be cool if we boost high frequencies along the way

		setFrequencyResponse(response);
	}

	/**
	 * Stub implementation of removeNoise()
	 * @return <code>false</code>
	 * @exception PreprocessingException
	 */
	public final boolean removeNoise() throws PreprocessingException
	{
		MARF.debug("HighPassFilter.removeNoise()");
		return false;
	}

	/**
	 * Stub implementation of removeSilence()
	 * @return <code>false</code>
	 * @exception PreprocessingException
	 */
	public final boolean removeSilence() throws PreprocessingException
	{
		MARF.debug("HighPassFilter.removeSilence()");
		return false;
	}

	/**
	 * Stub implementation of cropAudio()
	 * @return <code>false</code>
	 * @exception PreprocessingException
	 */
	public final boolean cropAudio(double pdStartingFrequency, double pdEndFrequency) throws PreprocessingException
	{
		MARF.debug("HighPassFilter.cropAudio()");
		return false;
	}
}

// EOF
