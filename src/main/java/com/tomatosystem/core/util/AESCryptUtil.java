package com.tomatosystem.core.util;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;


public class AESCryptUtil {
    private static final String ECB = "AES/ECB/PKCS5Padding";
    private static final String CBC = "AES/CBC/PKCS5Padding";
//	SSL을 적용 할 수 없는 환경 일경우 파라미터로 랜덤생성된 iv 과 암호화된 값을 전달하고 
// salt값은 사용자 id를 사용하는 방식으로 구현 필요 
    public static final int KEYSIZE = 128;
    public static final int   ITERATION_COUNT = 10000;
    public static String SALT = "0c68efe9e3c3a02b3f9f69a08987e4ab";
    public static String IV = "18b8e16db963ae9bfe9fccbe37d452e0";
    public static final String PASSPHRASE = "exb6Frame";
    
    public static byte[] encryptCBC(byte [] raw, byte[] key, byte[] iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(CBC);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
        
        return cipher.doFinal(raw);
    }
    
    public static String encryptECB(String raw, byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(ECB);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
        byte[] encodedBytes = Base64.encodeBase64(cipher.doFinal(raw.getBytes()));
        return new String(encodedBytes);

    }
    
    public static byte[] decryptCBC(byte [] encData, byte[] key, byte[] iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(CBC);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
        
        return cipher.doFinal(encData);
    }
    
    public static String decryptECB(String encData, byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
    	byte[] output = null;
    	Cipher cipher = Cipher.getInstance(ECB);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
//        BASE64Decoder decoder = new BASE64Decoder();
        byte[] decodedBytes = Base64.decodeBase64(encData);
        output = cipher.doFinal(decodedBytes);
        return new String(output);
    }
    
    public static String encrypt(String salt, String iv, String passphrase, String plaintext, int iterationCount, int keySize) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), Hex.decodeHex(salt.toCharArray()), iterationCount, keySize);
        SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(Hex.decodeHex(iv.toCharArray())));
        byte[] encrypted = cipher.doFinal(plaintext.getBytes("UTF-8"));
        return Base64.encodeBase64String(encrypted);
    }
    
    public static String decrypt(String salt, String iv, String passphrase, String ciphertext, int iterationCount, int keySize) throws Exception {        
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), Hex.decodeHex(salt.toCharArray()), iterationCount, keySize);
        SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(Hex.decodeHex(iv.toCharArray())));
        byte[] decrypted = cipher.doFinal(Base64.decodeBase64(ciphertext));
        return new String(decrypted, "UTF-8");
    }
        
    public static byte[] genKey(int size) throws NoSuchAlgorithmException {
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(size, secureRandom);

        SecretKey key = keyGenerator.generateKey();
        return key.getEncoded();
    }
}
