package com.temuraru;

import java.util.ArrayList;

public class GroupHandler {
    private ClientHandler owner;
    private ArrayList<ClientHandler> adminsList = new ArrayList<>();

    public GroupHandler(ClientHandler owner) {
        this.owner = owner;
        adminsList.add(owner);
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

}
