package com.temuraru;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

public class ClientHandler extends Thread {
    public static final String ROLE_GUEST = "guest";
    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_SUPERADMIN = "superadmin";

    private Server server;
    private final Socket clientSocket;
    private int clientId;
    private String role;
    private String username;

    public ClientHandler(Server server, Socket clientSocket, int clientId) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        this.clientId = clientId;
        this.role = ROLE_GUEST;
        this.username = "guest" + clientId;
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

    public void receiveMessage(String msg) throws IOException {
        OutputStream clientOutputStream = clientSocket.getOutputStream();
        clientOutputStream.write(msg.getBytes());
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
        String error = "";
        String choosenUsername = username;
        if (commandTokens.length != 2) {
            error = "Invalid number of parameters for login command!";
        } else {
            choosenUsername = commandTokens[1].replaceAll("[^a-zA-Z0-9]+", "").replaceFirst("^[^a-zA-Z]+", "");
            if (choosenUsername.length() > 15) {
                choosenUsername = choosenUsername.substring(0,14);
            }
            if (choosenUsername.length() == 0) {
                error = "Username invalid!";
            } else {
                ArrayList<String> forbiddenUsernamesList = new ArrayList<String>();
                forbiddenUsernamesList.add("all");
                forbiddenUsernamesList.add("user");
                forbiddenUsernamesList.add("admin");
                forbiddenUsernamesList.add("superadmin");
                forbiddenUsernamesList.add("guest");

                if (forbiddenUsernamesList.contains(choosenUsername)) {
                    error = "Username is not allowed!";
                }
            }
        }

        boolean result = true;
        if (error.length() > 0) {
            result = false;
            clientOutputStream.write(("Login failed! "+error).getBytes());
        } else {
            clientOutputStream.write("Login OK!\n".getBytes());
            server.broadcastMessage("User '" + username + "'- just logged in!\n");
            System.out.println("User: '"+username+"' logged in as: "+choosenUsername+": " + clientSocket);

            setRole(ROLE_USER);
        }

        return result;

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
