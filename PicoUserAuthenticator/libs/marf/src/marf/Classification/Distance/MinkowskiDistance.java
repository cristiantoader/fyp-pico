package marf.Classification.Distance;

import marf.*;
import marf.Classification.*;
import marf.FeatureExtraction.*;
import marf.Storage.*;
import marf.util.*;

import java.io.*;
import java.util.*;

/**
 * Class MinkowskiDistance
 * <p>$Header: /cvsroot/marf/marf/src/marf/Classification/Distance/MinkowskiDistance.java,v 1.3 2003/01/14 05:10:31 mokhov Exp $</p>
 * @since 0.2.0
 */
public class MinkowskiDistance extends Distance
{
	/**
	 * Minkowski Factor. Default 3; r = 1 is Chebyshev distance, r = 2 is Euclidean distance
	 */
	private double r = 3;

	/**
	 * MinkowskiDistance Constructor
	 * @param poFeatureExtraction FeatureExtraction module reference
	 */
	public MinkowskiDistance(FeatureExtraction poFeatureExtraction)
	{
		super(poFeatureExtraction);

		// See if there is a request for another r
		if(MARF.getModuleParams() != null)
		{
			Vector oParams = MARF.getModuleParams().getClassificationParams();

			if(oParams.size() > 1)
				this.r = ((Double)oParams.elementAt(1)).doubleValue();
		}
	}

	/**
	 * MinkowskiDistance implementation
	 * @param paVector1 first vector to compare
	 * @param paVector2 second vector to compare
	 * @return Minkowski distance between two feature vectors
	 */
	public final double distance(final double[] paVector1, final double[] paVector2)
	{
		double dDistance = 0;

		for(int f = 0; f < paVector1.length; f++)
			dDistance += Math.pow(Math.abs(paVector1[f] - paVector2[f]), this.r);

		return Math.pow(dDistance, 1 / this.r);
	}
}

// EOF
