package com.temuraru;

import com.temuraru.Exceptions.GroupNotFoundException;

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
    public static final String ROLE_JOIN_REQUESTED = "join_requested";

    public static final String SERVER_BOT_NAME = "serverbot";
    public static final Integer SERVER_BOT_CLIENT_ID = 1;
    public static final Integer MAIN_GROUP_ID = 1;
    public static final String MAIN_GROUP_NAME = "Main";

    private final int port;
    private int lastGroupId = MAIN_GROUP_ID;
    private static ArrayList<ClientHandler> clientsList = new ArrayList<>();
    private static ArrayList<GroupHandler> groupsList = new ArrayList<>();
    private static GroupHandler mainGroup;
    private ClientHandler serverBot;

    public Server(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        createServerBot();

        Socket clientSocket;
        ClientHandler clientHandler;
        int clientId = 0;
        try {
            ServerSocket ss = new ServerSocket(port);
            System.out.println("Server started on port: "+port+"!");

            while (true) {
                clientSocket = ss.accept();
                clientId++;
                clientHandler = new ClientHandler(this, clientSocket, clientId);
                clientHandler.setCurrentGroup(Server.getMainGroup());
                clientsList.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static GroupHandler getMainGroup() {
        return mainGroup;
    }

    public ClientHandler getServerBot() {
        return serverBot;
    }

    public static ArrayList<ClientHandler> getClientsList() {
        return clientsList;
    }

    public static ArrayList<GroupHandler> getGroupsList() {
        return groupsList;
    }

    public GroupHandler createGroup(ClientHandler owner, String groupName, String groupType) {
        GroupHandler newGroup = new GroupHandler(owner, groupName, groupType);
        newGroup.setId(++lastGroupId);

        Server.getGroupsList().add(newGroup);

        return newGroup;
    }

    private void createServerBot() {
        try {
            serverBot = new ClientHandler(this);
            mainGroup = new GroupHandler(serverBot, Server.MAIN_GROUP_NAME, GroupHandler.TYPE_PUBLIC);
            mainGroup.setId(Server.MAIN_GROUP_ID);
            Server.getGroupsList().add(mainGroup);
            serverBot.setCurrentGroup(mainGroup);
            Server.getClientsList().add(serverBot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void broadcastMessageToGroup(String msg, Integer destinationGroupId) throws IOException {
        for (ClientHandler client: Server.getClientsList()) {
            if (client.isServerBot()) {
                continue;
            }
            if (client.getCurrentGroupId().equals(destinationGroupId)) {
                client.receiveMessage(msg);
            }
        }
    }

    public static void broadcastMessageToGroup(String msg, Integer destinationGroupId, String skipUsername) throws IOException {
        for (ClientHandler client: Server.getClientsList()) {
            if (client.isServerBot()) {
                continue;
            }

            if (client.getUsername().equals(skipUsername)) {
                continue;
            }
            if (client.getCurrentGroupId().equals(destinationGroupId)) {
                client.receiveMessage(msg);
            }
        }
    }

    public static void broadcastMessageToGroup(String msg, Integer destinationGroupId, String skipUsername, String[] onlyToRoles) throws IOException {
        for (ClientHandler client: Server.getClientsList()) {
            if (client.isServerBot()) {
                continue;
            }
            if (skipUsername.length() > 0 && client.getUsername().equals(skipUsername)) {
                continue;
            }

            for (String role: onlyToRoles) {
                if (client.getGroupsList().get(destinationGroupId).equals(role)) {
                    client.receiveMessage(msg);
                }
            }
        }
    }

    public void broadcastMessage(String msg) throws IOException {
        for (ClientHandler client: Server.getClientsList()) {
            if (client.isServerBot()) {
                continue;
            }
            client.receiveMessage(msg);
        }
    }

    public static void broadcastMessage(String msg, boolean debug) throws IOException {
        for (ClientHandler client: Server.getClientsList()) {
            if (client.isServerBot()) {
                continue;
            }
            client.receiveMessage(msg);
        }
        if (debug) {
            System.out.println(msg);
        }
    }

    public static GroupHandler getGroupById(Integer groupId) {
        GroupHandler foundGroup = null;
        if (Server.getGroupsList().size() > 0) {
            for (GroupHandler group: Server.getGroupsList()) {
                if (group.getId() == groupId) {
                    foundGroup = group;
                    break;
                }
            }
        }

        return foundGroup;
    }

    public static GroupHandler getGroupByName(String groupName) {
        GroupHandler foundGroup = null;
        if (Server.getGroupsList().size() > 0) {
            for (GroupHandler group: Server.getGroupsList()) {
                if (group.getName().equals(groupName)) {
                    foundGroup = group;
                    break;
                }
            }
        }

        return foundGroup;
    }

    public static ClientHandler getClientByName(String clientName) throws Exception {
        ClientHandler foundClient = null;
        if (Server.getClientsList().size() > 0) {
            for (ClientHandler client: Server.getClientsList()) {
                if (client.getUsername().equals(clientName)) {
                    foundClient = client;
                    break;
                }
            }
        }
        if (foundClient == null) {
            throw new Exception("Client not found!");
        }

        return foundClient;
    }

    public String listServerGroups() {
        String groupInfo = "No groups on server!\n";
        if (Server.getGroupsList().size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (GroupHandler group: Server.getGroupsList()) {
                sb.append((sb.length() == 0 ? "" : ",") + group.getName() + " " + group.getGroupTypeSuffix());
            }
            groupInfo = sb.toString();
        }

        return groupInfo;
    }
}
