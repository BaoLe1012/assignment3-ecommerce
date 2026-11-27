import java.util.Random;
import java.io.File;
import java.util.Scanner;

public class UserOperation{

    private static UserOperation instance;
    private static final String FILE_PATH = "data/users.txt";

    private UserOperation() {
        // Private constructor to prevent instantiation
    }

    //returns new instance of UserOperation
    public static UserOperation getInstance(){
        if (instance == null) {
            instance = new UserOperation();
        }
        return instance;
    }

    //generateUniqueUserID
    public String generateUniqueUserId(){
        Random random = new Random();
        String newId;
        boolean isDuplicate;
        do {
            isDuplicate = false; // Reset the duplicate flag for each iteration

            long number = (long) (random.nextDouble() * 10_000_000_000L);
            newId = String.format("u_%010d", number);

            //Check if the generated ID is unique or not
        File file = new File("data/users.txt");
        if(file.exists()){
            try (Scanner scanner = new Scanner(file)) {
                isDuplicate = false;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.contains("\"user_id\":\"" + newId + "\"")) {
                        isDuplicate = true;       // means it is duplicate
                        break;                    //I out the loop to create new id
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        } while (isDuplicate); //keep generating new id until it is unique
        return newId;
    }

    //encruptPassword()
    public String encryptPassword(String userPassword) {

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        int len = userPassword.length();

        StringBuilder randomString = new StringBuilder();
        for(int i=0; i< len*2; i++){
            randomString.append(chars.charAt(random.nextInt(chars.length())));
        }

        StringBuilder encrypted= new StringBuilder("^^");
        for(int i=0; i< len; i++){
            encrypted.append(randomString.charAt(i));
            encrypted.append(randomString.charAt(i*2 +1));
            encrypted.append(userPassword.charAt(i));
        }
        encrypted.append("$$");
        return encrypted.toString();
    }

    public String decryptPassword(String encryptedPassword){

        String core= encryptedPassword.substring(2, encryptedPassword.length()-2);

        StringBuilder original= new StringBuilder();
        for(int i=0; i+2< core.length(); i+=3){
            original.append(core.charAt(i+2));
        }
        return original.toString();
    }

    //checkUserNameExists()- the same way as checkUserIdExists() 
    public boolean checkUserNameExists(String userName){
        File file = new File(FILE_PATH);
        if(file.exists()){
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.contains("\"user_name\":\"" + userName + "\"")) {
                        return true; // Username exists
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false; // Username does not exist
    }
    //validateUserName()
    public boolean validateUsername(String userName) {
        if(userName == null || userName.length()< 5){
            return false;
        }
        //check one by one character in the username
        for(int i = 0; i< userName.length(); i++){
            char c = userName.charAt(i);

            if(!Character.isLetterOrDigit(c) && c != '_'){
                return false;
            }
        }
        //pass the loop
        return true;
    }

    //validatePassword()
    public boolean validatePassword(String userPassword) {
        if(userPassword == null || userPassword.length() <5){
            return false;
        }
        
        //check char and digit
        boolean hasLetter = false;
        boolean hasDigit = false;
        for(int i=0; i< userPassword.length(); i++){
            char c = userPassword.charAt(i);
            if(Character.isLetter(c)){
                hasLetter= true;
            }
            if(Character.isDigit(c)){
                hasDigit= true;
            }
        }
        return hasLetter && hasDigit; //return true if both are true
    }

    //login
    public User login(String userName, String userPassword) {
        
        // userName and userPassword are null or not
        if(userName == null || userPassword == null){
            return null;
        }

        File file = new File("data/users.txt");
       if(!file.exists()){
        return null; // File does not exist, cannot log in
       }

       try (Scanner scanner = new Scanner(file) ){
        while (scanner.hasNextLine()){
            String line = scanner.nextLine().replace("{", "").replace("}", "").replace("\"", "");
            if (line.isEmpty()) {
                continue; // Skip empty lines
            }

            // Split the line space colon
            String[] parts = line.split(",");

            //get userName and userPassword from the line ( 1 & 2)
            String name = parts[1].split(":", 2)[1];
            String encryptedPwd = parts[2].split(":", 2)[1];

            //if name and password match, stept to id, role, role
            if(name.equals(userName) && decryptPassword(encryptedPwd).equals(userPassword)){
                String userId = parts[0].split(":", 2)[1];
                String time = parts[3].split(":", 2)[1];
                String role = parts[4].split(":", 2)[1];

                //admin role
                if(role.equals("admin")){
                    return new Admin(userId, name, encryptedPwd, time, role);
            } else {
                //customer role
                String email = parts[5].split(":", 2)[1];
                String phone = parts[6].split(":", 2)[1];
                return new Customer(userId, name, encryptedPwd, time, role, email, phone);
            }           
            }
       }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null; // No matching user found
    }
}
          