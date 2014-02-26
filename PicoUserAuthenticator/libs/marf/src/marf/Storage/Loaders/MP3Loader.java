package marf.Storage.Loaders;

import marf.Storage.*;
import marf.util.*;

import java.io.*;

/**
 * Not Implemented
 */
public class MP3Loader extends SampleLoader
{
	/**
	 * MP3 Loader Constructor
	 */
	public MP3Loader()
	{
	}

	/**
	 * Not Implemented
	 */
	public final int readAudioData(double[] pSample) throws Exception
	{
		throw new NotImplementedException("MP3Loader.readAudioData()");
	}

	/**
	 * Not Implemented
	 */
	public final int writeAudioData(final double[] pSample, final int nbrData) throws Exception
	{
		throw new NotImplementedException("MP3Loader.writeAudioData()");
	}

	/**
	 * Not Implemented
	 */
	public Sample loadSample(File poFile) throws Exception
	{
		throw new NotImplementedException("MP3Loader.loadSample()");
	}

	/**
	 * Not Implemented
	 */
	public void saveSample(File poFile) throws Exception
	{
		throw new NotImplementedException("MP3Loader.saveSample()");
	}
}

// EOF
