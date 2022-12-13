package cn.tycoding.entity;

import java.io.Serializable;

public class UserBean implements Serializable {
    // 属性声明
    private String username;
    private String password;
    private String userType;
    private int age;
    private String idcardno;
    private int workcondition;

    //构造方法
    public UserBean(){}
    // get 方法
    public String getUsername(){ return username; }
    public String getPassword(){ return password; }
    public String getUserType(){ return userType; }
    public int getUserAge(){ return age; }
    public String getUserIdcardno(){ return idcardno; }
    public int getUserWorkcondition(){ return workcondition; }
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
    public void setUserAge(int UserAge){
        this.age = UserAge;
    }
    public void setUserIdcardno(String UserIdcardno){
        this.idcardno = UserIdcardno;
    }
    public void setUserWorkcondition(int UserWorkcondition){
        this.workcondition = UserWorkcondition;
    }

    public void printSelf(){
        System.out.println(
                "user info: " + username + "  " + password + "  " + userType + "  " + age + "  " + idcardno + "  " + workcondition
        );
    }

}