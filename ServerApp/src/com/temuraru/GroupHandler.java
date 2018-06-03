package com.temuraru;

import com.temuraru.Exceptions.ForbiddenNameException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

public class GroupHandler {
    public static final String TYPE_PUBLIC = "public";
    public static final String TYPE_PRIVATE = "private";
    public static final String TYPE_CLOSED = "closed";
    private String type;
    private String name;
    private ClientHandler owner;
    public ArrayList<ClientHandler> adminsList = new ArrayList<>();

    public ArrayList<ClientHandler> getUsersList() {
        return usersList;
    }

    public ArrayList<ClientHandler> usersList = new ArrayList<>();
    private int id;

    public GroupHandler(ClientHandler owner, String name, String type) {
        this.owner = owner;
        this.name = name;
        this.type = type;
        adminsList.add(owner);
        usersList.add(owner);
    }

    public static String processGroupname(String newGroupName) {
        String chosenGroupName = newGroupName.replaceAll("[^a-zA-Z0-9_]+", "").replaceFirst("^[^a-zA-Z]+", "");
        if (chosenGroupName.length() > 15) {
            chosenGroupName = chosenGroupName.substring(0,14);
        }

        return chosenGroupName;
    }

    public static boolean checkGroupNameEligibility(String chosenGroupName) throws ForbiddenNameException {

        ArrayList<String> forbiddenNamesList = new ArrayList<String>();
        forbiddenNamesList.add("all");
        forbiddenNamesList.add("user");
        forbiddenNamesList.add("group");
        forbiddenNamesList.add("admin");
        forbiddenNamesList.add("serverbot");
        forbiddenNamesList.add("superadmin");
        forbiddenNamesList.add("guest");

        if (forbiddenNamesList.contains(chosenGroupName)) {
            throw new ForbiddenNameException();
        }

        return true;
    }

    public static String generateGroupName() {
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(10000);
        return "Group" + String.valueOf(randomInt);
    }

    public static String validateGroupType(OutputStream clientOutputStream, String chosenType, boolean fallback) throws IOException {
        String[] availableTypes = {TYPE_PUBLIC, TYPE_PRIVATE, TYPE_CLOSED};
        if (!Arrays.asList(availableTypes).contains(chosenType)) {
            String error = "The group type parameter ("+chosenType+") is invalid! ";
            error += "It should be one of: "+String.join(", ", availableTypes)+"!\n";
            if (fallback) {
                chosenType = GroupHandler.TYPE_PUBLIC;
                error += "Group type set to: "+GroupHandler.TYPE_PUBLIC+"!\n";
            } else {
                chosenType = "";
            }
            clientOutputStream.write(error.getBytes());
        }

        return chosenType;
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

    public void setId(int lastGroupId) {
        this.id = lastGroupId;
    }

    public int getId() {
        return id;
    }

    public String getGroupTypeSuffix() {
        String suffix = "";
        switch (this.getType()) {
            case GroupHandler.TYPE_PRIVATE:
                suffix = "[*]";
                break;

            case GroupHandler.TYPE_CLOSED:
                suffix = "[**]";
                break;

        }

        return suffix;
    }
}
