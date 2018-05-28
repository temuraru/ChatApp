package com.temuraru;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler extends Thread {
    public static final String ROLE_GUEST = "guest";
    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_SUPERADMIN = "superadmin";
    public static final String ROLE_SERVER_BOT = "serverbot";
    public static final String[][] commands = {{"Apple", "Banana"}, {"Pork", "Beef", "Chicken"}, {"Carrots"} };


    private Server server;
    private final Socket clientSocket;
    private int clientId;
    private String role;
    private String username;

    public ClientHandler(Server server, Socket clientSocket, int clientId, boolean isServerBot) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        this.clientId = clientId;
        this.role = isServerBot ? ROLE_SERVER_BOT : ROLE_GUEST;
        this.username = "guest" + clientId;

        Map<String, String[]> map = new HashMap<String, String[]>();

        map.put(ROLE_GUEST, new String[] {"login"});
        map.put(ROLE_USER, new String[] {"create", "join", "leave", "talk"});
        map.put(ROLE_ADMIN, new String[] {"kick", "invite", "promote", "demote"});
        map.put(ROLE_SUPERADMIN, new String[] {"delete"});

        System.out.println(String.join(",", map.get(ROLE_ADMIN)));
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
    private void handleClientSocket() throws IOException {
        OutputStream clientOutputStream = clientSocket.getOutputStream();
        InputStream clientInputStream = clientSocket.getInputStream();

        BufferedReader buffer = new BufferedReader(new InputStreamReader(clientInputStream));
        String line;
        String msg;
        while ((line = buffer.readLine()) != null) {
            msg = "Client "+clientId+" typed: " + line + "\n";
            String[] commandTokens = StringUtils.split(line);
            if (commandTokens != null && commandTokens.length > 0) {
                String cmd = commandTokens[0].toLowerCase();
                if (!cmd.startsWith("/")) {
                    msg = "Wrong command format! Type /help for a list of available commands!";
                    break;
                }
                cmd = cmd.replaceFirst("/", "");
                if ("quit".equals(cmd)) {
                    break;
                } else if ("login".equals(cmd)) {
                    boolean loggedIn = handleLogin(clientOutputStream, commandTokens);
                    if (loggedIn) {

                    }
                }
            }

            clientOutputStream.write(msg.getBytes());
        }

        goodbye();
    }

    private boolean handleLogin(OutputStream clientOutputStream, String[] commandTokens) throws IOException {
        String error = this.validateUsername(commandTokens);

        boolean result = true;
        if (error.length() > 0) {
            result = false;
            clientOutputStream.write(("Login failed! "+error).getBytes());
        } else {
            clientOutputStream.write("Login OK!\n".getBytes());
            server.broadcastMessage("User '" + username + "'- just logged in!\n");
            System.out.println("User: '"+username+"' logged in as: "+this.getUsername()+": " + clientSocket);

            setRole(ROLE_USER);
        }

        return result;
    }

    private String validateUsername(String[] commandTokens)
    {
        String error = "";
        String chosenUsername = this.getUsername();
        if (commandTokens.length != 2) {
            error = "Invalid number of parameters for login command!";
        } else {
            chosenUsername = commandTokens[1].replaceAll("[^a-zA-Z0-9]+", "").replaceFirst("^[^a-zA-Z]+", "");
            if (chosenUsername.length() > 15) {
                chosenUsername = chosenUsername.substring(0,14);
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
