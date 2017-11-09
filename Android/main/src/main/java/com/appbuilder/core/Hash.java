package com.appbuilder.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash
{
	private static String getHexString(byte digest[]) {
		StringBuffer hexString = new StringBuffer();
		for (int i=0; i<digest.length; i++)
			hexString.append(Integer.toHexString(0xFF & digest[i]));
		return hexString.toString();
	}
	
	private static String calculateFileAlg(File f, String alg) {
		InputStream is;
		try {
			is = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "";
		}
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance(alg);
			byte[] buffer = new byte[8192];
			int read;
			while( (read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}
			byte messageDigest[] = digest.digest();
			return Hash.getHexString(messageDigest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}
	
	private static String calculateStringAlg(String s, String alg) {
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance(alg);
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();
			return Hash.getHexString(messageDigest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String MD5(String s){
		return Hash.calculateStringAlg(s, "MD5");
	}
	
	public static String SHA1(String s){
		return Hash.calculateStringAlg(s, "SHA1");
	}
	
	public static String MD5(File f) {
		return Hash.calculateFileAlg(f, "MD5");
	}
	
	public static String SHA1(File f) {
		return Hash.calculateFileAlg(f, "SHA1");
	}
}

