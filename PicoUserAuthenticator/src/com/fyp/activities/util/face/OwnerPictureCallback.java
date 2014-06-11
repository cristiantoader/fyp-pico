package com.fyp.activities.util.face;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.FaceDetector;
import android.util.Log;

/**
 * Callback class used by FaceActivity.
 * 
 * The class is responsible for configuring and processing data generated when
 * taking a picture. This will be stored in internal memory and used by the face
 * recognition mechanism for training.
 * 
 * @author cristi
 * 
 */
public class OwnerPictureCallback implements PictureCallback {
	/** Object used for detecting if the face sample is valid. */
	private FaceDetector mFaceDetector = null;

	/** File name use when saving the sample. */
	private String fileName = null;
	/** Absolute file path used when saving the sample. */
	private String filePath = null;

	/** Logging tag used for debugging. */
	private static final String TAG = "OwnerPictureCallback";

	/**
	 * Main constructor for the class.
	 * 
	 * Based on the supplied application context, the constructor configures the
	 * file name and path where picture data will be saved.
	 * 
	 * @param ctx
	 */
	public OwnerPictureCallback(Context ctx) {
		this.filePath = ctx.getFilesDir().toString();
		this.fileName = "owner" + getNextOwner() + ".png";
	}

	/***
	 * Returns the next available owner id.
	 * 
	 * The method checks the application directory for existing owner data
	 * files. It then generates a new id that will be used by the constructor to
	 * create a new unique owner recording.
	 * 
	 * @return unique id that can be used when creating a new owner sample file.
	 */
	private int getNextOwner() {
		int lastOwner = 0;
		File dir = new File(this.filePath);

		if (!dir.exists() || !dir.isDirectory()) {
			Log.e(TAG, "Error with owner file!");
			return 0;
		}

		for (File file : dir.listFiles()) {
			String name = file.getName();

			if (file.isDirectory() || !name.endsWith(".png")) {
				continue;
			}

			if (name.startsWith("owner")) {
				int owner = 0;
				for (int i = 5; Character.isDigit(name.charAt(i)); i++) {
					owner *= 10;
					owner += name.charAt(i);
				}

				if (owner > lastOwner) {
					lastOwner = owner;
				}
			}
		}

		Log.d(TAG, "next owner: " + lastOwner + 1);
		return lastOwner + 1;
	}

	/**
	 * Processes data when a picture was taken.
	 * 
	 * The method creates a BMP scaled to 50% of the original size, and saves
	 * the data to internal storage if any faces were detected.
	 * 
	 */
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		camera.stopPreview();
		camera.release();

		Bitmap bmp = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;

		if (data == null) {
			Log.e(TAG, "null data");
			return;
		}

		bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
		if (bmp == null) {
			Log.e(TAG, "Decoded bmp is null!");
			return;
		} else {
			Log.d(TAG, "Decoded bmp is ok!");
		}

		Matrix rotMatrix = new Matrix();
		rotMatrix.postRotate(270);

		bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(),
				rotMatrix, true);
		bmp = Bitmap.createScaledBitmap(bmp, (int) (0.5 * bmp.getWidth()),
				(int) (0.5 * bmp.getHeight()), true);

		FaceDetector.Face[] faces = new FaceDetector.Face[10];
		this.mFaceDetector = new FaceDetector(bmp.getWidth(), bmp.getHeight(),
				10);
		int numFaces = this.mFaceDetector.findFaces(bmp, faces);

		Log.d(TAG, "Found " + numFaces + " faces.");
		if (numFaces == 1) {

			try {
				Log.d(TAG, "writing to " + getAbsoluteFilePath());

				FileOutputStream fos = new FileOutputStream(
						getAbsoluteFilePath());
				bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);

				fos.flush();
				fos.close();

			} catch (FileNotFoundException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();

			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			}

		} else {
			Log.e(TAG, "Too many faces!");
		}

		Log.d(TAG, "Terminated ok-");
	}

	/**
	 * Getter for the absolute file path, including the name of the file.
	 * 
	 * @return the absolute file path.
	 */
	private String getAbsoluteFilePath() {
		return this.filePath + "/" + this.fileName;
	}

}
