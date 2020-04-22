package com.aknayak.offchat.globaldata;

import android.util.Base64;
import android.util.Log;

import com.aknayak.offchat.MainActivity;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * OffChat
 * Created by Abdhesh Nayak on 4/22/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/

public class AESHelper {

    static String seed="XYbTtTQc6qjcYGIj5FxYsbfNLBgh4hoM";

    public static String encrypt(String clearText) {
        Log.d("UUUU",seed);
        byte[] encryptedText = null;
        try {
            byte[] keyData = seed.getBytes();
            SecretKey ks = new SecretKeySpec(keyData, "AES");
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, ks);
            encryptedText = c.doFinal(clearText.getBytes("UTF-8"));
            return Base64.encodeToString(encryptedText, Base64.DEFAULT);
        } catch (Exception e) {
            return "Message can not be encrypted.";
        }
    }

    /**
     * Decrypts the text
     * @param encryptedText The text you want to encrypt
     * @return Decrypted data if successful, or null if unsucessful
     */
    public static String decrypt (String encryptedText) {
        byte[] clearText = null;
        try {
            byte[] keyData = seed.getBytes();
            SecretKey ks = new SecretKeySpec(keyData, "AES");
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, ks);
            clearText = c.doFinal(Base64.decode(encryptedText, Base64.DEFAULT));
            return new String(clearText, "UTF-8");
        } catch (Exception e) {
            return "message can not be decrypted.";
        }
    }
}