package com.iba.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordEncoderMD5 {

    /**
     * Encode password and add salt.
     *
     * @param password - user password
     * @return encoded password
     * @throws NoSuchAlgorithmException if method of encode not found
     */
    public String createPassword(String password) throws NoSuchAlgorithmException {
        String salt = "zhulick_ne_vorui";
        password = encode(password);
        password += salt;
        password = encode(password);
        return password;
    }

    /**
     * Encode password.
     *
     * @param password - user password
     * @return encoded password
     * @throws NoSuchAlgorithmException if method of encode not found
     */
    private String encode(String password) throws NoSuchAlgorithmException {
        StringBuilder code = new StringBuilder();
        MessageDigest messageDigest;
        messageDigest = MessageDigest.getInstance("MD5");
        byte[] bytes = password.getBytes();
        byte[] digest = messageDigest.digest(bytes);
        for (byte aDigest : digest) {
            code.append(Integer.toHexString(0x0100 + (aDigest & 0x00FF)).substring(1));
        }
        password = code.toString();
        return password;
    }
}
