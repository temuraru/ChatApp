package com.temuraru;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientHandler extends Thread {

    private int clientId;
    private Server server;
    private String username;
    private Socket clientSocket;
    private Integer currentGroupId;
    private GroupHandler currentGroup;
    private OutputStream clientOutputStream;
    private Map<Integer, String> groupsList = new HashMap<Integer, String>();
    private ArrayList<ClientHandler> blockedClientsList = new ArrayList<ClientHandler>();
    private CommandProcessor processor;

    public ClientHandler(Server server) {
        this.server = server;
        this.clientId = Server.SERVER_BOT_CLIENT_ID;
        this.username = Server.SERVER_BOT_NAME;
        updateRoleInCurrentGroup(Server.MAIN_GROUP_ID, Server.ROLE_SERVER_BOT);
        this.processor = new CommandProcessor(this, server);
    }
    public ClientHandler(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
        this.clientId = server.getNewClientId();
        this.username = "guest" + this.clientId;
        updateRoleInCurrentGroup(Server.MAIN_GROUP_ID, Server.ROLE_GUEST);
        this.processor = new CommandProcessor(this, server);
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws Exception {
        this.welcome();

        this.setClientOutputStream(clientSocket.getOutputStream());
        InputStream clientInputStream = clientSocket.getInputStream();

        this.outputGroupInfo();
        this.processCommands(clientInputStream);

        this.goodbye();
    }

    public int getClientId() {
        return clientId;
    }

    public OutputStream getClientOutputStream() {
        return clientOutputStream;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public CommandProcessor getProcessor() {
        return processor;
    }

    public String getCurrentRole() {
        return groupsList.get(getCurrentGroupId());
    }

    public GroupHandler getCurrentGroup() {
        return currentGroup;
    }

    public void setCurrentGroup(GroupHandler newGroup) {
        this.currentGroup = newGroup;
        this.setCurrentGroupId(newGroup.getId());
    }

    public void setCurrentGroupId(Integer currentGroupId) {
        this.currentGroupId = currentGroupId;
    }

    public Integer getCurrentGroupId() {
        return currentGroupId;
    }

    public Map<Integer, String> getGroupsList() {
        return groupsList;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ArrayList<ClientHandler> getBlockedClientsList() {
        return blockedClientsList;
    }

    public void updateRoleInCurrentGroup(Integer groupId, String groupRole) {
        this.getGroupsList().put(groupId, groupRole);
    }

    public void setClientOutputStream(OutputStream clientOutputStream) {
        this.clientOutputStream = clientOutputStream;
    }

    private void processCommands(InputStream clientInputStream) throws Exception {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(clientInputStream));

        String line;
        while ((line = buffer.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            if (!line.startsWith("/")) {
                line = "/speak " + line;
            }
            System.out.println(getUsername()+" ["+line.length()+" chars]: " + line);

            String[] commandTokens = StringUtils.split(line);
            if (commandTokens != null && commandTokens.length > 0) {
                this.processor.processCommand(commandTokens);
            }
        }
    }

    public String listClientsGroups() {
        StringBuilder listing = new StringBuilder();
        // classic way, loop a Map
        for (Map.Entry<Integer, String> entry : this.getGroupsList().entrySet()) {
            GroupHandler group = Server.getGroupById(entry.getKey());
            String groupInfo = group.getName() + " " + group.getGroupTypeSuffix();
            groupInfo += " [id: " + entry.getKey() + "] - role: " + entry.getValue().toUpperCase();
            listing.append((listing.length() == 0 ? "" : "; ") + groupInfo);
        }

        return listing.toString();
    }

    public String processUsername(String commandToken) {
        String chosenUsername = commandToken.replaceAll("[^a-zA-Z0-9]+", "").replaceFirst("^[^a-zA-Z]+", "");
        if (chosenUsername.length() > 15) {
            chosenUsername = chosenUsername.substring(0,14);
        }
        return chosenUsername;
    }

    public String validateUsername(String chosenUsername)
    {
        String error = "";
        String currentUsername = this.getUsername();

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

        return error;
    }

    private String[] getRoleCommands(String currentRole) {
        return this.processor.getCommandsForRole(currentRole);
    }

    public void outputCommandsInfo() throws IOException {
        String[] commandsForRole = this.getRoleCommands(this.getCurrentRole());
        String info = "Available commands for role <"+this.getCurrentRole().toUpperCase()+">: /"+String.join(", /", commandsForRole)+"\n";
        this.getClientOutputStream().write(info.getBytes());
    }

    public void outputGroupInfo() throws IOException {
        String[] commandsForRole = this.getRoleCommands(this.getCurrentRole());

        String separator = "================================\n";
        String groupInfo = separator;
        groupInfo +="Your name is: "+getUsername();
        groupInfo +=", your current group is: "+this.getCurrentGroup().getName()+" and your current role is: "+this.getCurrentRole().toUpperCase()+"!!!\n";
        groupInfo += "Your commands are: /"+String.join(", /", commandsForRole)+"\n";
        groupInfo += separator;

        this.getClientOutputStream().write(groupInfo.getBytes());
    }

    public void welcome() throws IOException {
        String username = this.getUsername();
        System.out.println("Client: '"+username+"' connected on socket: " + this.getClientSocket());

        this.receiveMessage("Welcome, "+username+"!\n Please login with your own username! (letters, digits and '_', starting only with a letter, max 15 characters)\n");
        this.receiveMessage("Example: /login my_own_username \n");
    }

    public void goodbye() throws IOException {
        String username = this.getUsername();
        this.receiveMessage("Goodbye, '"+ username +"' !\n");

        this.getClientSocket().close();

        System.out.println("Client: '"+ username +"' closed connection on socket: " + this.getClientSocket());
    }

    public void receiveMessage(String msg) throws IOException {
        OutputStream clientOutputStream = this.getClientSocket().getOutputStream();
        clientOutputStream.write(msg.getBytes());
    }

    public boolean isLoggedIn() {
        return !isGuest();
    }
    public boolean isGuest() {
        return getCurrentRole().equals(Server.ROLE_GUEST);
    }
    public boolean isUserInGroup() {
        return getCurrentRole().equals(Server.ROLE_USER);
    }
    public boolean isAdminInGroup(Integer groupId) {
        String currentRole = getGroupsList().get(groupId);
        String[] adminRoles = new String[]{Server.ROLE_ADMIN, Server.ROLE_SUPERADMIN};

        return Arrays.asList(adminRoles).contains(currentRole);
    }
    public boolean isSimpleAdminInGroup() {
        return getCurrentRole().equals(Server.ROLE_ADMIN);
    }
    public boolean isSuperAdmin() {
        return getCurrentRole().equals(Server.ROLE_SUPERADMIN);
    }
    public boolean isServerBot() {
        return this.getUsername().equals(Server.SERVER_BOT_NAME);
    }

    /**
     * Check if the current client is member of the parameter group.
     * @param groupId Integer
     * @return String The role or the client in the group or empty string
     */
    public String getMembershipOfGroup(Integer groupId) {
        Map<Integer, String> groupsList = this.getGroupsList();
        String groupRole = groupsList.get(groupId);
        if (groupRole != null && groupRole.length() > 0) {
            return groupRole;
        }

        return "";
    }

    public boolean isMemberOf(Integer groupId) {
        return getMembershipOfGroup(groupId).length() > 0;
    }

    public boolean isNotMemberOf(Integer groupId) {
        return !isMemberOf(groupId);
    }

}
