package com.fyp.ctypto;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
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

import javax.security.auth.x500.X500Principal;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;

public class KeyManager {

	private Context ctx = null;

	private Boolean keyLoaded = false;
	private RSAPublicKey pubKey = null;
	private RSAPrivateKey privKey = null;
	
	
	private static final String ALIAS = "PicoAuthenticatorKeyPair";
	private static final String ALGORITHM = "RSA";
	
	public KeyManager(Context ctx) {
		this.ctx = ctx;
		this.keyLoaded = false;
	}

	public void generateKeyPair() {
		KeyPairGenerator kpGenerator = null;
		KeyPairGeneratorSpec kpSpec = null;
		
		try {
			kpGenerator = KeyPairGenerator.getInstance(ALGORITHM, "AndroidKeyStore");
			kpSpec = generateStandardKeySpec();
			
			kpGenerator.initialize(kpSpec);
			
			kpGenerator.generateKeyPair();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
	}
	
	public void loadKeyPair() {
		KeyStore ks = null;
		
		try {
			ks = KeyStore.getInstance("AndroidKeyStore");
			ks.load(null);
			
			KeyStore.PrivateKeyEntry ke = (KeyStore.PrivateKeyEntry)ks.getEntry(ALIAS, null);
			
			this.pubKey = (RSAPublicKey) ke.getCertificate().getPublicKey();
			this.privKey = (RSAPrivateKey) ke.getPrivateKey();
			
			this.keyLoaded = true;
			
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
	}
	
	public RSAPublicKey getPublicKey() {
		if (keyLoaded != true || this.pubKey == null) {
			return null;
		}
		
		return this.pubKey;
	}
	
	public RSAPrivateKey getPrivateKey() {
		if (keyLoaded != true || this.privKey == null) {
			return null;
		}
		
		return this.privKey;
	}

	private KeyPairGeneratorSpec generateStandardKeySpec() {
		KeyPairGeneratorSpec spec = null;

		Calendar notBefore = Calendar.getInstance();
		Calendar notAfter = Calendar.getInstance();
		notAfter.add(1, Calendar.YEAR);

		spec = new KeyPairGeneratorSpec.Builder(ctx)
				.setAlias(ALIAS)
				.setSubject(new X500Principal(
						String.format("CN=%s, OU=%s", ALIAS, ctx.getPackageName())))
				.setSerialNumber(BigInteger.ONE)
				.setStartDate(notBefore.getTime())
				.setEndDate(notAfter.getTime()).build();

		return spec;
	}

}
