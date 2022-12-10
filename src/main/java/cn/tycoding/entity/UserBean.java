package cn.tycoding.entity;

import java.io.Serializable;

public class UserBean implements Serializable {
    // 属性声明
    private String username;
    private String password;
    private String userType;

    //构造方法
    public UserBean(){}
    // get 方法
    public String getUsername(){ return username; }
    public String getPassword(){ return password; }
    public String getUserType(){ return userType; }
    // set 方法
    public void setUsername(String username){
        this.username = username;
    }
    public void setPassword(String password){
        this.password = password;
    }
    public void setUserType(String userType){
        this.userType = userType;
    }

}