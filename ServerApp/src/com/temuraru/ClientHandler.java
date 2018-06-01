package com.temuraru;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientHandler extends Thread {

    private Integer currentGroupId;
    private GroupHandler currentGroup;
    private Server server;
    private Socket clientSocket;
    private int clientId;
    private String role;
    private String username;
    //    private ArrayList<GroupHandler> groupsList = new ArrayList<>();
    private Map<Integer, String> groupsList = new HashMap<Integer, String>();
    private ArrayList<ClientHandler> blockedClientsList = new ArrayList<ClientHandler>();


    public ClientHandler(Server server) throws IOException {
        this.server = server;
        this.clientId = Server.SERVER_BOT_CLIENT_ID;
        this.username = Server.SERVER_BOT_NAME;
        updateRoleInCurrentGroup(Server.MAIN_GROUP_ID, Server.ROLE_SERVER_BOT);
    }
    public ClientHandler(Server server, Socket clientSocket, int clientId) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        this.clientId = clientId;
        this.username = "guest" + clientId;
        updateRoleInCurrentGroup(Server.MAIN_GROUP_ID, Server.ROLE_GUEST);
    }

    public void updateRoleInCurrentGroup(Integer groupId, String groupRole) {
        groupsList.put(groupId, groupRole);
    }

    public void listGroups() {
        // classic way, loop a Map
        for (Map.Entry<Integer, String> entry : groupsList.entrySet()) {
            System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
        }
    }

    public String getCurrentRole() {
        String currentRole = groupsList.get(getCurrentGroupId());
        return currentRole;
    }

    public GroupHandler getCurrentGroup() {
        return currentGroup;
    }

    public void setCurrentGroup(GroupHandler newGroup) {
        this.currentGroup = newGroup;
        setCurrentGroupId(newGroup.getId());
    }

    public Integer getCurrentGroupId() {
        return currentGroupId;
    }

    public void setCurrentGroupId(Integer currentGroupId) {
        this.currentGroupId = currentGroupId;
    }

    public Map<Integer, String> getGroupsList() {
        return groupsList;
    }



    @Override
    public void run() {
        try {
            welcome();
            handleClientSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws Exception {
        OutputStream clientOutputStream = clientSocket.getOutputStream();
        InputStream clientInputStream = clientSocket.getInputStream();

        outputGroupInfo(clientOutputStream);
        processCommands(clientOutputStream, clientInputStream);

        goodbye();
    }

    private void processCommands(OutputStream clientOutputStream, InputStream clientInputStream) throws Exception {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(clientInputStream));

        String line;
        String msg;
        while ((line = buffer.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            System.out.println(getUsername()+" [new line -"+line.length()+" chars-]: " + line);
            if (!line.startsWith("/")) {
                line = "/speak " + line;
            }
            System.out.println(getUsername()+" [new line -"+line.length()+" chars-]: " + line);

            String[] commandTokens = StringUtils.split(line);
            if (commandTokens != null && commandTokens.length > 0) {
                String cmd = commandTokens[0].toLowerCase().replaceFirst("/", "");

                String[] currentRoleCommands = server.getCommandsForRole(getCurrentRole());
                if (!Arrays.asList(currentRoleCommands).contains(cmd)) {
                    msg = "Unknown command for your group/role! Type '/help' for a list of available commands!\n";
                    clientOutputStream.write(msg.getBytes());
                } else {
                    processCommand(cmd, commandTokens, clientOutputStream);
                }
            }

//            clientOutputStream.write(msg.getBytes());
        }
    }

    private void processCommand(String cmd, String[] commandTokens, OutputStream clientOutputStream) throws Exception {
        String error = "";
        switch (cmd) {
            case "quit":
            case "logoff":
                goodbye();
                break;
            case "login":
                if (commandTokens.length != 2) {
                    error = "Invalid number of parameters for login command!";
                    error += "The correct format is: '/login <user_name>'";
                    receiveMessage(error);
                }
                handleLogin(clientOutputStream, commandTokens);
                break;
            case "info":
                outputGroupInfo(clientOutputStream);
                break;
            case "user":
                if (commandTokens.length != 2) {
                    error = "Invalid number of parameters for login command!";
                    error += "The correct format is: '/user <new_user_name>'";
                    receiveMessage(error);
                }
                changeUsername(commandTokens);
                break;
            case "help":
                server.outputHelp(clientOutputStream, this.getCurrentRole());
                break;
            case "list":
                server.outputGroups(clientOutputStream);
                break;
            case "speak":
                if (commandTokens.length < 2) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/speak <message>'";
                    receiveMessage(error);
                }
                speakToGroup(clientOutputStream, commandTokens);
                break;
            case "talk":
                if (commandTokens.length < 3) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/talk <user_name> [<message>]'";
                    receiveMessage(error);
                }
                talkToClient(clientOutputStream, commandTokens);
//                receiveMessage("Feature "+cmd+" not implemented yet!\n");
                break;
            case "create":
                if (commandTokens.length > 3 || commandTokens.length <= 1) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/create <group_name> [<group_type>]'";
                    receiveMessage(error);
                }
                createGroup(clientOutputStream, commandTokens);
                break;
            case "select":
                if (commandTokens.length != 2) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: /select <new_group>";
                    receiveMessage(error);
                } else {
                    selectGroup(commandTokens);
                }
                break;
            case "request":
                if (commandTokens.length != 2) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/request [<group_name>]'";
                    receiveMessage(error);
                } else {
                    requestJoinToGroup(commandTokens);
                }
                break;
            case "join":
                if (commandTokens.length != 2) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/join [<group_name>]'";
                    receiveMessage(error);
                } else {
                    joinGroup(commandTokens);
                }
                break;
            case "leave":
                if (commandTokens.length > 2) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/leave [<group_name>]'";
                    receiveMessage(error);
                } else {
                    leaveGroup(commandTokens);
                }
                break;
            case "block":
                if (commandTokens.length > 2) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/block [<user_name>]'";
                    receiveMessage(error);
                } else {
                    blockClient(commandTokens);
                }
                break;
            case "unblock":
                if (commandTokens.length > 2) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/unblock [<user_name>]'";
                    receiveMessage(error);
                } else {
                    unblockClient(commandTokens);
                }
                break;
            case "groupname":
                if (commandTokens.length != 2) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/groupname <new_name>'";
                    receiveMessage(error);
                } else {
                    changeGroupName(clientOutputStream, commandTokens);
                }
                break;
            case "grouptype":
                if (commandTokens.length != 2) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/grouptype <group_type>'";
                    receiveMessage(error);
                } else {
                    changeGroupType(clientOutputStream, commandTokens);
                }
                break;
            case "invite":
                receiveMessage("Feature "+cmd+" not implemented yet!\n");
                break;
            case "accept":
                receiveMessage("Feature "+cmd+" not implemented yet!\n");
                break;
            case "add":
                if (commandTokens.length != 3) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/add <user_name> [<group_name>]'";
                    receiveMessage(error);
                } else {
                    addClientToGroup(commandTokens);
                }
                break;
            case "kick":
                if (commandTokens.length > 4) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/kick <user_name> [<group_name> [<reason>]]'";
                    receiveMessage(error);
                } else {
                    kickClientFromGroup(commandTokens);
                }
                break;
            case "promote":
                if (commandTokens.length > 3) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/promote <user_name> [<group_name>]'";
                    receiveMessage(error);
                } else {
                    promoteClientInGroup(commandTokens);
                }
                break;
            case "demote":
                if (commandTokens.length > 3) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/demote <user_name> [<group_name>]'";
                    receiveMessage(error);
                } else {
                    demoteClientInGroup(commandTokens);
                }
                break;
            case "delete":
                deleteGroup();
                break;
            default:
                receiveMessage("Feature "+cmd+" not implemented yet!\n");
                break;
        }
    }

    private void speakToGroup(OutputStream clientOutputStream, String[] commandTokens) throws IOException {
        String[] lastPart = splitCommands(commandTokens, 1);
        String lastPartMessage = String.join(" ", lastPart);
        String msg = getUsername() + " [#"+clientId+"]: " + lastPartMessage + "\n";

        Server.broadcastMessageToGroup(msg, getCurrentGroupId(), getUsername()); // do not send msg to owner
        clientOutputStream.write(("You wrote: " + lastPartMessage+"\n").getBytes()); // write in his/her own stream
    }

    private void talkToClient(OutputStream clientOutputStream, String[] commandTokens) throws IOException {
        ClientHandler involvedClient;

        String clientName = commandTokens[1];
        try {
            involvedClient = Server.getClientByName(clientName);
        } catch (Exception e) {
            receiveMessage("Error! Client with name "+clientName+" not found!");
            return;
        }

        String[] lastPart = splitCommands(commandTokens, 2);
        String lastPartMessage = String.join(" ", lastPart);
        String msg = getUsername() + " [#"+clientId+"]:\n" + lastPartMessage + "\n";

        involvedClient.receiveMessage(msg);
        clientOutputStream.write(lastPartMessage.getBytes()); // write in his/her own stream
    }

    private String[] splitCommands(String[] commandTokens, Integer splitIndex) {
        return Arrays.copyOfRange(commandTokens, splitIndex, commandTokens.length);
    }

    private void unblockClient(String[] commandTokens) throws IOException {
        ClientHandler involvedClient;

        String clientName = commandTokens[1];
        try {
            involvedClient = Server.getClientByName(clientName);
        } catch (Exception e) {
            receiveMessage("Error! Client with name "+clientName+" not found!");
            return;
        }

        // validate request
        if (!blockedClientsList.contains(involvedClient)) {
            receiveMessage("Error! Client with name "+involvedClient.getUsername()+" is not blocked!\n");
            return;
        }

        // proceed
        blockedClientsList.remove(involvedClient);
        receiveMessage("Client with name "+involvedClient.getUsername()+" has been unblocked!\n");
        involvedClient.receiveMessage("Client with name "+involvedClient.getUsername()+" has unblocked you!\n");
    }

    private void blockClient(String[] commandTokens) throws IOException {
        ClientHandler involvedClient;

        String clientName = commandTokens[1];
        try {
            involvedClient = Server.getClientByName(clientName);
        } catch (Exception e) {
            receiveMessage("Error! Client with name "+clientName+" not found!");
            return;
        }

        // validate request
        if (blockedClientsList.contains(involvedClient)) {
            receiveMessage("Error! Client with name "+involvedClient.getUsername()+" is already blocked!\n");
            return;
        }

        // proceed
        blockedClientsList.add(involvedClient);
        receiveMessage("Client with name "+involvedClient.getUsername()+" has been blocked!\n");
        involvedClient.receiveMessage("Client with name "+involvedClient.getUsername()+" has blocked you!\n");
    }

    private void demoteClientInGroup(String[] commandTokens) throws IOException {
        GroupHandler involvedGroup;
        ClientHandler involvedClient;

        String clientName = commandTokens[1];
        try {
            involvedClient = Server.getClientByName(clientName);
        } catch (Exception e) {
            receiveMessage("Error! Client with name "+clientName+" not found!");
            return;
        }

        String groupName;
        if (commandTokens.length == 3) {
            groupName = commandTokens[2];
        } else {
            groupName = currentGroup.getName();
        }
        try {
            involvedGroup = Server.getGroupByName(groupName);
        } catch (Exception e) {
            receiveMessage("Error! Group "+groupName+" not found!");
            return;
        }

        // validate request
        if (!involvedClient.groupsList.get(involvedGroup.getId()).equals(Server.ROLE_ADMIN)) {
            receiveMessage("Error! Client with name "+involvedClient.getUsername()+" is not an admin in group "+involvedGroup.getName()+"!");
            return;
        }

        // proceed!
        if (isAdminInGroup(involvedGroup.getId())) {
            involvedClient.groupsList.put(involvedGroup.getId(), Server.ROLE_USER);
            involvedClient.receiveMessage("You have been demoted to User in the group "+involvedGroup.getName()+" by "+getUsername()+"!\n");
            Server.broadcastMessageToGroup("User "+involvedClient.getUsername()+" has been demoted to User in the group "+involvedGroup.getName()+" by "+getUsername()+"!\n", involvedGroup.getId());
        } else {
            receiveMessage("You are not allowed to demote clients in the group "+involvedGroup.getName()+" (since you're not an admin)!");
        }
    }

    private void promoteClientInGroup(String[] commandTokens) throws IOException {
        GroupHandler involvedGroup;
        ClientHandler involvedClient;

        String clientName = commandTokens[1];
        try {
            involvedClient = Server.getClientByName(clientName);
        } catch (Exception e) {
            receiveMessage("Error! Client with name "+clientName+" not found!");
            return;
        }

        String groupName;
        if (commandTokens.length == 3) {
            groupName = commandTokens[2];
        } else {
            groupName = currentGroup.getName();
        }
        try {
            involvedGroup = Server.getGroupByName(groupName);
        } catch (Exception e) {
            receiveMessage("Error! Group "+groupName+" not found!");
            return;
        }

        // validate request
        if (involvedClient.groupsList.get(involvedGroup.getId()).equals(Server.ROLE_ADMIN)) {
            receiveMessage("Error! Client with name "+involvedClient.getUsername()+" is already admin in group "+involvedGroup.getName()+"!");
            return;
        }

        // proceed!
        if (isAdminInGroup(involvedGroup.getId())) {
            involvedClient.groupsList.put(involvedGroup.getId(), Server.ROLE_ADMIN);
            involvedClient.receiveMessage("You have been promoted to Admin in the group "+involvedGroup.getName()+" by "+getUsername()+"!\n");
            Server.broadcastMessageToGroup("User "+involvedClient.getUsername()+" has been promoted to Admin in the group "+involvedGroup.getName()+" by "+getUsername()+"!\n", involvedGroup.getId());
        } else {
            receiveMessage("You are not allowed to promote clients in the group "+involvedGroup.getName()+" (since you're not an admin)!");
        }
    }

    private void kickClientFromGroup(String[] commandTokens) throws IOException {
        GroupHandler involvedGroup;
        ClientHandler involvedClient;
        String reason = "because!";

        try {
            involvedClient = Server.getClientByName(commandTokens[1]);
        } catch (Exception e) {
            receiveMessage("Error! Client not found!");
            return;
        }
        String groupName;
        if (commandTokens.length > 3) {
            reason = commandTokens[3];
            groupName = commandTokens[2];
        } else {
        if (commandTokens.length == 3) {
            groupName = commandTokens[2];
        } else {
            groupName = currentGroup.getName();
        }}
        try {
            involvedGroup = Server.getGroupByName(groupName);
        } catch (Exception e) {
            receiveMessage("Error! Group not found!");
            return;
        }

        // validate request
        if (involvedClient.groupsList.get(involvedGroup.getId()).equals(Server.ROLE_SUPERADMIN)) {
            receiveMessage("Error! Client with name "+involvedClient.getUsername()+" cannot be kicked from group "+involvedGroup.getName()+". Reason: super admin!");
            return;
        }

        // proceed!
        if (isAdminInGroup(involvedGroup.getId())) {
            involvedGroup.usersList.remove(involvedClient);
            involvedClient.groupsList.remove(involvedGroup.getId());
            involvedClient.receiveMessage("You have been kicked from the group "+involvedGroup.getName()+" by "+getUsername()+". Reason: "+reason+"!\n");
            Server.broadcastMessageToGroup("User "+involvedClient.getUsername()+" has been kicked from the group "+involvedGroup.getName()+" by "+getUsername()+". Reason: "+reason+"!\n", involvedGroup.getId());
        } else {
            receiveMessage("You are not allowed to kick clients from the group "+involvedGroup.getName()+" (since you're not an admin)!");
        }
    }

    private void addClientToGroup(String[] commandTokens) throws IOException {
        GroupHandler involvedGroup = null;
        ClientHandler involvedClient = null;

        String clientName = commandTokens[1];
        try {
            involvedClient = Server.getClientByName(clientName);
        } catch (Exception e) {
            receiveMessage("Error! Client with name "+clientName+" not found!");
            return;
        }

        String groupName;
        if (commandTokens.length == 3) {
            groupName = commandTokens[2];
        } else {
            groupName = currentGroup.getName();
        }
        try {
            involvedGroup = Server.getGroupByName(groupName);
        } catch (Exception e) {
            receiveMessage("Error! Group "+groupName+" not found!");
            return;
        }

        // validate request from client
        if (!involvedClient.groupsList.get(involvedGroup.getId()).equals(Server.ROLE_JOIN_REQUESTED)) {
            receiveMessage("Error! Client with name "+involvedClient.getUsername()+" did not request to join group "+involvedGroup.getName()+"!");
            return;
        }

        // proceed!
        if (isAdminInGroup(involvedGroup.getId())) {
            involvedGroup.usersList.add(involvedClient);
            involvedClient.groupsList.put(involvedGroup.getId(), Server.ROLE_USER);
            involvedClient.receiveMessage("You have been added to group "+involvedGroup.getName()+" by "+getUsername()+". You can switch to this group with: '/select "+involvedGroup.getName()+"'!\n");
            Server.broadcastMessageToGroup("User "+involvedClient.getUsername()+" has been added to group "+involvedGroup.getName()+" by "+getUsername()+"!\n", involvedGroup.getId());
        } else {
            receiveMessage("You are not allowed to make changes to the group "+involvedGroup.getName()+" (since you're not an admin)!");
        }
    }

    private void requestJoinToGroup(String[] commandTokens) throws IOException {
        GroupHandler groupToJoin = null;
        String groupName = commandTokens[1];
        try {
            groupToJoin = Server.getGroupByName(groupName);
        } catch (Exception e) {
            receiveMessage("Error! Group "+groupName+" not found!");
            return;
        }

        if (groupToJoin != null) {
            switch(groupToJoin.getType()) {
                case GroupHandler.TYPE_PUBLIC:
                    receiveMessage("Group type is: PUBLIC. You cannot request approval! You should use: '/join <group_name>'!");
                    break;
                case GroupHandler.TYPE_PRIVATE:
                    groupsList.put(groupToJoin.getId(), Server.ROLE_JOIN_REQUESTED);
                    String[] adminRoles = {Server.ROLE_ADMIN, Server.ROLE_SUPERADMIN};
                    String msg = "User " + getUsername() + " requested access to group " + groupToJoin.getName() + ". You could add him/her with: '/add " + groupToJoin.getName() + " " + getUsername() + "'!\n";
                    Server.broadcastMessageToGroup(msg, groupToJoin.getId(), "", adminRoles);
                    receiveMessage("Request has been sent! You will be added by an admin if you are eligible!");
                    break;
                case GroupHandler.TYPE_CLOSED:
                    groupsList.put(groupToJoin.getId(), Server.ROLE_JOIN_REQUESTED);
                    receiveMessage("Group type is CLOSED. You can only be added by an admin!");
                    break;
            }
        }
    }

    private void joinGroup(String[] commandTokens) throws IOException {
        GroupHandler groupToJoin = null;
        try {
            groupToJoin = Server.getGroupByName(commandTokens[1]);
        } catch (Exception e) {
            receiveMessage("Error! Group not found!");
            return;
        }

        if (groupToJoin != null) {
            switch(groupToJoin.getType()) {
                case GroupHandler.TYPE_PUBLIC:
                    groupToJoin.usersList.add(this);
                    groupsList.put(groupToJoin.getId(), Server.ROLE_USER);
                    setCurrentGroup(groupToJoin);
                    receiveMessage("Group type is: PUBLIC. You are now a member!");
                    break;
                case GroupHandler.TYPE_PRIVATE:
                    groupsList.put(groupToJoin.getId(), Server.ROLE_JOIN_REQUESTED);
                    receiveMessage("Group type is: PRIVATE. You have to request approval first: '/request <group_name>'!");
                    break;
                case GroupHandler.TYPE_CLOSED:
                    groupsList.put(groupToJoin.getId(), Server.ROLE_JOIN_REQUESTED);
                    receiveMessage("Group type is CLOSED. You have to receive an invite from an admin in order to join!");
                    break;
            }
        }
    }

    private void leaveGroup(String[] commandTokens) throws IOException {
        GroupHandler involvedGroup;
        if (commandTokens.length == 2) {
            String groupName = commandTokens[1];
            if (groupName.equals(Server.MAIN_GROUP_NAME)) {
                receiveMessage("Error! Group "+Server.MAIN_GROUP_NAME+" cannot be left!");
                return;
            }
            try {
                involvedGroup = Server.getGroupByName(groupName);
            } catch (Exception e) {
                receiveMessage("Error! Group "+groupName+" not found!");
                return;
            }
        } else {
            involvedGroup = currentGroup;
        }

        // validate request from client
        if (groupsList.get(involvedGroup.getId()).equals(Server.ROLE_SUPERADMIN)) {
            receiveMessage("Error! You are SUPERADMIN and you cannot leave the group "+involvedGroup.getName()+". You could use: '/delete "+involvedGroup.getName()+"'!");
            return;
        }

        groupsList.remove(involvedGroup.getId());
        // @TODO: if this client was the last in the group, should we delete the group also!?
        Server.broadcastMessageToGroup("User "+getUsername()+" left the group "+involvedGroup.getName()+"!\n", involvedGroup.getId());
    }

    private void changeGroupName(OutputStream clientOutputStream, String[] commandTokens) throws IOException {
        String newGroupName = GroupHandler.validateGroupName(clientOutputStream, commandTokens[1], this, Server.getGroupsList());
        if (newGroupName.length() == 0) {
            return;
        }
        String oldGroupName = currentGroup.getName();
        if (oldGroupName.equals(newGroupName))  {
            receiveMessage("The new group name is the same with the current one!!");
        }

        // proceed!
        if (isAdminInGroup(getCurrentGroupId())) {
            currentGroup.setName(newGroupName);
            Server.broadcastMessageToGroup("User "+getUsername()+" changed the group name from "+oldGroupName+" to "+currentGroup.getName()+"!\n", getCurrentGroupId());
        } else {
            receiveMessage("You are not allowed to make changes to the group "+getCurrentGroup()+" (since you're not an admin)!");
        }
    }

    private void changeGroupType(OutputStream clientOutputStream, String[] commandTokens) throws Exception {
        String newGroupType = GroupHandler.validateGroupType(clientOutputStream, commandTokens[1], false);
        if (newGroupType.length() == 0) {
            return;
        }
        String oldGroupType =currentGroup.getType();
        if (oldGroupType.equals(newGroupType))  {
            receiveMessage("The new group type is the same with the current one!!");
            return;
        }

        // proceed!
        if (isAdminInGroup(getCurrentGroupId())) {
            currentGroup.setType(newGroupType);
            Server.broadcastMessageToGroup("User "+getUsername()+" changed the group type from "+oldGroupType+" to "+currentGroup.getType()+"!\n", getCurrentGroupId());
        } else {
            receiveMessage("You are not allowed to make changes to the group "+getCurrentGroup()+" (since you're not an admin)!");
        }
    }

    private void deleteGroup() throws IOException {
        if (groupsList.get(currentGroup.getId()).equals(Server.ROLE_SUPERADMIN)) {
            Server.broadcastMessageToGroup("This group is being deleted! Sorry!\n", getCurrentGroupId());

            ArrayList<ClientHandler> clientsList = Server.getClientsList();
            for (ClientHandler client: clientsList) {
                if (client.isMemberOf(currentGroup.getId())) {
                    if (client.getCurrentGroupId() == currentGroup.getId()) {
                        client.updateRoleInCurrentGroup(Server.MAIN_GROUP_ID, client.groupsList.get(Server.MAIN_GROUP_ID));
                    }
                    client.groupsList.remove(currentGroup.getId());
                }
            }
            Server.getGroupsList().remove(currentGroup);
        } else {
            receiveMessage("You are not allowed to delete this group since you didn't create it!\n");
        }
    }

    private void selectGroup(String[] commandTokens) throws IOException {
        GroupHandler oldGroup = getCurrentGroup();
        GroupHandler newGroup;
        try {
            newGroup = Server.getGroupByName(commandTokens[1]);
        } catch (Exception e) {
            receiveMessage("Error! Group not found!");
            return;
        }

        if (isMemberOf(newGroup.getId())) {
            setCurrentGroup(newGroup);
            Server.broadcastMessageToGroup("User "+getUsername()+" left the group "+oldGroup.getName()+"!\n", oldGroup.getId());
            Server.broadcastMessageToGroup("User "+getUsername()+" joined the group "+newGroup.getName()+"!\n", newGroup.getId());
        } else {
            receiveMessage("You are not yet registered in group "+newGroup.getName()+"!");

            String helperMsg =  "You may create such a group with the command: '/create "+newGroup.getName()+"'!\n";
            ArrayList<GroupHandler> groupsList = Server.getGroupsList();
            for (GroupHandler group: groupsList) {
                if (group.getName().equals(newGroup.getName())) {
                    switch(group.getType()) {
                        case GroupHandler.TYPE_PUBLIC:
                            helperMsg = "The group is Public. You may join this group using the command '/join "+newGroup.getName()+"'!\n";
                            break;
                        case GroupHandler.TYPE_PRIVATE:
                            getGroupsList().put(group.getId(), Server.ROLE_JOIN_REQUESTED);
                            helperMsg = "The group is Private. You may request access to this group using the command '/request "+newGroup.getName()+"'!\n";
                            break;
                        case GroupHandler.TYPE_CLOSED:
                            getGroupsList().put(group.getId(), Server.ROLE_JOIN_REQUESTED);
                            helperMsg = "The group is Closed. You may only be added by an admin in this group!\n";
                            break;
                    }
                    break;
                }
            }

            receiveMessage(helperMsg);
        }
    }

    private void createGroup(OutputStream clientOutputStream, String[] commandTokens) throws Exception {
        ArrayList<GroupHandler> serverGroupsList = Server.getGroupsList();
        String groupName = GroupHandler.validateGroupName(clientOutputStream, commandTokens[1], this, serverGroupsList);
        if (groupName.length() == 0) {
            groupName = GroupHandler.generateGroupName();
            receiveMessage("A random name has been generated for you: "+groupName);
        }
        String chosenType = commandTokens.length == 3 ? commandTokens[2] : GroupHandler.TYPE_PUBLIC;
        String groupType = GroupHandler.validateGroupType(clientOutputStream, chosenType, true);

        GroupHandler newGroup = server.createGroup(this, groupName, groupType);

        setCurrentGroup(newGroup);
        updateRoleInCurrentGroup(newGroup.getId(), Server.ROLE_SUPERADMIN);
        clientOutputStream.write(("Group "+newGroup.getName()+" has been created and your role is SUPERADMIN!\n").getBytes());
    }

    private void outputGroupInfo(OutputStream clientOutputStream) throws IOException {
        String[] commandsForRole = server.getCommandsForRole(this.getCurrentRole());

        String separator = "================================\n";
        String groupInfo = separator;
        groupInfo +="Your current group is: "+currentGroup.getName()+" and your current role is: "+this.getCurrentRole().toUpperCase()+"!!!\n";
        groupInfo += "Your commands are: /"+String.join(", /", commandsForRole)+"\n";
        groupInfo += separator;
        clientOutputStream.write(groupInfo.getBytes());
    }

    private void handleLogin(OutputStream clientOutputStream, String[] commandTokens) throws IOException {
        String chosenUsername = processUsername(commandTokens[1]);
        String error = this.validateUsername(chosenUsername);

        if (error.length() > 0) {
            clientOutputStream.write(("Login failed! "+error).getBytes());
        } else {
            if (isLoggedIn()) {
                clientOutputStream.write(("You are already logged in! If you need to change the username, use the command: '/user <new_username>' !\n").getBytes());
            } else {
                String currentUsername = getUsername();

                this.setUsername(chosenUsername);

                clientOutputStream.write("Login OK!\n".getBytes());
                String msg = "User '" + currentUsername + "' just logged in as '" + username + "'!\n";
                Server.broadcastMessage(msg, true);
                updateRoleInCurrentGroup(currentGroupId, Server.ROLE_USER);

                outputGroupInfo(clientOutputStream);
            }
        }
    }

    private String processUsername(String commandToken) {
        String chosenUsername = commandToken.replaceAll("[^a-zA-Z0-9]+", "").replaceFirst("^[^a-zA-Z]+", "");
        if (chosenUsername.length() > 15) {
            chosenUsername = chosenUsername.substring(0,14);
        }
        return chosenUsername;
    }

    private void changeUsername(String[] commandTokens) throws IOException {
        String chosenUsername = processUsername(commandTokens[1]);
        String error = this.validateUsername(chosenUsername);

        if (error.length() > 0) {
            receiveMessage("Changing username failed! "+error);
        } else {
            String currentUsername = getUsername();

            this.setUsername(chosenUsername);
            receiveMessage("Changing username OK!\n");

            String msg = "User '" + currentUsername + "' just transformed into '" + chosenUsername + "'!\n";
            Server.broadcastMessage(msg, true);
        }
    }

    private String validateUsername(String chosenUsername)
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

    public void receiveMessage(String msg) throws IOException {
        OutputStream clientOutputStream = clientSocket.getOutputStream();
        clientOutputStream.write(msg.getBytes());
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
        System.out.println("Client: '"+username+"' - Accepted connection: "+username+": " + clientSocket);

        receiveMessage("Welcome, '"+username+"' ! Please login your own username!\n (letters, digits and '_' only, starting with a letter, max 15 characters)\n");
        receiveMessage("Example: /login my_own_username \n");
    }
    public void goodbye() throws IOException {
        receiveMessage("Goodbye, '"+username+"' !\n");

        clientSocket.close();

        System.out.println("Client: '"+username+"' - Closed connection: " + clientSocket);
    }

    public boolean isLoggedIn() {
        return !isGuest();
    }
    public boolean isGuest() {
        return getCurrentRole().equals(Server.ROLE_GUEST);
    }
    public boolean isSuperAdmin() {
        return getCurrentRole().equals(Server.ROLE_SUPERADMIN);
    }
    public boolean isServerBot() {
        System.out.println("getName(): "+getName());
        return getUsername().equals(Server.SERVER_BOT_NAME);
    }

    public boolean isAdminInGroup(Integer groupId) {
        String currentRole = getGroupsList().get(groupId);
        String[] adminRoles = new String[]{Server.ROLE_ADMIN, Server.ROLE_SUPERADMIN};

        return Arrays.asList(adminRoles).contains(currentRole);
    }

    /**
     * Check if the current client is member of the parameter group.
     * @param groupId Integer
     * @return String The role or the client in the group or empty string
     */
    public String getMembershipOfGroup(Integer groupId) {
        Map<Integer, String> groupsList = getGroupsList();
        String groupRole = groupsList.get(groupId);
        if (groupRole != null && groupRole.length() > 0) {
            return groupRole;
        }

        return "";
    }

    public boolean isMemberOf(Integer groupId) {
        return getMembershipOfGroup(groupId).length() > 0;
    }

}
