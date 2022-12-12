package com.example.superwe;

public class FriendInformation {
    private String name;
    private String nickname;
    private String wx;
    private String position;

    public FriendInformation(String name,String nickname,String wx,String position){
        this.name=name;
        this.nickname=nickname;
        this.wx=wx;
        this.position=position;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setWx(String wx) {
        this.wx = wx;
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPosition() {
        return position;
    }

    public String getWx() {
        return wx;
    }
}
