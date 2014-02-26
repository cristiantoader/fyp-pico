package marf.Storage;

import javax.sound.sampled.*;
import java.io.*;

/**
 * Absract Class SampleLoader
 * <p>Provides samle loading interface. Must be overriden by a concrete sample loader.</p>
 *
 * <p>$Header: /cvsroot/marf/marf/src/marf/Storage/SampleLoader.java,v 1.11.2.1 2003/02/16 18:59:24 mokhov Exp $</p>
 */
public abstract class SampleLoader
{
	/**
	 * Sample references of the sample to be loaded.
	 */
	protected Sample oSample = null;

	/**
	 * Properties of a wave.
	 */
	protected AudioFormat oAudioFormat = null;

	/**
	 * Stream representing wave sample.
	 */
	protected AudioInputStream oAudioInputStream = null;

	/**
	 * Output stream used for writing audio data.
	 */
	protected ByteArrayOutputStream oByteArrayOutputStream = null;

	/**
	 * Default constructor. Instantiates <code>ByteArrayOutputStream</code>.
	 */
	public SampleLoader()
	{
		this.oByteArrayOutputStream = new ByteArrayOutputStream();
	}

	/**
	 * Reads audio data from the sample's audio stream into paiAudioData.
	 * @param paiAudioData an array of doubles
	 * @return integer the number of data read
	 * @throws Exception
	 */
	public abstract int readAudioData(double[] paiAudioData) throws Exception;

	/**
	 * Writes audio data into the sample's audio stream.
	 * @param paiAudioData an array of doubles
	 * @param piWords the number of audio data to written from the paiAudiodata
	 * @return the number of data written
	 * @throws Exception
     */
	public abstract int writeAudioData(double[] paiAudioData, int piWords) throws Exception;

	/**
	 * SampleLoader interface. Must be overriden by a concrete loader that knows how to load a specific sample.
	 * @param poFile file object a sample to be read from
	 * @return Sample object refernce
	 * @throws Exception
	 */
	public abstract Sample loadSample(File poFile) throws Exception;

	/**
	 * Same as loadSample(File) but takes filename as an argument.
	 * @param pstrFilename filename of a sample to be read from
	 * @return Sample object refernce
	 * @throws Exception
	 */
	public Sample loadSample(final String pstrFilename) throws Exception
	{
		return loadSample(new File(pstrFilename));
	}

	/**
	 * SampleLoader interface. Must be overriden by a concrete loader that knows how to save a specific sample.
	 * @param poFile File object a sample to be saved to
	 * @throws Exception
	 */
	public abstract void saveSample(File poFile) throws Exception;

	/**
	 * Same as saveSample(File) but takes filename as an argument.
	 * @param pstrFilename filename of a sample to be saved to
	 * @throws Exception
	 */
	public void saveSample(final String pstrFilename) throws Exception
	{
		saveSample(new File(pstrFilename));
	}

	/**
	 * <p><code>UpdateSample()</code> is just used whenever the <code>AudioInputStream</code> is assigned to a new value (wave file).
	 * Then you would simply call this method to update the
	 * <code>Sample</code> member with the contents of the new <code>AudioInputStream</code>.</p>
	 * @throws Exception
	 */
	public final void updateSample() throws Exception
	{
		double[] SampleArray = new double[(int)getSampleSize()];
		readAudioData(SampleArray);
		this.oSample.setSampleArray(SampleArray);
	}

	/**
	 * Resets the marker for the audio stream. Used after writing audio data
	 * into the sample's audio stream.
	 * @throws Exception
	 */
	public final void reset() throws Exception
	{
		oAudioInputStream.reset();
	}

	/**
	 * Retrieves the length of the sample (# of audio data in the audio stream).
	 * @return sample size, long
	 * @throws Exception
	 */
	public final long getSampleSize() throws Exception
	{
		return (long)oAudioInputStream.getFrameLength();
	}

	/**
	 * @return Sample reference
	 */
	public final Sample getSample()
	{
		return this.oSample;
	}

	/**
	 * Sets internal sample reference from outside
	 * @param poSample Sample object
	 */
	public final void setSample(Sample poSample)
	{
		this.oSample = poSample;
	}
}

// EOF
