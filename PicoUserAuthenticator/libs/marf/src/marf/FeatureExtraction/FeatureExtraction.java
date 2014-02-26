package marf.FeatureExtraction;

import marf.Storage.*;
import marf.Preprocessing.*;

import java.util.*;

/**
 * Class FeatureExtraction
 * <p>Generic Feature Extraction Module</p>
 * <p>$Header: /cvsroot/marf/marf/src/marf/FeatureExtraction/FeatureExtraction.java,v 1.18.2.1 2003/02/16 18:26:46 mokhov Exp $</p>
 */
public abstract class FeatureExtraction implements StorageManager
{
	/**
	 * Internal reference to the Preprocessing module
	 */
	protected Preprocessing oPreprocessing = null;

	/**
	 * An array of features extracted (coefficiencies and/or amplitude values)
	 */
	protected double[] adFeatures = null;

	/**
	 * FeatureExtraction constructor
	 * @param poPreprocessing preprocessing object ref.
	 */
	protected FeatureExtraction(Preprocessing poPreprocessing)
	{
		this.oPreprocessing = poPreprocessing;
	}

	/**
	 * Abstract feature extraction routine. Must be defined by the derivatives.
	 * @return boolean true if there were features extracted, false otherwise
	 */
	public abstract boolean extractFeatures() throws FeatureExtractionException;

	/**
	 * Allows retrieval of a feature vector
	 * @return array of features
	 */
	public final double[] getFeaturesArray()
	{
		return this.adFeatures;
	}

	// XXX: The below will have to be moved somewhere to util.math one day.
	// SM, Jan 11, 2003

	/**
	 * Retrieves a single value of hamming window based on length and index within the widow
	 * @param n index into the window
	 * @param length the total length of the window
	 * @return the hamming value within the window length; 0 if outside the window
	 */
	public static final double hamming(final int n, final int length)
	{
		if(n <= (length - 1) && n >= 0)
			return 0.54 - (0.46 * Math.cos((2 * Math.PI * n) / (double)(length - 1)));

		return 0;
	}

	/**
	 * Applies hamming window to an array of doubles
	 * @param padWindow array of doubles to apply windowing to
	 * @since 0.2.0
	 */
	public static final void hamming(double[] padWindow)
	{
		for(int i = 0; i < padWindow.length; i++)
			padWindow[i] *= (0.54 - (0.46 * Math.cos((2 * Math.PI * i) / (double)(padWindow.length - 1))));
	}
}

// EOF
