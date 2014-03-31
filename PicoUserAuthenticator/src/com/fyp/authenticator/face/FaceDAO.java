package com.fyp.authenticator.face;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import facerecognition.javafaces.FaceRec;
import facerecognition.javafaces.MatchResult;

public class FaceDAO {

	private FaceRec faceRec = null;

	private String trainDirectory = null;

	private static final String OWNER_IMG = "owner.png";
	private static final String NUM_FACES = "1";
	private static final String THRESHOLD = "2";

	private static final String TAG = "FaceDAO";

	public FaceDAO(Context ctx) {
		this.faceRec = new FaceRec(ctx);
		this.trainDirectory = ctx.getFilesDir().toString();
		this.trainFaceRecognizer();
	}

	public void trainFaceRecognizer() {
		MatchResult r = this.faceRec.processSelections(getAbsoluteFilePath(),
				trainDirectory, NUM_FACES, THRESHOLD);

		Log.d(TAG, "train-");
		printMatch(r);
	}

	public double getMatch(Bitmap faceObject) {
		double result = 0;
		MatchResult r = this.faceRec.findMatchResult(faceObject,
				Integer.parseInt(NUM_FACES), Double.parseDouble(THRESHOLD));

		Log.d(TAG, "getMatch-");
		printMatch(r);

		if (r.getMatchSuccess() == false) {
			Log.d(TAG, "no match");
			result = -1;
		} else {
			Log.d(TAG, "match found");
			result = r.getMatchDistance();
		}

		return result;
	}

	private String getAbsoluteFilePath() {
		String res = this.trainDirectory + "/" + OWNER_IMG;
		Log.d(TAG, res);

		File test = new File(res);
		if (test.exists()) {
			Log.d(TAG, "File exists.");
		} else {
			Log.d(TAG, "File does not exist.");
		}

		return res;
	}

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
}
