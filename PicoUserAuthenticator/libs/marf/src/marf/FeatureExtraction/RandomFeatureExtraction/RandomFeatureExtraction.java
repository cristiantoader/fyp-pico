package marf.FeatureExtraction.RandomFeatureExtraction;

import marf.util.*;
import marf.Storage.*;
import marf.Preprocessing.*;
import marf.FeatureExtraction.*;

import java.io.*;
import java.util.*;


/**
 * Class RandomFeatureExtraction. Implementation of random feature extraction for testing.
 * <p>$Header: /cvsroot/marf/marf/src/marf/FeatureExtraction/RandomFeatureExtraction/RandomFeatureExtraction.java,v 1.3.2.1 2003/02/16 18:26:46 mokhov Exp $<p>
 * @since 0.2.0
 */
public class RandomFeatureExtraction extends FeatureExtraction
{
	/**
	 * Default number (256) of doubles per chunk in a feature vector.
	 */
	public static final int DEFAULT_CHUNK_SIZE = 256;

	/**
	 * RandomFeatureExtraction Constructor
	 * @param poPreprocessing Preprocessing object reference
	 */
	public RandomFeatureExtraction(Preprocessing poPreprocessing)
	{
		super(poPreprocessing);

		// XXX: ModuleParams
	}

	/**
	 * Random Gaussian feature extracton.
	 * @return <code>true</code>
	 * @exception FeatureExtractionException
	 */
	public final boolean extractFeatures() throws FeatureExtractionException
	{
		double[] adChunk = new double[DEFAULT_CHUNK_SIZE];
		this.adFeatures  = new double[DEFAULT_CHUNK_SIZE];

		int iDataRecv = this.oPreprocessing.getSample().getNextChunk(adChunk);

		while(iDataRecv > 0)
		{
			for(int i = 0; i < DEFAULT_CHUNK_SIZE; i++)
				this.adFeatures[i] += adChunk[i] * (new Random(i).nextGaussian());

			iDataRecv = this.oPreprocessing.getSample().getNextChunk(adChunk);

			// Padding to ^2 for the last chunk
			if(iDataRecv < DEFAULT_CHUNK_SIZE && iDataRecv > 0)
			{
				Arrays.fill(adChunk, iDataRecv, DEFAULT_CHUNK_SIZE - 1, 0);
				iDataRecv = 0;
			}
		}

		return true;
	}

	/* From Storage Manager */

	/**
	 * Not Implemented
	 */
	public void dump() throws java.io.IOException
	{
		throw new NotImplementedException("RandomFeatureExtraction.dump()");
	}

	/**
	 * Not Implemented
	 */
	public void restore() throws java.io.IOException
	{
		throw new NotImplementedException("RandomFeatureExtraction.restore()");
	}
}

// EOF
