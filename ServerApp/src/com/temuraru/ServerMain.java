package com.temuraru;

public class ServerMain {

    public static void main(String[] args) {
        int port = 8867;
        Server server = new Server(port);
        server.start();
    }
}
