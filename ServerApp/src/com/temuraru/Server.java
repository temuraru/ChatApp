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

    private final int port;

    public static final Integer SERVER_BOT_CLIENT_ID = 1;
    public static final Integer MAIN_GROUP_ID = 1;
    public static final String MAIN_GROUP_NAME = "Main";
    private int lastGroupId = MAIN_GROUP_ID;
    private static ArrayList<ClientHandler> clientsList = new ArrayList<>();
    private static ArrayList<GroupHandler> groupsList = new ArrayList<>();
    private Map<String, String> commands;
    private static GroupHandler mainGroup;

    public Server(int port) {
        this.port = port;
        setCommands();
    }

    public static GroupHandler getMainGroup() {
        return mainGroup;
    }

    @Override
    public void run() {

        ClientHandler serverBot;
        try {
            serverBot = new ClientHandler(this, SERVER_BOT_CLIENT_ID);
            mainGroup = createGroup(serverBot, MAIN_GROUP_NAME, GroupHandler.TYPE_PUBLIC);
            groupsList.add(mainGroup);
            clientsList.add(serverBot);
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    public GroupHandler createGroup(ClientHandler owner, String groupName, String groupType) throws Exception {
        GroupHandler newGroup = new GroupHandler(owner, groupName, groupType);
        newGroup.setId(++lastGroupId);

        groupsList.add(newGroup);

        return newGroup;
    }

    private void setCommands() {
        commands = new HashMap<String, String>();

        commands.put(ROLE_GUEST, "login,help,quit");
        commands.put(ROLE_USER, "user,list,create,select,request,join,leave,talk,groupname,grouptype");
        commands.put(ROLE_ADMIN, "add,invite,kick,promote,demote");
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

    public void broadcastMessageToGroup(String msg, Integer destinationGroupId) throws IOException {
        for (ClientHandler client: clientsList) {
            if (client.getCurrentGroupId().equals(destinationGroupId)) {
                client.receiveMessage(msg);
            }
        }
    }

    public void broadcastMessageToGroup(String msg, Integer destinationGroupId, String skipUsername) throws IOException {
        for (ClientHandler client: clientsList) {
            if (client.getUsername().equals(skipUsername)) {
                continue;
            }
            if (client.getCurrentGroupId().equals(destinationGroupId)) {
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
        if (foundGroup == null) {
            throw new Exception("Group not found!");
        }
        
        return foundGroup;
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
