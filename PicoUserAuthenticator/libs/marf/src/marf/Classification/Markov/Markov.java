package marf.Classification.Markov;

import marf.Classification.*;
import marf.FeatureExtraction.*;
import marf.Storage.*;
import marf.util.*;

import java.io.*;

/**
 * Class Markov
 * <p>Not Implemented</p>
 * <p>$Header: /cvsroot/marf/marf/src/marf/Classification/Markov/Markov.java,v 1.11.2.1 2003/02/16 18:08:54 mokhov Exp $</p>
 */
public class Markov extends Classification
{
	/**
	 * Markov Constructor
	 * @param poFeatureExtraction FeatureExtraction module reference
	 */
	public Markov(FeatureExtraction poFeatureExtraction)
	{
		super(poFeatureExtraction);
	}

	/**
	 * Not Implemented
	 */
	public boolean classify() throws ClassificationException
	{
		throw new NotImplementedException("Markov.classify()");
	}

	/**
	 * Not Implemented
	 */
	public boolean train() throws ClassificationException
	{
		throw new NotImplementedException("Markov.train()");
	}

	/* From Storage Manager */

	/**
	 * Not Implemented
	 */
	public void dump() throws java.io.IOException
	{
		throw new NotImplementedException("Markov.dump()");
	}

	/**
	 * Not Implemented
	 */
	public void restore() throws java.io.IOException
	{
		throw new NotImplementedException("Markov.restore()");
	}
}

// EOF
