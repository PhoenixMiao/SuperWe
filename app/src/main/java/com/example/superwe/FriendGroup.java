package com.example.superwe;

public class FriendGroup {

    private String groupName;
    private String friendList;

    public FriendGroup(String groupName,String friendList){
        this.groupName=groupName;
        this.friendList=friendList;
    }

    public String getFriendList() {
        return friendList;
    }

    public String getGroupName() {
        return groupName;
    }
}
