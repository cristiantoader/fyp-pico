package marf.Classification.Distance;

import marf.*;
import marf.Classification.*;
import marf.FeatureExtraction.*;
import marf.Storage.*;
import marf.util.*;

import java.io.*;
import java.util.*;

/**
 * Class ChebyshevDistance
 * <p>$Header: /cvsroot/marf/marf/src/marf/Classification/Distance/ChebyshevDistance.java,v 1.6.2.1 2003/02/16 18:08:53 mokhov Exp $</p>
 */
public class ChebyshevDistance extends Distance
{
	/**
	 * ChebyshevDistance Constructor
	 * @param poFeatureExtraction FeatureExtraction module reference
	 */
	public ChebyshevDistance(FeatureExtraction poFeatureExtraction)
	{
		super(poFeatureExtraction);
	}

	/**
	 * ChebyshevDistance implementation
	 * @param paVector1 first vector to compare
	 * @param paVector2 second vector to compare
	 * @return Chebyshev (a.k.a city-block/Manhattan) distance between two feature vectors
	 */
	public final double distance(final double[] paVector1, final double[] paVector2)
	{
		double dDistance = 0;

		for(int f = 0; f < paVector1.length; f++)
			dDistance += Math.abs(paVector1[f] - paVector2[f]);

		return dDistance;
	}
}

// EOF
