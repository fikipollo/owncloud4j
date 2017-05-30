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
            Owncloud4j client = new Owncloud4j("localhost", 8085, "/remote.php/webdav");
            client.login("rafa", "123123");

            //********************************************************************************************
            // FILES
            //********************************************************************************************
            //Test 1. list root with infinite option 
            WebDAVResponse response = client.list("/"); //TODO: NOT WORKING!!
            //Test 2. list dir with max 2 child
            response = client.list("/testdir/", 2);

            //Test 3. Get information for an specific file
            response = client.fileInfo("/testdir/remotefile.txt");
            //Test 4. Get content for a file as string
            String content = client.getFileContents("/testdir/remotefile.txt");

            //Test 5. Download file to local directory (valid remote file, valid local dir)
            boolean success = client.getFile("/Documents/Example.odt", "/home/rhernandez/Desktop/caca/tmp");
            //Test 6. Download file to local directory (invalid remote file, valid local dir)
            success = client.getFile("/Documents/Example1.odt", "/home/rhernandez/Desktop/caca/tmp/");
            //Test 7. Download file to local directory (invalid remote file (dir), valid local dir)
            success = client.getFile("/Documents/", "/home/rhernandez/Desktop/caca/tmp");

            //Test 8. Upload local file (valid remote dir, valid local file)
            success = client.putFile("/Documents/", "/home/rhernandez/Desktop/caca/tmp/protein.csv");
            //Test 9. Upload local file (valid remote dir, valid local file (dir), change remote name)
            success = client.putFile("/Documents/test.csv", "/home/rhernandez/Desktop/caca/tmp/protein.csv");
            //Test 10. Upload local file (valid remote dir, invalid local file)
            success = client.putFile("/Documents/", "/home/rhernandez/Desktop/caca/tmp/remotefilke.txt");
            //Test 11. Upload local file (invalid remote dir, valid local file)
            success = client.putFile("/Documentsa/", "/home/rhernandez/Desktop/caca/tmp/protein.csv");
            //Test 12. Upload local file (valid remote dir, invalid local file (dir))
            success = client.putFile("/Documents/", "/home/rhernandez/Desktop/caca/tmp");

            return;
        } catch (Exception ex) {
            Logger.getLogger(Tests.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
