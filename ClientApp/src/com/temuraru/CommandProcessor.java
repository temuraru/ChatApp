package com.temuraru;

import com.temuraru.Exceptions.ForbiddenNameException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CommandProcessor {

    private final ClientHandler client;
    private final Server server;
    private OutputStream clientOutputStream;
    private Map<String, String> commands;

    public CommandProcessor(ClientHandler clientHandler, Server server) {
        this.client = clientHandler;
        this.server = server;
        setCommands();
    }

    private void setCommands() {
        commands = new HashMap<String, String>();

        commands.put(Server.ROLE_GUEST, "speak,talk,login,help,info,quit,list");
        commands.put(Server.ROLE_USER, "user,create,select,request,join,leave,accept");
        commands.put(Server.ROLE_ADMIN, "groupname,grouptype,add,kick,promote,demote,invite");
        commands.put(Server.ROLE_SUPERADMIN, "delete");
    }

    public void processCommand(String[] commandTokens) throws Exception {
        this.clientOutputStream = this.client.getClientOutputStream();
        String cmd = commandTokens[0].toLowerCase().replaceFirst("/", "");

        String[] currentRoleCommands = this.getCommandsForRole(this.client.getCurrentRole());
        if (!Arrays.asList(currentRoleCommands).contains(cmd)) {
            String msg = "Invalid command for your group/role! Type '/help' for a list of available commands!\n";
            clientOutputStream.write(msg.getBytes());
            return;
        }

        String error = "";
        switch (cmd) {
            case "quit":
            case "logoff":
                this.client.goodbye();
                break;
            case "login":
                if (commandTokens.length != 2) {
                    error = "Invalid number of parameters for login command!";
                    error += "The correct format is: '/login <user_name>'";
                    this.client.receiveMessage(error);
                }
                handleLogin(commandTokens);
                break;
            case "info":
                this.client.outputGroupInfo();
                break;
            case "user":
                if (commandTokens.length != 2) {
                    error = "Invalid number of parameters for login command!";
                    error += "The correct format is: '/user <new_user_name>'";
                    this.client.receiveMessage(error);
                }
                changeUsername(commandTokens);
                break;
            case "help":
                this.client.outputCommandsInfo();
                break;
            case "list":
                listGroups();
                break;
            case "speak":
                if (commandTokens.length < 2) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/speak <message>'";
                    this.client.receiveMessage(error);
                }
                speakToGroup(commandTokens);
                break;
            case "talk":
                if (commandTokens.length < 3) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/talk <user_name> [<message>]'";
                    this.client.receiveMessage(error);
                }
                talkToClient(commandTokens);
//                this.client.receiveMessage("Feature "+cmd+" not implemented yet!\n");
                break;
            case "create":
                if (commandTokens.length > 3 || commandTokens.length <= 1) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/create <group_name> [<group_type>]'";
                    this.client.receiveMessage(error);
                }
                createGroup(commandTokens);
                break;
            case "select":
                if (commandTokens.length != 2) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: /select <new_group>";
                    this.client.receiveMessage(error);
                } else {
                    selectGroup(commandTokens);
                }
                break;
            case "request":
                if (commandTokens.length != 2) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/request [<group_name>]'";
                    this.client.receiveMessage(error);
                } else {
                    requestJoinToGroup(commandTokens);
                }
                break;
            case "join":
                if (commandTokens.length != 2) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/join [<group_name>]'";
                    this.client.receiveMessage(error);
                } else {
                    joinGroup(commandTokens);
                }
                break;
            case "leave":
                if (commandTokens.length > 2) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/leave [<group_name>]'";
                    this.client.receiveMessage(error);
                } else {
                    leaveGroup(commandTokens);
                }
                break;
            case "block":
                if (commandTokens.length > 2) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/block [<user_name>]'";
                    this.client.receiveMessage(error);
                } else {
                    blockClient(commandTokens);
                }
                break;
            case "unblock":
                if (commandTokens.length > 2) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/unblock [<user_name>]'";
                    this.client.receiveMessage(error);
                } else {
                    unblockClient(commandTokens);
                }
                break;
            case "groupname":
                if (commandTokens.length != 2) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/groupname <new_name>'";
                    this.client.receiveMessage(error);
                } else {
                    changeGroupName(clientOutputStream, commandTokens);
                }
                break;
            case "grouptype":
                if (commandTokens.length != 2) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/grouptype <group_type>'";
                    this.client.receiveMessage(error);
                } else {
                    changeGroupType(clientOutputStream, commandTokens);
                }
                break;
            case "invite":
                this.client.receiveMessage("Feature "+cmd+" not implemented yet!\n");
                break;
            case "accept":
                this.client.receiveMessage("Feature "+cmd+" not implemented yet!\n");
                break;
            case "add":
                if (commandTokens.length != 3) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/add <user_name> [<group_name>]'";
                    this.client.receiveMessage(error);
                } else {
                    addClientToGroup(commandTokens);
                }
                break;
            case "kick":
                if (commandTokens.length > 4) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/kick <user_name> [<group_name> [<reason>]]'";
                    this.client.receiveMessage(error);
                } else {
                    kickClientFromGroup(commandTokens);
                }
                break;
            case "promote":
                if (commandTokens.length > 3) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/promote <user_name> [<group_name>]'";
                    this.client.receiveMessage(error);
                } else {
                    promoteClientInGroup(commandTokens);
                }
                break;
            case "demote":
                if (commandTokens.length > 3) {
                    error = "Invalid number of parameters!";
                    error += "The correct format is: '/demote <user_name> [<group_name>]'";
                    this.client.receiveMessage(error);
                } else {
                    demoteClientInGroup(commandTokens);
                }
                break;
            case "delete":
                deleteGroup();
                break;
            default:
                this.client.receiveMessage("Feature "+cmd+" not implemented yet!\n");
                break;
        }
    }

    private void listGroups() throws IOException {
        String serverGroups = server.listServerGroups();
        serverGroups = "Available groups on server: "+serverGroups+"\n";

        String clientGroups = this.client.listClientsGroups();
        clientGroups = "Your groups: "+clientGroups+"\n";

        String listGroups = serverGroups + clientGroups;
        clientOutputStream.write(listGroups.getBytes());
    }

    private void speakToGroup(String[] commandTokens) throws IOException {
        String[] lastPart = splitCommands(commandTokens, 1);
        String lastPartMessage = String.join(" ", lastPart);
        String msg = this.client.getUsername() + " [#"+this.client.getClientId()+"]: " + lastPartMessage + "\n";

        Server.broadcastMessageToGroup(msg, this.client.getCurrentGroupId(), this.client.getUsername()); // do not send msg to owner
        clientOutputStream.write(("You wrote: " + lastPartMessage+"\n").getBytes()); // write in his/her own stream
    }

    private void talkToClient(String[] commandTokens) throws IOException {
        ClientHandler involvedClient;

        String clientName = commandTokens[1];
        try {
            involvedClient = Server.getClientByName(clientName);
        } catch (Exception e) {
            this.client.receiveMessage("Error! Client with name "+clientName+" not found!\n");
            return;
        }

        String[] lastPart = splitCommands(commandTokens, 2);
        String lastPartMessage = String.join(" ", lastPart);
        String msg = this.client.getUsername() + " [#"+this.client.getClientId()+"]:\n" + lastPartMessage + "\n";

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
            this.client.receiveMessage("Error! Client with name "+clientName+" not found!");
            return;
        }

        ArrayList<ClientHandler> blockedClientsList = this.client.getBlockedClientsList();
        // validate request
        if (!blockedClientsList.contains(involvedClient)) {
            this.client.receiveMessage("Error! Client with name "+involvedClient.getUsername()+" is not blocked!\n");
            return;
        }

        // proceed
        blockedClientsList.remove(involvedClient);
        this.client.receiveMessage("Client with name "+involvedClient.getUsername()+" has been unblocked!\n");
        involvedClient.receiveMessage("Client with name "+involvedClient.getUsername()+" has unblocked you!\n");
    }

    private void blockClient(String[] commandTokens) throws IOException {
        ClientHandler involvedClient;

        String clientName = commandTokens[1];
        try {
            involvedClient = Server.getClientByName(clientName);
        } catch (Exception e) {
            this.client.receiveMessage("Error! Client with name "+clientName+" not found!");
            return;
        }

        ArrayList<ClientHandler> blockedClientsList = this.client.getBlockedClientsList();
        // validate request
        if (blockedClientsList.contains(involvedClient)) {
            this.client.receiveMessage("Error! Client with name "+involvedClient.getUsername()+" is already blocked!\n");
            return;
        }

        // proceed
        blockedClientsList.add(involvedClient);
        this.client.receiveMessage("Client with name "+involvedClient.getUsername()+" has been blocked!\n");
        involvedClient.receiveMessage("Client with name "+involvedClient.getUsername()+" has blocked you!\n");
    }

    private void demoteClientInGroup(String[] commandTokens) throws IOException {
        GroupHandler involvedGroup;
        ClientHandler involvedClient;

        String clientName = commandTokens[1];
        try {
            involvedClient = Server.getClientByName(clientName);
        } catch (Exception e) {
            this.client.receiveMessage("Error! Client with name "+clientName+" not found!");
            return;
        }

        String groupName;
        if (commandTokens.length == 3) {
            groupName = commandTokens[2];
        } else {
            groupName = this.client.getCurrentGroup().getName();
        }
        try {
            involvedGroup = Server.getGroupByName(groupName);
        } catch (Exception e) {
            this.client.receiveMessage("Error! Group "+groupName+" not found!");
            return;
        }

        // validate request
        if (!involvedClient.getGroupsList().get(involvedGroup.getId()).equals(Server.ROLE_ADMIN)) {
            this.client.receiveMessage("Error! Client with name "+involvedClient.getUsername()+" is not an admin in group "+involvedGroup.getName()+"!");
            return;
        }

        // proceed!
        if (this.client.isAdminInGroup(involvedGroup.getId())) {
            involvedClient.getGroupsList().put(involvedGroup.getId(), Server.ROLE_USER);

            String involvedClientMsg = "You have been demoted to User in the group " + involvedGroup.getName();
            involvedClientMsg += " by " + this.client.getUsername() + "!\n";
            involvedClient.receiveMessage(involvedClientMsg);

            String broadcastMsg = "User " + involvedClient.getUsername() + " has been demoted to User in the group ";
            broadcastMsg += involvedGroup.getName() + " by " + this.client.getUsername() + "!\n";
            Server.broadcastMessageToGroup(broadcastMsg, involvedGroup.getId());
        } else {
            this.client.receiveMessage("You are not allowed to demote clients in the group "+involvedGroup.getName()+" (since you're not an admin)!");
        }
    }

    private void promoteClientInGroup(String[] commandTokens) throws IOException {
        GroupHandler involvedGroup;
        ClientHandler involvedClient;

        String clientName = commandTokens[1];
        try {
            involvedClient = Server.getClientByName(clientName);
        } catch (Exception e) {
            this.client.receiveMessage("Error! Client with name "+clientName+" not found!");
            return;
        }

        String groupName;
        if (commandTokens.length == 3) {
            groupName = commandTokens[2];
        } else {
            groupName = this.client.getCurrentGroup().getName();
        }
        try {
            involvedGroup = Server.getGroupByName(groupName);
        } catch (Exception e) {
            this.client.receiveMessage("Error! Group "+groupName+" not found!");
            return;
        }

        // validate request
        if (involvedClient.getGroupsList().get(involvedGroup.getId()).equals(Server.ROLE_ADMIN)) {
            this.client.receiveMessage("Error! Client with name "+involvedClient.getUsername()+" is already admin in group "+involvedGroup.getName()+"!");
            return;
        }

        // proceed!
        if (this.client.isAdminInGroup(involvedGroup.getId())) {
            involvedClient.getGroupsList().put(involvedGroup.getId(), Server.ROLE_ADMIN);

            String involvedClientMsg = "You have been promoted to Admin in the group " + involvedGroup.getName();
            involvedClientMsg += " by " + this.client.getUsername() + "!\n";
            involvedClient.receiveMessage(involvedClientMsg);

            String broadcastMsg = "User " + involvedClient.getUsername() + " has been promoted to Admin in the group ";
            broadcastMsg += involvedGroup.getName() + " by " + this.client.getUsername() + "!\n";
            Server.broadcastMessageToGroup(broadcastMsg, involvedGroup.getId());
        } else {
            this.client.receiveMessage("You are not allowed to promote clients in the group "+involvedGroup.getName()+" (since you're not an admin)!");
        }
    }

    private void kickClientFromGroup(String[] commandTokens) throws IOException {
        GroupHandler involvedGroup;
        ClientHandler involvedClient;
        String reason = "because!";

        try {
            involvedClient = Server.getClientByName(commandTokens[1]);
        } catch (Exception e) {
            this.client.receiveMessage("Error! Client not found!");
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
                groupName = this.client.getCurrentGroup().getName();
            }}
        try {
            involvedGroup = Server.getGroupByName(groupName);
        } catch (Exception e) {
            this.client.receiveMessage("Error! Group not found!");
            return;
        }

        // validate request
        if (involvedClient.getGroupsList().get(involvedGroup.getId()).equals(Server.ROLE_SUPERADMIN)) {
            this.client.receiveMessage("Error! Client with name "+involvedClient.getUsername()+" cannot be kicked from group "+involvedGroup.getName()+". Reason: super admin!");
            return;
        }

        // proceed!
        if (this.client.isAdminInGroup(involvedGroup.getId())) {
            involvedGroup.getUsersList().remove(involvedClient);

            involvedClient.getGroupsList().remove(involvedGroup.getId());
            String msg = "You have been kicked from the group " + involvedGroup.getName() + " by ";
            msg += this.client.getUsername() + ". Reason: " + reason + "!\n";
            involvedClient.receiveMessage(msg);

            String broadcastMsg = "User " + involvedClient.getUsername() + " has been kicked from the group ";
            broadcastMsg += involvedGroup.getName() + " by " + this.client.getUsername() + ". Reason: " + reason + "!\n";
            Server.broadcastMessageToGroup(broadcastMsg, involvedGroup.getId());
        } else {
            this.client.receiveMessage("You are not allowed to kick clients from the group "+involvedGroup.getName()+" (since you're not an admin)!");
        }
    }

    private void addClientToGroup(String[] commandTokens) throws IOException {
        GroupHandler involvedGroup = null;
        ClientHandler involvedClient = null;

        String clientName = commandTokens[1];
        try {
            involvedClient = Server.getClientByName(clientName);
        } catch (Exception e) {
            this.client.receiveMessage("Error! Client with name "+clientName+" not found!");
            return;
        }

        String groupName;
        if (commandTokens.length == 3) {
            groupName = commandTokens[2];
        } else {
            groupName = this.client.getCurrentGroup().getName();
        }
        try {
            involvedGroup = Server.getGroupByName(groupName);
        } catch (Exception e) {
            this.client.receiveMessage("Error! Group "+groupName+" not found!");
            return;
        }

        // validate request from client
        if (!involvedClient.getGroupsList().get(involvedGroup.getId()).equals(Server.ROLE_JOIN_REQUESTED)) {
            this.client.receiveMessage("Error! Client with name "+involvedClient.getUsername()+" did not request to join group "+involvedGroup.getName()+"!");
            return;
        }

        // proceed!
        if (this.client.isAdminInGroup(involvedGroup.getId())) {
            involvedGroup.usersList.add(involvedClient);
            involvedClient.getGroupsList().put(involvedGroup.getId(), Server.ROLE_USER);
            String msg = "You have been added to group " + involvedGroup.getName() + " by " + this.client.getUsername();
            msg += ". You can switch to this group with: '/select " + involvedGroup.getName() + "'!\n";
            involvedClient.receiveMessage(msg);
            String broadcastMsg = "User " + involvedClient.getUsername() + " has been added to group ";
            broadcastMsg += involvedGroup.getName() + " by " + this.client.getUsername() + "!\n";
            Server.broadcastMessageToGroup(broadcastMsg, involvedGroup.getId());
        } else {
            this.client.receiveMessage("You are not allowed to make changes to the group "+involvedGroup.getName()+" (since you're not an admin)!");
        }
    }

    private void requestJoinToGroup(String[] commandTokens) throws IOException {
        GroupHandler groupToJoin = null;
        String groupName = commandTokens[1];
        try {
            groupToJoin = Server.getGroupByName(groupName);
        } catch (Exception e) {
            this.client.receiveMessage("Error! Group "+groupName+" not found!");
            return;
        }

        if (groupToJoin != null) {
            switch(groupToJoin.getType()) {
                case GroupHandler.TYPE_PUBLIC:
                    this.client.receiveMessage("Group type is: PUBLIC. You cannot request approval! You should use: '/join <group_name>'!\n");
                    break;
                case GroupHandler.TYPE_PRIVATE:
                    this.client.getGroupsList().put(groupToJoin.getId(), Server.ROLE_JOIN_REQUESTED);
                    String[] adminRoles = {Server.ROLE_ADMIN, Server.ROLE_SUPERADMIN};
                    String msg = "User " + this.client.getUsername() + " requested access to group " + groupToJoin.getName();
                    msg += ". You could add him/her with: '/add " + this.client.getUsername() + " " + groupToJoin.getName() + "'!\n";
                    Server.broadcastMessageToGroup(msg, groupToJoin.getId(), "", adminRoles);
                    this.client.receiveMessage("Request has been sent! You will be added by an admin if you are eligible!\n");
                    break;
                case GroupHandler.TYPE_CLOSED:
                    this.client.getGroupsList().put(groupToJoin.getId(), Server.ROLE_JOIN_REQUESTED);
                    this.client.receiveMessage("Group type is CLOSED. You can only be added by an admin!\n");
                    break;
            }
        }
    }

    private void joinGroup(String[] commandTokens) throws IOException {
        GroupHandler groupToJoin = null;
        try {
            groupToJoin = Server.getGroupByName(commandTokens[1]);
        } catch (Exception e) {
            this.client.receiveMessage("Error! Group not found!");
            return;
        }

        if (this.client.isMemberOf(groupToJoin.getId())) {
            this.client.receiveMessage("You already are a member of this group!\n");
            return;
        }

        if (groupToJoin != null) {
            switch(groupToJoin.getType()) {
                case GroupHandler.TYPE_PUBLIC:
                    groupToJoin.getUsersList().add(this.client);
                    this.client.getGroupsList().put(groupToJoin.getId(), Server.ROLE_USER);
                    this.client.setCurrentGroup(groupToJoin);
                    this.client.receiveMessage("Group type is: PUBLIC. You are now a member!\n");
                    break;
                case GroupHandler.TYPE_PRIVATE:
                    this.client.getGroupsList().put(groupToJoin.getId(), Server.ROLE_JOIN_REQUESTED);
                    this.client.receiveMessage("Group type is: PRIVATE. You have to request approval first: '/request "+groupToJoin.getName()+"'!\n");
                    break;
                case GroupHandler.TYPE_CLOSED:
                    this.client.getGroupsList().put(groupToJoin.getId(), Server.ROLE_JOIN_REQUESTED);
                    this.client.receiveMessage("Group type is CLOSED. You have to receive an invite from an admin in order to join!\n");
                    break;
            }
        }
    }

    private void leaveGroup(String[] commandTokens) throws IOException {
        GroupHandler involvedGroup;
        if (commandTokens.length == 2) {
            String groupName = commandTokens[1];
            if (groupName.equals(Server.MAIN_GROUP_NAME)) {
                this.client.receiveMessage("Error! Group "+Server.MAIN_GROUP_NAME+" cannot be left!");
                return;
            }
            try {
                involvedGroup = Server.getGroupByName(groupName);
            } catch (Exception e) {
                this.client.receiveMessage("Error! Group "+groupName+" not found!");
                return;
            }
        } else {
            involvedGroup = this.client.getCurrentGroup();
        }

        // validate request from client
        if (this.client.getGroupsList().get(involvedGroup.getId()).equals(Server.ROLE_SUPERADMIN)) {
            this.client.receiveMessage("Error! You are SUPERADMIN and you cannot leave the group "+involvedGroup.getName()+". You could use: '/delete "+involvedGroup.getName()+"'!");
            return;
        }

        this.client.getGroupsList().remove(involvedGroup.getId());
        // @TODO: if this client was the last in the group, should we delete the group also!?
        Server.broadcastMessageToGroup("User "+this.client.getUsername()+" left the group "+involvedGroup.getName()+"!\n", involvedGroup.getId());
    }

    private void changeGroupName(OutputStream clientOutputStream, String[] commandTokens) throws IOException {
//        String newGroupName = GroupHandler.validateGroupName(clientOutputStream, commandTokens[1], this, Server.getGroupsList());
        String error = "";
        String oldGroupName = this.client.getCurrentGroup().getName();
        String chosenGroupName = commandTokens[1];

        // validate group name
        String processedGroupName = GroupHandler.processGroupName(chosenGroupName);
        if (processedGroupName.length() == 0) {
            processedGroupName = GroupHandler.generateGroupName();
            error = "Group name contains only invalid characters! Should contain only letters, _ and/or numbers and start with a letter!\n!";
            error += "A random name has been generated for you: "+processedGroupName;
            this.client.receiveMessage(error);
        }
        try {
            GroupHandler.checkGroupNameEligibility(processedGroupName);
        } catch (ForbiddenNameException e) {
            error += "Group name "+chosenGroupName+" not allowed!!\n";
            this.client.receiveMessage(error);
        }
        if (oldGroupName.equals(processedGroupName))  {
            error += "The new group name is the same with the current one!!";
        }
        if (error.length() > 0 ) {
            clientOutputStream.write(error.getBytes());
            return;
        }

        // the name is valid, proceed!
        if (this.client.isAdminInGroup(this.client.getCurrentGroupId())) {
            this.client.getCurrentGroup().setName(processedGroupName);
            String confirmationMsg = "User " + this.client.getUsername() + " changed the group name from " + oldGroupName + " to " + this.client.getCurrentGroup().getName() + "!\n";
            Server.broadcastMessageToGroup(confirmationMsg, this.client.getCurrentGroupId());
        } else {
            this.client.receiveMessage("You are not allowed to make changes to the group "+this.client.getCurrentGroup()+" (since you're not an admin)!");
        }
    }

    private void changeGroupType(OutputStream clientOutputStream, String[] commandTokens) throws Exception {
        String newGroupType = GroupHandler.validateGroupType(clientOutputStream, commandTokens[1], false);
        if (newGroupType.length() == 0) {
            return;
        }
        String oldGroupType =this.client.getCurrentGroup().getType();
        if (oldGroupType.equals(newGroupType))  {
            this.client.receiveMessage("The new group type is the same with the current one!!");
            return;
        }

        // proceed!
        if (this.client.isAdminInGroup(this.client.getCurrentGroupId())) {
            this.client.getCurrentGroup().setType(newGroupType);
            Server.broadcastMessageToGroup("User "+this.client.getUsername()+" changed the group type from "+oldGroupType+" to "+this.client.getCurrentGroup().getType()+"!\n", this.client.getCurrentGroupId());
        } else {
            this.client.receiveMessage("You are not allowed to make changes to the group "+this.client.getCurrentGroup()+" (since you're not an admin)!");
        }
    }

    private void changeUsername(String[] commandTokens) throws IOException {
        String chosenUsername = this.client.processUsername(commandTokens[1]);
        String error = this.client.validateUsername(chosenUsername);

        if (error.length() > 0) {
            this.client.receiveMessage("Changing username failed! "+error);
        } else {
            String currentUsername = this.client.getUsername();

            this.client.setUsername(chosenUsername);
            this.client.receiveMessage("Changing username OK!\n");

            String msg = "User '" + currentUsername + "' just transformed into '" + chosenUsername + "'!\n";
            Server.broadcastMessage(msg, true);
        }
    }

    private void deleteGroup() throws IOException {
        GroupHandler oldGroup = this.client.getCurrentGroup();
        if (this.client.getGroupsList().get(oldGroup.getId()).equals(Server.ROLE_SUPERADMIN)) {
            Server.broadcastMessageToGroup("This group is being deleted! Sorry!\n", oldGroup.getId());

            ArrayList<ClientHandler> clientsList = Server.getClientsList();
            for (ClientHandler client: clientsList) {
                if (client.isMemberOf(oldGroup.getId())) {
                    if (client.getCurrentGroupId() == oldGroup.getId()) {
                        client.setCurrentGroup(Server.getMainGroup());
                        client.updateRoleInCurrentGroup(Server.MAIN_GROUP_ID, client.getGroupsList().get(Server.MAIN_GROUP_ID));
                    }
                    client.getGroupsList().remove(oldGroup.getId());
                }
            }
            Server.getGroupsList().remove(oldGroup);
            // select th emain group as the current group for the current client
            this.client.setCurrentGroup(Server.getMainGroup());
        } else {
            this.client.receiveMessage("You are not allowed to delete this group since you didn't create it!\n");
        }
    }

    private void selectGroup(String[] commandTokens) throws IOException {
        GroupHandler oldGroup = this.client.getCurrentGroup();
        GroupHandler newGroup;
        try {
            newGroup = Server.getGroupByName(commandTokens[1]);
        } catch (Exception e) {
            this.client.receiveMessage("Error! Group not found!");
            return;
        }

        if (this.client.isMemberOf(newGroup.getId())) {
            this.client.setCurrentGroup(newGroup);
            Server.broadcastMessageToGroup("User "+this.client.getUsername()+" left the group "+oldGroup.getName()+"!\n", oldGroup.getId());
            Server.broadcastMessageToGroup("User "+this.client.getUsername()+" joined the group "+newGroup.getName()+"!\n", newGroup.getId());
        } else {
            this.client.receiveMessage("You are not yet registered in group "+newGroup.getName()+"!");

            String helperMsg =  "You may create such a group with the command: '/create "+newGroup.getName()+"'!\n";
            ArrayList<GroupHandler> groupsList = Server.getGroupsList();
            for (GroupHandler group: groupsList) {
                if (group.getName().equals(newGroup.getName())) {
                    switch(group.getType()) {
                        case GroupHandler.TYPE_PUBLIC:
                            helperMsg = "The group is Public. You may join this group using the command '/join "+newGroup.getName()+"'!\n";
                            break;
                        case GroupHandler.TYPE_PRIVATE:
                            this.client.getGroupsList().put(group.getId(), Server.ROLE_JOIN_REQUESTED);
                            helperMsg = "The group is Private. You may request access to this group using the command '/request "+newGroup.getName()+"'!\n";
                            break;
                        case GroupHandler.TYPE_CLOSED:
                            this.client.getGroupsList().put(group.getId(), Server.ROLE_JOIN_REQUESTED);
                            helperMsg = "The group is Closed. You may only be added by an admin in this group!\n";
                            break;
                    }
                    break;
                }
            }

            this.client.receiveMessage(helperMsg);
        }
    }

    private void createGroup(String[] commandTokens) throws Exception {
        String error = "";
        GroupHandler chosenGroup;
        String chosenGroupName = commandTokens[1];

        // validate group name
        String processedGroupName = GroupHandler.processGroupName(chosenGroupName);
        if (processedGroupName.length() == 0) {
            processedGroupName = GroupHandler.generateGroupName();
            error = "Group name contains only invalid characters! Should contain only letters, _ and/or numbers and start with a letter!\n!";
            error += "A random name has been generated for you: "+processedGroupName;
            this.client.receiveMessage(error);
        }
        try {
            GroupHandler.checkGroupNameEligibility(processedGroupName);
        } catch (ForbiddenNameException e) {
            error += "Group name "+chosenGroupName+" not allowed!!\n";
            this.client.receiveMessage(error);
        }
        if (error.length() > 0 ) {
            clientOutputStream.write(error.getBytes());
            return;
        }

        // the name is valid, proceed!
        chosenGroup = Server.getGroupByName(processedGroupName);
        if (chosenGroup != null) {
            error = "The group: '"+processedGroupName+"' already exists!\n";
            if (this.client.isNotMemberOf(chosenGroup.getId())) {
                error += "You may join this group using the command '/join "+processedGroupName+"'!\n";
            } else {
                error += " You are already registered in the group '"+processedGroupName+"'!\n";
                if  (!this.client.getCurrentGroup().getName().equals(processedGroupName)) {
                    error += " You should switch to it by using the command: '/select "+processedGroupName+"'!\n";
                }
            }
            clientOutputStream.write(error.getBytes());
        } else {
            String chosenType = commandTokens.length == 3 ? commandTokens[2] : GroupHandler.TYPE_PUBLIC;
            String groupType = GroupHandler.validateGroupType(clientOutputStream, chosenType, true);

            GroupHandler newGroup = server.createGroup(this.client, processedGroupName, groupType);

            this.client.setCurrentGroup(newGroup);
            this.client.updateRoleInCurrentGroup(newGroup.getId(), Server.ROLE_SUPERADMIN);

            String confirmationMsg = "Group " + newGroup.getName() + " has been created and your role is SUPERADMIN!\n";
            clientOutputStream.write(confirmationMsg.getBytes());
            Server.broadcastMessageToGroup("Group " + newGroup.getName() + " has been created!\n", this.client.getCurrentGroupId(), this.client.getUsername());

        }

//        try {
//            error += " You should create it with the command: '/create "+processedGroupName+"'!\n";
//        } catch (GroupNotFoundException e) {
//            error = "There is no group with the name: '"+processedGroupName+"'! You should create it with the command: '/create "+processedGroupName+"'!\n";
//        } catch (DuplicateGroupException e) {
//            error = "The group: '"+processedGroupName+"' already exists!\n";
//            if (this.isNotMemberOf(chosenGroup.getId())) {
//                error += " You should create it with the command: '/create "+processedGroupName+"'!\n";
//            }
//        }

    }

    private void handleLogin(String[] commandTokens) throws IOException {
        String chosenUsername = this.client.processUsername(commandTokens[1]);
        String error = this.client.validateUsername(chosenUsername);

        if (error.length() > 0) {
            clientOutputStream.write(("Login failed! "+error).getBytes());
        } else {
            if (this.client.isLoggedIn()) {
                clientOutputStream.write(("You are already logged in! If you need to change the username, use the command: '/user <new_username>' !\n").getBytes());
            } else {
                String defaultUsername = this.client.getUsername();

                this.client.setUsername(chosenUsername);

                System.out.println("clientOutputStream:");
                System.out.println(clientOutputStream);
                clientOutputStream.write("Login OK!\n".getBytes());
                String msg = "User '" + defaultUsername + "' just logged in as '" + this.client.getUsername() + "'!\n";
                Server.broadcastMessage(msg, true);
                this.client.updateRoleInCurrentGroup(this.client.getCurrentGroupId(), Server.ROLE_USER);

                this.client.outputGroupInfo();
            }
        }
    }

    public String[] getCommandsForRole(String currentRole) {
        String[] order = {Server.ROLE_GUEST, Server.ROLE_USER, Server.ROLE_ADMIN, Server.ROLE_SUPERADMIN};

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

}
