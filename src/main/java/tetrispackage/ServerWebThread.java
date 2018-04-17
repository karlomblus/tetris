package tetrispackage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
                 ServerMain.debug(7,"thread võttis socketi");

                 try (BufferedReader bis = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                     String inputLine;
                     while ((inputLine = bis.readLine()) != null) {
                         //instr.append(inputLine);
                         System.out.println(inputLine);
                         if (inputLine.equals(".q")) {
                             socket.close();
                             break;
                         }
                         if (inputLine.equals(".k")) throw new RuntimeException(); // test
                     } // loeme ridu socketist
                 } // try with resources


             } // WHILE
         } catch (Exception e) {
             throw new RuntimeException(e);
         }


     } // run
 }
