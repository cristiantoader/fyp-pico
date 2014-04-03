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
import android.os.Environment;
import android.util.Log;

public class OwnerPictureCallback implements PictureCallback {

	private FaceDetector mFaceDetector = null;

	private String fileName = null;
	private String filePath = null;

	private static final String TAG = "OwnerPictureCallback";

	public OwnerPictureCallback(Context ctx) {
		this.filePath = ctx.getFilesDir().toString();
		this.fileName = "owner" + getNextOwner() + ".png";
	}
	
	private int getNextOwner() {
		int lastOwner = 0;
		File dir = new File (this.filePath);
		
		if (!dir.exists() || !dir.isDirectory()) {
			Log.e(TAG, "Error with owner file!");
			return 0;
		}
		
		for(File file : dir.listFiles()) {
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

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		camera.stopPreview();
		camera.release();

		Bitmap bmp = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;	
		
		if (data == null) {
			Log.e("FaceService", "null data");
			return;
		}

		bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
		if (bmp == null) {
			Log.e("FaceService", "Decoded bmp is null!");
			return;
		} else {
			Log.d("FaceService", "Decoded bmp is ok!");
		}
		
		Matrix rotMatrix = new Matrix();
		rotMatrix.postRotate(270);
		
		bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), rotMatrix, true);
		bmp = Bitmap.createScaledBitmap(bmp, 
				(int) (0.5 * bmp.getWidth()), 
				(int) (0.5 * bmp.getHeight()), true);
		
		
		// TODO: extract face and save face only!
		FaceDetector.Face[] faces = new FaceDetector.Face[10];
		this.mFaceDetector = new FaceDetector(bmp.getWidth(), bmp.getHeight(), 10);
		int numFaces = this.mFaceDetector.findFaces(bmp, faces);

		Log.d(TAG, "Found " + numFaces + " faces.");
		if (numFaces == 1) {

			try {
				Log.d(TAG, "writing to " + getAbsoluteFilePath());

				FileOutputStream fos = new FileOutputStream(getAbsoluteFilePath());
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

	private String getAbsoluteFilePath() {
		 return this.filePath + "/" + this.fileName;
	}
	
	@SuppressWarnings("unused")
	private String getSdAbsoulteFilePath() {
		File root = Environment.getExternalStorageDirectory();
		File dir = new File(root.getAbsolutePath() + "/test-faces");

		if (!dir.exists() && dir.mkdirs() == false) {
			Log.e(TAG, "could not create dirs..");
		}

		File file = new File(dir, "owner.png");

		return file.getAbsolutePath();
	}

}