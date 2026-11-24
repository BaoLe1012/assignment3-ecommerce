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

    @Override 
    public String toString(){
        String baseJson = super.toString();
        return String.format("&s,\"user_email\":\"&s\",\"user_mobile\":\"&s\"}", baseJson.substring(0, baseJson.length()-1), this.userEmail, this.userMobile);
    }

}
