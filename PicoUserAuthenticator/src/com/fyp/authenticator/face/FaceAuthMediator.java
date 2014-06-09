package com.fyp.authenticator.face;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.util.Log;
import facerecognition.javafaces.FaceRec;
import facerecognition.javafaces.MatchResult;

public class FaceAuthMediator {

	private FaceRec faceRec = null;

	private String trainDirectory = null;
	private String ownerImage = null;
	
	private static final String NUM_FACES = "1";
	private static final String THRESHOLD = "2";

	private static final String TAG = "FaceDAO";

	public FaceAuthMediator(Context ctx) {
		Log.d(TAG, "FaceDAO+");
		
		this.faceRec = new FaceRec(ctx);
		
		this.trainDirectory = ctx.getFilesDir().toString();
		this.ownerImage = getOwnerImage();
		
		this.trainFaceRecognizer();
		
		Log.d(TAG, "FaceDAO-");
	}

	public void trainFaceRecognizer() {
		Log.d(TAG, "trainFaceRecognizer+");
		
		MatchResult r = this.faceRec.processSelections(getAbsoluteFilePath(),
				trainDirectory, NUM_FACES, THRESHOLD);
		printMatch(r);
		
		Log.d(TAG, "trainFaceRecognizer-");
	}

	public double getMatch(Bitmap faceObject) {
		double result = 0;
		MatchResult r = null;
		
		Log.d(TAG, "getMatch+");
		
		r = this.faceRec.findMatchResult(faceObject,
				Integer.parseInt(NUM_FACES), Double.parseDouble(THRESHOLD));
		printMatch(r);

		if (r.getMatchSuccess() == false) {
			Log.d(TAG, "getMatch: No match found!");
			result = -1;
		} else {
			Log.d(TAG, "getMatch: Match found!");
			result = r.getMatchDistance();
		}

		Log.d(TAG, "getMatch- " + result);
		return result;
	}

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
	
	public static boolean hasFace(Bitmap img) {
		int maxFaces = 10;
		
		Face[] faces = new Face[maxFaces];
		
		FaceDetector fr = new FaceDetector(
				img.getWidth() - img.getWidth() % 2, 
				img.getHeight(), 
				maxFaces);
		
		fr.findFaces(img, faces);
		
		return faces.length != 0;
	}
}
