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

    private static void sendFile(File file, DataOutputStream out, String contentType) throws IOException {
        out.writeBytes("HTTP/1.1 200 OK\n");
        out.writeBytes("Content-Length: " + file.length() + "\n");
        out.writeBytes("Content-Type: " + contentType + "\n");
        out.writeBytes("\n");

        try (InputStream input = new FileInputStream(file)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = input.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
        }
    }

    private static String readBody(BufferedReader in, int contentLength) throws IOException {
        if (contentLength <= 0) {
            return "";
        }
        char[] buf = new char[contentLength];
        int read = 0;
        while (read < contentLength) {
            int n = in.read(buf, read, contentLength - read);
            if (n == -1) {
                break;
            }
            read += n;
        }
        return new String(buf, 0, read);
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
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                out.println("HTTP/1.0 405 Method Not Allowed");
                s.close();
                return;
            }

            int contentLength = 0;
            String accept = "";
            String line;
            while (!(line = in.readLine()).isEmpty()) {
                System.out.println(line);
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(" ")[1]);
                }
                if (line.startsWith("Accept:")) {
                    accept = (line.split(" ")[1]);
                }
            }
            if (method.equals("GET")) {
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
                    String notFoundMessage = "<html><body><h1>404 Not Found SCEMO DI MERDA</h1></body></html>";
                    out.println("HTTP/1.1 404 Not Found");
                    out.println("Content-Type: text/html");
                    out.println("Content-Length: " + notFoundMessage.length());
                    out.println();
                    out.println(notFoundMessage);
                    out.flush();

                }
            } else if (method.equals("HEAD")) {
                if (path.equals("/"))
                    path = "/index.html";
                File file = new File("htdocs" + path);
                if (file.exists()) {
                    out.println("HTTP/1.0 200 OK");
                    out.println("Content-Type:" + getContentType(path));
                    out.println("Content-Length: " + file.length());
                    out.println("");
                } else {
                    String notFoundMessage = "<html><body><h1>404 Not Found SCEMO DI MERDA</h1></body></html>";
                    out.println("HTTP/1.1 404 Not Found");
                    out.println("Content-Type: text/html");
                    out.println("Content-Length: 0");
                    out.println();
                    out.println(notFoundMessage);
                    out.flush();
                }
            } else if (method.equals("POST")) {

                String body = readBody(in, contentLength);
                String responseMessage = "<h3>POST Data Received</h3><p>" + body + "</p>";
                System.out.println("Body: " + body);

                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/html");
                out.println("Content-Length: " + responseMessage.length());
                out.println();
                out.println(responseMessage);
                out.flush();
            }
            /*
             * String h;
             * do {
             * h = in.readLine();
             * System.out.println(h);
             * } while (!h.isEmpty());
             */
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}