package marf.Preprocessing.FFTFilter;

import marf.*;
import marf.Storage.*;
import marf.Preprocessing.*;
import marf.FeatureExtraction.*;
import marf.FeatureExtraction.FFT.*;

import java.util.*;

/**
 * FFTFilter class
 * <p>Implements filtering using FFT algorithm</p>
 * <p>Derivatives must set frequency response based on the type of filter they are.</p>
 * @author Stephen Sinclair
 */
public abstract class FFTFilter extends Preprocessing
{
	/**
	 * Frequency repsonse to be multiplied by the incoming value
	 */
	protected double[] freqResponse = null;

	/**
	 * FFTFilter Constructor
	 * @param poSample incoming sample
	 */
	public FFTFilter(Sample poSample)
	{
		super(poSample);
	}

	/**
	 * FFTFilter implementation of <code>preprocess()</code>
	 * <p>It does call <code>removeNoise()</code> and <code>removeSilence()</code> if they were explicitly
	 * requested by an app <em>before</em> applying filtering.</p>
	 * @return true if there was something filtered out
	 * @exception PreprocessingException
	 */
	public boolean preprocess() throws PreprocessingException
	{
		if(freqResponse == null)
			throw new PreprocessingException("FFTFilter.preprocess() - freqResponse is null");

		boolean bChanges = false;

		double[] sample = this.oSample.getSampleArray();
		double[] filtered = new double[sample.length];

		boolean bRemoveNoise = false;
		boolean bRemoveSilence = false;

		// Exract any additional params if supplied

		ModuleParams oModuleParams = MARF.getModuleParams();

		if(oModuleParams != null)
		{
			Vector oParams = oModuleParams.getPreprocessingParams();

			if(oParams.size() > 0)
			{
				bRemoveNoise = ((Boolean)oParams.elementAt(0)).booleanValue();
				bRemoveSilence = ((Boolean)oParams.elementAt(1)).booleanValue();
			}
		}

		if(bRemoveNoise == true)
			bChanges |= removeNoise();

		if(bRemoveSilence == true)
			bChanges |= removeSilence();

		bChanges |= filter(sample, filtered);

		this.oSample.setSampleArray(filtered);

		return bChanges;
	}

	/**
	 * Sets frequency response.
	 * Derivatives must call this method in their constructors.
	 * @param response desired frequency response
	 * @exception PreprocessingException
	 */
	public final void setFrequencyResponse(final double[] response)
	{
		freqResponse = new double[response.length * 2];

		for(int i = 0; i < response.length; i++)
		{
			freqResponse[i] = response[i];
			freqResponse[freqResponse.length - i - 1] = response[i];
		}
	}

	/**
	 * Perform a filter by the following algorithm:
	 * sample -> window -> FFT -> buffer
	 * buffer * frequency response
	 * buffer -> IFFT -> window -> sample
	 *
	 * Window used is square root of Hamming window, because
	 * the sum at half-window overlap is a constant, which
	 * avoids amplitude distortion from noise.
	 * Also, start sampling at -responseSize/2, in order to avoid
	 * amplitude distortion of the first half of the first window.
	 * "filtered" must be at least as long as "sample"
	 *
	 * @param sample incoming sample analog data
	 * @param filtered will contain data after the filter was applied
	 * @return true if some filtering actually happened
	 * @exception PreprocessingException
	 */
	public final boolean filter(final double[] sample, double[] filtered) throws PreprocessingException
	{
		try
		{
			int responseSize = freqResponse.length;

			double[] buffer = new double[responseSize];
			double[] bufferImag = new double[responseSize];
			double[] outputReal = new double[responseSize];
			double[] outputImag = new double[responseSize];

			if(filtered.length < sample.length)
				throw new PreprocessingException
				(
					"FFTFilter: Output buffer not long enough (" +
					filtered.length + "<" + sample.length + ")."
				);

			int i;

			int pos = -responseSize / 2;

			while(pos < sample.length)
			{
				for(i = 0; i < responseSize; i++)
				{
					if(((pos + i) < sample.length) && ((pos + i) >= 0))
						buffer[i] = sample[pos+i] * Math.sqrt(1 - 0.85185 * Math.cos((2 * i - 1) * Math.PI / responseSize));
					else
						buffer[i] = 0;

					bufferImag[i] = 0;
				}

				FFT.DoFFT(buffer, bufferImag, outputReal, outputImag, 1);

				for(i = 0; i < responseSize; i++)
				{
					outputReal[i] *= freqResponse[i];
					outputImag[i] *= freqResponse[i];
				}

				FFT.DoFFT(outputReal, outputImag, buffer, bufferImag, -1);

				// copy & normalize
				for(i = 0; (i < responseSize) && ((pos + i) < sample.length); i++)
				{
					if((pos + i) >= 0)
						filtered[pos + i] +=
							buffer[i] *
							Math.sqrt(1 - 0.85185 * Math.cos((2 * i - 1) * Math.PI / responseSize)) /
							(double)responseSize;
				}

				pos += responseSize / 2;
			}

			return true;
		}
		catch(NullPointerException e)
		{
			throw new PreprocessingException("FFTFilter: frequency response hasn't been set.");
		}
		catch(FeatureExtractionException e)
		{
			throw new PreprocessingException("FFTFilter: " + e.getMessage());
		}
	}
}

// EOF
