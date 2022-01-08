package com.common.msg.api.util;

import com.alibaba.fastjson.JSON;
import com.common.msg.api.config.ConfigManager;
import com.common.msg.api.exception.MqClientException;
import com.common.msg.api.exception.MqConfigException;
import com.common.msg.api.exception.SerializationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Iterator;


public class StringUtil {
    public static String[] parseSvrUrl(String svrUrl) {
        String[] strs = svrUrl.split("\\|");
        if (strs.length != 2 && (isNullOrEmpty(strs[0]) || isNullOrEmpty(strs[1]))) {
            throw new MqConfigException("Parse 'svrUrl' failed. svrUrl:" + svrUrl);
        }
        return strs;
    }

    public static String getValue(String key) {
        if (key.contains("${")) {
            return ConfigManager.getString(key, "");
        }
        return key;
    }


    public static String join(Collection var0, String var1) {
        StringBuilder var2 = new StringBuilder();

        for (Iterator var3 = var0.iterator(); var3.hasNext(); var2.append(String.valueOf(var3.next()))) {
            if (var2.length() != 0) {
                var2.append(var1);
            }
        }

        return var2.toString();
    }

    public static String join(String separator, String... vars) {
        StringBuilder var1 = new StringBuilder();

        for (String var2 : vars) {
            if (var2.length() != 0) {
                if (var1.length() != 0) {
                    var1.append(separator);
                }
                var1.append(var2);
            }
        }

        return var1.toString();
    }

    public static String toString(byte[] bytes) throws Exception {
        return new String(bytes, "UTF-8");
    }

    public static byte[] toBytes(Object obj, int maxSize) throws Exception {
        String value;
        if (obj == null) throw new SerializationException("Data can't be null.");


        if (obj instanceof String) {
            value = (String) obj;
        } else {
            value = toJsonString(obj);
        }

        if (isNullOrEmpty(value)) {
            throw new SerializationException("Data can't be null.");
        }

        byte[] bytes = value.getBytes("UTF-8");
        if (maxSize == -1 || bytes.length <= maxSize) {
            return bytes;
        }
        throw new Exception("Data size over max size. size:" + bytes.length + ", max size:" + maxSize);
    }

    public static String toJsonString(Object obj) {
        return JSON.toJSONString(obj);
    }

    public static String removeSpecialCharacter(String str) {
        return str.replaceAll("[.-]", "_").trim();
    }

    public static String remove(String target, String[] replacement) {
        String result = target;
        for (String val : replacement) {
            result = target.replace(val, "");
        }
        return result;
    }

    public static boolean isNullOrEmpty(String string) {
        return (string == null || string.length() == 0);
    }

    public static String buildMessageId(int partition, long offset) {
        StringBuilder sb = new StringBuilder(2);
        sb.append(partition).append("_");
        sb.append(offset);
        return sb.toString();
    }


    static String messageFormat(String template, Object... args) {
        if (args == null) return "";

        template = String.valueOf(template);


        StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
        int templateStart = 0;
        int i = 0;
        while (i < args.length) {
            int placeholderStart = template.indexOf("%s", templateStart);
            if (placeholderStart == -1) {
                break;
            }
            builder.append(template.substring(templateStart, placeholderStart));
            builder.append(args[i++]);
            templateStart = placeholderStart + 2;
        }
        builder.append(template.substring(templateStart));


        if (i < args.length) {
            builder.append(" [");
            builder.append(args[i++]);
            while (i < args.length) {
                builder.append(", ");
                builder.append(args[i++]);
            }
            builder.append(']');
        }

        return builder.toString();
    }


    public static String getMd5_16(String str) {

        return getMd5(str).substring(8, 24);
    }


    public static String getMd5(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(str.getBytes("UTF-8"));
            return byte2Hex(messageDigest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static String getSha256(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            return byte2Hex(messageDigest.digest());
        } catch (Exception e) {
            throw new MqClientException("Can not get sha256 from:" + str, e);
        }
    }


    private static String byte2Hex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();

        for (byte aByte : bytes) {
            String temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                builder.append("0");
            }
            builder.append(temp);
        }
        return builder.toString();
    }

    public static String stackToString(Throwable throwable) throws IOException {
        if (null == throwable) return null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            throwable.printStackTrace(new PrintStream(baos));
        } finally {
            baos.close();
        }
        return baos.toString();
    }

    public static void main(String[] args) {
        System.out.println(removeSpecialCharacter(" .eklw-.3kes- "));
        System.out.println(getMd5("dsfkjoweieefesefsfsefb7052c207b5a43bef107a2d073d9b02eeeefojl"));
        System.out.println("fcp-creditcore-creditcard-creditcore-creditcard-trade-201807191119");
        System.out.println(join("_", new String[]{"tt33", "", "kk66"}));
    }
}


