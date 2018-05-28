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
    public ArrayList<GroupHandler> getGroupsList() { return groupsList; }

    public ClientHandler getServerBot() throws Exception {
        for (ClientHandler client: clientsList) {
            if (client.getRole().equals(ClientHandler.ROLE_SERVER_BOT)) {
                return client;
            }
        }

        throw new Exception("No client with role serverbot found!!");
    }

    public void broadcastMessage(String msg) throws IOException {
        for (ClientHandler client: clientsList) {
            client.receiveMessage(msg);
        }
    }

    @Override
    public void run() {
        Socket clientSocket;
        ClientHandler clientHandler;
        int clientId = 0;
        try {
            ServerSocket ss = new ServerSocket(port);
            System.out.println("Server started on port: "+port+"!");

            clientSocket = ss.accept();
            clientId++;
            clientHandler = new ClientHandler(this, clientSocket, clientId, false);
            clientsList.add(clientHandler);

            while (true) {
                clientSocket = ss.accept();
                clientId++;
                clientHandler = new ClientHandler(this, clientSocket, clientId, false);
                clientsList.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
