package marf.Storage;

import marf.util.*;

import javax.sound.sampled.*;
import java.io.*;

/**
 * <p>Class Sample</p>
 * <p>Audio sample data container</p>
 * <p>$Header: /cvsroot/marf/marf/src/marf/Storage/Sample.java,v 1.23.2.1 2003/02/16 18:59:24 mokhov Exp $</p>
 */
public class Sample
{
	/*
	 * -------------------
	 * Sample formats
	 * -------------------
	 */

	/**
	 * Unknown sample format
	 */
	public static final int UNK  = -1;

	/**
	 * WAVE sample format
	 */
	public static final int WAV  = 0;

	/**
	 * ULAW sample format
	 */
	public static final int ULAW = 1;

	/**
	 * MP3 sample format
	 */
	public static final int MP3  = 2;

	/**
	 * Lowest possible sample format value. For boundaries check.
	 */
	private static final int LOWEST_FORMAT  = UNK;

	/**
	 * Highest possible sample format value. For boundaries check.
	 */
	private static final int HIGHEST_FORMAT = MP3;


	/*
	 * -------------------
	 * Data members
	 * -------------------
	 */

	/**
	 * Sample's format
	 */
	protected int      iFormat     = UNK;

	/**
	 * Sample data array (amplitudes)
	 */
	protected double[] adSample    = null;

	/**
	 * Chunk pointer in the sample array
	 */
	protected int      iArrayIndex = 0;


	/*
	 * -------------------
	 * Methods
	 * -------------------
	 */

	/**
	 * Default constructor
	 */
	public Sample()
	{
	}

	/**
	 * Accepts pre-set sample for testing
	 * @param padData preset amplitude values
	 */
	public Sample(double[] padData)
	{
		setSampleArray(padData);
	}

	/**
	 * Constructor with format indication
	 * @param piFormat format number for the enumeration
	 * @exception InvalidSampleFormatException
	 */
	public Sample(final int piFormat) throws InvalidSampleFormatException
	{
		setAudioFormat(piFormat);
	}

	/**
	 * @return an integer representing the format of the sample
	 */
	public final int getAudioFormat()
	{
		return this.iFormat;
	}

	/**
	 * Sets current format of a sample
	 * @param piFormat format number from the enumeration
	 * @exception InvalidSampleFormatException
	 */
	public final void setAudioFormat(final int piFormat) throws InvalidSampleFormatException
	{
		if(piFormat < LOWEST_FORMAT || piFormat > HIGHEST_FORMAT)
			throw new InvalidSampleFormatException("Not a valid audio format: " + piFormat);

		this.iFormat = piFormat;
	}

	/**
	 * Sets the internal sample array (adSample) with the specified argument.
	 * Index gets reset as well.
	 * @param paSampleArray an array of doubles
	 */
	public final void setSampleArray(double[] paSampleArray)
	{
		this.adSample = paSampleArray;
		this.iArrayIndex = 0;
	}

	/**
	 * Retrieves array containing audio data of the entire sample.
	 * @return double Array
	 */
	public final double[] getSampleArray()
	{
		return this.adSample;
	}

	/**
	 * Gets the next chunk of audio data and places it into chunkArray.
	 * Similar to readAudioData() method only it reads from the array instead of
	 * the audio stream (file).
	 * @param chunkArray An array of doubles
	 * @return number of data retrieved
	 */
	public final int getNextChunk(double[] chunkArray)
	{
		int count = 0;
		long sampleSize = getSampleSize();

		while(count < chunkArray.length && this.iArrayIndex < sampleSize)
		{
			chunkArray[count] = this.adSample[this.iArrayIndex];
			count++;
			this.iArrayIndex++;
		}

		return count;
	}

	/**
	 * Resets the marker used for reading audio data from sample array
	 */
	public final void resetArrayMark()
	{
		this.iArrayIndex = 0;
	}

	/**
	 * Returns the length of the sample.
	 * @return long Array length
	 */
	public final long getSampleSize()
	{
		if(this.adSample == null)
			return 0;
		else
			return this.adSample.length;
	}
}

// EOF
