package com.tingfeng.syrun.common.util;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

public class Base64Util {
    public static String enCodeToBase64(String content) throws UnsupportedEncodingException {
        String asB64 = Base64.getEncoder().encodeToString(content.getBytes("utf-8"));
        return asB64;
    }

    public static String deCodeFromBase64(String content) throws UnsupportedEncodingException {
        byte[] asBytes = Base64.getDecoder().decode(content);
        return new String(asBytes, "utf-8");
    }
}
