/*
 * ownCloud client module
 * Makes it possible to access files on a remote ownCloud instance,
 * share them or access application attributes.
 */


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
            
            Owncloud4j client = new Owncloud4j("localhost", 8085, "/remote.php/webdav");
            client.login("rafa", "123123");

            //********************************************************************************************
            // FILES
            //********************************************************************************************
            //Test 1. Create a new dirs
            success = client.mkdir("/testdir/");
            success = client.mkdir("/testdir/child_1");

            //Test 2. Upload local file 
            //  A - Valid remote dir, valid local file
            success = client.putFile("/testdir/", "/data/test/test.txt");
            //  B - Valid remote dir, valid local file, change remote name 
            success = client.putFile("/testdir/child_1/other_test.txt", "/data/test/test.txt");
            //  C - Valid remote dir, invalid local file
            success = client.putFile("/testdir/", "/data/test/fakefile.txt");
            //  D - Invalid remote dir, valid local file
            success = client.putFile("/testdir/fakedir/", "/data/test/test.text");
            //  E - Valid remote dir, invalid local file (directory)
            success = client.putFile("/testdir/", "/data/child_2");

            //Test 3. List directories
            //  A - list root with infinite option 
            //TODO: NOT WORKING PROPERLY!!
            response = client.list("/"); 
            //  B - list dir with max 3 child
            response = client.list("/testdir/", 3);

            //Test 4. Get data
            //  A - Get information for an specific file
            response = client.fileInfo("/testdir/test.txt");
            //  B - Get content for a file as string
            content = client.getFileContents("/testdir/test.txt");

            //Test 5. Download data to local directory 
            //  A - Valid remote file, valid local dir
            success = client.getFile("/testdir/test.txt", "/tmp/");
            //  B - Invalid remote file, valid local dir
            success = client.getFile("/testdir/fakefile.txt", "/tmp/");
            //  C - Invalid remote file (directory), valid local dir
            success = client.getFile("/testdir/", "/tmp");
            //  D - Valid remote file, invalid local dir)
            success = client.getFile("/testdir/test.txt", "/tmp/test2/");
            
            //Test 6. Delete remote files or dir
            //  A - Valid remote file
            success = client.delete("/testdir/test.txt");
            //  B - Invalid remote file
            success = client.delete("/testdir/test2.txt");
            //  C - Valid remote dir
            success = client.delete("/testdir/");


            return;
        } catch (Exception ex) {
            Logger.getLogger(Tests.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
