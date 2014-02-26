package marf.Storage;

import marf.*;

import java.util.*;

/**
 * <p>Class ModuleParams</p>
 * <p>Provides ability to pass module-specific parameters from an application.</p>
 * <p>The specific module should know in which order and how to downcast those params.</p>
 */
public class ModuleParams
{
	/*
	 * ---------------------
	 * Data members
	 * ---------------------
	 */

	/**
	 * A Vector of preprocessing parameters.
	 */
	private Vector oPreprocessingParams = new Vector();

	/**
	 * A Vector of feature extraction parameters.
	 */
	private Vector oFeatureExtraction   = new Vector();

	/**
	 * A Vector of classification parameters.
	 */
	private Vector oClassification      = new Vector();


	/*
	 * ---------------------
	 * Enumeraion
	 * ---------------------
	 */

	/**
	 * Indicates that we manipulate on the Preprocessing Vector
	 */
	private static final int PREPROCESSING      = 0;

	/**
	 * Indicates that we manipulate on the Feature Extraction Vector
	 */
	private static final int FEATURE_EXTRACTION = 1;

	/**
	 * Indicates that we manipulate on the Classification Vector
	 */
	private static final int CLASSIFICATION     = 2;


	/*
	 * ---------------------
	 * Methods
	 * ---------------------
	 */

	/**
	 * Default Constructor
	 */
	public ModuleParams()
	{
	}

	/**
	 * Generic set*() method. Overwrites any possible previous value for a given module with new
	 * @param oParams Vector of paramaters
	 * @param piModule The modules these params are for
	 */
	private final void setParams(Vector oParams, final int piModule)
	{
		switch(piModule)
		{
			case PREPROCESSING:
				this.oPreprocessingParams = oParams;
				break;

			case FEATURE_EXTRACTION:
				this.oFeatureExtraction = oParams;
				break;

			case CLASSIFICATION:
				this.oClassification = oParams;
				break;

			default:
				MARF.debug("ModuleParams.setParams() - Unknown module type: " + piModule);
		}
	}

	/**
	 * Generic add*() method. Appends params vector to whatever there possibly was.
	 * @param oParams Vector of paramaters
	 * @param piModule the module these parameters are for
	 */
	private final void addParams(Vector oParams, final int piModule)
	{
		switch(piModule)
		{
			case PREPROCESSING:
				this.oPreprocessingParams.addAll(oParams);
				break;

			case FEATURE_EXTRACTION:
				this.oFeatureExtraction.addAll(oParams);
				break;

			case CLASSIFICATION:
				this.oClassification.addAll(oParams);
				break;

			default:
				MARF.debug("ModuleParams.addParam() - Unknown module type: " + piModule);
		}
	}

	/**
	 * Generic addParam() method. Adds a single object to the corresponding Vector.
	 * @param oParam Parameter object
	 * @param piModule the module this parameter is for
	 */
	private final void addParam(Object oParam, final int piModule)
	{
		switch(piModule)
		{
			case PREPROCESSING:
				this.oPreprocessingParams.add(oParam);
				break;

			case FEATURE_EXTRACTION:
				this.oFeatureExtraction.add(oParam);
				break;

			case CLASSIFICATION:
				this.oClassification.add(oParam);
				break;

			default:
				MARF.debug("ModuleParams.addParam() - Unknown module type: " + piModule);
		}
	}

	/**
	 * Generic get*() method. Returns for a given module it's parameters vector
	 * @return Vector of paramaters
	 */
	private final Vector getParams(final int piModule)
	{
		switch(piModule)
		{
			case PREPROCESSING:
				return this.oPreprocessingParams;

			case FEATURE_EXTRACTION:
				return this.oFeatureExtraction;

			case CLASSIFICATION:
				return this.oClassification;

			default:
				MARF.debug("ModuleParams.getParams() - Unknown module type: " + piModule);
		}

		// Should never reach here, but 1.2.* compilers complain
		return null;
	}

	/**
	 * @return preprocessing parameters vector
	 */
	public final Vector getPreprocessingParams()
	{
		return getParams(PREPROCESSING);
	}

	/**
	 * Sets preprocessing parameters vector
	 * @param oParams parameters vector
	 */
	public final void setPreprocessingParams(Vector oParams)
	{
		setParams(oParams, PREPROCESSING);
	}

	/**
	 * Adds (appends) preprocessing parameters vector
	 * @param oParams parameters vector to append
	 */
	public final void addPreprocessingParams(Vector oParams)
	{
		addParams(oParams, PREPROCESSING);
	}

	/**
	 * Adds (appends) a single preprocessing parameter object
	 * @param oParam object to append
	 */
	public final void addPreprocessingParam(Object oParam)
	{
		addParam(oParam, PREPROCESSING);
	}

	/**
	 * @return feature extraction parameters vector
	 */
	public final Vector getFeatureExtractionParams()
	{
		return getParams(FEATURE_EXTRACTION);
	}

	/**
	 * Sets feature extraction parameters vector
	 * @param oParams parameters vector
	 */
	public final void setFeatureExtractionParams(Vector oParams)
	{
		setParams(oParams, FEATURE_EXTRACTION);
	}

	/**
	 * Adds (appends) feature extraction parameters vector
	 * @param oParams parameters vector to append
	 */
	public final void addFeatureExtractionParams(Vector oParams)
	{
		addParams(oParams, FEATURE_EXTRACTION);
	}

	/**
	 * Adds (appends) a single feature extraction parameter object
	 * @param oParam object to append
	 */
	public final void addFeatureExtractionParam(Object oParam)
	{
		addParam(oParam, FEATURE_EXTRACTION);
	}

	/**
	 * @return classification parameters vector
	 */
	public final Vector getClassificationParams()
	{
		return getParams(CLASSIFICATION);
	}

	/**
	 * Sets classification parameters vector
	 * @param oParams parameters vector
	 */
	public final void setClassificationParams(Vector oParams)
	{
		setParams(oParams, CLASSIFICATION);
	}

	/**
	 * Adds (appends) classification parameters vector
	 * @param oParams parameters vector to append
	 */
	public final void addClassificationParams(Vector oParams)
	{
		addParams(oParams, CLASSIFICATION);
	}

	/**
	 * Adds (appends) a single classification parameter object
	 * @param oParam object to append
	 */
	public final void addClassificationParam(Object oParam)
	{
		addParam(oParam, CLASSIFICATION);
	}
}

// EOF
