package com.example;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Miothread extends Thread {

    Socket s;

    public Miothread(Socket s) {
        this.s = s;
    }

    private static String getContentType(String path) {
        if (path.endsWith(".html") || path.endsWith(".htm")) {
            return "text/html";
        } else if (path.endsWith(".css")) {
            return "text/css";
        } else if (path.endsWith(".js")) {
            return "application/javascript";
        } else if (path.endsWith(".png")) {
            return "image/png";
        } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (path.endsWith(".gif")) {
            return "image/gif";
        } else {
            return "";
        }
    }

    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;

        try {
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream(), true);
            DataOutputStream outBinary = new DataOutputStream(s.getOutputStream());

            String firstline = in.readLine();
            System.out.println(firstline);
            String path = firstline.split(" ")[1];
            String method = firstline.split(" ")[0];
            if (!method.equals("GET")) {
                out.println("HTTP/1.0 405 Method Not Allowed");
                s.close();
                return;
            }
            if (path.equals("/"))
                path = "/index.html";

            File file = new File("htdocs" + path);
            if (file.exists()) {
                out.println("HTTP/1.0 200 OK");
                out.println("Content-Type:" + getContentType(path));
                out.println("Content-Length: " + file.length());
                out.println("");
                InputStream input = new FileInputStream(file);
                byte[] buf = new byte[8192];
                int n;
                while ((n = input.read(buf)) != -1) {
                    outBinary.write(buf, 0, n);
                }
                input.close();
            } else {
                String notFoundMessage = "<html><body><h1>404 Not Found</h1></body></html>";
                out.println("HTTP/1.1 404 Not Found");
                out.println("Content-Type: text/html");
                out.println("Content-Length: " + notFoundMessage.length());
                out.println();
                out.println(notFoundMessage);
                out.flush();

            }
            String h;
            do {
                h = in.readLine();
                System.out.println(h);
            } while (!h.isEmpty());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}