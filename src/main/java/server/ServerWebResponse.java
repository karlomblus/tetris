package server;

import java.io.*;
import java.net.Socket;
import java.util.Set;
import java.util.regex.Pattern;

public class ServerWebResponse {

    String filename = null; // fail mida välja saadame. Muutuja väärtustame ainult siis kui see on OK
    String rawurl = "";
    String moodul = "file"; // shtml = parsime muutujaid   file=saadame tuimalt faili
    String contentType = null;
    String userAgent = "";    // väärtustame viisaka apache stiilis logimise jaoks
    String hostIP;
    String referer = "";

    Socket socket;
    PrintWriter out;
    private static final String lubatud = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890.";
    private static final Set<String> BINARYTYPES = Set.of("jar", "exe");

    public ServerWebResponse(Socket socket, PrintWriter out) {
        this.out = out;
        this.socket = socket;
        hostIP = socket.getInetAddress().toString();
    }

    public void addheader(String inputLine) {
        //'GET /favicon.ico HTTP/1.1'
        //'Host: tetris.carlnet.ee'
        //'Connection: keep-alive'
        //'User-Agent: Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36'
        //'Accept: image/webp,image/apng,image/*,*/*;q=0.8'
        //'Referer: http://tetris.carlnet.ee/'
        //'Accept-Encoding: gzip, deflate'
        //'Accept-Language: et-EE,et;q=0.9,en-US;q=0.8,en;q=0.7'
        String[] tykid = inputLine.split(" ");
        if (tykid[0].equals("GET")) {
            rawurl = tykid[1].substring(1);
            if (tykid[1].equals("/")) rawurl = "index.shtml";

            if (ulrllubatud(rawurl)) { // kas urli sümbolid on OK?
                filename = rawurl;
                // laiendi järgi leiame mis selle päringuga teha tuleb
                String[] urlitykk = filename.split(Pattern.quote("."));
                if (urlitykk[urlitykk.length - 1].equals("shtml")) {
                    this.moodul = "shtml";
                    this.contentType = "text/html; charset=UTF-8";
                } else if (urlitykk[urlitykk.length - 1].equals("html")) {
                    this.contentType = "text/html; charset=UTF-8";
                } else if (urlitykk[urlitykk.length - 1].equals("json")) {
                    this.contentType = "text/html; charset=UTF-8";
                    this.moodul = "json";
                } else if (urlitykk[urlitykk.length - 1].equals("txt")) {
                    this.contentType = "text/plain; charset=UTF-8";
                } else if (urlitykk[urlitykk.length - 1].equals("png")) {
                    this.contentType = "image/png";
                } else if (
                        BINARYTYPES.contains(urlitykk[urlitykk.length - 1])) {
                    this.contentType = "application/octet-stream";
                }

            } // url ok


        } else if (tykid[0].equals("User-Agent:")) {
            userAgent = tykid[1];
        } else if (tykid[0].equals("Referer:")) {
            referer = tykid[1];
        }

    }

    private boolean ulrllubatud(String url) {
        for (int i = 0; i < url.length(); i++) {
            if (keelatudtaht(url.substring(i, i + 1))) return false;
        }
        return true;
    }

    private boolean keelatudtaht(String substring) {
        for (int i = 0; i < lubatud.length(); i++) {
            if (substring.equals(lubatud.substring(i, i + 1))) return false;
        }
        return true;
    }

    public void sendresponse() throws IOException {

        ServerMain.debug(6, "url: " + rawurl);
        if (contentType != null) ServerMain.debug(6, "contenttype: " + contentType);
        ServerMain.debug(6, "moodul: " + moodul);
        ServerMain.debug(6, "host: " + hostIP);

        if (filename == null) {
            senderror(); // ma ei tea mida must tahetakse või oli url keelatud
            return;
        }

        if (moodul.equals("file")) {
            sendfile();
            return;
        } else if (moodul.equals("json")) {
            sendJson();
            return;
        } else if (moodul.equals("shtml")) {
            sendfile();
            return;
        } // todo: parsimine teha

        // kui siia jõuame, siis ma ei tea mida must tahetakse
        senderror();

    }

    private void sendJson() {
        System.out.println("json faili tahetakse: " + filename);

        if (filename.equals("top10.json")) {
            sendHeader(200, "OK");
            String tulemus = "{\n" +
                    "    1: {username:\"nimi\",score:55}, \r\n" +
                    "    2: {username:\"nimi\",score:55}, \n\n" +
                    "    3: {username:\"nimi\",score:55} \n\n" +
                    "   }";
            out.printf(tulemus);

        } else {
            send404();
        }
    }

    private void sendfile() throws IOException {

        ServerMain.debug(4, "WEB: requested file: " + rawurl + "");

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream inStream = classloader.getResourceAsStream("webroot/" + filename); BufferedOutputStream outStream = new BufferedOutputStream(socket.getOutputStream())) {
            //try (BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(new File("webroot/"+filename)));BufferedOutputStream outStream = new BufferedOutputStream(socket.getOutputStream()) ) {

            if (inStream == null) {
                ServerMain.debug(4, "WEB: sendfile: instream oli NULL, faili pole.");
                send404();
                return;
            }

            sendHeader(200, "OK");


            final byte[] buffer = new byte[4096];
            for (int read = inStream.read(buffer); read >= 0; read = inStream.read(buffer))
                outStream.write(buffer, 0, read);

        }

    }


    private void send404() {
        ServerMain.debug(6, "WEB: 404: ei leia faili: " + rawurl);
        sendHeader(404, "File Not Found");
        out.println("<html><body>  Sorry. Faili  " + rawurl + " ei leitud<br>\n </body></html>");   // html sisus pole korrektne reavahetus vist enam oluline sest browser peaks failiga ka sel kujul hakkama saama?
    }

    private void senderror() {
        ServerMain.debug(6, "Weebiserver saadab vastu 400 errori");
        sendHeader(400, "bad reques");
        out.println("<html><body>  Kahjuks ei mõista server seda päringut <br><br>\nURL: " + rawurl + "\n<br>Moodul: " + moodul + "<br>\n </body></html>");
    }

    private void sendHeader(int code, String message) {
        out.printf("HTTP/1.1 " + code + " " + message + "\r\n");
        out.printf("Server: Tetris scoreserver\r\n");
        out.printf("Cache-Control: no-cache, no-store, must-revalidate\r\n");
        out.printf("Pragma: no-cache\r\n");
        out.printf("Connection: Close\r\n");
        if (contentType != null) out.printf("Content-Type: " + contentType + "\r\n");
        out.printf("\r\n");
    }
}
