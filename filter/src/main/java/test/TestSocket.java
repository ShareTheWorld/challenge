package test;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.RandomAccess;

public class TestSocket {
    public static void main(String args[]) throws Exception {
        Socket socket = new Socket("127.0.0.1", 8080);
        InputStream in = socket.getInputStream();

        byte bs[] = new byte[1024];
        int len = in.read(bs);
        System.out.println(new String(bs, 0, len));
//        RandomAccess randomAccess=new RandomAccess();

    }
}
