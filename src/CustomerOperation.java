import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CustomerOperation {
    
    //instance only one saved
    public static CustomerOperation instance;

    //againtst multiple instance
    private CustomerOperation() {
    }

    //getInstance()
    public static CustomerOperation getInstance() {
        if (instance == null) {
            instance = new CustomerOperation();
        }
        return instance;
    }

    //check email
    public boolean validateEmail(String userEmail){
        //sapce and null check
        if(userEmail== null || userEmail.contains(" ") ){
            return false;
        }
        //position of @ and . check
        int atIndex = userEmail.indexOf("@");
        int lastDotIndex = userEmail.lastIndexOf(".");

        //check @ not 1st and just 1 @
        if(atIndex <= 0 || atIndex != lastDotIndex){
            return false;
        }

        //fine . after @
        int dotIndex = userEmail.indexOf('.', atIndex);
        //check . not just after @ and not last char
        if(dotIndex <= atIndex+1 || dotIndex == userEmail.length()-1){
            return false;
        }
        return true;
    }

    //check mobile number
    public boolean validateMobile(String userMobile){
        if(userMobile== null || userMobile.contains(" ") ){
            return false;
        }
        //check length
        if(userMobile.length() != 10){
            return false;
        }
        //check all digit
        for(int i=0; i<userMobile.length(); i++){
            if(!Character.isDigit(userMobile.charAt(i))){
                return false;
            }
        }
        return true;
    }

    //register
    public boolean registerCustomer(String userName, String userPassword, String userEmail, String userMobile){
        UserOperation userOp = UserOperation.getInstance();
        //check legit 4 information

        if(!userOp.validateUsername(userName) || !userOp.validatePassword(userPassword) || !validateEmail(userEmail) || !validateMobile(userMobile)){
            return false;
        }

        //check username exist or not
        if (userOp.checkUsernameExist(userName)) {
            return false; // Username already exists
        }

        //make unique id
        String userId = userOp.generateUniqueUserId();
        String encryptedPassword = userOp.encryptPassword(userPassword);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String registerTime = LocalDateTime.now().format(formatter);

        //create new user
        Customer newCustomer = new Customer(userId, userName, encryptedPassword, registerTime,"customer", userEmail, userMobile);

        //save to file
        try{
            File file = new File("data/users.txt");
            if(file.getParent()!= null){
                file.getParentFile().mkdirs();
            }

            try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {
                out.println(newCustomer.toString());
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        return false;
        }
    }

    //update customer information
    public boolean updateProfile(String attributeName, String value, Customer customerObject){
        UserOperation userOp = UserOperation.getInstance();
        //check attribute name
        switch( attributeName){
            case "user_name":
                if(!userOp.validateUsername(value) || userOp.checkUsernameExist(value)){
                    return false;
                }
                customerObject.setUserName(value);
                break;
            
            case "user_password":
                if(!userOp.validatePassword(value)){
                    return false;
                }
                //make sure encrypted new password
                customerObject.setUserPassword(userOp.encryptPassword(value));
                break;
            case "user_email":
                if(!validateEmail(value)){
                    return false;
                }
                customerObject.setUserEmail(value);
                break;
            case "user_mobile":
                if(!validateMobile(value)){
                    return false;
                }
                customerObject.setUserMobile(value);
                break;
            default:
                return false;
        }
        //read all users from file and change with same userId
        File file = new File("data/users.txt");
        if(!file.exists()){
            return false;
        }
        List<String> lines = new ArrayList<>();
        boolean isUpdated = false;
        String targetUserId = "\"user_id\":\"" + customerObject.getUserId() + "\"";

        try (Scanner scanner = new Scanner(file)){
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                if(line.contains(targetUserId)){
                    lines.add(customerObject.toString());
                    isUpdated = true;
                }
                else{
                    lines.add(line);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

     if(isUpdated){
        try (PrintWriter out = new PrintWriter(new FileWriter(file, false))) {
            for(String l : lines){
                out.println(l);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
     }
     return false;
}
    //delete customer account
    public boolean deleteCustomer(String customerId){
        if(customerId == null || customerId.isEmpty()){
            return false;
        }
        File file = new File("data/users.txt");
        if(!file.exists()){
            return false;
        }
        List<String> remainingLines = new ArrayList<>();
        boolean isDeleted = false;
        String targetId="\"user_id\":\"" + customerId + "\"";

        //read all lines and check id, role customer, if found, skip that line, else add to remainingLines
        try (Scanner scanner = new Scanner(file)){
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                if(line.contains(targetId) && line.contains("\"user_role\":\"customer\"")){
                    isDeleted = true; // no add to list, means delete
                }
                else{
                    remainingLines.add(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        //if found, rewrite file without that line
        if(isDeleted){
            try (PrintWriter out= new PrintWriter(new FileWriter(file, false))){
                for(String l : remainingLines){
                    out.println(l);
                }
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;// can not fine cusId
    }
}
