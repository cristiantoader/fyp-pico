package marf.FeatureExtraction.FFT;

import marf.*;
import marf.util.*;
import marf.gui.*;
import marf.Storage.*;
import marf.Preprocessing.*;
import marf.FeatureExtraction.*;
import marf.Storage.Loaders.*;

import java.io.*;
import java.util.*;

/**
 * Class FFT
 * <p>Implements Fast Fourier Transform</p>
 * <p>$Header: /cvsroot/marf/marf/src/marf/FeatureExtraction/FFT/FFT.java,v 1.31.2.1 2003/02/16 18:26:46 mokhov Exp $</p>
 *
 * @author Stephen Sinclair
 */
public class FFT extends FeatureExtraction
{
	/**
	 * Default number (1024) of doubles per chunk in the window.
	 * Feature vector will be half of the chunk size.
	 */
	public static final int DEFAULT_CHUNK_SIZE = 1024;

	/**
	 * FFT Constructor
	 * @param poPreprocessing Preprocessing module reference
	 */
	public FFT(Preprocessing poPreprocessing)
	{
		super(poPreprocessing);
	}

	/* Feature Extraction API */

	/**
	 * FFT Implementation of <code>extractFeatures()</code>
	 * @return true if there were features extracted, false otherwise
	 * @exception FeatureExtractionException
	 */
	public final boolean extractFeatures() throws FeatureExtractionException
	{
		try
		{
			MARF.debug("FFT.extractFeatures() has begun...");

			int ChunkSize     = DEFAULT_CHUNK_SIZE;
			int halfChunkSize = ChunkSize / 2;

			double[] SampleChunk = new double[ChunkSize];
			double[] SampleArray = null;

			int nbrDataRecv = 0;
			int windowPos = 0;
			int count = 0;

			int i, j;

			Spectrogram oSpectrogram = null;

			this.adFeatures = new double[halfChunkSize];

			double[] adMagnitude  = new double[halfChunkSize];
			double[] adPhaseAngle = new double[halfChunkSize];

			// For the case when we want intermediate spectrogram
			if(MARF.getDumpSpectrogram() == true)
				oSpectrogram = new Spectrogram("fft");

			windowPos = 0;
			nbrDataRecv = this.oPreprocessing.getSample().getNextChunk(SampleChunk);

			while(nbrDataRecv > 0)
			{
				// Fill SampleArray with new window
				for(i = 0; i < ChunkSize; i++)
				{
					if(windowPos >= ChunkSize)
					{
						nbrDataRecv = this.oPreprocessing.getSample().getNextChunk(SampleChunk);
						windowPos = 0;

						// Padding to ^2 for the last chunk
						if(nbrDataRecv < ChunkSize && nbrDataRecv > 0)
						{
							Arrays.fill(SampleChunk, nbrDataRecv, ChunkSize - 1, 0);
							nbrDataRecv = 0;
						}
					}

					//SampleArray[i] = SampleChunk[windowPos++] * hamming(i, ChunkSize);
					windowPos++;
				}

				//XXX: hamming(SampleChunk);
				SampleArray = (double[])SampleChunk.clone();
				hamming(SampleArray);

				// overlap windows
				windowPos = (windowPos - halfChunkSize) % ChunkSize;

				if(windowPos < 0)
					windowPos += ChunkSize;

				//XXX: NormalFFT(SampleChunk, adMagnitude, adPhaseAngle);
				NormalFFT(SampleArray, adMagnitude, adPhaseAngle);

				count++;

				if(MARF.getDumpSpectrogram() == true)
					oSpectrogram.addFFT(adMagnitude);

				for(i = 0; i < halfChunkSize; i++)
		    		this.adFeatures[i] += adMagnitude[i];

				nbrDataRecv = this.oPreprocessing.getSample().getNextChunk(SampleChunk);
			}

			if(count > 1)
				for(i = 0; i < halfChunkSize; i++)
					this.adFeatures[i] /= count;

			// For the case when we want intermediate spectrogram
			if(MARF.getDumpSpectrogram() == true)
				oSpectrogram.dump();

			// If we want to graph the FFT output
			if(MARF.getDumpWaveGraph() == true)
			{
				new WaveGrapher
				(
					this.adFeatures,
					0,
					WAVLoader.DEFAULT_FREQUENCY / 2,
					MARF.getSampleFile(),
					"fft"
				).dump();
			}

			MARF.debug("FFT.extractFeatures() has finished.");

			return (this.adFeatures.length > 0);
		}
		catch(Exception e)
		{
			throw new FeatureExtractionException(e.toString());
		}
	}

	/* From Storage Manager */

	/**
	 * Not Implemented
	 */
	public void dump() throws java.io.IOException
	{
		throw new NotImplementedException("FFT.dump()");
	}

	/**
	 * Not Implemented
	 */
	public void restore() throws java.io.IOException
	{
		throw new NotImplementedException("FFT.restore()");
	}

	/* FFT Methods */

	/**
	 * <p>FFT algorithm, translated from "Numerical Recipes in C++"
	 * Implements the Fast Fourier Transform, which performs a discrete Fourier transform
	 * in O(n*log(n)).</p>
	 *
	 * @param InputReal InputReal is real part of input array
	 * @param InputImag InputImag is imaginary part of input array
	 * @param OutputReal OutputReal is real part of output array
	 * @param OutputImag OutputImag is imaginary part of output array
	 * @param direction Direction is 1 for normal FFT, -1 for inverse FFT
	 * @exception FeatureExtractionException
	 */
	public static final void DoFFT(final double[] InputReal, double[] InputImag, double[] OutputReal, double[] OutputImag, int direction) throws FeatureExtractionException
	{
		// Ensure input length is a power of two
		int length = InputReal.length;

		if((length < 1) | ((length & (length - 1)) != 0))
			throw new FeatureExtractionException("Length of input (" + length + ") is not a power of 2.");

		if((direction != 1) && (direction != -1))
			throw new FeatureExtractionException("Bad direction specified.  Should be 1 or -1.");

		if(OutputReal.length < InputReal.length)
			throw new FeatureExtractionException("Output length (" + OutputReal.length + ") < Input length (" + InputReal.length + ")");

		// Determine max number of bits
		int maxbits, n = length;

		for(maxbits = 0; maxbits < 16; maxbits++)
		{
			if(n == 0) break;
			n /= 2;
		}

		maxbits -= 1;

		// Binary reversion & interlace result real/imaginary
		int i, t, bit;

		for(i = 0; i < length; i++)
		{
			t = 0;
			n = i;

			for(bit = 0; bit < maxbits; bit++)
			{
				t = (t * 2) | (n & 1);
				n /= 2;
			}

			OutputReal[t] = InputReal[i];
			OutputImag[t] = InputImag[i];
		}

		// put it all back together (Danielson-Lanczos butterfly)
		int mmax = 2, istep, j, m;								// counters
		double theta, wtemp, wpr, wr, wpi, wi, tempr, tempi;	// trigonometric recurrences

		n = length * 2;

		while(mmax < n)
		{
			istep = mmax * 2;
			theta = (direction * 2 * Math.PI) / mmax;
			wtemp = Math.sin(0.5 * theta);
			wpr   = -2.0 * wtemp * wtemp;
			wpi   = Math.sin(theta);
			wr    = 1.0;
			wi    = 0.0;

			for(m = 0; m < mmax; m += 2)
			{
				for(i = m; i < n; i += istep)
				{
					j = i + mmax;
					tempr = wr * OutputReal[j / 2] - wi * OutputImag[j / 2];
					tempi = wr * OutputImag[j / 2] + wi * OutputReal[j / 2];

					OutputReal[j / 2] = OutputReal[i / 2] - tempr;
					OutputImag[j / 2] = OutputImag[i / 2] - tempi;

					OutputReal[i / 2] += tempr;
					OutputImag[i / 2] += tempi;
				}

				wr = (wtemp = wr) * wpr - wi * wpi + wr;
				wi = wi * wpr + wtemp * wpi + wi;
			}

			mmax = istep;
		}
	}

	/**
	 * <p>Performs a normal FFT, taking a real input (supposedly an audio sample) and returns
	 * the frequency analysis in terms of "magnitude" and "phase angle".</p>
	 *
	 * @param sample must be an array of size (2^k)
	 * @param magnitude must be half the size of "sample"
	 * @param phaseAngle must be half the size of "sample"
	 * @exception FeatureExtractionException
	 */
	public static final void NormalFFT(final double[] sample, double[] magnitude, double[] phaseAngle) throws FeatureExtractionException
	{
		double[] sampleImag = new double[sample.length];
		double[] outputReal = new double[sample.length];
		double[] outputImag = new double[sample.length];

		DoFFT(sample, sampleImag, outputReal, outputImag, 1);

		// convert complex output to magnitude and phase angle
		int len = magnitude.length;

		if(magnitude.length > (sample.length / 2))
			len = sample.length / 2;

		for(int i = 0; i < len; i++)
		{
			magnitude[i] = Math.sqrt(outputReal[i] * outputReal[i] + outputImag[i] * outputImag[i]);

			if(phaseAngle != null)
				phaseAngle[i] = Math.atan(outputImag[i] / outputReal[i]);
		}
	}

	/**
	 * <p>Performs a normal FFT, taking a real input (supposedly an audio sample) and returns
	 * the frequency analysis in terms of "magnitude".</p>
	 *
	 * @param sample must be an array of size (2^k)
	 * @param magnitude must be half the size of "sample"
	 * @exception FeatureExtractionException
	 */
	public static final void NormalFFT(final double[] sample, double[] magnitude) throws FeatureExtractionException
	{
		NormalFFT(sample, magnitude, null);
	}
}

// EOF
