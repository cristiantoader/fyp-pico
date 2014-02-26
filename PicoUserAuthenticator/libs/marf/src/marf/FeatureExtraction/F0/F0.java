package marf.FeatureExtraction.F0;

import marf.util.*;
import marf.Storage.*;
import marf.Preprocessing.*;
import marf.FeatureExtraction.*;

import java.io.*;

/**
 * Class F0
 * <p>$Header: /cvsroot/marf/marf/src/marf/FeatureExtraction/F0/F0.java,v 1.6.2.1 2003/02/16 18:26:46 mokhov Exp $</p>
 */
public class F0 extends FeatureExtraction
{
	/**
	 * F0 Constructor
	 * @param poPreprocessing Preprocessing module reference
	 */
	public F0(Preprocessing poPreprocessing)
	{
		super(poPreprocessing);
	}

	/**
	 * Not Implemented
	 */
	public final boolean extractFeatures() throws FeatureExtractionException
	{
		throw new NotImplementedException("F0.extractFeatures()");
	}

	/* From Storage Manager */

	/**
	 * Not Implemented
	 */
	public void dump() throws java.io.IOException
	{
		throw new NotImplementedException("F0.dump()");
	}

	/**
	 * Not Implemented
	 */
	public void restore() throws java.io.IOException
	{
		throw new NotImplementedException("F0.restore()");
	}
}

// EOF
