package marf.Classification;

import marf.*;
import marf.Storage.*;
import marf.FeatureExtraction.*;

import java.io.*;
import java.util.*;

/**
 * Class Classification
 * <p>Abstract Classification Module</p>
 * <p>$Header: /cvsroot/marf/marf/src/marf/Classification/Classification.java,v 1.16.2.1 2003/02/16 18:08:53 mokhov Exp $</p>
 */
public abstract class Classification implements StorageManager
{
	/* Data Members */

	/**
	 * Reference to the FeatureExtraction object
	 */
	protected FeatureExtraction oFeatureExtraction = null;

	/**
	 * TrainingSet Container
	 */
	protected TrainingSet oTrainingSet = null;

	/**
	 * Classification Result
	 */
	protected Result oResult = null;

	/**
	 * Indicates in which format dump training data.
	 * <p>Can either be <code>TrainingSet.DUMP_GZIP_BINARY</code> or <code>TrainingSet.DUMP_CSV_TEXT</code>, with the former being the default.</p>
	 * @since 0.2.0
	 */
	private int iDumpMode = TrainingSet.DUMP_GZIP_BINARY;

	/* Constructors */

	/**
	 * Generic Classification Constructor
	 * @param poFeatureExtraction FeatureExtraction module reference
	 */
	protected Classification(FeatureExtraction poFeatureExtraction)
	{
		// XXX null validation?
		this.oFeatureExtraction = poFeatureExtraction;

		// See if there is a request for dump format
		if(MARF.getModuleParams() != null)
		{
			Vector oParams = MARF.getModuleParams().getClassificationParams();

			// XXX Must be validated of what's coming in
			if(oParams != null && oParams.size() > 0)
				this.iDumpMode = ((Integer)oParams.elementAt(0)).intValue();
		}
	}

	/* Classification API */

	/**
	 * Generic classification routine.
	 * @return <code>true</code> if classification was successful; <code>false</code> otherwise
	 * @exception ClassificationException
	 */
	public abstract boolean classify() throws ClassificationException;

	/**
	 * Generic training routine for building/updating mean vectors in the training set.
	 * Can be overriden, and if overriding classifier is using TrainingSet, it should call <code>super.train();</code>
	 * @return <code>true</code> if training successful (i.e. mean vector was updated), <code>false</code> otherwise
	 * @exception ClassificationException
	 */
	public boolean train() throws ClassificationException
	{
		// For exception handling
		String strPhase = "[start]";

		try
		{
			if(this.oTrainingSet != null)
				// Wrong global cluster loaded, reload the correct one.
				if
				(
					(oTrainingSet.getPreprocessingMethod() != MARF.getPreprocessingMethod())
					||
					(oTrainingSet.getFeatureExtractionMethod() != MARF.getFeatureExtractionMethod())
				)
				{
					strPhase = "[dumping previous cluster]";

					saveTrainingSet();
					this.oTrainingSet = null;
				}

			strPhase = "[restoring training set]";
			loadTrainingSet();

			// Add the new feature vector
			strPhase = "[adding feature vector]";
			boolean bVectorAdded = this.oTrainingSet.addFeatureVector
			(
				this.oFeatureExtraction.getFeaturesArray(),
				MARF.getSampleFile(),
				MARF.getCurrentSubject(),
				MARF.getPreprocessingMethod(),
				MARF.getFeatureExtractionMethod()
			);

			if(bVectorAdded)
			{
				strPhase = "[dumping updated training set]";
				saveTrainingSet();
			}

			return true;
		}
/*		catch(FileNotFoundException e)
		{
			MARF.debug("Classification.train() -- File not found: " + e.getMessage());
		}
*/		catch(NullPointerException e)
		{
			throw new ClassificationException
			(
				"NullPointerException in Classification.train(): oTrainingSet = " + this.oTrainingSet +
				", oFeatureExtraction = " + this.oFeatureExtraction +
				", FeaturesArray = " + this.oFeatureExtraction.getFeaturesArray() +
				", phase: " + strPhase
			);
		}
		catch(Exception e)
		{
			throw new ClassificationException(e.getMessage());
		}
	}

	/* From Storage Manager */

	/**
	 * Generic implementation of dump() for TrainingSet
	 * @since 0.2.0
	 * @exception IOException
	 */
	public void dump() throws java.io.IOException
	{
		this.saveTrainingSet();
	}

	/**
	 * Generic implementation of restore() for TrainingSet
	 * @since 0.2.0
	 * @exception IOException
	 */
	public void restore() throws java.io.IOException
	{
		this.loadTrainingSet();
	}

	/**
	 * Saves TrainingSet to a file. Called by dump()
	 * @since 0.2.0
	 * @exception IOException
	 */
	private final void saveTrainingSet() throws java.io.IOException
	{
		// Dump stuff is there's anything to dump
		if(this.oTrainingSet != null)
		{
			this.oTrainingSet.setDumpMode(this.iDumpMode);
			this.oTrainingSet.setTrainingSetFile(getTrainingSetFilename());
			this.oTrainingSet.dump();
		}
	}

	/**
	 * Loads TrainingSet from a file. Called by <code>restore()</code>
	 * @since 0.2.0
	 * @exception IOException
	 */
	private final void loadTrainingSet() throws java.io.IOException
	{
		if(oTrainingSet == null)
		{
			this.oTrainingSet = new TrainingSet();
			this.oTrainingSet.setDumpMode(this.iDumpMode);
			this.oTrainingSet.setTrainingSetFile(getTrainingSetFilename());
			this.oTrainingSet.restore();
		}
	}

	/**
	 * Retrieves the classification result
	 * @return Result object
	 */
	public final Result getResult()
	{
		return this.oResult;
	}

	/**
	 * Constructs global cluster finlename for the TrainingSet.
	 * <p>Filename constructed using fully-qualified class of
	 * either TrainingSet or a classifier name with global
	 * clustering info such as preprocessing and feature
	 * extraction methods, so that ony that cluster can be reloaded
	 * after.</p>
	 * @return String, filename
	 * @since 0.2.0
	 */
	private final String getTrainingSetFilename()
	{
		return new String
		(
			// Fully qualified class name
			this.oTrainingSet.getClass().getName() + "." +

			// Global cluster: <PR>.<FE>.<FVS>
			// For the same FE method we may have different feature vector sizes
			MARF.getPreprocessingMethod() + "." +
			MARF.getFeatureExtractionMethod() + "." +
			this.oFeatureExtraction.getFeaturesArray().length +

			// Extension depending on the dump type
			(this.iDumpMode == TrainingSet.DUMP_GZIP_BINARY ? ".bin" : ".csv")
		);
	}
}

// EOF
