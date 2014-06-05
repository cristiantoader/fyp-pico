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

public class KeyManager {

	private Context ctx = null;

	private SecretKey aesKey = null;
	
	private String filePath = null;
	
	private static final String ALIAS = "PicoAuthenticatorKP1";
	private static final String ALGORITHM = "RSA";
	private static final String AES_FILENAME = "master-key.dat";
	
	private static final int AES_KEYSIZE = 32;
	
	private static final String TAG = "KeyManager";
	
	private static KeyManager singleton = null;
	
	public static KeyManager getInstance(Context ctx) {
		Log.d(TAG, "getInstance+");
		
		if (singleton == null) {
			Log.d(TAG, "getInstance: generating singleton.");
			singleton = new KeyManager(ctx.getApplicationContext());
		}
		
		if (!singleton.isMasterKeyLoaded()) {
			singleton.loadMasterKey();
		}
		
		return singleton;
	}
	
	private KeyManager(Context ctx) {
		this.ctx = ctx;
		this.filePath = this.ctx.getFilesDir().toString();
	}

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
	
	private boolean hasMasterKey() {
		return new File(getMasterKeyPath()).exists();
	}
	
	private RSAPrivateKey getPrivateKey() {
		KeyStore ks = null;
		RSAPrivateKey privKey = null;
		
		try {
			ks = KeyStore.getInstance("AndroidKeyStore");
			ks.load(null);
			
			KeyStore.PrivateKeyEntry ke = (KeyStore.PrivateKeyEntry)ks.getEntry(ALIAS, null);
			if (ke == null) {
				Log.e(TAG, "Null key entry!");
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
	

	private KeyPairGeneratorSpec generateStandardKeySpec() {
		KeyPairGeneratorSpec spec = null;

		Calendar notBefore = Calendar.getInstance();
		Calendar notAfter = Calendar.getInstance();
		notAfter.add(1, Calendar.YEAR);

		spec = new KeyPairGeneratorSpec.Builder(ctx)
				.setAlias(ALIAS)
				.setSubject(new X500Principal(
						String.format("CN=%s", ALIAS)))
				.setSerialNumber(BigInteger.ONE)
				.setStartDate(notBefore.getTime())
				.setEndDate(notAfter.getTime()).build();

		return spec;
	}
	
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
	
	private String getMasterKeyPath() {
		return this.filePath + "/" + AES_FILENAME;
	}

	
	private boolean isMasterKeyLoaded() {
		return this.aesKey != null;
	}

}
