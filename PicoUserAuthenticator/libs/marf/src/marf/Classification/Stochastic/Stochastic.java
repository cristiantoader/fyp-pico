package marf.Classification.Stochastic;

import marf.util.*;
import marf.Storage.*;
import marf.Classification.*;
import marf.FeatureExtraction.*;

import java.io.*;

/**
 * Class Stochastic
 * <p>Not Implemented</p>
 * <p>$Header: /cvsroot/marf/marf/src/marf/Classification/Stochastic/Stochastic.java,v 1.10 2003/01/25 21:00:27 mokhov Exp $</p>
 */
public class Stochastic extends Classification
{
	/**
	 * Stochastic Constructor
	 * @param poFeatureExtraction FeatureExtraction module reference
	 */
	public Stochastic(FeatureExtraction poFeatureExtraction)
	{
		super(poFeatureExtraction);
	}

	/**
	 * Not Implemented
	 */
	public boolean classify() throws ClassificationException
	{
		throw new NotImplementedException("Stochastic.classify()");
	}

	/**
	 * Not Implemented
	 */
	public boolean train() throws ClassificationException
	{
		throw new NotImplementedException("Stochastic.train()");
	}

	/* From Storage Manager */

	/**
	 * Not Implemented
	 */
	public void dump() throws IOException
	{
		throw new NotImplementedException("Stochastic.dump()");
	}

	/**
	 * Not Implemented
	 */
	public void restore() throws IOException
	{
		throw new NotImplementedException("Stochastic.restore()");
	}
}

// EOF
