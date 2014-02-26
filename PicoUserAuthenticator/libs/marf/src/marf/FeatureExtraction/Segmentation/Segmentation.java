package marf.FeatureExtraction.Segmentation;

import marf.util.*;
import marf.Storage.*;
import marf.Preprocessing.*;
import marf.FeatureExtraction.*;

import java.io.*;


/**
 * Class Segmentation
 * <p>$Header: /cvsroot/marf/marf/src/marf/FeatureExtraction/Segmentation/Segmentation.java,v 1.6.2.1 2003/02/16 18:26:47 mokhov Exp $</p>
 */
public class Segmentation extends FeatureExtraction
{
	/**
	 * Segmentation Constructor
	 * @param poPreprocessing Preprocessing object reference
	 */
	public Segmentation(Preprocessing poPreprocessing)
	{
		super(poPreprocessing);
	}

	/**
	 * Not Implemented
	 */
	public final boolean extractFeatures() throws FeatureExtractionException
	{
		throw new NotImplementedException("Segmentation.extractFeatures()");
	}

	/* From Storage Manager */

	/**
	 * Not Implemented
	 */
	public void dump() throws java.io.IOException
	{
		throw new NotImplementedException("Segmentation.dump()");
	}

	/**
	 * Not Implemented
	 */
	public void restore() throws java.io.IOException
	{
		throw new NotImplementedException("Segmentation.restore()");
	}
}

// EOF
