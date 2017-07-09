package com.zhg.bluetooth.util;

/**
 * Created zhg on 2017/3/16.
 * byte[] to decimals
 * byte[] to hex
 * hex to byte[]
 * hex to decimal
 */

public class BluetoothValueUtil {

    private static BluetoothValueUtil instance = null;

    private BluetoothValueUtil(){
    }

    public static BluetoothValueUtil getInstance() {
        synchronized (BluetoothValueUtil.class) {
            if (instance == null) {
                instance = new BluetoothValueUtil();
            }
        }
        return instance;
    }

    public static void destroy(){
        if (null!=instance) {
            instance = null;
        }
    }

    /**
     * byte[] 转十进制
     * @param bytes byte数组
     * @return 十进制数
     */
    public int[] bytesToDemicals(byte[] bytes)
    {
        int[] array = new int[bytes.length];
        int i = 0;
        for (byte b : bytes)
            array[i++] = b & 0xff;
        return array;
    }

    /**
     * 十六进制转十进制
     * @param hexStr 十六进制字符串
     * @return 十进制数
     */
    public  int hexToDecimal(String hexStr) {
        String digits = "0123456789ABCDEF";
        hexStr = hexStr.toUpperCase();
        int val = 0;
        for (int i = 0; i < hexStr.length(); i++) {
            char c = hexStr.charAt(i);
            int d = digits.indexOf(c);
            val = 16*val + d;
        }
        return val;
    }

    /**
     * 十进制转十六进制
     * @param decimal 十进制数
     * @return 十六进制字符串
     */
    public  String decimalToHex(int decimal) {
        String digits = "0123456789ABCDEF";
        if (decimal == 0) return "0";
        String hex = "";
        while (decimal > 0) {
            int digit = decimal % 16;                // rightmost digit
            hex = digits.charAt(digit) + hex;  // string concatenation
            decimal = decimal / 16;
        }
        return hex;
    }

    /***
     * byte[]转十六进制("BE B0 BC 92")
     * @param bytes byte数组
     * @return 十六进制字符串
     */
    public String bytesToHexStr(byte[] bytes) {

        String stmp;
        StringBuilder sb = new StringBuilder("");
        for (byte aByte : bytes) {
            stmp = Integer.toHexString(aByte & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    /**
     * 十六进制字符串("BEBOBC92")转bytes
     * @param hexstr 十六进制字符串
     * @return byte数组
     */
    public byte[] HexStringToBytes(String hexstr) {
        byte[] b = new byte[hexstr.length() / 2];
        int j = 0;
        for (int i = 0; i < b.length; i++) {
            char c0 = hexstr.charAt(j++);
            char c1 = hexstr.charAt(j++);
            b[i] = (byte) ((parse(c0) << 4) | parse(c1));
        }
        return b;
    }
    private static int parse(char c) {
        if (c >= 'a')
            return (c - 'a' + 10) & 0x0f;
        if (c >= 'A')
            return (c - 'A' + 10) & 0x0f;
        return (c - '0') & 0x0f;
    }

}
