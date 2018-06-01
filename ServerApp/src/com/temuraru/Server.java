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
    public static final String ROLE_JOIN_REQUESTED = "join_requested";

    private final int port;

    public static final String SERVER_BOT_NAME = "serverbot";
    public static final Integer SERVER_BOT_CLIENT_ID = 1;
    public static final Integer MAIN_GROUP_ID = 1;
    public static final String MAIN_GROUP_NAME = "Main";
    private int lastGroupId = MAIN_GROUP_ID;
    private static ArrayList<ClientHandler> clientsList = new ArrayList<>();
    private static ArrayList<GroupHandler> groupsList = new ArrayList<>();
    private Map<String, String> commands;
    private static GroupHandler mainGroup;
    private ClientHandler serverBot;

    public Server(int port) {
        this.port = port;
        setCommands();
    }

    public static GroupHandler getMainGroup() {
        return mainGroup;
    }

    public GroupHandler createGroup(ClientHandler owner, String groupName, String groupType) throws Exception {
        GroupHandler newGroup = new GroupHandler(owner, groupName, groupType);
        newGroup.setId(++lastGroupId);

        groupsList.add(newGroup);

        return newGroup;
    }

    private void createServerBot() {
        try {
            serverBot = new ClientHandler(this);
            mainGroup = new GroupHandler(serverBot, Server.MAIN_GROUP_NAME, GroupHandler.TYPE_PUBLIC);
            mainGroup.setId(Server.MAIN_GROUP_ID);
            groupsList.add(mainGroup);
            serverBot.setCurrentGroup(mainGroup);
            clientsList.add(serverBot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ClientHandler getServerBot() throws Exception {
        return serverBot;
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

    private void setCommands() {
        commands = new HashMap<String, String>();

        commands.put(ROLE_GUEST, "speak,talk,login,help,info,quit,list");
        commands.put(ROLE_USER, "user,create,select,request,join,leave,accept");
        commands.put(ROLE_ADMIN, "groupname,grouptype,add,kick,promote,demote,invite");
        commands.put(ROLE_SUPERADMIN, "delete");
    }

    public static ArrayList<ClientHandler> getClientsList() {
        return clientsList;
    }

    public static ArrayList<GroupHandler> getGroupsList() {
        return groupsList;
    }

    public String[] getCommandsForRole(String currentRole) {
        String[] order = {ROLE_GUEST, ROLE_USER, ROLE_ADMIN, ROLE_SUPERADMIN};

        StringBuilder sb = new StringBuilder();
        for (String role:order) {
            String roleCommands = commands.get(role);
            sb.append((sb.length() > 0 ? "," : ""));
            sb.append(roleCommands);
            if (role.equals(currentRole)) {
                break;
            }
        }
        String newCommands = sb.toString();

        return newCommands.split(",");
    }

    public static void broadcastMessageToGroup(String msg, Integer destinationGroupId) throws IOException {
        for (ClientHandler client: clientsList) {
            if (client.isServerBot()) {
                continue;
            }
            if (client.getCurrentGroupId().equals(destinationGroupId)) {
                client.receiveMessage(msg);
            }
        }
    }

    public static void broadcastMessageToGroup(String msg, Integer destinationGroupId, String skipUsername) throws IOException {
        for (ClientHandler client: clientsList) {
            if (client.isServerBot()) {
                continue;
            }
//            System.out.println("skipUsername: " + skipUsername);
//            System.out.println("client.getUsername(): " + client.getUsername());
            if (client.getUsername().equals(skipUsername)) {
                continue;
            }
            if (client.getCurrentGroupId().equals(destinationGroupId)) {
                client.receiveMessage(msg);
            }
        }
    }

    public static void broadcastMessageToGroup(String msg, Integer destinationGroupId, String skipUsername, String[] onlyToRoles) throws IOException {
        System.out.println("onlyToRoles:"+onlyToRoles.toString());
        for (ClientHandler client: clientsList) {
            if (client.isServerBot()) {
                continue;
            }
            if (skipUsername.length() > 0 && client.getUsername().equals(skipUsername)) {
                continue;
            }
            System.out.println("client.getUsername():"+client.getUsername());

            for (String role: onlyToRoles) {
//                System.out.println("destinationGroupId:"+destinationGroupId);
//                System.out.println("client.getGroupsList().get(destinationGroupId):"+client.getGroupsList().get(destinationGroupId));
                if (client.getGroupsList().get(destinationGroupId).equals(role)) {
                    client.receiveMessage(msg);
                }
            }
        }
    }

    public void broadcastMessage(String msg) throws IOException {
        for (ClientHandler client: clientsList) {
            if (client.isServerBot()) {
                continue;
            }
            client.receiveMessage(msg);
        }
    }

    public static void broadcastMessage(String msg, boolean debug) throws IOException {
        for (ClientHandler client: clientsList) {

            System.out.println("getName(): "+client.getUsername());
            if (client.isServerBot()) {
                continue;
            }
            client.receiveMessage(msg);
        }
        if (debug) {
            System.out.println(msg);
        }
    }

    public static GroupHandler getGroupByName(String groupName, boolean isMandatory) throws Exception {
        GroupHandler foundGroup = Server.getGroupByName(groupName);
        if (foundGroup == null && isMandatory) {
            throw new Exception("Group not found!");
        }
        
        return foundGroup;
    }

    public static GroupHandler getGroupByName(String groupName) throws Exception {
        GroupHandler foundGroup = null;
        if (groupsList.size() > 0) {
            for (GroupHandler group: groupsList) {
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
        if (clientsList.size() > 0) {
            for (ClientHandler client: clientsList) {
                if (client.getName().equals(clientName)) {
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

    public void outputGroups(OutputStream clientOutputStream) throws IOException {
        String groupInfo = "No groups on server!\n";
        if (groupsList.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (GroupHandler group: groupsList) {
                sb.append((sb.length() == 0 ? "" : ", "));
                sb.append(group.getName());
            }
            String groupsNames = sb.toString();
            groupInfo = "Available groups on server: "+groupsNames+"\n";
        }

        clientOutputStream.write(groupInfo.getBytes());
    }

    public void outputHelp(OutputStream clientOutputStream, String role) throws IOException {
        String[] commandsForRole = this.getCommandsForRole(role);
        String groupInfo = "Available commands for role <"+role+">: /"+String.join(", /", commandsForRole)+"\n";
        clientOutputStream.write(groupInfo.getBytes());
    }

}
