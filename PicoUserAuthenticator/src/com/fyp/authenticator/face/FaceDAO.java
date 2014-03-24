package com.fyp.authenticator.face;

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

	public FaceDAO() {
		this.faceRec = new FaceRec();
		// TODO;
		this.trainDirectory = "TODO";

		this.trainFaceRecognizer();
	}

	public void trainFaceRecognizer() {
		this.faceRec.processSelections(OWNER_IMG, trainDirectory, NUM_FACES,
				THRESHOLD);
	}

	public double getMatch(Bitmap faceObject) {
		MatchResult r = this.faceRec.findMatchResult(faceObject,
				Integer.parseInt(NUM_FACES), Double.parseDouble(THRESHOLD));

		if (r.getMatchSuccess() == false) {
			Log.d("FaceDAO", "no match");
		}

		if (r.getMatchMessage() != null) {
			Log.d("FaceDAO", r.getMatchMessage());
		}
		
		return r.getMatchDistance();
	}

}
