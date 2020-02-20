package com.iba.utils;

import org.springframework.stereotype.Component;

@Component
public class EncodeChanger {

    /**
     * Encode string to Unicode for write it in properties file.
     *
     * @param uniStr - string to encode
     * @return encoded string
     */
    public String unicode2UnicodeEsc(String uniStr) {
        StringBuffer ret = new StringBuffer();
        String hexStr;
        if (uniStr == null) {
            return null;
        }
        int maxLoop = uniStr.length();
        for (int i = 0; i < maxLoop; i++) {
            char character = uniStr.charAt(i);
            if (character <= '') {
                ret.append(character);
            } else {
                ret.append("\\u");
                hexStr = Integer.toHexString(character).toLowerCase();
                int zeroCount = 4 - hexStr.length();
                for (int j = 0; j < zeroCount; j++) {
                    ret.append('0');
                }
                ret.append(hexStr);
            }
        }
        return ret.toString();
    }
}
