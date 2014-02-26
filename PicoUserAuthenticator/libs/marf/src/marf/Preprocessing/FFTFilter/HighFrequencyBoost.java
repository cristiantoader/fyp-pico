package marf.Preprocessing.FFTFilter;

import marf.*;
import marf.Storage.*;
import marf.Preprocessing.*;

/**
 * HighFrequencyBoost Class
 * <p>Implements filtering using high frequency booster on top of the FFT Filter</p>
 *
 * <p>$Header: /cvsroot/marf/marf/src/marf/Preprocessing/FFTFilter/HighFrequencyBoost.java,v 1.8 2003/02/09 19:02:26 mokhov Exp $</p>
 */
public class HighFrequencyBoost extends FFTFilter
{
	/**
	 * HighFrequencyBoost Constructor
	 * @param poSample incoming sample
	 */
	public HighFrequencyBoost(Sample poSample)
	{
		super(poSample);

		try
		{
			double[] response = new double[128];

			// create a response that boosts all frequencies above 1000 Hz
			// Note: 1000Hz ~= 25 * 128 / 8000Hz
			for(int i = 0; i < response.length; i++)
			{
				if(i < 25)
					response[i] = 1;
				else
					response[i] = 5 * Math.PI;
			}

			setFrequencyResponse(response);
		}
		catch(Exception e)
		{
			System.err.println(e.toString());
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Overrides FFTFilter's preprocess with extra normalization after boost.
	 * @return <code>true</code> if there were changes to the sample
	 * @since 0.2.0
	 * @exception PreprocessingException
	 */
	public final boolean preprocess() throws PreprocessingException
	{
		boolean bChanges = super.preprocess();
		bChanges |= normalize();
		return bChanges;
	}

	/**
	 * Stub implementation of removeNoise()
	 * @return <code>false</code>
	 * @exception PreprocessingException
	 */
	public final boolean removeNoise() throws PreprocessingException
	{
		MARF.debug("HighFrequencyBoost.removeNoise()");
		return false;
	}

	/**
	 * Stub implementation of removeSilence()
	 * @return <code>false</code>
	 * @exception PreprocessingException
	 */
	public final boolean removeSilence() throws PreprocessingException
	{
		MARF.debug("HighFrequencyBoost.removeSilence()");
		return false;
	}

	/**
	 * Stub implementation of cropAudio()
	 * @return <code>false</code>
	 * @exception PreprocessingException
	 */
	public final boolean cropAudio(double pdStartingFrequency, double pdEndFrequency) throws PreprocessingException
	{
		MARF.debug("HighFrequencyBoost.cropAudio()");
		return false;
	}
}

// EOF
