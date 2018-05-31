package com.temuraru;

import javafx.scene.Group;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientHandler extends Thread {

    private String currentGroup;
    private Server server;
    private final Socket clientSocket;
    private int clientId;
    private String role;
    private String username;

    public Map<String, String> getGroupsList() {
        return groupsList;
    }

    public void setGroupsList(Map<String, String> groupsList) {
        this.groupsList = groupsList;
    }

    //    private ArrayList<GroupHandler> groupsList = new ArrayList<>();
    private Map<String, String> groupsList = new HashMap<String, String>();


    public ClientHandler(Server server, Socket clientSocket, int clientId, boolean isServerBot) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        this.clientId = clientId;
        this.role = isServerBot ? Server.ROLE_SERVER_BOT : Server.ROLE_GUEST;
        this.username = "guest" + clientId;
        setCurrentGroup(Server.GROUP_MAIN);
        updateRoleInCurrentGroup();
    }

    private void updateRoleInCurrentGroup() {
        groupsList.put(this.getCurrentGroup(), this.getRole());
    }

    @Override
    public void run() {
        try {
            welcome();
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentGroup() {
        return currentGroup;
    }

    public void setCurrentGroup(String currentGroup) {
        this.currentGroup = currentGroup;
    }

    public String getCurrentRole() {
        return groupsList.get(currentGroup);
    }

    private void handleClientSocket() throws IOException {
        OutputStream clientOutputStream = clientSocket.getOutputStream();
        InputStream clientInputStream = clientSocket.getInputStream();

        outputGroupInfo(clientOutputStream);
        processCommands(clientOutputStream, clientInputStream);

        goodbye();
    }

    private void processCommands(OutputStream clientOutputStream, InputStream clientInputStream) throws IOException {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(clientInputStream));

        Scanner scanner = new Scanner(clientInputStream);
        String msg;
        String line = buffer.readLine();

        while (line != null) {
            System.out.println("new line: " + line);
            String[] commandTokens = StringUtils.split(line);
            if (commandTokens != null && commandTokens.length > 0) {
                String cmd = commandTokens[0].toLowerCase();
                String validCommand = cmd;
                if (!cmd.startsWith("/")) {
                    msg = getUsername() + " (client "+clientId+") typed:\n" + line + "\n";
                    server.broadcastMessageToGroup(msg, currentGroup, getUsername());

                    clientOutputStream.write(msg.getBytes());
                    validCommand = "";
                }
                cmd = cmd.replaceFirst("/", "");

                String[] currentRoleCommands = server.getCommandsForRole(getCurrentRole());
                if (!Arrays.asList(currentRoleCommands).contains(cmd)) {
                    msg = "Unknown command for your group/role! Type /help for a list of available commands!\n";
                    clientOutputStream.write(msg.getBytes());
                    validCommand = "";
                }

                System.out.println("validCommand: " + validCommand);
                if (validCommand.length() > 0) {
                    processCommand(cmd, commandTokens, clientOutputStream);
                }
                try {
                    line = buffer.readLine();
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }

//            clientOutputStream.write(msg.getBytes());
        }
    }

    private void processCommand(String cmd, String[] commandTokens, OutputStream clientOutputStream) throws IOException {
        switch (cmd) {
            case "quit":
                break;
            case "help":
                server.outputHelp(clientOutputStream, this.getCurrentRole());
                break;
            case "login":
                handleLogin(clientOutputStream, commandTokens);
                break;
            case "logoff":
                goodbye();
                break;
            case "user":
                handleChange(commandTokens);
                break;
            case "list":
                server.outputGroups(clientOutputStream);
                break;
            case "create":
                createGroup(clientOutputStream, commandTokens);
                break;
            case "select":
                changeGroup(clientOutputStream, commandTokens);
                break;
            case "request":
                receiveMessage("Feature "+cmd+" not implemented yet!\n");
                break;
            case "join":
                receiveMessage("Feature "+cmd+" not implemented yet!\n");
                break;
            case "leave":
                receiveMessage("Feature "+cmd+" not implemented yet!\n");
                break;
            case "groupname":
                changeGroup(clientOutputStream, commandTokens);
                break;
            case "changetype":
                receiveMessage("Feature "+cmd+" not implemented yet!\n");
                break;
            case "add":
                receiveMessage("Feature "+cmd+" not implemented yet!\n");
                break;
            case "invite":
                receiveMessage("Feature "+cmd+" not implemented yet!\n");
                break;
            case "kick":
                receiveMessage("Feature "+cmd+" not implemented yet!\n");
                break;
            case "promote":
                receiveMessage("Feature "+cmd+" not implemented yet!\n");
                break;
            case "demote":
                receiveMessage("Feature "+cmd+" not implemented yet!\n");
                break;
            case "delete":
                deleteGroup();
                break;
        }
    }

    private void deleteGroup() throws IOException {
        if (groupsList.get(currentGroup).equals(Server.ROLE_SUPERADMIN)) {
            server.broadcastMessageToGroup("This group is being deleted! Sorry!\n", currentGroup);
            ArrayList<ClientHandler> clientsList = server.getClientsList();
            for (ClientHandler client: clientsList) {
                if (client.isMemberOf(currentGroup).length() > 0) {
                    client.groupsList.remove(currentGroup);
                }
            }
        } else {
            receiveMessage("You are not allowed to delete this group since you didn't create it!\n");
        }
    }

    private String isMemberOf(String currentGroup) {
        Map<String, String> groupsList = getGroupsList();
        String groupRole = groupsList.get(currentGroup);
        if (groupRole.length() > 0) {
            return groupRole;
        }

        return "";
    }

    private void changeGroup(OutputStream clientOutputStream, String[] commandTokens) throws IOException {
        String groupName = commandTokens[1];
        String groupRole = groupsList.get(groupName);
        if (groupRole.length() > 0) {
            setCurrentGroup(groupName);
            server.broadcastMessageToGroup("User "+getUsername()+" joined the group "+groupName+"!\n", groupName);
        } else {
            receiveMessage("You are not yet in group "+groupName+"!");

            String helperMsg =  "You may create such a group with the command: /create "+groupName+" !\n";
            ArrayList<GroupHandler> groupsList = server.getGroupsList();
            for (GroupHandler group: groupsList) {
                if (group.getName().equals(groupName)) {

                    helperMsg = "You may join this group using the command /join "+groupName+"!\n";
                    break;
                }
            }

            receiveMessage(helperMsg);
        }
    }

    private void createGroup(OutputStream clientOutputStream, String[] commandTokens) throws IOException {

        ArrayList<GroupHandler> groupsList = server.getGroupsList();
        String groupName = GroupHandler.validateName(clientOutputStream, commandTokens, this, groupsList);
        String groupType = GroupHandler.validateType(clientOutputStream, commandTokens);

        GroupHandler newGroup = new GroupHandler(this, groupName, groupType);

        server.getGroupsList().add(newGroup);

        setRole(Server.ROLE_SUPERADMIN);
        setCurrentGroup(groupName);

        updateRoleInCurrentGroup();
    }

    private void outputGroupInfo(OutputStream clientOutputStream) throws IOException {
        String[] commandsForRole = server.getCommandsForRole(this.getCurrentRole());

        String groupInfo ="Your current group is: "+this.getCurrentGroup()+" and your current role is: "+this.getCurrentRole()+"!!!\n";
        groupInfo += "Your commands are: /"+String.join(", /", commandsForRole)+"\n";
        clientOutputStream.write(groupInfo.getBytes());
    }

    private boolean handleLogin(OutputStream clientOutputStream, String[] commandTokens) throws IOException {
        String currentUsername = getUsername();
        String error = this.validateUsername(commandTokens);

        boolean result = true;
        if (error.length() > 0) {
            result = false;
            clientOutputStream.write(("Login failed! "+error).getBytes());
        } else {
            if (!isLoggedIn()) {
                result = false;
                clientOutputStream.write(("You are already logged in! If you need to change the username, use the command: '/change new_username' !\n").getBytes());
            } else {
                clientOutputStream.write("Login OK!\n".getBytes());
                String msg = "User '" + currentUsername + "' just logged in as '" + username + "'!\n";
                server.broadcastMessage(msg, true);

                setRole(Server.ROLE_USER);

                updateRoleInCurrentGroup();
                outputGroupInfo(clientOutputStream);
            }
        }

        return result;
    }

    private boolean isLoggedIn() {
        return getCurrentRole().equals(server.ROLE_GUEST);
    }

    private boolean handleChange(String[] commandTokens) throws IOException {
        String currentUsername = getUsername();
        String error = this.validateUsername(commandTokens);

        boolean result = true;
        if (error.length() > 0) {
            result = false;
            receiveMessage("Changing username failed! "+error);
        } else {
            receiveMessage("Changing username OK!\n");
            String msg = "User '" + currentUsername + "' just transformed into '" + username + "'!\n";
            server.broadcastMessage(msg, true);
        }

        return result;
    }

    private String validateUsername(String[] commandTokens)
    {
        String error = "";
        String currentUsername = this.getUsername();

        if (commandTokens.length != 2) {
            error = "Invalid number of parameters for login command!";
            return error;
        }

        String chosenUsername = commandTokens[1].replaceAll("[^a-zA-Z0-9]+", "").replaceFirst("^[^a-zA-Z]+", "");
        if (chosenUsername.length() > 15) {
            chosenUsername = chosenUsername.substring(0,14);
        }
        if (chosenUsername.equals(currentUsername)) {
            error = "The current username is the same: "+currentUsername+"!";
            return error;
        }


        if (chosenUsername.length() == 0) {
            error = "Username invalid!";
        } else {
            ArrayList<String> forbiddenUsernamesList = new ArrayList<String>();
            forbiddenUsernamesList.add("all");
            forbiddenUsernamesList.add("user");
            forbiddenUsernamesList.add("group");
            forbiddenUsernamesList.add("admin");
            forbiddenUsernamesList.add("serverbot");
            forbiddenUsernamesList.add("superadmin");
            forbiddenUsernamesList.add("guest");

            if (forbiddenUsernamesList.contains(chosenUsername)) {
                error = "Username is not allowed!";
            }
        }

        if (error.length() == 0) {
            this.setUsername(chosenUsername);
        }

        return error;
    }


    public void receiveMessage(String msg) throws IOException {
        OutputStream clientOutputStream = clientSocket.getOutputStream();
        clientOutputStream.write(msg.getBytes());
    }

    private void handleClientSocket_v1() throws IOException, InterruptedException {
        OutputStream clientOutputStream = clientSocket.getOutputStream();
        clientOutputStream.write("Hi!\n".getBytes());
        for(int i = 0; i < 10; i++) {
            clientOutputStream.write(("Time is " + new Date() + "\r\n").getBytes());
            Thread.sleep(1000);
        }
        clientSocket.close();
        System.out.println("Closed connection for client: " + clientSocket);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void welcome() throws IOException {
        System.out.println("User: '"+username+"' - Accepted connection: "+username+": " + clientSocket);

        receiveMessage("Welcome, '"+username+"' ! Please login your own username!\n (letters, digits and '_' only, starting with a letter, max 15 characters)\n");
        receiveMessage("Example: /login my_own_username \n");
    }
    public void goodbye() throws IOException {
        receiveMessage("Goodbye, '"+username+"' !\n");

        clientSocket.close();

        System.out.println("User: '"+username+"' - Closed connection: " + clientSocket);
    }
}
