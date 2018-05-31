package com.temuraru;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

public class GroupHandler {
    public static final String TYPE_PUBLIC = "public";
    public static final String TYPE_PRIVATE = "private";
    public static final String TYPE_CLOSED = "private";
    private String type;
    private String name;
    private ClientHandler owner;
    private ArrayList<ClientHandler> adminsList = new ArrayList<>();

    public GroupHandler(ClientHandler owner, String name, String type) {
        this.owner = owner;
        this.name = name;
        this.type = type;
        adminsList.add(owner);
    }

    public static String validateName(OutputStream clientOutputStream, String[] commandTokens, ClientHandler client, ArrayList<GroupHandler> groupsList) throws IOException {

        String error = "";
        if (commandTokens.length != 2) {
            error = "Invalid number of parameters for login command!";
            return error;
        }

        String chosenGroupName = commandTokens[1].replaceAll("[^a-zA-Z0-9_]+", "").replaceFirst("^[^a-zA-Z]+", "");
        if (chosenGroupName.length() > 15) {
            chosenGroupName = chosenGroupName.substring(0,14);
        }
        if (chosenGroupName.length() == 0) {
            error = "Group name contains only invalid characters! Should contain only letters, _ and/or numbers and start with a letter!\n!";
        } else {
            error = checkEligibility(client, chosenGroupName, groupsList);
        }

        String currentGroupName = chosenGroupName;
        if (error.length() > 0) {
            clientOutputStream.write(error.getBytes());
            currentGroupName = generateGroupName();
        }

        return currentGroupName;
    }

    private static String checkEligibility(ClientHandler client, String chosenGroupName, ArrayList<GroupHandler> groupsList) {
        Map<String, String> clientGroupsList = client.getGroupsList();
        if (clientGroupsList.get(chosenGroupName).length() > 0) {
            return "You are already part of the group "+chosenGroupName+"! You should use the comand /select "+chosenGroupName+"!\n";
        }

        ArrayList<String> forbiddenNamesList = new ArrayList<String>();
        forbiddenNamesList.add("all");
        forbiddenNamesList.add("user");
        forbiddenNamesList.add("group");
        forbiddenNamesList.add("admin");
        forbiddenNamesList.add("serverbot");
        forbiddenNamesList.add("superadmin");
        forbiddenNamesList.add("guest");

        if (forbiddenNamesList.contains(chosenGroupName)) {
            return "Group name "+chosenGroupName+" not allowed! a random one has been generated for you!\n";
        } else {
            for (GroupHandler group: groupsList) {
                if (group.getName().equals(chosenGroupName)) {
                    return "You may join this group using the command /join "+chosenGroupName+"!\n";
                }
            }
        }

        return "";
    }

    private static String generateGroupName() {
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(10000);
        return "group" + String.valueOf(randomInt);
    }

    public static String validateType(OutputStream clientOutputStream, String[] commandTokens) throws IOException {
        String type = GroupHandler.TYPE_PUBLIC;
        String[] availableTypes = {TYPE_PUBLIC, TYPE_PRIVATE, TYPE_CLOSED};
        String chosenType = commandTokens[2];
        if (Arrays.asList(availableTypes).contains(chosenType)) {
            type = chosenType;
        } else {
            String error = "The third parameter (group type: "+chosenType+") is invalid! ";
            error += "It should be one of: public, private, closed!\n";
            clientOutputStream.write(error.getBytes());
        }

        return type;
    }

    public ClientHandler getOwner() {
        return owner;
    }

    public void setOwner(ClientHandler owner) {
        this.owner = owner;
    }

    public ArrayList<ClientHandler> getAdminsList() {
        return adminsList;
    }

    public void setAdminsList(ArrayList<ClientHandler> adminsList) {
        this.adminsList = adminsList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
