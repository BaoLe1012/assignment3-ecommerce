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
    public String encryptPassword(String password) {
    
}