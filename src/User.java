public class User{
    private String userId;
    private String userName;
    private String userPassword;
    private String userRegisterTime;
    private String userRole;

    //contructor
    public User (String userId, String userName, String userPassword, String userRegeString, String userRole ){
        this.userId= userId;
        this.userName = userName;
        this.userPassword= userPassword;
        this.userRegisterTime= userRegeString;
        this.userRole= userRole;
    }

    //default contructor
    public User(){
        this.userId="";
        this.userName="";
        this.userPassword="";
        this.userRegisterTime= "";
        this.userRole= "";
    }

    //getter and setter
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserRegisterTime() {
        return userRegisterTime;
    }

    public void setUserRegisterTime(String userRegisterTime) {
        this.userRegisterTime = userRegisterTime;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    @Override 
    public String toString(){
        return String.format("{\"user_id\": \"%s\", \"user_name\": \"%s\", \"user_password\": \"%s\", \"user_register_time\": \"%s\", \"user_role\": \"%s\" }", this.userId, this.userName, this.userPassword, this.userRegisterTime, this.userRole);
    }
}