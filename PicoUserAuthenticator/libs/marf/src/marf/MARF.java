/*
 * The MARF "Server"
 */

package marf;

import marf.util.*;
import marf.gui.*;

import marf.Storage.*;
import marf.Storage.Loaders.*;

import marf.Preprocessing.*;
import marf.Preprocessing.FFTFilter.*;
import marf.Preprocessing.Dummy.*;
import marf.Preprocessing.Endpoint.*;

import marf.FeatureExtraction.*;
import marf.FeatureExtraction.LPC.*;
import marf.FeatureExtraction.FFT.*;
import marf.FeatureExtraction.F0.*;
import marf.FeatureExtraction.Segmentation.*;
import marf.FeatureExtraction.Cepstral.*;
import marf.FeatureExtraction.RandomFeatureExtraction.*;

import marf.Classification.*;
import marf.Classification.NeuralNetwork.*;
import marf.Classification.Stochastic.*;
import marf.Classification.Markov.*;
import marf.Classification.Distance.*;
import marf.Classification.RandomClassification.*;

import java.io.*;

/**
 * Class MARF
 * <p>Implements a so-called MARF server.</p>
 * <p>Provides basic recognition pipeline and its configuration.</p>
 * <p>$Header: /cvsroot/marf/marf/src/marf/MARF.java,v 1.51.2.1 2003/02/16 17:48:48 mokhov Exp $</p>
 * @author The MARF Development Group
 */
public class MARF
{
	/*
	 * --------------------------------------------------------
	 * General
	 * --------------------------------------------------------
	 */

	/**
	 * Debug flag.
	 * <p>if is set to <code>true</code>, prompts debug() method to output to STDERR</p>
	 */
	public static boolean DEBUG = false;

	/**
	 * Value indecating that some configuration param is not set.
	 */
	private static final int UNSET = -1;


	/*
	 * --------------------------------------------------------
	 * Preprocessing Modules Enumeration
	 * --------------------------------------------------------
	 */

	/**
	 * Indicates to use Dummy preprocessing module (just normalization)
	 */
	public static final int DUMMY                           = 0;

	/**
	 * Indicates to use filter boosting high frequencies
	 */
	public static final int HIGH_FREQUENCY_BOOST_FFT_FILTER = 1;

	/**
	 * Indicates to use bandpass filter
	 */
	public static final int BANDPASS_FFT_FILTER             = 2;

	/**
	 * Indicates to use endpointing
	 */
	public static final int ENDPOINT                        = 3;

	/**
	 * Indicates to use low pass FFT filter
	 */
	public static final int LOW_PASS_FFT_FILTER             = 4;

	/**
	 * Indicates to use high pass FFT filter
	 */
	public static final int HIGH_PASS_FFT_FILTER            = 5;


	/*
	 * --------------------------------------------------------
	 * Feature Exraction Modules Enumeration
	 * --------------------------------------------------------
	 */

	/**
	 * Indicates to use LPC
	 */
	public static final int LPC                       = 0;

	/**
	 * Indicates to use FFT
	 */
	public static final int FFT                       = 1;

	/**
	 * Indicates to use F0
	 */
	public static final int F0                        = 2;

	/**
	 * Indicates to use segmentation
	 */
	public static final int SEGMENTATION              = 3;

	/**
	 * Indicates to use cepstral analysis
	 */
	public static final int CEPSTRAL                  = 4;

	/**
	 * Indicates to use random feature extraction
	 * @since 0.2.0
	 */
	public static final int RANDOM_FEATURE_EXTRACTION = 5;


	/*
	 * --------------------------------------------------------
	 * Classification Modules Enumeration
	 * --------------------------------------------------------
	 */

	/**
	 * Indicates to use Neural Network for classification
	 */
	public static final int NEURAL_NETWORK        = 0;

	/**
	 * Indicates to use stochastic models for classification
	 */
	public static final int STOCHASTIC            = 1;

	/**
	 *  Indicates to use Hidden Markov Models for classification
	 */
	public static final int MARKOV                = 2;

	/**
	 * Indicates to use Euclidean distance for classification
	 */
	public static final int EUCLIDEAN_DISTANCE    = 3;

	/**
	 * Indicates to use Chebyshev distance for classification
	 */
	public static final int CHEBYSHEV_DISTANCE    = 4;

	/**
	 * Indicates to use Minkowski distance for classification
	 * @since 0.2.0
	 */
	public static final int MINKOWSKI_DISTANCE    = 5;

	/**
	 * Indicates to use Mahalanobis distance for classification
	 * @since 0.2.0
	 */
	public static final int MAHALANOBIS_DISTANCE  = 6;

	/**
	 * Indicates to use random classification
	 * @since 0.2.0
	 */
	public static final int RANDOM_CLASSIFICATION = 7;


	/*
	 * --------------------------------------------------------
	 * Supported Sample File Formats Enumeration
	 * --------------------------------------------------------
	 */

	/**
	 * Indicates WAV incoming sample file format
	 */
	public static final int WAV  = 0;

	/**
	 * Indicates MP3 incoming sample file format
	 */
	public static final int MP3  = 1;

	/**
	 * Indicates ULAW incoming sample file format
	 */
	public static final int ULAW = 2;


	/*
	 * --------------------------------------------------------
	 * Module Instance References
	 * --------------------------------------------------------
	 */

	/**
	 * Internal <code>Sample</code> reference
	 * @since 0.2.0
	 */
	private static Sample            oSample            = null;

	/**
	 * Internal <code>SampleLoader</code> reference
	 * @since 0.2.0
	 */
	private static SampleLoader      oSampleLoader      = null;

	/**
	 * Internal <code>Preprocessing</code> reference
	 * @since 0.2.0
	 */
	private static Preprocessing     oPreprocessing     = null;

	/**
	 * Internal <code>FeatureExtraction</code> reference
	 * @since 0.2.0
	 */
	private static FeatureExtraction oFeatureExtraction = null;

	/**
	 * Internal <code>Classification</code> reference
	 * @since 0.2.0
	 */
	private static Classification    oClassification    = null;


	/*
	 * --------------------------------------------------------
	 * Versionning
	 * --------------------------------------------------------
	 */

	/**
	 * Indicates major MARF version, like <b>1</b>.x.x
	 */
	private static final int MAJOR_VERSION = 0;

	/**
	 * Indicates minor MARF version, like 1.<b>1</b>.x
	 */
	private static final int MINOR_VERSION = 2;

	/**
	 * Indicates MARF revision, like 1.1.<b>1</b>
	 */
	private static final int REVISION      = 1;


	/*
	 * --------------------------------------------------------
	 * Current state of MARF
	 * --------------------------------------------------------
	 */

	/**
	 * Indicates what preprocessing method to use in the pipeline
	 */
	private static int          iPreprocessingMethod     = UNSET;

	/**
	 * Indicates what feature extraction method to use in the pipeline
	 */
	private static int          iFeatureExtractionMethod = UNSET;

	/**
	 * Indicates what classification method to use in the pipeline
	 */
	private static int          iClassificationMethod    = UNSET;

	/**
	 * Indicates what sample format is in use
	 */
	private static int          iSampleFormat            = UNSET;

	/**
	 * ID of the currently trained speaker
	 */
	private static int          iCurrentSubject          = UNSET;

	/**
	 * Indicates current incoming sample filename
	 */
	private static String       strFileName              = "";

	/**
	 * Indicates directory name with training samples
	 */
	private static String       strSamplesDir            = "";

	/**
	 * Stores the result of classification
	 */
	private static Result       oResult                  = null;

	/**
	 * Stores module-specific parameters in an independent way.
	 */
	private static ModuleParams oModuleParams            = null;

	/**
	 * Indicates whether or not to dump a spectrogram at the end of feature extraction
	 */
	private static boolean      bDumpSpectrogram         = false;

	/**
	 * Indicates whether or not to dump a wave graph
	 */
	private static boolean      bDumpWaveGraph           = false;


	/*
	 * --------------------------------------------------------
	 * Methods
	 * --------------------------------------------------------
	 */

	/**
	 * Must never be instantiated or inherited from... Or should it be allowed?
	 * <p><b>TODO:</b> The server part</p>
	 */
	private MARF()
	{
	}


	/*
	 * --------------------------------------------------------
	 * Setting/Getting MARF Configuration Parameters
	 * --------------------------------------------------------
	 */

	/**
	 * Sets preprocessing method to be used
	 * @param piPreprocessingMethod one of the allowed preprocessing methods
	 */
	public static final void setPreprocessingMethod(final int piPreprocessingMethod)
	{
		iPreprocessingMethod = piPreprocessingMethod;
	}

	/**
	 * Gets currently selected preprocessing method.
	 * @return one of the preprocessing methods
	 */
	public static final int getPreprocessingMethod()
	{
		return iPreprocessingMethod;
	}

	/**
	 * Sets feature extraction method to be used
	 * @param piFeatureExtractionMethod one of the allowed feature extraction methods
	 */
	public static final void setFeatureExtractionMethod(final int piFeatureExtractionMethod)
	{
		iFeatureExtractionMethod = piFeatureExtractionMethod;
	}

	/**
	 * Gets currently selected feature extraction method.
	 * @return current feature extraction method
	 */
	public static final int getFeatureExtractionMethod()
	{
		return iFeatureExtractionMethod;
	}

	/**
	 * Sets classification method to be used
	 * @param piClassificationMethod one of the allowed classification methods
	 */
	public static final void setClassificationMethod(final int piClassificationMethod)
	{
		iClassificationMethod = piClassificationMethod;
	}

	/**
	 * Gets classification method to be used
	 * @return current classification method
	 */
	public static final int getClassificationMethod()
	{
		return iClassificationMethod;
	}

	/**
	 * Sets input sample file format
	 * @param piSampleFormat one of the allowed sample formats
	 */
	public static final void setSampleFormat(final int piSampleFormat)
	{
		iSampleFormat = piSampleFormat;
	}

	/**
	 * Gets input sample file format
	 * @return current sample format
	 */
	public static final int getSampleFormat()
	{
		return iSampleFormat;
	}

	/**
	 * Sets input sample file name
	 * @param pstrFileName string representing sample file to be read
	 */
	public static final void setSampleFile(final String pstrFileName)
	{
		strFileName = pstrFileName;
	}

	/**
	 * Obtains filename of a sample currently being processed
	 * @return file name of a string representing sample file
	 */
	public static final String getSampleFile()
	{
		return strFileName;
	}

	/**
	 * Sets directory with sample files to be read from
	 * @param pstrSamplesDir string representing directory name
	 */
	public static final void setSamplesDir(final String pstrSamplesDir)
	{
		strSamplesDir = pstrSamplesDir;
	}

	/**
	 * Sets module-specific parameters an application programmer wishes to pass on to the module
	 * @param poModuleParams parameters' instance
	 */
	public static final void setModuleParams(final ModuleParams poModuleParams)
	{
		oModuleParams = poModuleParams;
	}

	/**
	 * Gets module-specific parameters an application programmer passed on to the module
	 * @return ModuleParams object reference
	 */
	public static final ModuleParams getModuleParams()
	{
		return oModuleParams;
	}

	/**
	 * Indicates whether spectrogram is wanted as an output of a FeatureExtraction module
	 * @param pbDump <code>true</code> if wanted, <code>false</code> if not
	 */
	public static final void setDumpSpectrogram(final boolean pbDump)
	{
		bDumpSpectrogram = pbDump;
	}

	/**
	 * Whether spectrogram wanted or not
	 * @return <code>true</code> if spectrogram being dumped, <code>false</code> otherwise
	 */
	public static final boolean getDumpSpectrogram()
	{
		return bDumpSpectrogram;
	}

	/**
	 * Indicates whether wave graph is wanted as an output
	 * @param bDump <code>true</code> if wanted, <code>false</code> if not
	 */
	public static final void setDumpWaveGraph(final boolean pbDump)
	{
		bDumpWaveGraph = pbDump;
	}

	/**
	 * Whether wave graph wanted or not
	 * @return <code>true</code> if graph wanted being dumped, <code>false</code> otherwise
	 */
	public static final boolean getDumpWaveGraph()
	{
		return bDumpWaveGraph;
	}

	/**
	 * Sets ID of a subject currently being trained on
	 * @param piSubjectID integer ID of the subject
	 */
	public static final void setCurrentSubject(final int piSubjectID)
	{
		iCurrentSubject = piSubjectID;
	}

	/**
	 * Gets ID of a subject currently being trained on
	 * @return integer ID of the subject
	 * @since 0.2.0
	 */
	public static final int getCurrentSubject()
	{
		return iCurrentSubject;
	}

	/**
	 * Returns a string representation of the MARF version
	 * @return version String
	 */
	public static final String getVersion()
	{
		return MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION;
	}

	/**
	 * Returns an integer representation of the MARF version
	 * @return integer version
	 */
	public static final int getIntVersion()
	{
		return MAJOR_VERSION * 100 + MINOR_VERSION * 10 + REVISION;
	}

	/**
	 * Returns a string representation of the current MARF configuration
	 * @return configuration string
	 */
	public static final String getConfig()
	{
		// TODO: more human-readable output
		return new String
		(
			"[" +
			"PR: " + iPreprocessingMethod +
			"FE: " + iFeatureExtractionMethod +
			"CL: " + iClassificationMethod +
			"ID: " + iCurrentSubject +
			"]"
		);
	}

	/**
	 * Retrieves current <code>Sample</code> reference
	 * @return Sample object
	 * @since 0.2.0
	 */
	public static final Sample getSample()
	{
		return oSample;
	}

	/**
	 * Retrieves current <code>SampleLoader</code> reference
	 * @return SampleLoader object
	 * @since 0.2.0
	 */
	public static final SampleLoader getSampleLoader()
	{
		return oSampleLoader;
	}

	/**
	 * Retrieves current <code>Preprocessing</code> reference
	 * @return Preprocessing object
	 * @since 0.2.0
	 */
	public static final Preprocessing getPreprocessing()
	{
		return oPreprocessing;
	}

	/**
	 * Retrieves current <code>FeatureExtraction</code> reference
	 * @return FeatureExtraction object
	 * @since 0.2.0
	 */
	public static final FeatureExtraction getFeatureExtraction()
	{
		return oFeatureExtraction;
	}

	/**
	 * Retrieves current <code>Classification</code> reference
	 * @return Classification object
	 * @since 0.2.0
	 */
	public static final Classification getClassification()
	{
		return oClassification;
	}

	/**
	 * Queries for the final classification result
	 * @return integer ID of the indentified subject
	 */
	public static final int queryResultID()
	{
		return oResult.getID();
	}

	/**
	 * Gets the entire Result object
	 * @return Result ID and all the stats of the classification
	 */
	public static final Result getResult()
	{
		return oResult;
	}

	/* API */

	/**
	 * Recognition/Identification mode
	 * @since 0.2.0
	 * @exception MARFException
	 */
	public static final void recognize() throws MARFException
	{
		try
		{
			startRecognitionPipeline();

			MARF.debug("MARF: Classifying...");

			if(oClassification.classify() == false)
				throw new ClassificationException("Classification returned false.");

			oResult = oClassification.getResult();
		}
		catch(ClassificationException e)
		{
			throw new MARFException(e.getMessage(), e);
		}
	}

	/**
	 * Training mode
	 * @since 0.2.0
	 * @exception MARFException
	 */
	public static final void train() throws MARFException
	{
		try
		{
			if(iCurrentSubject == UNSET)
				throw new MARFException("Unset subject ID for training.");

			startRecognitionPipeline();

			//XXX batch: oClassification.restore();

			MARF.debug("MARF: Training...");

			if(oClassification.train() == false)
				throw new ClassificationException("Training returned false.");

			//XXX batch: oClassification.dump();
		}
		catch(ClassificationException e)
		{
			throw new MARFException(e.getMessage(), e);
		}
/*		catch(IOException e)
		{
			throw new MARFException(e.getMessage(), e);
		}
*/	}

	// Not API

	/**
	 * The core processing pipeline
	 * @exception MARFException
	 */
	private static final void startRecognitionPipeline() throws MARFException
	{
		boolean bSetupErrors   = false;
		String  strSetupErrMsg = "";

		try
		{
			/*
			 * Checking minimal required settings
			 */

			if
			(
				iPreprocessingMethod     == UNSET ||
				iFeatureExtractionMethod == UNSET ||
				iClassificationMethod    == UNSET ||
				iSampleFormat            == UNSET ||

				// XXX either filename or dir must present
				strFileName              == ""
			)
			{
				// TODO: Enhance error reporting here
				strFileName	=
					"MARF.startRecognitionPipeline() - Some configuration parameters were unset.\n" +
					getConfig();

				bSetupErrors = true;
			}

			if(bSetupErrors == true)
				throw new MARFException(strSetupErrMsg);

			/*
			 * Sample Loading Stage
			 */

			MARF.debug("Loading sample \"" + strFileName + "\"");

			switch(iSampleFormat)
			{
				case WAV:
					oSampleLoader = new WAVLoader();
					break;

				case MP3:
					oSampleLoader = new MP3Loader();
					break;

				case ULAW:
					oSampleLoader = new ULAWLoader();
					break;

				default:
					throw new MARFException("Unknown sample file format: " + iSampleFormat);
			}

			oSample = oSampleLoader.loadSample(strFileName);

			/*
			 * Preprocessing Stage
			 */

			MARF.debug("Preprocessing...");

			switch(iPreprocessingMethod)
			{
				case DUMMY:
					oPreprocessing = new Dummy(oSample);
					break;

				case BANDPASS_FFT_FILTER:
					oPreprocessing = new BandpassFilter(oSample);
					break;

				case ENDPOINT:
					oPreprocessing = new Endpoint(oSample);
					break;

				case HIGH_FREQUENCY_BOOST_FFT_FILTER:
					oPreprocessing = new HighFrequencyBoost(oSample);
					break;

				case LOW_PASS_FFT_FILTER:
					oPreprocessing = new LowPassFilter(oSample);
					break;

				case HIGH_PASS_FFT_FILTER:
					oPreprocessing = new HighPassFilter(oSample);
					break;

				default:
					throw new MARFException("Unknown preprocessing method " + iPreprocessingMethod);
			}

			// [SM]: Should this be in the preprocessing itself somewhere?
			// NOTE: this dump and the dump after preprocess() below will be
			//       exactly the same for DUMMY (0), because normalization
			//       happens unconditionally when Preprocessing object is constructed
			//       above, and Dummy's preprocess() doesn't do anything...
			if(bDumpWaveGraph)
			{
				new WaveGrapher
				(
					oSample.getSampleArray(),
					0,
					oSample.getSampleArray().length,
					strFileName,
					"normalized"
				).dump();
			}

			oPreprocessing.preprocess();

			if(bDumpWaveGraph)
			{
				new WaveGrapher
				(
					oSample.getSampleArray(),
					0,
					oSample.getSampleArray().length,
					strFileName,
					"preprocessed"
				).dump();
			}

			/*
			 * Feature Extraction Stage
			 */

			MARF.debug("Feature extraction...");

			switch(iFeatureExtractionMethod)
			{
				case LPC:
					oFeatureExtraction = new LPC(oPreprocessing);
					break;

				case FFT:
					oFeatureExtraction = new FFT(oPreprocessing);
					break;

				case F0:
					oFeatureExtraction = new F0(oPreprocessing);
					break;

				case SEGMENTATION:
					oFeatureExtraction = new Segmentation(oPreprocessing);
					break;

				case CEPSTRAL:
					oFeatureExtraction = new Cepstral(oPreprocessing);
					break;

				case RANDOM_FEATURE_EXTRACTION:
					oFeatureExtraction = new RandomFeatureExtraction(oPreprocessing);
					break;

				default:
					throw new MARFException("Unknown feature extraction method " + iFeatureExtractionMethod);
			}

			oFeatureExtraction.extractFeatures();

			/*
			 * Classification Stage
			 */

			switch(iClassificationMethod)
			{
				case NEURAL_NETWORK:
					oClassification = new NeuralNetwork(oFeatureExtraction);
					break;

				case STOCHASTIC:
					oClassification = new Stochastic(oFeatureExtraction);
					break;

				case MARKOV:
					oClassification = new Markov(oFeatureExtraction);
					break;

				case EUCLIDEAN_DISTANCE:
					oClassification = new EuclideanDistance(oFeatureExtraction);
					break;

				case CHEBYSHEV_DISTANCE:
					oClassification = new ChebyshevDistance(oFeatureExtraction);
					break;

				case MINKOWSKI_DISTANCE:
					oClassification = new MinkowskiDistance(oFeatureExtraction);
					break;

				case MAHALANOBIS_DISTANCE:
					oClassification = new MahalanobisDistance(oFeatureExtraction);
					break;

				case RANDOM_CLASSIFICATION:
					oClassification = new RandomClassification(oFeatureExtraction);
					break;

				default:
					throw new MARFException("Unknown classification method " + iClassificationMethod);
			}
		}

		// Push all the module exceptions down here for now.
		catch(Exception e)
		{
			// Just this for now
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Meant to provide implementation of the buffered sample processing for large samples.
	 * Not implemented.
	 */
	public static final void streamedRecognition()
	{
		throw new NotImplementedException("MARF.streamedRecognition()");
	}

	/**
	 * Outputs param to STDERR if DEBUG is enabled
	 * @param pstrMsg message to output.
	 */
	public static final void debug(final String pstrMsg)
	{
		if(DEBUG == true)
			System.err.println(pstrMsg);
	}
}

// EOF
