package test;

import java.io.*;
import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.util.*;

public class Main {
    public static String trace1 = "trace1.data";
    public static String trace2 = "trace2.data";

    public static void main(String args[]) throws Exception {
        getInputStream();
    }

    public static void getInputStream() throws Exception {
        long startTime = System.currentTimeMillis();
        String path = "/Users/fht/d_disk/chellenger/data/";
//        String path = "/home/fu/Desktop/challege/";
        Reader in1 = new FileReader(path + trace1);
        Reader in2 = new FileReader(path + trace2);
        LineNumberReader r1 = new LineNumberReader(in1);
        String tmp;
        HashMap<String, List<String>> map = new HashMap<>();
        while ((tmp = r1.readLine()) != null) {
            String arr[] = tmp.split("\\|");
            List<String> list = map.get(arr[0]);
            if (list == null) {
                list = new ArrayList<>();
                map.put(arr[0], list);
            }
            list.add(tmp);
        }
        LineNumberReader r2 = new LineNumberReader(in2);
        while ((tmp = r2.readLine()) != null) {
            String arr[] = tmp.split("\\|");
            List<String> list = map.get(arr[0]);
            if (list == null) {
                list = new ArrayList<>();
                map.put(arr[0], list);
            }
            list.add(tmp);
        }

        Set<String> set = map.keySet();
        Iterator<String> iterator = set.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            String key = iterator.next();
            List<String> list = map.get(key);
            Collections.sort(list);
            String t = "";
            boolean isPrint = false;
            for (String str : list) {
                if (str.indexOf("error=1") > 0) {
                    isPrint = true;
                }
                if (str.indexOf("http.status_code") > 0 && str.indexOf("http.status_code=200") < 0) {
                    isPrint = true;
                }

                t += str + "\n";
            }

            if (isPrint) {
                count++;
//                System.out.println(count + "\n" + t + "\n\n\n");
            }
        }
        System.out.println("time=" + (System.currentTimeMillis() - startTime));

    }

    public static String MD5(String key) {
        char hexDigits[] = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        };
        try {
            byte[] btInput = key.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

}
