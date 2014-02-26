package marf.Classification.Distance;

import marf.*;
import marf.Classification.*;
import marf.FeatureExtraction.*;
import marf.Storage.*;
import marf.util.*;

import java.io.*;
import java.util.*;

/**
 * Class Distance
 * <p>Abstract Distance Classifier</p>
 * <p>$Header: /cvsroot/marf/marf/src/marf/Classification/Distance/Distance.java,v 1.12.2.1 2003/02/16 18:08:54 mokhov Exp $</p>
 */
public abstract class Distance extends Classification
{
	/**
	 * Distance Constructor
	 * @param poFeatureExtraction FeatureExtraction module reference
	 */
	public Distance(FeatureExtraction poFeatureExtraction)
	{
		super(poFeatureExtraction);
	}

	/**
	 * Classify the feature vector based on whatever <code>distance()</code> derivatives implement.
	 * @return true if classification successful whatever that means
	 */
	public final boolean classify() throws ClassificationException
	{
		try
		{
			// Features of the incoming sample
			double[] adIncomingFeatures = oFeatureExtraction.getFeaturesArray();

			restore();

			// Features in the training set
			Vector oTrainingSamples = this.oTrainingSet.getTrainingSamples();

			// These data will become part of the Result
			int    iSubjectID       = 0;
			double dMinDistance     = Double.MAX_VALUE;
			Vector oSubjectDistance = new Vector();

			/*
			 * Run through the stored training samples set (mean vetors)
			 * and determine the two closest subjects to the incoming features sample
			 */
			for(int i = 0; i < oTrainingSamples.size(); i++)
			{
				TrainingSet.TrainingSample oTrainingSample = (TrainingSet.TrainingSample)oTrainingSamples.get(i);

				double[] adMeanVector = oTrainingSample.getMeanVector();

				// Sanity check: stored mean vector must never be null
				if(adMeanVector == null)
					throw new ClassificationException
					(
						"Distance.clasify() - Stored mean vector is null for subject (" + oTrainingSample.getSubjectID() +
						", preprocessing method: " + this.oTrainingSet.getPreprocessingMethod() +
						", feature extraction methods: "  + this.oTrainingSet.getFeatureExtractionMethod()
					);

				// Sanity check: vectors must be of the same length
				if(adMeanVector.length != adIncomingFeatures.length)
					throw new ClassificationException
					(
						"Distance.clasify() - Mean vector length (" + adMeanVector.length +
						") is not same as of incoming feature vector (" + adIncomingFeatures.length + ")"
					);

				/*
				 * We have a mean vector of the samples for this iCurrentSubjectID
				 * Compare using whatever distance classifier it is...
				 */
				double dCurrentDistance = this.distance(adMeanVector, adIncomingFeatures);

				MARF.debug("Distance for subject " + oTrainingSample.getSubjectID() + " = " + dCurrentDistance);

				// XXX: What should we do in this (very rare and subtle) case?
				if(dCurrentDistance == dMinDistance)
					MARF.debug("This distance had happened before!");

				if(dCurrentDistance < dMinDistance)
				{
					dMinDistance = dCurrentDistance;
					iSubjectID   = oTrainingSample.getSubjectID();
				}

				// Collect for stats
				// XXX: Move to StatsCollector
				Vector oIDDistancePair = new Vector();
				oIDDistancePair.add(new Integer(oTrainingSample.getSubjectID()));
				oIDDistancePair.add(new Double(dCurrentDistance));

				oSubjectDistance.add(oIDDistancePair);
			}

			this.oResult = new Result(iSubjectID, oSubjectDistance);

			return true;
		}
		catch(IOException e)
		{
			throw new ClassificationException(e.getMessage());
		}
	}

	/**
	 * Generic distance routine. To be overriden.
	 * @return distance between two feature vectors
	 */
	public abstract double distance(final double[] paVector1, final double[] paVector2);

}

// EOF
