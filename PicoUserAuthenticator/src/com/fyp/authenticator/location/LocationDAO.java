package com.fyp.authenticator.location;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import com.fyp.ctypto.KeyManager;

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

	public void loadOwnerData() {
		KeyManager km = null;
		
		CipherInputStream cis = null;
		FileInputStream fis = null;
		
		Scanner scanner = null;
		
		Log.d(TAG, "loadLocationData+");

		try {
			// check for owner files
			if (!isOwnerFile()) {
				Log.e(TAG, "Owner location file not found!");
				return;
			}
			
			km = KeyManager.getInstance(ctx);
			
			// initialising read
			Log.d(TAG, "loadLocationData: initialising read.");
			fis = new FileInputStream(getAbsoluteFilePath());
			cis = new CipherInputStream(fis, km.getDecryptionCipher());
			
			int b;
			ArrayList<Byte> decByteList = new ArrayList<Byte>();
			while((b = cis.read()) != -1) {
				decByteList.add((byte) b);
			}
			
			byte[] byteArray = new byte[decByteList.size()];
			for (int i = 0; i < byteArray.length; i++) {
				byteArray[i] = decByteList.get(i);
			}
			
			String decRes = new String(byteArray);
			scanner = new Scanner(decRes);
			
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
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void saveOwnerData(LinkedList<Location> locations) {
		KeyManager km = null;
		
		FileOutputStream fos = null;
		CipherOutputStream cos = null;
		
		String path = getAbsoluteFilePath();
		StringBuffer sb = new StringBuffer();

		Log.d(TAG, "saveLocationData+");

		try {
			Log.d(TAG, "saveLocationData: initialising key manager.");
			km = KeyManager.getInstance(ctx);
			
			Log.d(TAG, "saveLocationData: initialising output stream.");
			fos = new FileOutputStream(path);
			cos = new CipherOutputStream(fos, km.getEncryptionCipher());

			Log.d(TAG, "saveLocationData: building locations.");
			for (Location location : locations) {
				String line = location.getProvider() + " "
						+ location.getLatitude() + " "
						+ location.getLongitude() + "\n";
				
				sb.append(line);
			}

			Log.d(TAG, "saveLocationData: saving locations.");
			Log.d(TAG, sb.toString());
			cos.write(sb.toString().getBytes());
			cos.flush();
			cos.close();

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
