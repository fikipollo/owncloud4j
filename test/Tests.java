/*
 * ownCloud client module
 * Makes it possible to access files on a remote ownCloud instance,
 * share them or access application attributes.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rafa Hernandez <https://github.com/fikipollo>
 */
public class Tests {

    public static void main(String[] args) {
        try {
            boolean success;
            WebDAVResponse response;
            String content;
            String originalContent;

            Owncloud4j client = new Owncloud4j("localhost", 80, "/remote.php/webdav");
            client.login("admin", "123123");

            //********************************************************************************************
            // FILES
            //********************************************************************************************
            createRandomFile("/tmp/test.txt", 2);
            originalContent = readFile("/tmp/test.txt");

            //Test 1. Create a new dirs
            System.out.println("********************************************************");
            System.out.println("Test 1. Create a new dirs");
            success = client.mkdir("/testdir/");
            printTestResult("Test 1.A - Create a new dirs", true, success);
            success = client.mkdir("/testdir/child_1/subchild_1", true);
            printTestResult("Test 1.B - Create a new dirs recursive", true, success);

            //Test 2. Upload local file 
            System.out.println("");
            System.out.println("********************************************************");
            System.out.println("Test 2. Upload local file ");

            //  A - Valid remote dir, valid local file
            success = client.putFile("/testdir/", "/tmp/test.txt");
            printTestResult("Test 2.A - Valid remote dir, valid local file", true, success);

            //  B - Valid remote dir, valid local file, change remote name 
            success = client.putFile("/testdir/child_1/other_test.txt", "/tmp/test.txt");
            printTestResult("Test 2.B - Valid remote dir, valid local file, change remote name", true, success);

            //  C - Valid remote dir, invalid local file
            success = client.putFile("/testdir/", "/tmp/fakefile.txt");
            printTestResult("Test 2.C - Valid remote dir, invalid local file", false, success);

            //  D - Invalid remote dir, valid local file
            success = client.putFile("/testdir/fakedir/", "/tmp/test.txt");
            printTestResult("Test 2.D - Invalid remote dir, valid local file", false, success);

            //  E - Invalid remote dir, valid local file, create remote dir
            success = client.putFile("/testdir/fakedir/fakechild/", "/tmp/test.txt", true);
            printTestResult("Test 2.E - Invalid remote dir, valid local file, create remote dir", true, success);

            //  F - Valid remote dir, invalid local file (directory)
            success = client.putFile("/testdir/", "/tmp/child_2");
            printTestResult("Test 2.F - Valid remote dir, invalid local file (directory)", false, success);
            
            //  G - Valid remote dir, valid local file, change remote name, create remote dirs
            success = client.putFile("/testdir/child_1/subchild/other_test.txt", "/tmp/test.txt", true);
            printTestResult("Test 2.G - Valid remote dir, valid local file, change remote name, create remote dirs", true, success);

            //Test 3. List directories
            System.out.println("");
            System.out.println("********************************************************");
            System.out.println("Test 3. List directories");

            //  A - list root with infinite option 
            //TODO: NOT WORKING PROPERLY!!
            response = client.list("/");
            printTestListResult("Test 3.A - List root with infinite option", response);

            //  B - list dir with max 3 child
            response = client.list("/testdir/", 3);
            printTestListResult("Test 3.B - List dir with max 3 child", response);

            //Test 4. Get data
            System.out.println("");
            System.out.println("********************************************************");
            System.out.println("Test 4. Get data");

            //  A - Get information for an specific file
            response = client.fileInfo("/testdir/test.txt");
            printTestListResult("Test 4.A - Get information for an specific file", response);

            //  B - Get content for a file as string
            content = client.getFileContents("/testdir/test.txt");
            printTestResult("Test 4.B - Get content for a file as string", content, originalContent);

            //Test 5. Download data to local directory 
            System.out.println("");
            System.out.println("********************************************************");
            System.out.println("Test 5. Download data to local directory");

            //  A - Valid remote file, valid local dir
            success = client.getFile("/testdir/child_1/other_test.txt", "/tmp/");
            printTestResult("Test 5.A - Valid remote file, valid local dir", true, success);

            //  B - Invalid remote file, valid local dir
            success = client.getFile("/testdir/fakefile.txt", "/tmp/");
            printTestResult("Test 5.B - Invalid remote file, valid local dir", false, success);

            //  C - Invalid remote file (directory), valid local dir
            success = client.getFile("/testdir/", "/tmp");
            printTestResult("Test 5.C - Invalid remote file (directory), valid local dir", false, success);

            //  D - Valid remote file, invalid local dir
            success = client.getFile("/testdir/test.txt", "/tmp/test2/");
            printTestResult("Test 5.D - Valid remote file, invalid local dir", false, success);

            //Test 6. Delete remote files or dir
            System.out.println("");
            System.out.println("********************************************************");
            System.out.println("Test 6. Delete remote files or dir");

            //  A - Valid remote file
            success = client.delete("/testdir/test.txt");
            printTestResult("Test 6.A - Valid remote file", true, success);

            //  B - Invalid remote file
            success = client.delete("/testdir/test2.txt");
            printTestResult("Test 6.B - Invalid remote file", false, success);

            //  C - Valid remote dir
            success = client.delete("/testdir/");
            printTestResult("Test 6.C - Valid remote dir", true, success);

            return;
        } catch (Exception ex) {
            Logger.getLogger(Tests.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void printTestResult(String testTitle, Object expected, Object result) {
        if (expected.equals(result)) {
            System.out.println(("     - " + testTitle + "....................................................................................................................").substring(0, 100) + "SUCCESS");
        } else {
            System.out.println(("     - " + testTitle + "....................................................................................................................").substring(0, 100) + "ERROR");
        }
    }

    private static void printTestListResult(String testTitle, WebDAVResponse response) {
        System.out.println("     - " + testTitle);
        System.out.println("        Content:");
        for(OCFile file : response.getItems()){
            System.out.println("           " + file.getPath().replace("/remote.php/webdav", ""));
        }
    }

    private static void createRandomFile(String filePath, double fileSize) throws UnsupportedEncodingException, FileNotFoundException {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890 ";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        File file = new File(filePath);
        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")), false);
        while (true) {
            for (int i = 0; i < 100; i++) {
                while (salt.length() < 50) { // length of the random string.
                    int index = (int) (rnd.nextFloat() * SALTCHARS.length());
                    salt.append(SALTCHARS.charAt(index));
                }
                String saltStr = salt.toString();
                writer.print(saltStr);
            }
            writer.println();
            if (file.length() >= fileSize * 1e6) {
                writer.close();
                break;
            }
        }
    }

    private static String readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }
}
