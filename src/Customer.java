public class Customer extends User{
    private String userEmail;
    private String userMobile;

    public Customer(String userId, String userName, String userPassword, String userRegeString, String userRole, String userEmail, String userMobile){
        super(userId, userName, userPassword, userRegeString, userRole);
        this.userEmail= userEmail;
        this.userMobile= userMobile;
    }
    
    //default contructor
    public Customer(){
        super();
        this.userEmail="";
        this.userMobile="";
    }
    public String getUserEmail() {
        return userEmail;
    }
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    public String getUserMobile() {
        return userMobile;
    }
    public void setUserMobile(String userMobile) {
        this.userMobile = userMobile;
    }

    @Override 
    public String toString(){
        String baseJson = super.toString();
        return String.format("&s,\"user_email\":\"&s\",\"user_mobile\":\"&s\"}", baseJson.substring(0, baseJson.length()-1), this.userEmail, this.userMobile);
    }

}
