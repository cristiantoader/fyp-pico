package marf.Storage;

import marf.*;

import java.util.*;

/**
 * Class Result
 * <p>Represents classification result - ID and some stats</p>
 * <p>$Header: /cvsroot/marf/marf/src/marf/Storage/Result.java,v 1.10 2003/01/28 23:53:07 mokhov Exp $</p>
 */
public class Result
{
	/**
	 * Identified subject's ID
	 */
	private int iID = 0;

	/**
	 * Distances from other speakers and other stats (growable and shrinkable)
	 */
	private Vector oResultSet = null;

	/**
	 * Some stats (if present)
	 */
	private double[] adResultSet = null;

	/**
	 * Default Constructor
	 * Equivalent to <code>Result(0)</code>
	 */
	public Result()
	{
		this(0);
	}

	/**
	 * ID Constructor
	 * @param piID integer ID of the speaker
	 */
	public Result(int piID)
	{
		this.iID = piID;
	}

	/**
	 * ResultSet Constructor
	 * Equivalent to <code>Result(0, padResultSet)</code>
	 * @param padResultSet array of relevant data
	 */
	public Result(double[] padResultSet)
	{
		this(0, padResultSet);
	}

	/**
	 * ID ResultSet Constructor
	 * @param piID integer ID of the speaker
	 * @param padResultSet array of relevant data
	 */
	public Result(int piID, double[] padResultSet)
	{
		this.iID = piID;
		this.adResultSet = padResultSet;
	}

	/**
	 * ID Vector ResultSet Constructor
	 * @param piID integer ID of the subject
	 * @param poResultSet Vector of relevant data
	 */
	public Result(int piID, Vector poResultSet)
	{
		this.iID = piID;
		this.oResultSet = poResultSet;
	}

	/**
	 * Returns result's ID
	 * @return ID of an entity (speaker, instrument, etc)
	 */
	public final int getID()
	{
		return this.iID;
	}

	/**
	 * Returns second closest ID
	 * @return ID of an entity (speaker, instrument, etc)
	 */
	public final int getSecondClosestID()
	{
		int id = 0;

		double dMinDistance = Double.MAX_VALUE;

		if(this.oResultSet != null)
		{
			// Traverse the result set for the second minimum distance
			for(int i = 0; i < this.oResultSet.size(); i++)
			{
				Vector oIDDistancePair = (Vector)this.oResultSet.elementAt(i);

				int iCurrentID = ((Integer)oIDDistancePair.elementAt(0)).intValue();

				// If it's us, skip
				if(iCurrentID == this.iID)
					continue;

				double dCurrentDistance = ((Double)oIDDistancePair.elementAt(1)).doubleValue();

				if(dMinDistance > dCurrentDistance)
				{
					dMinDistance = dCurrentDistance;
					id = iCurrentID;
				}
			}
		}
		else
		{
			MARF.debug("Result.getSecondClosestID() --- oResultSet is null");
		}

		return id;
	}

	/**
	 * Sets ID, should only be called by a Classification module
	 * @param piID ID of the subject
	 */
	public final void setID(final int piID)
	{
		this.iID = piID;
	}
}

// EOF
