package com.temuraru;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server extends Thread {
    public static final String ROLE_GUEST = "guest";
    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_SUPERADMIN = "superadmin";
    public static final String ROLE_SERVER_BOT = "serverbot";
    public static final String GROUP_MAIN = "Main";
    private final int port;
    private ArrayList<ClientHandler> clientsList = new ArrayList<>();
    private ArrayList<GroupHandler> groupsList = new ArrayList<>();
    private Map<String, String[]> commands;

    public Server(int port) {
        this.port = port;
        setCommands();
    }

    private void setCommands() {
        commands = new HashMap<String, String[]>();

        commands.put(ROLE_GUEST, new String[] {"login", "help"});
        commands.put(ROLE_USER, new String[] {"create", "join", "leave", "talk"});
        commands.put(ROLE_ADMIN, new String[] {"kick", "invite", "promote", "demote"});
        commands.put(ROLE_SUPERADMIN, new String[] {"delete"});
    }

    public ArrayList<ClientHandler> getClientsList() {
        return clientsList;
    }
    public ArrayList<GroupHandler> getGroupsList() { return groupsList; }

    public ClientHandler getServerBot() throws Exception {
        for (ClientHandler client: clientsList) {
            if (client.getRole().equals(Server.ROLE_SERVER_BOT)) {
                return client;
            }
        }

        throw new Exception("No client with role serverbot found!!");
    }

    public String[] getCommandsForRole(String role) {
        String[] roleCommands = {};
        Iterator it = commands.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();

//            System.out.println(pair.getValue());
//            List list = new ArrayList(Arrays.asList(roleCommands));
//            list.addAll(Arrays.asList(roleCommands));
//            String[] newCommands = pair.getValue();
//            Object[] c = list.toArray(newCommands);
//            if (pair.getKey() == role) {
//                System.out.println(c.toString());
//                break;
//            }
        }
        return commands.get(role);
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
