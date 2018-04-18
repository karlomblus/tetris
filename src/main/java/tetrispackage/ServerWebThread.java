package tetrispackage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

class ServerWebThread implements Runnable {
    BlockingQueue<Socket> socketid;

    public ServerWebThread(BlockingQueue<Socket> socketid) {
        this.socketid = socketid;
    }

    @Override
    public void run() {
        try {
            while (true) {

                Socket socket = socketid.take();
                ServerMain.debug(7, "thread võttis socketi");


                // PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);

                try (BufferedReader bis = new BufferedReader(new InputStreamReader(socket.getInputStream())); PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                    String inputLine;
                    while ((inputLine = bis.readLine()) != null) {
                        //instr.append(inputLine);
                        System.out.println("'" + inputLine + "'");
                        if (inputLine.equals(".q")) {
                            socket.close();
                            break;
                        }
                        if (inputLine.equals(".k")) throw new RuntimeException(); // test
                        if (inputLine.equals("")) { // päring sai läbi
                            System.out.println("saadame mingi kamarajura vastu");
                            out.println("HTTP/1.1 200 OK");
                            out.println("Server: Tetris scoreserver");
                            out.println("Cache-Control: no-cache, no-store, must-revalidate");
                            out.println("Pragma: no-cache");
                            out.println("Connection: Close");
                            out.println("Content-Type: text/html; charset=UTF-8");
                            out.println("");
                            out.println("<html><body>  Oled ühendunud tetrise serveri külge. <br><br>Varsti näed siin skoore ja saad mängu alla laadida. </body></html>");
                            out.close();
                            break;

                        } // päring sai läbi


                    } // loeme ridu socketist
                } // try with resources
                finally {
                    System.out.println("paneme socketi kinni");
                    socket.close();
                }

            } // WHILE
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    } // run
}
