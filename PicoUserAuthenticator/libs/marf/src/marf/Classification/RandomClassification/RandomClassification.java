package marf.Classification.RandomClassification;

import marf.*;
import marf.util.*;
import marf.Storage.*;
import marf.Classification.*;
import marf.FeatureExtraction.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;


/**
 * Class RandomClassification
 * <p>Random Classification for testing purposes. The bottomline of the classification results.
 * All the other modules should be better than this 99% of the time. If they are not, debug them.</p>
 *
 * <p>$Header: /cvsroot/marf/marf/src/marf/Classification/RandomClassification/RandomClassification.java,v 1.2 2003/01/26 06:13:56 mokhov Exp $</p>
 *
 * @since 0.2.0
 */
public class RandomClassification extends Classification implements Serializable
{
	/**
	 * Vector of integer IDs
	 */
	private Vector oIDs = new Vector();

	/**
	 * RandomClassification Constructor
	 * @param poFeatureExtraction FeatureExtraction module reference
	 */
	public RandomClassification(FeatureExtraction poFeatureExtraction)
	{
		// No point to care for features, what we care about is ID
		// Nor we care for ModuleParams
		super(null);
	}

	/**
	 * Picks an ID at random
	 * @return <code>true</code>
	 * @exception ClassificationException
	 */
	public final boolean classify() throws ClassificationException
	{
		try
		{
			Vector oSubjectDistance = new Vector();
			int iFirstID = 0;
			int iSecondID = 0;

			restore();

			if(this.oIDs.size() == 0)
			{
				MARF.debug("RandomClassification.classify() --- ID set is of 0 length");

				Vector oZeros = new Vector();

				oZeros.add(new Integer(iFirstID));
				oZeros.add(new Double(0));

				oSubjectDistance.add(oZeros);
				oSubjectDistance.add(oZeros);
			}
			else
			{
				// Collect for stats
				// XXX: Move to StatsCollector
				Vector oIDDistancePair;

				iFirstID = ((Integer)this.oIDs.elementAt((int)(this.oIDs.size() * (new Random().nextDouble())))).intValue();
				iSecondID = iFirstID;

				// Pick a different second best ID if there are > 1 IDs in there
				while(iSecondID == iFirstID && this.oIDs.size() > 1)
					iSecondID = ((Integer)this.oIDs.elementAt((int)(this.oIDs.size() * (new Random().nextDouble())))).intValue();

				MARF.debug("RandomClassification.classify() --- ID1 = " + iFirstID + ", ID2 = " + iSecondID);

				oIDDistancePair = new Vector();

				oIDDistancePair.add(new Integer(iFirstID));
				oIDDistancePair.add(new Double(0));
				oSubjectDistance.add(oIDDistancePair);

				oIDDistancePair = new Vector();

				oIDDistancePair.add(new Integer(iSecondID));
				oIDDistancePair.add(new Double(1));
				oSubjectDistance.add(oIDDistancePair);
			}

			this.oResult = new Result(iFirstID, oSubjectDistance);

			return true;
		}
		catch(IOException e)
		{
			throw new ClassificationException(e.getMessage());
		}
	}

	/**
	 * Simply stores incoming ID's
	 * @return <code>true</code>
	 * @exception ClassificationException
	 */
	public final boolean train() throws ClassificationException
	{
		try
		{
			Integer oIntegerID = new Integer(MARF.getCurrentSubject());

			// XXX: this test isn't working -- objects are not the same, so dupes are possible
			// maybe need to fix, though it's not of primary importance
			if(this.oIDs.contains(oIntegerID) == false)
			{
				restore();

				this.oIDs.add(oIntegerID);

				dump();

				MARF.debug("RandomClassification.train() --- added ID " + MARF.getCurrentSubject());
			}

			return true;
		}
		catch(IOException e)
		{
			throw new ClassificationException("IOException in RandomClassification.train() --- " + e.getMessage());
		}
	}

	/* From Storage Manager */

	/**
	 * Dumps "training set" of IDs
	 * @exception IOException
	 */
	public final void dump() throws java.io.IOException
	{
		FileOutputStream fos = new FileOutputStream(this.getClass().getName() + ".bin");
		GZIPOutputStream gzos = new GZIPOutputStream(fos);
		ObjectOutputStream out = new ObjectOutputStream(gzos);

		out.writeObject(this.oIDs);
		out.flush();
		out.close();
	}

	/**
	 * Restores "training set" of IDs
	 * @exception IOException
	 */
	public final void restore() throws java.io.IOException
	{
		try
		{
			FileInputStream fis = new FileInputStream(this.getClass().getName() + ".bin");
			GZIPInputStream gzis = new GZIPInputStream(fis);
			ObjectInputStream in = new ObjectInputStream(gzis);

			this.oIDs = (Vector)in.readObject();

			in.close();
		}
		catch(FileNotFoundException e)
		{
			this.oIDs = new Vector();
			dump();
		}
		catch(ClassNotFoundException e)
		{
			throw new IOException("RandomClassification.restore() --- ClassNotFoundException: " + e.getMessage());
		}
	}
}

// EOF
