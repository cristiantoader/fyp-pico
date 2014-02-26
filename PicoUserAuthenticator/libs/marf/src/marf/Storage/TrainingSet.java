package marf.Storage;

import marf.*;
import marf.util.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * TrainingSet -- Encapsulates Speaker ID and speaker's clusters of "feature" sets
 *
 * <p>$Header: /cvsroot/marf/marf/src/marf/Storage/TrainingSet.java,v 1.17.2.1 2003/02/16 18:59:24 mokhov Exp $</p>
 *
 * @since December 5, 2002
 */
public class TrainingSet implements Serializable, StorageManager
{
	/*
	 * NOTE: Be careful when you mess with this file. Any new fields
	 *       or structural changes will change the on-disk layout of
	 *       whole TrainingSet. Until an upgrade utility is available,
	 *       you will have to retrain ALL your models.
	 */

	/**
	 * TrainingSample contains one item in the training set.
	 * Each training sample consists of the feature vector plus information describing that feature vector.
	 */
	public class TrainingSample implements Serializable
	{
		/**
		 * Which subject this feature vector is associated with.
		 */
		private int iSubjectID;

		/**
		 * Array represinting mean vector describing the cluster.
		 */
		private double[] adMeanVector;

		/**
		 * How many times mean was computed. Used in recomputation of it.
		 */
		private int iMeanCount = 0;

		/**
		 * A list of filenames that were used in training for this cluster.
		 * Used to avoid duplicate training on the same filename.
		 */
		private Vector oFilenames = new Vector();

		/**
		 * A filename to the training set.
		 * @param pstrFilename filename to check
		 * @return <code>true</code> if the filename is there; <code>false</code> if not
		 * @see existsFilename
		 */
		public final boolean addFilename(String pstrFilename)
		{
			if(existsFilename(pstrFilename))
				return false;

			this.oFilenames.add(pstrFilename);
			return true;
		}

		/**
		 * Check existance of the file in the training set. Servers as an indication that we already trained on the given file.
		 * @param pstrFilename filename to check
		 * @return <code>true</code> if the filename is there; <code>false</code> if not
		 */
		public final boolean existsFilename(String pstrFilename)
		{
			if(oFilenames.contains(pstrFilename))
				return true;

			return false;
		}

		/**
		 * Retrieves Subject ID of a particular training sample
		 * @return int ID
		 */
		public final int getSubjectID()
		{
			return this.iSubjectID;
		}

		/**
		 * Retrieves current mean count
		 * @return mean count
		 */
		public final int getMeanCount()
		{
			return this.iMeanCount;
		}

		/**
		 * Increases mean count by one
		 * @return new mean count
		 */
		public final int incMeanCount()
		{
			return (++this.iMeanCount);
		}

		/**
		 * Retrieves the mean vector
		 * @return array of doubles representing the mean for that cluster
		 */
		public final double[] getMeanVector()
		{
			return this.adMeanVector;
		}

		/**
		 * Sets new Subject ID
		 * @param piSubjectID integer ID
		 */
		public final void setSubjectID(int piSubjectID)
		{
			this.iSubjectID = piSubjectID;
		}

		/**
		 * Sets new mean vector
		 * @param padMeanVector double array representing the mean vector
		 */
		public final void setMeanVector(double[] padMeanVector)
		{
			this.adMeanVector = padMeanVector;
		}

		/**
		 * Write one training sample to a text file.
		 * @param bw BufferedWriter
		 */
		public final void dump(BufferedWriter bw) throws IOException
		{
			String str = this.iSubjectID + ", " + this.adMeanVector.length;

			for(int i = 0; i < this.adMeanVector.length; i++)
				str += ", " + this.adMeanVector[i];

			bw.write(str);
			bw.newLine();
		}

		/**
		 * Retrieve one training sample from a text file.
		 * @param br BufferedReader
		 * @exception IOException
		 */
		public final void restore(BufferedReader br) throws java.io.IOException
		{
			StringTokenizer stk = new StringTokenizer(br.readLine(), ", ");
			int len = 0;

			if(stk.hasMoreTokens())
				iSubjectID = Integer.parseInt(stk.nextToken());

			if(stk.hasMoreTokens())
				iFeatureExtractionMethod = Integer.parseInt(stk.nextToken());

			if(stk.hasMoreTokens())
				iPreprocessingMethod = Integer.parseInt(stk.nextToken());

			if(stk.hasMoreTokens())
				len = Integer.parseInt(stk.nextToken());

			adMeanVector = new double[len];

			int i = 0;

			while(stk.hasMoreTokens())
				adMeanVector[i++] = Double.parseDouble(stk.nextToken());
		}
	} // TrainingSample

	/**
	 * Indicates to dump training set data as gzipped binary file.
	 */
	public static final int DUMP_GZIP_BINARY = 0;

	/**
	 * Indicates to dump training set data as CSV text file.
	 */
	public static final int DUMP_CSV_TEXT    = 1;

	/**
	 * In which format dump TrainingSet data. Default <code>DUMP_GZIP_BINARY</code>
	 */
	private int iDumpMode = DUMP_GZIP_BINARY;

	/**
	 * Sets the dump mode
	 * @param piDumpMode the mode
	 */
	public final void setDumpMode(final int piDumpMode)
	{
		this.iDumpMode = piDumpMode;
	}

	/**
	 * Retrieves current dump mode
	 * @return the mode, integer
	 */
	public final int getDumpMode()
	{
		return this.iDumpMode;
	}

	/**
	 * A Vector of TrainingSamples
	 */
	private Vector oTrainingSamples = new Vector();

	/**
	 * TrainingSet file name
	 */
	public String strTrainingSetFile = "training.set";

	/**
	 * Which feature extraction method was used to determine this feature vector.
	 */
	private int iFeatureExtractionMethod;

	/**
	 * Which preprocessing method was applied to the sample before this feature vector was extracted.
	 */
	private int iPreprocessingMethod;

	/**
	 * Construct a training set object.
	 */
	public TrainingSet()
	{
	}

	/**
	 * Retrieves training samples
	 * @return vector of training samples.
	 */
	public final Vector getTrainingSamples()
	{
		return this.oTrainingSamples;
	}

	/**
	 * Retrieves current training set file name
	 * @return String filename
	 */
	public final String getTrainingSetFile()
	{
		return this.strTrainingSetFile;
	}

	/**
	 * Returns preprocessing method used on this training set
	 * @return the method
	 */
	public final int getPreprocessingMethod()
	{
		return this.iPreprocessingMethod;
	}

	/**
	 * Returns preprocessing method used on this training set
	 * @return piPreprocessingMethod the method
	 */
	public final int getFeatureExtractionMethod()
	{
		return this.iFeatureExtractionMethod;
	}

	/**
	 * Sets feature extraction method used on this training set
	 * @param piFeatureExtractionMethod the method
	 */
	public final void setPreprocessingMethod(int piPreprocessingMethod)
	{
		this.iPreprocessingMethod = iPreprocessingMethod;
	}

	/**
	 * Sets feature extraction method used on this training set
	 * @param piFeatureExtractionMethod the method
	 */
	public final void setFeatureExtractionMethod(int piFeatureExtractionMethod)
	{
		this.iFeatureExtractionMethod = piFeatureExtractionMethod;
	}

	/**
	 * Sets current training set file name
	 * @return String filename
	 */
	public final void setTrainingSetFile(String pstrTrainingSetFile)
	{
		this.strTrainingSetFile = pstrTrainingSetFile;
	}

	/**
	 * Adds new feature vector to the mean and recomputes the mean
	 * @param padFeatureVector vector to add
	 * @param iSubjectID for which subject that vector is
	 * @param iPreprocessingMethod preprocessing method used
	 * @param iFeatureExtractionMethod feature extraction method used
	 * @return <code>true</code> if the vector was added; <code>false</code> otherwise
	 */
	public final boolean addFeatureVector
	(
		double[] padFeatureVector,
		String pstrFilename,
		int piSubjectID,
		int piPreprocessingMethod,
		int piFeatureExtractionMethod
	)
	{
		/*
		 * check if this sample is already in the training set
		 * for these feature extraction & preprocessing methods
		 */
		TrainingSample oTrainingSample = null;
		boolean        bNewSample      = true;

		double[] adMeanVector = null;

		for(int i = 0; (i < oTrainingSamples.size()) && (bNewSample); i++)
		{
			oTrainingSample = (TrainingSample)oTrainingSamples.get(i);

			if(piSubjectID == oTrainingSample.getSubjectID())
			{
				// Disallow training on the same file twice
				if(oTrainingSample.existsFilename(pstrFilename))
				{
					MARF.debug
					(
						"TrainingSet.addFeatureVector() --- Attempt to train on the same file: " +
						pstrFilename
					);

					return false;
				}

				bNewSample = false;
				adMeanVector = oTrainingSample.getMeanVector();
			}
		}

		if(bNewSample)
		{
			oTrainingSample = new TrainingSample();
			adMeanVector = (double[])padFeatureVector.clone();
		}
		else
		{
			int iMeanCount = oTrainingSample.getMeanCount();

			// Recompute the mean
			for(int f = 0; f < adMeanVector.length; f++)
				adMeanVector[f] = (adMeanVector[f] * iMeanCount + padFeatureVector[f]) / (iMeanCount + 1);
		}

		oTrainingSample.setMeanVector(adMeanVector);
		oTrainingSample.setSubjectID(piSubjectID);
		oTrainingSample.addFilename(pstrFilename);
		oTrainingSample.incMeanCount();

		setFeatureExtractionMethod(piFeatureExtractionMethod);
		setPreprocessingMethod(piPreprocessingMethod);

		if(bNewSample)
		{
			this.oTrainingSamples.add(oTrainingSample);

			MARF.debug
			(
				"Added feature vector for subject " + piSubjectID +
				", preprocessing method " + piPreprocessingMethod +
				", feature extraction method " + piFeatureExtractionMethod
			);
		}
		else
		{
			MARF.debug
			(
				"Updated mean vector for subject " + piSubjectID +
				", preprocessing method " + piPreprocessingMethod +
				", feature extraction method " + piFeatureExtractionMethod
			);
		}

		return true;
	}

	/**
	 * Gets the size of the feature vectors set
	 * @return number of training samples in the set
	 */
	public final int size()
	{
		return this.oTrainingSamples.size();
	}

	/**
	 * Retrieve the current training set from disk
	 * @exception IOException
	 */
	public final void restore() throws java.io.IOException
	{
		try
		{
			switch(this.iDumpMode) // Text input only in debug mode
			{
				case DUMP_CSV_TEXT:
				{
					BufferedReader br = new BufferedReader(new FileReader(this.strTrainingSetFile));

					int num = Integer.parseInt(br.readLine());

					for(int i = 0; i < num; i++)
					{
						TrainingSample ts = new TrainingSample();

						ts.restore(br);
						this.oTrainingSamples.add(ts);
					}

					br.close();

					break;
				}

				case DUMP_GZIP_BINARY:
				{
					FileInputStream fis = new FileInputStream(this.strTrainingSetFile);
					GZIPInputStream gzis = new GZIPInputStream(fis);
					ObjectInputStream in = new ObjectInputStream(gzis);

					TrainingSet oTrainingSet = (TrainingSet)in.readObject();

					in.close();

					this.strTrainingSetFile = oTrainingSet.getTrainingSetFile();
					this.oTrainingSamples = oTrainingSet.getTrainingSamples();

					break;
				}

				default:
					throw new IOException("TrainingSet.restore() --- Invalid file format: " + this.iDumpMode);
			}

			MARF.debug("Training set loaded successfully: " + this.oTrainingSamples.size() + " mean vectors.");
		}
		catch(FileNotFoundException e)
		{
/*			throw new IOException
			(
				"TrainingSet.restore() --- FileNotFoundException for file: \"" + this.strTrainingSetFile + "\", " +
				e.getMessage()
			);
*/
			MARF.debug
			(
				"TrainingSet.restore() --- FileNotFoundException for file: \"" + this.strTrainingSetFile + "\", " +
				e.getMessage() + "\n" +
				"Creating one now..."
			);

			dump();
		}
		catch(NumberFormatException e)
		{
			throw new IOException
			(
				"TrainingSet.restore() --- NumberFormatException: " +
				e.getMessage()
			);
		}
		catch(ClassNotFoundException e)
		{
			throw new IOException("TrainingSet.restore() --- ClassNotFoundException: " + e.getMessage());
		}
	}

	/**
	 * Dump the current training set to disk
	 * @exception IOException
	 */
	public final void dump() throws java.io.IOException
	{
		switch(this.iDumpMode) // Text input only in debug mode
		{
			case DUMP_CSV_TEXT:
			{
				BufferedWriter bw = new BufferedWriter(new FileWriter(this.strTrainingSetFile));

				bw.write(Integer.toString(oTrainingSamples.size()));
				bw.newLine();

				MARF.debug
				(
					"Wrote " + Integer.toString(oTrainingSamples.size()) +
					" to file " + this.strTrainingSetFile
				);

				for(int i = 0; i < oTrainingSamples.size(); i++)
					((TrainingSample)oTrainingSamples.get(i)).dump(bw);

				bw.close();

				break;
			}

			case DUMP_GZIP_BINARY:
			{
				FileOutputStream fos = new FileOutputStream(this.strTrainingSetFile);
				GZIPOutputStream gzos = new GZIPOutputStream(fos);
				ObjectOutputStream out = new ObjectOutputStream(gzos);

				out.writeObject(this);
				out.flush();
				out.close();
				break;
			}

			default:
				throw new IOException("TrainingSet.dump() --- Invalid file format: " + this.iDumpMode);
		}
	}
}

// EOF
