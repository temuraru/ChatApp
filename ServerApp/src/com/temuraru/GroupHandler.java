package com.temuraru;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;

public class GroupHandler {
    private String name;
    private ClientHandler owner;
    private ArrayList<ClientHandler> adminsList = new ArrayList<>();

    public GroupHandler(ClientHandler owner, String name) {
        this.owner = owner;
        this.name = name;
        adminsList.add(owner);
    }

    public static String validateName(OutputStream clientOutputStream, String[] commandTokens) throws IOException {

        String error = "";
        if (commandTokens.length != 2) {
            error = "Invalid number of parameters for login command!";
            return error;
        }

        String chosenGroupName = commandTokens[1].replaceAll("[^a-zA-Z0-9]+", "").replaceFirst("^[^a-zA-Z]+", "");
        if (chosenGroupName.length() > 15) {
            chosenGroupName = chosenGroupName.substring(0,14);
        }
        if (chosenGroupName.length() == 0) {
            error = "Group name invalid!";
        } else {
            ArrayList<String> forbiddenNamesList = new ArrayList<String>();
            forbiddenNamesList.add("all");
            forbiddenNamesList.add("user");
            forbiddenNamesList.add("group");
            forbiddenNamesList.add("admin");
            forbiddenNamesList.add("serverbot");
            forbiddenNamesList.add("superadmin");
            forbiddenNamesList.add("guest");

            if (forbiddenNamesList.contains(chosenGroupName)) {
                error = "Group name not allowed!";
            }
        }

        String currentGroupName = chosenGroupName;
        if (error.length() > 0) {
            clientOutputStream.write(error.getBytes());
            currentGroupName = generateGroupName();
        }

        return currentGroupName;
    }

    private static String generateGroupName() {
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(10000);
        return "group" + String.valueOf(randomInt);
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
}
