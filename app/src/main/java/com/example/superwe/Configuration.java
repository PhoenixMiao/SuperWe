package com.example.superwe;

public class Configuration {

    private String name;
    private String description;

    public Configuration(){
        super();
    }

    public Configuration(String name,String description){
        this.name=name;
        this.description = description;
    }

    public void setName(String name){
        this.name=name;
    }

    public void setDescription(String description){
        this.description=description;
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }
}
