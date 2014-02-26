package marf.FeatureExtraction.LPC;

import marf.*;
import marf.util.*;
import marf.Storage.*;
import marf.gui.*;
import marf.Preprocessing.*;
import marf.FeatureExtraction.*;

import java.io.*;
import java.util.*;

/**
 * Class LPC
 * <p>Implements Linear Predictive Coding</p>
 * <p>$Header: /cvsroot/marf/marf/src/marf/FeatureExtraction/LPC/LPC.java,v 1.26.2.1 2003/02/16 18:26:46 mokhov Exp $</p>
 *
 * @author Ian Clement
 */
public class LPC extends FeatureExtraction
{
	/**
	 * p - Number of poles.
	 * <p>A pole is a root of the denominator in the Laplace transform of the
	 * input-to-output representation of the speech signal.</p>
	 */
	private int p;

	/**
	 * Window length
	 */
	private int windowLen;

	/**
	 * LPC Constructor
	 * @param poPreprocessing Preprocessing module reference
	 */
	public LPC(Preprocessing poPreprocessing)
	{
		super(poPreprocessing);
		setDefaults();

		// LPC-specific parameters, if any
		ModuleParams oModuleParams = MARF.getModuleParams();

		if(oModuleParams != null)
		{
			Vector oParams = oModuleParams.getFeatureExtractionParams();

			if(oParams.size() > 0)
			{
				p = ((Integer)oParams.elementAt(0)).intValue();
				windowLen = ((Integer)oParams.elementAt(1)).intValue();
			}
		}

	}

	/**
	 * Sets the default values of p and windowLen if none were supplied by an application
	 */
	private final void setDefaults()
	{
		this.p         = 20;
		this.windowLen = 128;
	}

	/**
	 * LPC Implementation of <code>extractFeatures()</code>
	 * @return <code>true</code> if features were extracted, <code>false</code> otherwise
	 */
	public final boolean extractFeatures() throws FeatureExtractionException
	{
		try
		{
			MARF.debug("LPC.extractFeatures() has begun...");

			double[] adSample = this.oPreprocessing.getSample().getSampleArray();

			//System.out.println("adSample.length: " +adSample.length);
			//System.out.println("p: " + p);
			//System.out.println("Windowlen: " + windowLen);

			Spectrogram oSpectrogram = null;

			// For the case when we want intermediate spectrogram
			if(MARF.getDumpSpectrogram() == true)
				oSpectrogram = new Spectrogram("lpc");

			this.adFeatures = new double[this.p];

			double[] windowed   = new double[this.windowLen];
			double[] lpc_coeffs = new double[this.p];
			double[] lpc_error  = new double[this.p];

			// num of windows
			int num = 1;

			int iHalfWindow = this.windowLen / 2;

			for(int count = iHalfWindow; (count + iHalfWindow) <= adSample.length; count += iHalfWindow)
			{
				// Window the input.
				for(int j = 0; j < this.windowLen; j++)
				{
					windowed[j] = adSample[count - iHalfWindow + j];
					//windowed[j] = adSample[count - iHalfWindow + j] * hamming(j, this.windowLen);
					//System.out.println("window: " + windowed[j]);
				}

				hamming(windowed);

				DoLPC(windowed, lpc_coeffs, lpc_error, this.p);

				if(MARF.getDumpSpectrogram() == true)
					oSpectrogram.addLPC(lpc_coeffs, this.p, iHalfWindow);

				for(int j = 0; j < this.p; j++)
				{
					adFeatures[j] += lpc_coeffs[j];
					//System.out.println("lpc_coeffs[" + j + "]"  + lpc_coeffs[j]);
				}

				num++;
			}

			// ...
			if(num > 1)
				for(int j = 0; j < p; j++)
					adFeatures[j] /= num;

			MARF.debug("LPC.extractFeatures() - num = " + num);

			// For the case when we want intermediate spectrogram
			if(MARF.getDumpSpectrogram() == true)
				oSpectrogram.dump();

			MARF.debug("LPC.extractFeatures() has finished.");

			return (this.adFeatures.length > 0);
		}
		catch(IOException e)
		{
			throw new FeatureExtractionException(e.toString());
		}
	}

	/**
	 * Does LPC algorithm
	 * <b>NOTE:</b> input is assumed to be windowed, ie: input.length = N
	 * @param input windowed part of incoming sample
	 * @param output resulting LPC coefficiencies
	 * @param error output LPC error
	 * @param p number of poles
	 */
	public static final void DoLPC(final double[] input, double[] output, double[] error, int p) throws FeatureExtractionException
	{
		if(p <= 0)
			throw new FeatureExtractionException("p should be > 0; supplied: " + p);

		if(output.length != p)
			throw new FeatureExtractionException("Output array should be of length p (" + p + ")!");

		if(error.length != p)
			throw new FeatureExtractionException("Error array should be of length p (" + p + ")!");

		double[]   k = new double[p];
		double[][] A = new double[p][p];

		error[0] = autocorrelation(input, 0);

		A[0][0] = k[0] = 0.0;

		for(int m = 1; m < p; m++)
		{
			// calculate k[m]
			double tmp = autocorrelation(input, m);

			for(int i = 1; i < m; i++)
				tmp -= A[m - 1][i] * autocorrelation(input, m - i);

			k[m] = tmp / error[m - 1];

			// update A[m][*]
			for(int i = 0; i < m; i++)
				A[m][i] = A[m - 1][i] - k[m] * A[m - 1][m - i];

			A[m][m] = k[m];

			// update error[m]
			error[m] = (1 - (k[m] * k[m])) * error[m - 1];
		}

		// [SM]: kludge?
		for(int i = 0; i < p; i++)
		{
			if(Double.isNaN(A[p - 1][i]))
				output[i] = 0.0;
			else
				output[i] = A[p - 1][i];
		}
	}

	/**
	 * Implements the least-square autocorrelation method
	 * @param input windowed input signal
	 * @param x coefficient number
	 * @return double - correlation number
	 */
	public static final double autocorrelation(final double[] input, int x)
	{
		double ret = 0.0;

		for(int i = x; i < input.length; i++)
			ret += input[i] * input[i - x];

		return ret;
	}

	/* From Storage Manager */

	/**
	 * Not Implemented
	 */
	public void dump() throws java.io.IOException
	{
		throw new NotImplementedException("LPC.dump()");
	}

	/**
	 * Not Implemented
	 */
	public void restore() throws java.io.IOException
	{
		throw new NotImplementedException("LPC.restore()");
	}
}

// EOF
