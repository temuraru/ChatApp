package com.temuraru;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Thread {
    private final int port;
    private ArrayList<ClientHandler> clientsList = new ArrayList<>();
    private ArrayList<GroupHandler> groupsList = new ArrayList<>();

    public Server(int port) {
        this.port = port;
    }

    public ArrayList<ClientHandler> getClientsList() {
        return clientsList;
    }
    public ArrayList<GroupHandler> getGroupsList() {
        return groupsList;
    }

    public void broadcastMessage(String msg) throws IOException {
        for (ClientHandler client: clientsList) {
            client.receiveMessage(msg);
        }
    }

    @Override
    public void run() {
        int clientId = 0;
        try {
            ServerSocket ss = new ServerSocket(port);
            System.out.println("Server started on port: "+port+"!");
            while (true) {
                Socket clientSocket = ss.accept();
                clientId++;

                ClientHandler clientHandler = new ClientHandler(this, clientSocket, clientId);
                clientsList.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
