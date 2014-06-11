package com.fyp.authenticator.face;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.util.Log;
import facerecognition.javafaces.FaceRec;
import facerecognition.javafaces.MatchResult;

/**
 * Class used for mediating calls to the Recognito biometric library.
 * 
 * @author cristi
 * 
 */
public class FaceAuthMediator {
	/** Face recognition recognito object. */
	private FaceRec faceRec = null;

	/** Directory containing training data. */
	private String trainDirectory = null;
	/** Name of the owner image used for training. */
	private String ownerImage = null;

	/** Number of registered faces in training data. */
	private static final String NUM_FACES = "1";
	
	/** Euclidean distance threshold used with the library. */
	private static final String THRESHOLD = "2";

	/** Tag used for debugging. */
	private static final String TAG = "FaceDAO";

	/**
	 * Main constructor for the class.
	 * 
	 * The initialises the face recognition library and performs training on the
	 * owner data.
	 * 
	 * @param ctx
	 *            Context used for file access.
	 */
	public FaceAuthMediator(Context ctx) {
		Log.d(TAG, "FaceDAO+");

		this.faceRec = new FaceRec(ctx);

		this.trainDirectory = ctx.getFilesDir().toString();
		this.ownerImage = getOwnerImage();

		this.trainFaceRecognizer();

		Log.d(TAG, "FaceDAO-");
	}

	/**
	 * Method used for training the recognito face recogniser.
	 */
	private void trainFaceRecognizer() {
		Log.d(TAG, "trainFaceRecognizer+");

		MatchResult r = this.faceRec.processSelections(getAbsoluteFilePath(),
				trainDirectory, NUM_FACES, THRESHOLD);
		printMatch(r);

		Log.d(TAG, "trainFaceRecognizer-");
	}

	/**
	 * Method used for getting an Euclidean distance confidence level for the
	 * supplied input data.
	 * 
	 * The method uses the recognito library to perform biometric authentication
	 * based on the supplied data.
	 * 
	 * @param faceObject
	 *            Challenge used for biometric authentication.
	 * @return Euclidean distance confidence level for supplied challenge.
	 */
	public double getMatch(Bitmap faceObject) {
		double result = 0;
		MatchResult r = null;

		Log.d(TAG, "getMatch+");

		r = this.faceRec.findMatchResult(faceObject,
				Integer.parseInt(NUM_FACES), Double.parseDouble(THRESHOLD));
		printMatch(r);

		if (r.getMatchSuccess() == false) {
			Log.d(TAG, "getMatch: No match found!");
			result = Double.MAX_VALUE;
		} else {
			Log.d(TAG, "getMatch: Match found!");
			result = r.getMatchDistance();
		}

		Log.d(TAG, "getMatch- " + result);
		return result;
	}

	/**
	 * Getter for the owner image absolute file path. 
	 * @return
	 */
	private String getAbsoluteFilePath() {
		String res = this.trainDirectory + "/" + ownerImage;
		Log.d(TAG, res);

		File test = new File(res);
		if (test.exists()) {
			Log.d(TAG, "File exists.");
		} else {
			Log.d(TAG, "File does not exist.");
		}

		return res;
	}

	/**
	 * Debugging method used for printing the MatchResult from the recognito
	 * library.
	 * 
	 * @param r
	 *            match result that will get printed.
	 */
	private static void printMatch(MatchResult r) {
		if (r != null) {
			Log.d(TAG, "MatchResult success: " + r.getMatchSuccess());
			Log.d(TAG, "MatchResult distance: " + r.getMatchDistance());

			String aux;
			aux = r.getMatchFileName() == null ? r.getMatchFileName() : "";
			Log.d(TAG, "MatchResult file name: " + aux);

			aux = r.getMatchMessage() == null ? r.getMatchFileName() : "";
			Log.d(TAG, "MatchResult message: " + aux);
		}
	}

	/**
	 * Returns the name of the owner image from the training data directory.
	 * 
	 * @return the name of the owner image from the training data directory.
	 */
	private String getOwnerImage() {
		String owner = null;

		Log.d(TAG, "getOwnerImage+");

		File dir = new File(this.trainDirectory);
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				continue;
			}

			String fn = file.getName();
			if (fn.startsWith("owner") && fn.endsWith(".png")) {
				owner = fn;
				break;
			}
		}

		Log.d(TAG, "getOwnerImage- " + owner);
		return owner;
	}

	/**
	 * Checks if the supplied image contains at least one face.
	 * 
	 * This method is used by the FaceService in order to check if the sampled
	 * data is valid for face recognition.
	 * 
	 * @param img
	 *            Image for which the check is made.
	 * @return true if img contains at least one face.
	 */
	public static boolean hasFace(Bitmap img) {
		int maxFaces = 10;
		int numFaces = 0;

		Face[] faces = new Face[maxFaces];

		FaceDetector fr = new FaceDetector(img.getWidth() - img.getWidth() % 2,
				img.getHeight(), maxFaces);

		numFaces = fr.findFaces(img, faces);

		Log.d(TAG, "hasFace: " + numFaces);
		return numFaces != 0;
	}
}
