/**
 *
 */
package com.vkpapps.wifimanager.util;

import android.text.TextUtils;

/**
 * 字符串操作工具类
 *
 * @author chuck
 */
public class StringUtil {

    public static  String BLE_PREFIX="llkjs";

    public static final String EMPTY = "";

    public static boolean isEmpty(String text) {
        if (null == text || "".equals(text) || " ".equals(text) || "null".equals(text))
            return true;
        return false;
    }

    /**
     * @param : [str]
     * @return type: java.lang.String
     *
     * @method name: substring
     * @des: 截取价格类型字符串后面的.00, 例如11.00截取后返回11
     * @date 创建时间：2015/11/23 10:36
     */
    public static String substring(String str) {
        String s = "";
        if (!TextUtils.isEmpty(str)) {
            if (str.indexOf(".") != -1) {
                s = str.substring(0, str.indexOf("."));
            }
        }
        return s;
    }

    public static String str2HexStr(String str) {
        if(TextUtils.isEmpty(str)){
            return "";
        }
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
        }
        return sb.toString();
    }

    /**
     * 16进制字符串转换为字符串
     *
     * @param s
     * @return
     */
    public static String hexStringToString(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        s = s.replace(" ", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(
                        s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "utf-8");
            new String();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }
}
