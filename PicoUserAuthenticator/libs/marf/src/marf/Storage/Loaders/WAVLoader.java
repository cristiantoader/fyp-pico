package marf.Storage.Loaders;

import marf.Storage.*;
import marf.util.*;

import javax.sound.sampled.*;
import java.io.*;

/**
 * <p>Class WAVLoader</p>
 * <p>Loads/stores samples of WAV format</p>
 * <p>$Header: /cvsroot/marf/marf/src/marf/Storage/Loaders/WAVLoader.java,v 1.10 2003/02/09 16:24:03 mokhov Exp $</p>
 * @author Jimmy Nicolacopoulos
 */
public class WAVLoader extends SampleLoader
{
	/*
	 * ----------------
	 * Constants
	 * ----------------
	 */

	/**
	 * Default 16
	 */
	public static final int DEFAULT_SAMPLE_BIT_SIZE = 16;

	/**
	 * Default 1
	 */
	public static final int DEFAULT_CHANNELS = 1;

	/**
	 * Default 8000 Hz
	 */
	public static final float DEFAULT_FREQUENCY = 8000;

	/*
	 * ----------------
	 * Data Members
	 * ----------------
	 */

	/**
	 * Current bit size of a sample
	 */
	private int iRequiredBitSize = DEFAULT_SAMPLE_BIT_SIZE;

	/**
	 * Current number of channels
	 */
	private int iRequiredChannels = DEFAULT_CHANNELS;

	/**
	 * Current frequency
	 */
	private float iRequiredFrequency = DEFAULT_FREQUENCY;

	/*
	 * ----------------
	 * Methods
	 * ----------------
	 */

	/**
	 * WAVLoader Constructor.
	 * @exception InvalidSampleFormatException
	 */
	public WAVLoader() throws InvalidSampleFormatException
	{
		this.oSample = new Sample(Sample.WAV);
		AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;

		float rate = DEFAULT_FREQUENCY;
		int bitSampleSize = DEFAULT_SAMPLE_BIT_SIZE;
		int channels = DEFAULT_CHANNELS;

		oAudioFormat = new AudioFormat
		(
			encoding,
			rate,
			bitSampleSize,
			channels,
			(bitSampleSize / 8) * channels,
			rate,
			false
		);
	}

	/**
	 * Loads WAV sample data from a file.
	 * @param inFile incoming sample File object
	 * @return Sample object
	 * @exception Exception
	 */
	public Sample loadSample(File inFile) throws Exception
	{
		AudioInputStream oNewInputStream;

		if(inFile != null && inFile.isFile())
		{
			AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(inFile);

			if(fileFormat.getType() != AudioFileFormat.Type.WAVE)
				throw new InvalidSampleFormatException("Audio file type is not WAVE");

			oNewInputStream = AudioSystem.getAudioInputStream(inFile);
			AudioFormat oNewFormat = oNewInputStream.getFormat();

			if(oNewFormat.getSampleSizeInBits() != iRequiredBitSize)
				throw new UnsupportedAudioFileException("Wave file not " + this.iRequiredBitSize + "-bit");

			if(oNewFormat.getChannels() != iRequiredChannels)
				throw new UnsupportedAudioFileException("Wave file is not mono.");

			if(oNewFormat.getFrameRate() != iRequiredFrequency)
				throw new UnsupportedAudioFileException("Wave file is not " + this.iRequiredFrequency + " Hz");
		}
		else
			throw new FileNotFoundException();

		this.oAudioInputStream = oNewInputStream;
		this.updateSample();

		return this.oSample;
	}

	/**
	 * Buffers out the contents of audioBuffer into audioData
	 * @return the number of data read.
	 * @exception Exception
	 */
	public final int readAudioData(double[] audioData) throws Exception
	{
		int wcount;
		int MSB, LSB;

		byte[] audioBuffer = new byte[audioData.length * 2];
		int nbrBytes = this.oAudioInputStream.read(audioBuffer);

		wcount = (nbrBytes  / 2) + (nbrBytes % 2);

		for(int i = 0; i < wcount; i++)
		{
			if(this.oAudioFormat.isBigEndian())
			{
				// First byte is MSB (high order)
				MSB = (int)audioBuffer[2 * i];
				// Second byte is LSB (low order)
				LSB = (int)audioBuffer[2 * i + 1];
			}
			else
			{
				// Vice-versa...
				LSB = (int)audioBuffer[2 * i];
				MSB = (int)audioBuffer[2 * i + 1];
			}

			// Merge high-order and low-order byte to form a 16-bit double value.
			// Values are divided by maximum range
			audioData[i] = (double) (MSB << 8 | (255 & LSB)) / 32768;
		}

		return wcount;
	}

	/**
	 * Buffers the contents of audioData into audioBuffer
	 * @param audioData array of data to be written
	 * @param nbrWords number of words to be written
	 * @return the number of data written
	 * @exception Exception
	 */
	public final int writeAudioData(final double[] audioData, final int nbrWords) throws Exception
	{
		int word = 0;

		byte[] audioBytes;
		byte[] audioBuffer = new byte[nbrWords * 2];

		for(int i = 0; i < nbrWords; i++)
		{
			word = (int) (audioData[i] * 32768);
			audioBuffer[2 * i] = (byte) (word & 255);
			audioBuffer[2 * i + 1] = (byte) (word >>> 8);
		}

		this.oByteArrayOutputStream.write(audioBuffer, 0, audioBuffer.length);
		audioBytes = oByteArrayOutputStream.toByteArray();

		ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);

		this.oAudioInputStream = new AudioInputStream
		(
			bais,
			this.oAudioFormat,
			audioBytes.length / this.oAudioFormat.getFrameSize()
		);

		return audioBuffer.length;
	}

	/**
	 * Saves the wave into a file for playback
	 * @param outFile File object for output
	 * @exception Exception
	 */
	public final void saveSample(File outFile) throws Exception
	{
		AudioSystem.write(this.oAudioInputStream, AudioFileFormat.Type.WAVE, outFile);
		this.reset();
		this.oByteArrayOutputStream.reset();
	}
}

// EOF
