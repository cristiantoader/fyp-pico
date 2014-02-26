package marf.Classification.Distance;

import marf.*;
import marf.Classification.*;
import marf.FeatureExtraction.*;
import marf.Storage.*;
import marf.util.*;

import java.io.*;
import java.util.*;

/**
 * Class MahalanobisDistance
 * <p><b>NOTE</b>: Implemented as equivalent to Euclidean Distance in 0.2.0, i.e. the Covariance matrix is always an Indentity one</p>
 * <p>$Header: /cvsroot/marf/marf/src/marf/Classification/Distance/MahalanobisDistance.java,v 1.5.2.1 2003/02/16 18:08:54 mokhov Exp $</p>
 * @since 0.2.0
 */
public class MahalanobisDistance extends Distance
{
	/**
	 * Covariance Matrix
	 */
	private double[][] C = null;

	/**
	 * MahalanobisDistance Constructor
	 * @param poFeatureExtraction FeatureExtraction module reference
	 */
	public MahalanobisDistance(FeatureExtraction poFeatureExtraction)
	{
		super(poFeatureExtraction);

		int d = this.oFeatureExtraction.getFeaturesArray().length;
		C = new double[d][d];

		/*
		 * Make the default an indetity matrix rendering it at least
		 * equivalent to Euclidean distance. Will be moved elsewhere
		 * in 0.3.0
		 */
		 for(int i = 0; i < d; i++)
		 	C[i][i] = 1;
	}

	/**
	 * Partial MahalanobisDistance implementation
	 * @param paVector1 first vector to compare
	 * @param paVector2 second vector to compare
	 * @return Mahalanobis distance between two feature vectors
	 */
	public final double distance(final double[] paVector1, final double[] paVector2)
	{
		MARF.debug
		(
			"MahalanobisDistance.distance() - WARNING: in 0.2.0 Mahalanobis distance is equivalent to Euclidean."
		);

		double dDistance = 0;

		double[] adCovariedVector = new double[paVector1.length];

		// XXX: Temp solution for identity matrix, so no need to do the
		// inverse. This will change in 0.3.0
		for(int i = 0; i < paVector1.length; i++)
			for(int j = 0; j < paVector1.length; j++)
				adCovariedVector[i] += paVector1[j] * C[i][j];

		for(int f = 0; f < paVector1.length; f++)
			dDistance += (adCovariedVector[f] - paVector2[f]) * (adCovariedVector[f] - paVector2[f]);

		return dDistance;
	}
}

// EOF
