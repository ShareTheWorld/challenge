package test;

import java.io.*;
import java.util.Arrays;

public class Main {
    public static String trace1 = "trace1.data";
    public static String trace2 = "trace2.data";

    public static void main(String args[]) throws Exception {
        getInputStream();
    }

    public static void getInputStream() throws Exception {
        Reader in1 = new FileReader("/Users/fht/Desktop/chellenger/" + trace2);
        Reader in2 = new FileReader("/Users/fht/Desktop/chellenger/" + trace2);
        LineNumberReader r1 = new LineNumberReader(in1);
        LineNumberReader r2 = new LineNumberReader(in2);
        String str = r1.readLine();
        String tmp;
        while ((tmp = r1.readLine()) != null) {
            String arr[] = tmp.split("\\|");
            System.out.println(Arrays.toString(arr));
        }

    }
}
