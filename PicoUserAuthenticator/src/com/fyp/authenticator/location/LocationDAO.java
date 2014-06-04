package com.fyp.authenticator.location;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.LinkedList;

import android.content.Context;
import android.location.Location;
import android.util.Log;

public class LocationDAO {

	private Context ctx = null;
	
	LinkedList<Location> locations = null;
	
	private static final String TAG = "LocationDAO";

	/** Audio file data. */
	private String fileName = null;
	private String filePath = null;

	public LocationDAO(Context ctx, String fileName) {
		this.ctx = ctx;

		this.fileName = fileName;
		this.filePath = this.ctx.getFilesDir().toString();
	}

	public void loadLocationData() {
		locations = new LinkedList<Location>();

		String path = getAbsoluteFilePath();
		FileInputStream fis = null;
		ObjectInputStream is = null;
		
		try {
			fis = ctx.openFileInput(path);
			is = new ObjectInputStream(fis);
			
			while (true) {
				try {
					Location loc = (Location) is.readObject();
					locations.add(loc);
				} catch (EOFException e) {
					break;
				} catch (OptionalDataException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			is.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void saveLocationData(LinkedList<Location> locations) {
		String path = getAbsoluteFilePath();
		FileWriter fwrite = null;

		Log.d(TAG, "saveLocationData+");
		
		try {
			Log.d(TAG, "saveLocationData: initialising output stream.");
			fwrite = new FileWriter(path);
			
			Log.d(TAG, "saveLocationData: saving locations.");
			for (Location location : locations) {
				String line = "" + location.getLatitude() + "," + location.getLongitude();
				Log.d(TAG, "saveLocationData: " + line);
				
				fwrite.append(line + "\n");
			}
			
			fwrite.flush();
			fwrite.close();
			

			Log.d(TAG, "saveLocationData: save ok.");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.locations = locations;
	}

	private String getAbsoluteFilePath() {
		return this.filePath + "/" + this.fileName;
	}

}
