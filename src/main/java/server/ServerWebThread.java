package server;

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
                    ServerWebResponse web = new ServerWebResponse(socket, out);
                    while ((inputLine = bis.readLine()) != null) {
                        //instr.append(inputLine);
                        //System.out.println("'" + inputLine + "'");
                        web.addheader(inputLine);
                        if (inputLine.equals(".q")) {  // testimiseks
                            socket.close();
                            break;
                        } else if (inputLine.equals(".k")) throw new RuntimeException(); // test
                        else if (inputLine.equals("")) { // päring sai läbi
                            web.sendresponse();
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
