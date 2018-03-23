package chati_leiutis;

import java.net.Socket;

public class ClientThread implements Runnable {
    Socket socket;
    boolean cont = true;
    Klient client;

    public ClientThread(Socket socket, Klient client) {
        this.socket = socket;
        this.client = client;
    }

    @Override
    public void run() {

    }
}

//TODO teha client class lõime peale, et üks lõim kuulab serverit, teine ootan konsoolilt sisendeid