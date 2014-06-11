package com.fyp.ctypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.util.Log;

/**
 * Class used for managing the application's cryptographic material.
 * 
 * The class uses the Android KeyStore in order to securely store an RSA key
 * pair used for protecting a master AES key. The master AES key is used for
 * cryptographic protection of owner data files.
 * 
 * @author cristi
 * 
 */
public class KeyManager {
	/** Application context used for file access. */
	private Context ctx = null;

	/** AES master key used for decryption of owner data files*/
	private SecretKey aesKey = null;
	/** File path for the AES master key.*/
	private String aesFilePath = null;
	/** AES master key file name.*/
	private static final String AES_FILENAME = "master-key.dat";
	/** Key size in bytes of the AES master key. */
	private static final int AES_KEYSIZE = 32;
	
	/**KeyStore alias used for RSA key pair. */
	private static final String ALIAS = "PicoAuthenticatorKP1";
	/**KeyStore algorithm for RSA key pair. */
	private static final String ALGORITHM = "RSA";
	
	/** Logging tag used for debugging. */
	private static final String TAG = "KeyManager";
	
	/** Singleton object for the class. */
	private static KeyManager singleton = null;
	
	/**
	 * Getter for the singleton class object.
	 * 
	 * @param ctx
	 *            application context.
	 * @return singleton class object.
	 */
	public static KeyManager getInstance(Context ctx) {
		Log.d(TAG, "getInstance+");
		
		if (singleton == null) {
			Log.d(TAG, "getInstance: generating singleton.");
			singleton = new KeyManager(ctx.getApplicationContext());
		}
		
		if (!singleton.isMasterKeyLoaded()) {
			singleton.loadMasterKey();
		}
		
		Log.d(TAG, "getInstance- " + (singleton != null));
		return singleton;
	}

	/**
	 * Private class constructor.
	 * 
	 * @param ctx
	 *            application context.
	 */
	private KeyManager(Context ctx) {
		this.ctx = ctx;
		this.aesFilePath = this.ctx.getFilesDir().toString();
	}

	/**
	 * Private method used for generating the AES master key.
	 * 
	 * A new RSA key pair is generated, stored in the Android KeyStore, and used
	 * for saving an encrypted copy of the AES master key in internal storage.
	 */
	private void generateMasterKey() {
		KeyPair kp = null;

		KeyPairGenerator kpGenerator = null;
		KeyPairGeneratorSpec kpSpec = null;
		RSAPublicKey pubKey = null;
		
		Log.d(TAG, "generateMasterKey()+");
		
		try {
			// generate RSA key pair in key store
			Log.d(TAG, "generate RSA key pair.");
			kpGenerator = KeyPairGenerator.getInstance(ALGORITHM, "AndroidKeyStore");
			kpSpec = generateStandardKeySpec();
			
			kpGenerator.initialize(kpSpec);
			kp = kpGenerator.generateKeyPair();
			
			// load RSA key pair
			Log.d(TAG, "load RSA key pair.");
			pubKey = (RSAPublicKey) kp.getPublic();
			
			// generate AES key
			Log.d(TAG, "generate AES key.");
			this.aesKey = generateSecretKey();

			// Write AES key encrypted with RSA key pair
			Log.d(TAG, "Write encrypted AES key.");
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);

			FileOutputStream fos = new FileOutputStream(getMasterKeyPath());
			CipherOutputStream cos = new CipherOutputStream(fos, cipher);
			
			byte[] byteAesKey = this.aesKey.getEncoded();
			cos.write(byteAesKey);
			cos.flush();
			cos.close();
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Log.d(TAG, "generateMasterKey()-");
	}
	
	/**
	 * Method used for loading the AES master key from internal storage. If no
	 * master key exists, then a new master key is created.
	 */
	private void loadMasterKey() {
		RSAPrivateKey privKey = null;
		Cipher cipher = null;
		
		FileInputStream fis = null;
		CipherInputStream cis = null;
		
		// check that master key is in storage
		if (!hasMasterKey()) {
			Log.w(TAG, "Need to generate a master key before loading!");
			generateMasterKey();
			return;
		}
		
		// load RSA Private Key
		privKey = getPrivateKey();
		if (privKey == null) {
			Log.e(TAG, "Private key is null!");
			return;
		}
		
		try {
			Log.d(TAG, "Initialising AES key load");
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, privKey);

			fis = new FileInputStream(getMasterKeyPath());
			cis = new CipherInputStream(fis, cipher);

			byte[] aesBytes = new byte[AES_KEYSIZE];
			int readAesBytes = cis.read(aesBytes);
			if (readAesBytes != AES_KEYSIZE) {
				Log.e(TAG, "Cipher input does not match keysize! ("
						+ readAesBytes + ")");
			}
			
			cis.close();
			fis.close();

			Log.d(TAG, "Reconstructing AES secret key.");
			this.aesKey = new SecretKeySpec(aesBytes, 0, AES_KEYSIZE, "AES");
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Method used for checking if the master key exists in internal storage.
	 * 
	 * @return true if the master key exists in internal storage.
	 */
	private boolean hasMasterKey() {
		return new File(getMasterKeyPath()).exists();
	}
	
	/**
	 * Method used for acquiring the RSA private key from the Android KeyStore
	 * that can be used for decoding the AES master key.
	 * 
	 * @return RSA private key used for decoding AES master key.
	 */
	private RSAPrivateKey getPrivateKey() {
		KeyStore ks = null;
		RSAPrivateKey privKey = null;
		
		try {
			ks = KeyStore.getInstance("AndroidKeyStore");
			ks.load(null);
			
			KeyStore.PrivateKeyEntry ke = (KeyStore.PrivateKeyEntry)ks.getEntry(ALIAS, null);
			if (ke == null) {
				Log.e(TAG, "Null key entry!");
				return null;
			}
			
			privKey = (RSAPrivateKey) ke.getPrivateKey();

		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnrecoverableEntryException e) {
			e.printStackTrace();
		}
		
		return privKey;
	}
	
	/**
	 * Method used by clients to get an AES master key encryption cipher.
	 * 
	 * @return AES master key encryption cipher
	 */
	public Cipher getEncryptionCipher() {
		Cipher cipher = null;

		if (this.aesKey == null) {
			Log.e(TAG, "Need to load AES key first!");
			return null;
		}
		
		try {
  			cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, this.aesKey);
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		
		return cipher;
	}
	
	/**
	 * Method used by clients to get an AES master key decryption cipher.
	 * 
	 * @return AES master key decryption cipher
	 */
	public Cipher getDecryptionCipher() {
		Cipher cipher = null;

		if (this.aesKey == null) {
			Log.e(TAG, "Need to load AES key first!");
			return null;
		}

		try {
			cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, this.aesKey);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}

		return cipher;
	}
	

	/**
	 * Method used for generating an RSA key specification used when generating
	 * an RSA key.
	 * 
	 * @return RSA key specification.
	 */
	private KeyPairGeneratorSpec generateStandardKeySpec() {
		KeyPairGeneratorSpec spec = null;

		Calendar start = new GregorianCalendar();
		Calendar end = new GregorianCalendar();
		end.add(Calendar.YEAR, 50);

		spec = new KeyPairGeneratorSpec.Builder(ctx)
				.setAlias(ALIAS)
				.setSubject(new X500Principal("CN=" + ALIAS))
				.setSerialNumber(BigInteger.ONE)
				.setStartDate(start.getTime())
				.setEndDate(end.getTime())
				.build();

		return spec;
	}
	
	/**
	 * Method used for generating the master AES key.
	 * 
	 * @return master AES key.
	 */
	private SecretKey generateSecretKey() {
		SecretKey skey = null;
		KeyGenerator kgen = null;

		try {
			kgen = KeyGenerator.getInstance("AES", "BC");
			kgen.init(AES_KEYSIZE * 8);
			
			skey = kgen.generateKey();
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		}
		
		return skey;
	}
	
	/**
	 * Getter for AES master key path, including file name.
	 * 
	 * @return AES master key path, including file name.
	 */
	private String getMasterKeyPath() {
		return this.aesFilePath + "/" + AES_FILENAME;
	}
	
	/**
	 * Method used for checking if the AES master key is loaded in the
	 * KeyManager.
	 * 
	 * @return true if the key is loaded in the KeyManager.
	 */
	private boolean isMasterKeyLoaded() {
		return this.aesKey != null;
	}

}
