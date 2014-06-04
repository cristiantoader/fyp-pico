package com.fyp.authenticator.location;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

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
		Log.d(TAG, "loadLocationData+");
		Scanner scanner = null;

		try {
			// check for owner files
			if (!isOwnerFile()) {
				Log.e(TAG, "Owner location file not found!");
				return;
			}
			
			// initialising read
			Log.d(TAG, "loadLocationData: initialising read.");
			scanner = new Scanner(new File(getAbsoluteFilePath()));

			// read location data
			Log.d(TAG, "loadLocationData: reading locations.");
			locations = new LinkedList<Location>();
			while (scanner.hasNext()) {
				String provider = scanner.next();
				Double latitude = scanner.nextDouble();
				Double longitude = scanner.nextDouble();

				Log.d(TAG, "Location: " + provider + " " + latitude + " "
						+ longitude);
				Location loc = new Location(provider);
				loc.setLatitude(latitude);
				loc.setLongitude(longitude);

				locations.add(loc);
			}

			scanner.close();
		} catch (FileNotFoundException e) {
			Log.d(TAG, "Owner file not found.");
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

				String line = location.getProvider() + " "
						+ location.getLatitude() + " "
						+ location.getLongitude();

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
	
	public double getMinDistance(Location current) {
		double min = Double.MAX_VALUE;
		
		Log.d(TAG, "getMinDistance+");
		
		// check if locations are not loaded
		if (this.locations == null) {
			Log.e(TAG, "getClosestMatchDistance: locations not loaded");
			return Double.MAX_VALUE;
		}
		
		// calculate minimum distance
		for (Location location : this.locations) {
			double distance = current.distanceTo(location);
			if (distance < min) {
				min = distance;
			}
		}
		
		Log.d(TAG, "getMinDistance- " + min);
		return min;
	}

	private String getAbsoluteFilePath() {
		return this.filePath + "/" + this.fileName;
	}

	private boolean isOwnerFile() {
		File ownerFile = new File(getAbsoluteFilePath());
		return ownerFile.exists();
	}
	
}
