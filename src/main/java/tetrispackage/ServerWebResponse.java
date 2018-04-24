package tetrispackage;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class ServerWebResponse {
    String url = "";
    String moodul = "";
    PrintWriter out;
    private static final String lubatud = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890.";

    public ServerWebResponse(PrintWriter out) {
        this.out = out;
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
            String myurl = tykid[1].substring(1);
            if (tykid[1].equals("/")) myurl="index.shtml";

            if (ulrllubatud(myurl)) { // kas urli sümbolid on OK?
                String[] urlitykk = myurl.split(Pattern.quote("."));
                if (urlitykk[urlitykk.length - 1].equals("shtml")) {
                    this.moodul = "shtml";
                }
                if (urlitykk[urlitykk.length - 1].equals("html")) {
                    this.moodul = "html";
                }
                this.url=myurl;
                System.out.println(urlitykk[1]);
            } // url ok


        }

    }

    private boolean ulrllubatud(String url) {
        for (int i = 0; i < url.length(); i++) {
            if (keelatudtaht(url.substring(i, i+1))) return false;
        }
        return true;
    }

    private boolean keelatudtaht(String substring) {
        for (int i = 0; i < lubatud.length(); i++) {
            if (substring.equals(lubatud.substring(i, i+1))) return false;
        }
        return true;
    }

    public void sendresponse() {

        if (moodul.length() < 2 || url.length() < 2) {
            senderror(); // ma ei tea mida must tahetakse
            return;
        }
        System.out.println("saadame mingi kamarajura vastu");
        System.out.println("url: " + url);
        System.out.println("moodul: " + moodul);
        out.println("HTTP/1.1 200 OK");
        out.println("Server: Tetris scoreserver");
        out.println("Cache-Control: no-cache, no-store, must-revalidate");
        out.println("Pragma: no-cache");
        out.println("Connection: Close");
        out.println("Content-Type: text/html; charset=UTF-8");
        out.println("");
        out.println("<html><body>  Oled ühendunud tetrise serveri külge. <br><br>Varsti näed siin skoore ja saad mängu alla laadida. </body></html>");

    }

    private void senderror() {
        ServerMain.debug(6,"Weebiserver saadab vastu errori");
        out.println("HTTP/1.1 500 Error");
        out.println("Server: Tetris scoreserver");
        out.println("Cache-Control: no-cache, no-store, must-revalidate");
        out.println("Pragma: no-cache");
        out.println("Connection: Close");
        out.println("Content-Type: text/html; charset=UTF-8");
        out.println("");
        out.println("<html><body>  Kahjuks ei mõista server seda päringut <br><br>\nURL: "+url+"\n<br>Moodul: "+moodul+"<br>\n </body></html>");
    }
}
