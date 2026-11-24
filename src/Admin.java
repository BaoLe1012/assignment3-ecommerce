public class Admin extends User{

    public Admin(String userId, String userName, String userPassword, String userRegisterTime, String userRole){
        super(userId, userName, userPassword, userRole, userRole);
    }

    //default contructor
    public Admin(){
        super();
        this.setUserRole("admin");   
    }
    @Override
    public String toString(){
        return super.toString();
    }
    
}