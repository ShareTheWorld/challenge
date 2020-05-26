package com.aliyun.filter.socket;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;

public class SocketHandle {
    private Socket socket;

    public SocketHandle(Socket socket) {
        try {
            this.socket = socket;
            this.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() throws Exception {
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        dispatchRequest(in, out);
        in.close();
        out.close();
    }

    /**
     * GET /ready HTTP/1.1
     * Host: localhost:8001
     * Connection: Keep-Alive
     * Accept-Encoding: gzip
     * User-Agent: okhttp/3.10.0
     * <p>
     * <p>
     * HTTP/1.1  200  OK
     * <p>
     * success
     */

    public void dispatchRequest(InputStream in, OutputStream out) {
        try {
            byte bs[] = new byte[1024];
            int len = in.read(bs);
            String req = new String(bs, 0, len);
            System.out.println(req);
            if (req.contains("ready")) {
                ready(req, out);
            }
            if (req.contains("setParameter")) {
                setParameter(req, out);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ready(String req, OutputStream out) throws Exception {
        out.write("HTTP/1.1 200 OK\r\n\r\nsuc".getBytes());
    }

    public void setParameter(String req, OutputStream out) throws Exception {
//        GET /setParameter?port=9000
        int s = req.indexOf('=');
        int e = req.indexOf(' ', s);
        int port = Integer.valueOf(req.substring(s + 1, e));
        System.out.println(port);
        out.write("HTTP/1.1 200 OK\r\n\r\nsuc".getBytes());

    }
}
