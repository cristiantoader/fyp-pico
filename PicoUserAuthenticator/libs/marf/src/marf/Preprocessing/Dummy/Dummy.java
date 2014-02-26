package marf.Preprocessing.Dummy;

import marf.Preprocessing.*;
import marf.Storage.*;
import marf.util.*;
import marf.*;

import java.io.*;

/**
 * Class Dummy
 * <p>Implements dummy preprocessing module for testing purposes</p>
 *
 * <p>$Header: /cvsroot/marf/marf/src/marf/Preprocessing/Dummy/Dummy.java,v 1.9 2003/01/26 06:10:47 mokhov Exp $</p>
 */
public class Dummy extends Preprocessing
{
	/**
	 * Dummy Constructor
	 * @param poSample incomping sample
	 */
	public Dummy(Sample poSample)
	{
		super(poSample);
	}

	/**
	 * Dummy implementation of preprocess() for testing.
	 * @return true
	 */
	public final boolean preprocess() throws PreprocessingException
	{
		MARF.debug("Dummy.preprocess()");
		return true;
	}

	/**
	 * Dummy implementation of removeNoise() for testing.
	 * @return <code>false</code>
	 * @exception PreprocessingException
	 */
	public final boolean removeNoise() throws PreprocessingException
	{
		MARF.debug("Dummy.removeNoise()");
		return false;
	}

	/**
	 * Dummy implementation of removeSilence() for testing.
	 * @return <code>false</code>
	 * @exception PreprocessingException
	 */
	public final boolean removeSilence() throws PreprocessingException
	{
		MARF.debug("Dummy.removeSilence()");
		return false;
	}

	/**
	 * Dummy implementation of cropAudio() for testing.
	 * @return <code>false</code>
	 * @exception PreprocessingException
	 */
	public final boolean cropAudio(double pdStartingFrequency, double pdEndFrequency) throws PreprocessingException
	{
		MARF.debug("Dummy.cropAudio()");
		return false;
	}

	/* From Storage Manager */

	/**
	 * Dummy implementation of dump() for testing.
	 * @exception IOException
	 */
	public final void dump() throws java.io.IOException
	{
		MARF.debug("Dummy.dump()");
	}

	/**
	 * Dummy implementation of restore() for testing.
	 * @exception IOException
	 */
	public final void restore() throws java.io.IOException
	{
		MARF.debug("Dummy.restore()");
	}
}

// EOF
