package marf.Preprocessing;

import marf.*;
import marf.util.*;
import marf.Storage.*;

/**
 * <p>Class Preprocessing</p>
 * <p>Abstract Preprocessing Module</p>
 * <p>$Header: /cvsroot/marf/marf/src/marf/Preprocessing/Preprocessing.java,v 1.19.2.1 2003/02/16 21:39:53 mokhov Exp $</p>
 */
public abstract class Preprocessing implements StorageManager
{
	/**
	 * Sample container
	 */
	protected Sample oSample = null;

	/**
	 * Preprocessing Constructor
	 * @param poSample loaded sample by a SampleLoader
	 */
	protected Preprocessing(Sample poSample)
	{
		try
		{
			oSample = poSample;

			normalize();
		}
		catch(Exception e)
		{
			System.err.println(e.toString());
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Derivatives implement this method to do general preprocessing and perhaps calling <code>removeNoise()</code> and <code>removeSilence()</code>.
	 * @return boolean that sample has changed as a result of preprocessing
	 * @exception PreprocessingException
	 */
	public abstract boolean preprocess() throws PreprocessingException;

	/**
	 * Derivatives implement this method to remove noise from the sample.
	 * @return boolean that sample has changed (noise removed)
	 * @exception PreprocessingException
	 */
	public boolean removeNoise() throws PreprocessingException
	{
		throw new NotImplementedException(this.getClass().getName() + ".removeNoise()");
	}

	/**
	 * Derivatives implement this method to remove silence.
	 * @return boolean that sample has changed (silence removed)
	 * @exception PreprocessingException
	 */
	public boolean removeSilence() throws PreprocessingException
	{
		throw new NotImplementedException(this.getClass().getName() + ".removeSilence()");
	}

	/**
	 * Normalization of incoming samples by amplitude.
	 * @return <code>true</code> when the sample has been successfully normalized
	 * @exception PreprocessingException
	 */
	public final boolean normalize() throws PreprocessingException
	{
		if(this.oSample == null)
			throw new PreprocessingException("Preprocessing.normalize() - oSample is not avail (null)");

		MARF.debug("Preprocessing.normalize() has begun...");

		double dMax = 0.0;

		double[] adAmplitude = this.oSample.getSampleArray();

		// Find max amplitude
		for(int i = 0; i < adAmplitude.length; i++)
			if(Math.abs(adAmplitude[i]) > dMax)
				dMax = Math.abs(adAmplitude[i]);

		// Actual normalization
		for(int i = 0; i < adAmplitude.length; i++)
			adAmplitude[i] /= dMax;

		MARF.debug("Preprocessing.normalize() has finished...");

		return true;
	}

	/**
	 * Derivatives implement this method to crop arbitrary part of the audio sample.
	 * @param pdStartingFrequency double Fequency to start to crop from
	 * @param pdEndFrequency double Frequency to crop the sample to
	 * @return boolean <code>true</code> - cropped, <code>false</code> - not
	 * @exception PreprocessingException
	 */
	public boolean cropAudio(double pdStartingFrequency, double pdEndFrequency) throws PreprocessingException
	{
		throw new NotImplementedException(this.getClass().getName() + ".cropAudio()");
	}

	/**
	 * Returns enclosed sample.
	 * @return Sample object
	 */
	public final Sample getSample()
	{
		return this.oSample;
	}

	/* StorageManager Interface */

	/**
	 * Not Implemented
	 * @exception IOException
	 */
	public void dump() throws java.io.IOException
	{
		throw new NotImplementedException(this.getClass().getName() + ".dump()");
	}

	/**
	 * Not Implemented
	 * @exception IOException
	 */
	public void restore() throws java.io.IOException
	{
		throw new NotImplementedException(this.getClass().getName() + ".restore()");
	}
}

// EOF
