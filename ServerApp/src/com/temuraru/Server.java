package com.temuraru;

import java.io.IOException;
import java.io.OutputStream;
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
    private Map<String, String> commands;

    public Server(int port) {
        this.port = port;
        setCommands();
    }

    @Override
    public void run() {
        Socket clientSocket;
        ClientHandler clientHandler;
        int clientId = 0;
        try {
            ServerSocket ss = new ServerSocket(port);
            System.out.println("Server started on port: "+port+"!");

//            clientSocket = ss.accept();
//            clientId++;
//            clientHandler = new ClientHandler(this, clientSocket, clientId, true);
//            clientsList.add(clientHandler);
//            clientHandler.start();

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

    public ClientHandler getServerBot() throws Exception {
        for (ClientHandler client: clientsList) {
            if (client.getRole().equals(Server.ROLE_SERVER_BOT)) {
                return client;
            }
        }

        throw new Exception("No client with role serverbot found!!");
    }


    private void setCommands() {
        commands = new HashMap<String, String>();

        commands.put(ROLE_GUEST, "login,help");
        commands.put(ROLE_USER, "list,create,join,request,leave,talk,change");
        commands.put(ROLE_ADMIN, "kick,invite,promote,demote");
        commands.put(ROLE_SUPERADMIN, "delete");
    }

    public ArrayList<ClientHandler> getClientsList() {
        return clientsList;
    }

    public ArrayList<GroupHandler> getGroupsList() {
        return groupsList;
    }

    public String[] getCommandsForRole(String currentRole) {
        String[] order = {ROLE_GUEST, ROLE_USER, ROLE_ADMIN, ROLE_SUPERADMIN};
        String newCommands = "";
        for (String role:order) {
            String roleCommands = commands.get(role);
            newCommands = newCommands + ((newCommands.length() > 0 ? "," : "") + roleCommands);
            if (role.equals(currentRole)) {
                break;
            }
        }
        String[] commands = newCommands.split(",");

        return commands;
    }

    public void broadcastMessageToGroup(String msg, String destinationGroup) throws IOException {
        for (ClientHandler client: clientsList) {
            if (client.getCurrentGroup().equals(destinationGroup)) {
                client.receiveMessage(msg);
            }
        }
    }

    public void broadcastMessageToGroup(String msg, String destinationGroup, String skipUsername) throws IOException {
        for (ClientHandler client: clientsList) {
            if (client.getUsername().equals(skipUsername)) {
                continue;
            }
            if (client.getCurrentGroup().equals(destinationGroup)) {
                client.receiveMessage(msg);
            }
        }
    }

    public void broadcastMessage(String msg) throws IOException {
        for (ClientHandler client: clientsList) {
            client.receiveMessage(msg);
        }
    }

    public void broadcastMessage(String msg, boolean debug) throws IOException {
        for (ClientHandler client: clientsList) {
            client.receiveMessage(msg);
        }
        if (debug) {
            System.out.println(msg);
        }
    }

    public void outputGroups(OutputStream clientOutputStream) throws IOException {
        ArrayList<GroupHandler> groupsList = this.getGroupsList();
        String groupInfo;
        if (groupsList.size() > 0) {
            String groupsNames = "";
            for (GroupHandler group: groupsList) {
                groupsNames += (groupsNames.length() == 0 ? "" : ", ") + group.getName();
            }
            groupInfo = "Available groups on server: "+groupsNames+"\n";
        } else {
            groupInfo = "No groups on server!\n";

        }
        clientOutputStream.write(groupInfo.getBytes());
    }

    public void outputHelp(OutputStream clientOutputStream, String role) throws IOException {
        String[] commandsForRole = this.getCommandsForRole(role);
        String groupInfo = "Your commands are: /"+String.join(", /", commandsForRole)+"\n";
        clientOutputStream.write(groupInfo.getBytes());
    }

}
