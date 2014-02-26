package com.fyp.authenticator.voice;

import marf.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.awt.*;

public class AuthDevVoiceDAO implements Serializable {

	private static final long serialVersionUID = 8857377720955408792L;

	/**
	 * DB filename
	 */
	private String strDbFile;

	/**
	 * Hashes "config string" -> Vector(FirstMatchPoint(XSuccesses, YFailures),
	 * SecondMatchPoint(XSuccesses, YFailures))
	 */
	private Hashtable oStatsPerConfig = null;

	/**
	 * A vector of vectors of speakers info pre-loded on connect()
	 */
	private Hashtable<Integer, Vector<String>> oDB = null;

	/**
	 * Indicate whether we are connected or not
	 */
	private boolean bConnected = false;

	/**
	 * "Database connection"
	 */
	private BufferedReader br = null;

	/**
	 * Constructor
	 * 
	 * @param strDbFile
	 *          filename of a CSV file with IDs and names of speakers
	 */
	public AuthDevVoiceDAO(String strDbFile) {
		this.strDbFile = strDbFile;
		this.oDB = new Hashtable<Integer, Vector<String>>();
		this.oStatsPerConfig = new Hashtable();
	}

	/**
	 * Retrieves Speaker's ID by a sample filename
	 * 
	 * @param wavFile
	 *          Name of a .wav file for which ID must be returned
	 * @param training
	 *          indicates whether the filename is a training (<code>true</code>)
	 *          sample or testing (<code>false</code>)
	 * @return int ID
	 * @exception Exception
	 */
	public int getIDByFilename(String wavFile, boolean training) throws Exception {
		String str = parsePathFilename(wavFile);
		Enumeration<Integer> IDs = oDB.keys();

		// Traverse all the info vectors looking for sample filename
		while (IDs.hasMoreElements()) {
			Integer id = (Integer) IDs.nextElement();

			Vector<String> entry = oDB.get(id);

			// Start from 1 because 0 is speaker's name
			for (int i = 1; i < entry.size(); i++) {
				String tmp = entry.get(i);

				if (tmp.compareTo(str) == 0)
					return id.intValue();
			}
		}

		return -1;
	}

	private String parsePathFilename(String wavFile) {
		String str;

		// Extract actual file name without preceding path (if any)
		if (wavFile.lastIndexOf('/') >= 0) {
			str = wavFile.substring(wavFile.lastIndexOf('/') + 1, wavFile.length());
		} else if (wavFile.lastIndexOf('\\') >= 0) {
			str = wavFile.substring(wavFile.lastIndexOf('\\') + 1, wavFile.length());
		} else {
			str = wavFile;
		}

		return str;
	}

	/**
	 * Retrieves speaker's name by their ID
	 * 
	 * @param piID
	 *          ID of a person in the DB to return a name for
	 * @return name string
	 * @exception Exception
	 */
	public final String getName(final int piID) throws Exception {
		String strName;

		Vector<String> oDBEntry = oDB.get(Integer.valueOf(piID));

		if (oDBEntry == null) {
			strName = "Unknown Speaker (" + piID + ")";
		} else {
			strName = (String) oDBEntry.elementAt(0);
		}

		return strName;
	}

	/**
	 * Connects to the "database" of speakers (opens the text file :-)).
	 * 
	 * @exception Exception
	 */
	public void connect() throws Exception {
		// That's where we should establish file linkage and keep it until closed
		try {
			this.br = new BufferedReader(new FileReader(this.strDbFile));
			this.bConnected = true;
		} catch (IOException e) {
			throw new Exception("Error opening speaker DB: \"" + this.strDbFile
					+ "\": " + e.getMessage() + ".");
		}
	}

	/**
	 * Retrieves speaker's data from the text file and populates internal data
	 * structures
	 * 
	 * @exception Exception
	 */
	public void query() throws Exception {
		// That's where we should load db results into internal data structure

		String tmp;
		int id = -1;

		try {
			tmp = br.readLine();

			while (tmp != null) {
				StringTokenizer stk = new StringTokenizer(tmp, ",");
				Vector<String> oSpeakerInfo = new Vector<String>();

				// get ID
				if (stk.hasMoreTokens())
					id = Integer.parseInt(stk.nextToken());

				// speaker's name
				if (stk.hasMoreTokens()) {
					tmp = stk.nextToken();
					oSpeakerInfo.add(tmp);
				}

				// training file names
				Vector<String> oTrainingFilenames = new Vector<String>();

				if (stk.hasMoreTokens()) {
					StringTokenizer oSTK = new StringTokenizer(stk.nextToken(), "|");

					while (oSTK.hasMoreTokens()) {
						tmp = oSTK.nextToken();
						oTrainingFilenames.add(tmp);
					}
				}

				oSpeakerInfo.addAll(oTrainingFilenames);

				this.oDB.put(id, oSpeakerInfo);

				tmp = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Closes (file) database connection.
	 * 
	 * @exception Exception
	 */
	public void close() throws Exception {
		// Close file
		if (bConnected == false)
			throw new Exception("SpeakersIdentDb.close() - not connected");

		try {
			this.br.close();
			this.bConnected = false;
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	/**
	 * Adds one more classification statics entry
	 * 
	 * @param pstrConfig
	 *          String representation of the configuration the stats are for
	 * @param pbSuccess
	 *          <code>true</code> if classification was successful;
	 *          <code>false</code> otherwise
	 */
	public void addStats(String pstrConfig, boolean pbSuccess) {
		addStats(pstrConfig, pbSuccess, false);
	}

	/**
	 * Adds one more classification statics entry and accounts for the second best
	 * choice
	 * 
	 * @param pstrConfig
	 *          String representation of the configuration the stats are for
	 * @param pbSuccess
	 *          <code>true</code> if classification was successful;
	 *          <code>false</code> otherwise
	 * @param pbSecondBest
	 *          <code>true</code> if classification was successful;
	 *          <code>false</code> otherwise
	 */
	public void addStats(String pstrConfig, boolean pbSuccess,
			boolean pbSecondBest) {
		Vector oMatches = (Vector) oStatsPerConfig.get(pstrConfig);
		Point oPoint = null;

		if (oMatches == null) {
			oMatches = new Vector(2);
			oMatches.add(new Point());
			oMatches.add(new Point());
		} else {
			if (pbSecondBest == false)
				oPoint = (Point) oMatches.elementAt(0); // Firts match
			else
				oPoint = (Point) oMatches.elementAt(1); // Second best match
		}

		int x = 0; // # of successes
		int y = 0; // # of failures

		if (oPoint == null) // Didn't exist yet; create new
		{
			if (pbSuccess == true)
				x = 1;
			else
				y = 1;

			oPoint = new Point(x, y);

			if (oPoint == null) {
				System.out
						.println("SpeakersIdentDb.addStats() - oPoint null! Out of memory?");
				System.exit(-1);
			}

			if (oMatches == null) {
				System.out
						.println("SpeakersIdentDb.addStats() - oMatches null! Out of memory?");
				System.exit(-1);
			}

			if (oMatches.size() == 0) {
				System.out.println("SpeakersIdentDb.addStats() - oMatches.size = 0");
				System.exit(-1);
			}

			if (pbSecondBest == false)
				oMatches.setElementAt(oPoint, 0);
			else
				oMatches.setElementAt(oPoint, 1);

			oStatsPerConfig.put(pstrConfig, oMatches);
		}

		else // There is an entry for this config; update
		{
			if (pbSuccess == true)
				oPoint.x++;
			else
				oPoint.y++;
		}
	}

	/**
	 * Dumps collected statistics to STDOUT
	 * 
	 * @exception Exception
	 */
	public final void printStats() throws Exception {
		String[] astrResults = new String[oStatsPerConfig.size() * 2];
		Enumeration oStatsEnum = oStatsPerConfig.keys();

		int iResultNum = 0;

		while (oStatsEnum.hasMoreElements()) {
			String strConfig = (String) oStatsEnum.nextElement();

			for (int i = 0; i < 2; i++) {
				Point oGoodBadPoint = (Point) ((Vector) oStatsPerConfig.get(strConfig))
						.elementAt(i);
				String strGuess = (i == 0) ? "1st" : "2nd";

				astrResults[iResultNum++] = strGuess
						+ " "
						+ "CONFIG: "
						+ strConfig
						+ ", GOOD: "
						+ oGoodBadPoint.x
						+ ", BAD: "
						+ oGoodBadPoint.y
						+ ", %: "
						+ ((double) oGoodBadPoint.x / (double) (oGoodBadPoint.x + oGoodBadPoint.y))
						* 100;
			}
		}

		Arrays.sort(astrResults);

		for (int i = 0; i < astrResults.length; i++)
			System.out.println(astrResults[i]);
	}

	/**
	 * Resets in-memory and on-disk statistics
	 * 
	 * @exception IOException
	 */
	public final void resetStats() throws IOException {
		oStatsPerConfig.clear();
		dump();
	}

	/**
	 * Dumps statistic's Hashtable object as gzipped binary to disk
	 * 
	 * @exception IOException
	 */
	public void dump() throws IOException {
		FileOutputStream fos = new FileOutputStream(this.strDbFile + ".stats");
		GZIPOutputStream gzos = new GZIPOutputStream(fos);
		ObjectOutputStream out = new ObjectOutputStream(gzos);

		out.writeObject(this.oStatsPerConfig);
		out.flush();
		out.close();
	}

	/**
	 * Reloads statistic's Hashtable object from disk. If the file did not exist,
	 * it creates a new one.
	 * 
	 * @exception IOException
	 */
	public void restore() throws IOException {
		try {
			FileInputStream fis = new FileInputStream(this.strDbFile + ".stats");
			GZIPInputStream gzis = new GZIPInputStream(fis);
			ObjectInputStream in = new ObjectInputStream(gzis);

			this.oStatsPerConfig = (Hashtable) in.readObject();
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("NOTICE: File " + this.strDbFile
					+ ".stats does not seem to exist. Creating a new one....");

			resetStats();
		} catch (ClassNotFoundException e) {
			throw new IOException(
					"SpeakerIdentDd.retore() - ClassNotFoundException: " + e.getMessage());
		}
	}
}

// EOF
