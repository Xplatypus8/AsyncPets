package com.example.taylo.asyncpets;

/**
 * Created by taylo on 3/19/2017.
 */

public class Pet {

    private String name;
    private String link;

    public Pet(String name, String link){
        this.name = name;
        this.link = link;

    }

    public String getName(){
        return name;
    }

    public String getLink(){
        return link;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setLink(String link){
        this.link = link;
    }

}
