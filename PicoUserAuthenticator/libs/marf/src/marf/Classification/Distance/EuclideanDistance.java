package marf.Classification.Distance;

import marf.*;
import marf.Classification.*;
import marf.FeatureExtraction.*;
import marf.Storage.*;
import marf.util.*;

import java.io.*;
import java.util.*;

/**
 * <p>Class EuclideanDistance</p>
 * <p>$Header: /cvsroot/marf/marf/src/marf/Classification/Distance/EuclideanDistance.java,v 1.13 2003/01/29 18:58:37 mokhov Exp $</p>
 */
public class EuclideanDistance extends Distance
{
	/**
	 * EuclideanDistance Constructor
	 * @param poFeatureExtraction FeatureExtraction module reference
	 */
	public EuclideanDistance(FeatureExtraction poFeatureExtraction)
	{
		super(poFeatureExtraction);
	}

	/**
	 * EuclideanDistance implementation
	 * @param paVector1 first vector to compare
	 * @param paVector2 second vector to compare
	 * @return Euclidean distance between two feature vectors
	 */
	public final double distance(final double[] paVector1, final double[] paVector2)
	{
		double dDistance = 0;

		for(int f = 0; f < paVector1.length; f++)
			dDistance += (paVector1[f] - paVector2[f]) * (paVector1[f] - paVector2[f]);

		return dDistance;
	}
}

// EOF
