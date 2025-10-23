package com.example.demo.Domain.Common.Entity;

public enum Gender{
    M("남자"),
    F("여자"),
    N("선택안함");
    
    private final String description;
    
    Gender(String description){
        this.description = description;
    }
    
    public String getDescription(){
        return description;
    }
}
