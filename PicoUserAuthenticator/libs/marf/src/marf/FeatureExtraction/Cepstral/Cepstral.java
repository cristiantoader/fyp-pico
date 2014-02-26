package marf.FeatureExtraction.Cepstral;

import marf.util.*;
import marf.Storage.*;
import marf.Preprocessing.*;
import marf.FeatureExtraction.*;

import java.io.*;


/**
 * Class Cepstral
 * <p>$Header: /cvsroot/marf/marf/src/marf/FeatureExtraction/Cepstral/Cepstral.java,v 1.4.2.1 2003/02/16 18:26:46 mokhov Exp $</p>
 */
public class Cepstral extends FeatureExtraction
{
	/**
	 * Cepstral Constructor
	 * @param poPreprocessing Preprocessing module reference
	 */
	public Cepstral(Preprocessing poPreprocessing)
	{
		super(poPreprocessing);
	}

	/**
	 * Not Implemented
	 */
	public final boolean extractFeatures() throws FeatureExtractionException
	{
		throw new NotImplementedException("Cepstral.extractFeatures()");
	}

	/* From Storage Manager */

	/**
	 * Not Implemented
	 */
	public void dump() throws java.io.IOException
	{
		throw new NotImplementedException("Cepstral.dump()");
	}

	/**
	 * Not Implemented
	 */
	public void restore() throws java.io.IOException
	{
		throw new NotImplementedException("Cepstral.restore()");
	}
}

// EOF
