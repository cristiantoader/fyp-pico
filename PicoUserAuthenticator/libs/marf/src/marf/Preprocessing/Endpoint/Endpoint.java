package marf.Preprocessing.Endpoint;

import marf.Preprocessing.*;
import marf.Storage.*;
import marf.util.*;
import marf.*;

import java.io.*;

/**
 * <p>Class Endpoint</p>
 * <p>Not Implemented</p>
 * <p>$Header: /cvsroot/marf/marf/src/marf/Preprocessing/Endpoint/Endpoint.java,v 1.7 2003/01/26 06:10:47 mokhov Exp $</p>
 */
public class Endpoint extends Preprocessing
{
	/**
	 * Endpoint Constructor
	 * @param poSample incoming sample
	 */
	public Endpoint(Sample poSample)
	{
		super(poSample);
	}

	/**
	 * Not Implemented
	 * @exception PreprocessingException
	 */
	public boolean preprocess() throws PreprocessingException
	{
		throw new NotImplementedException("Endpoint.preprocess()");
	}

	/**
	 * Not Implemented
	 * @exception PreprocessingException
	 */
	public boolean removeNoise() throws PreprocessingException
	{
		throw new NotImplementedException("Endpoint.removeNoise()");
	}

	/**
	 * Not Implemented
	 * @exception PreprocessingException
	 */
	public boolean removeSilence() throws PreprocessingException
	{
		throw new NotImplementedException("Endpoint.removeSilence()");
	}

	/**
	 * Not Implemented
	 * @exception PreprocessingException
	 */
	public boolean cropAudio(double pdStartingFrequency, double pdEndFrequency)  throws PreprocessingException
	{
		throw new NotImplementedException("Endpoint.cropAudio()");
	}

	/* From Storage Manager */

	/**
	 * Not Implemented
	 * @exception IOException
	 */
	public void dump() throws java.io.IOException
	{
		throw new NotImplementedException("Endpoint.dump()");
	}

	/**
	 * Not Implemented
	 * @exception IOException
	 */
	public void restore() throws java.io.IOException
	{
		throw new NotImplementedException("Endpoint.restore()");
	}
}

// EOF
