package com.temuraru;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientHandler extends Thread {

    private final String currentGroup;
    private Server server;
    private final Socket clientSocket;
    private int clientId;
    private String role;
    private String username;
//    private ArrayList<GroupHandler> groupsList = new ArrayList<>();
    private Map<String, String> groupsList = new HashMap<String, String>();


    public ClientHandler(Server server, Socket clientSocket, int clientId, boolean isServerBot) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        this.clientId = clientId;
        this.role = isServerBot ? Server.ROLE_SERVER_BOT : Server.ROLE_GUEST;
        this.username = "guest" + clientId;
        this.currentGroup = Server.GROUP_MAIN;
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

        String line;
        String msg;
        while ((line = buffer.readLine()) != null) {
            String[] commandTokens = StringUtils.split(line);
            if (commandTokens != null && commandTokens.length > 0) {
                String cmd = commandTokens[0].toLowerCase();
                if (!cmd.startsWith("/")) {
                    msg = getUsername() + " (client "+clientId+") typed:\n" + line + "\n";
                    server.broadcastMessageToGroup(msg, currentGroup, getUsername());

                    clientOutputStream.write(msg.getBytes());
                    continue;
                }
                cmd = cmd.replaceFirst("/", "");

                String[] currentRoleCommands = server.getCommandsForRole(getCurrentRole());
                if (!Arrays.asList(currentRoleCommands).contains(cmd)) {
                    msg = "Unknown command for your group/role! Type /help for a list of available commands!\n";
                    clientOutputStream.write(msg.getBytes());
                    continue;
                }

                processCommand(cmd, commandTokens, clientOutputStream);
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
            case "change":
                handleChange(clientOutputStream, commandTokens);
                break;
            case "list":
                server.outputGroups(clientOutputStream);
                break;
        }
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

    private boolean handleChange(OutputStream clientOutputStream, String[] commandTokens) throws IOException {
        String currentUsername = getUsername();
        String error = this.validateUsername(commandTokens);

        boolean result = true;
        if (error.length() > 0) {
            result = false;
            clientOutputStream.write(("Changing username failed! "+error).getBytes());
        } else {
            clientOutputStream.write("Changing username OK!\n".getBytes());
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
