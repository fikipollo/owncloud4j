/* ***************************************************************
 *  This file is part of Owncloud4j.
 *
 *  Owncloud4j is free software: you can redistribute it and/or 
 *  modify it under the terms of the MIT License.
 * 
 *  Owncloud4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  MIT License for more details.
 * 
 *  You should have received a copy of the MIT License
 *  along with Owncloud4j. If not, see <https://mit-license.org/>.
 * 
 *  More info https://github.com/fikipollo/owncloud4j
 *  *************************************************************** */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.io.FileUtils;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.DavMethod;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;

/**
 *
 * @author Rafa Hernandez <https://github.com/fikipollo>
 */
/**
 * Resources 
 * http://jackrabbit.apache.org/api/2.14/overview-summary.html
 * http://svn.bonitasoft.org/bonita-connectors/tags/bonita-connectors-5.1/webdav/src/main/java/org/bonitasoft/connectors/webdav/common/WebDAVClient.java
 * https://hc.apache.org/httpclient-3.x/apidocs/
 */
public class Owncloud4j {

    //********************************************************************************************
    // ATTRIBUTES
    //********************************************************************************************
    private HttpClient client = null;
    private String host;
    private int port;
    private String davPrefix;
    private int maxHostConnections = 20;

    //********************************************************************************************
    // CONSTRUCTORS
    //********************************************************************************************
    public Owncloud4j(String host) {
        this.host = host.replaceAll("\\/$", "");
        this.port = 80;
        this.davPrefix = "";
    }

    public Owncloud4j(String url, int port) {
        this.host = url.replaceAll("\\/$", "");
        this.port = port;
        this.davPrefix = "";
    }

    public Owncloud4j(String url, int port, String davPrefix) {
        this.host = url.replaceAll("\\/$", "");
        this.port = port;
        //Ensures starting "/" and Remove last "/"
        davPrefix = "/" + davPrefix.replaceAll("^\\/", "").replaceAll("\\/$", "");

        if (davPrefix.equalsIgnoreCase("/")) {
            davPrefix = "";
        }
        this.davPrefix = davPrefix;
    }

    //********************************************************************************************
    // Sessions
    //********************************************************************************************
    /**
     * Authenticate to ownCloud. This will create a session on the server.
     * @param username
     * @param password
     * @throws IOException in case an HTTP error status was returned
     */
    public void login(String username, String password) throws IOException {
        HostConfiguration hostConfig = new HostConfiguration();
        hostConfig.setHost(host, port);

        HttpConnectionManager connectionManager = new SimpleHttpConnectionManager();
        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setMaxConnectionsPerHost(hostConfig, maxHostConnections);
        connectionManager.setParams(params);

        client = new HttpClient(connectionManager);
        Credentials creds = new UsernamePasswordCredentials(username, password);
        client.getState().setCredentials(AuthScope.ANY, creds);
        client.setHostConfiguration(hostConfig);
    }

    /**
     * Log out the authenticated user and close the session.
     * return true if the operation succeeded, false otherwise
     * @throws IOException  in case an HTTP error status was returned
     */
    public boolean logout() throws IOException {
        try {
            if (client != null) {
                client.getHttpConnectionManager().closeIdleConnections(1);
            }
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            client = null;
        }
    }

    //********************************************************************************************
    // FILES
    //********************************************************************************************
    /**
     * Returns the file info for the given remote file.
     * @param path path to the remote file
     * @return file info
     * @throws Exception 
     */
    public WebDAVResponse fileInfo(String path) throws Exception {
        return this.list(path, 0);
    }

    /**
     * Returns the listing/contents of the given remote directory.
     * @param path path to the remote directory
     * @return directory listing
     * @throws Exception 
     */
    public WebDAVResponse list(String path) throws Exception {
        return this.list(path, -1);
    }

    /**
     * Returns the listing/contents of the given remote directory.
     * @param path path to the remote directory
     * @param depth depth of the listing, integer
     * @return directory listing
     * @throws Exception 
     */
    public WebDAVResponse list(String path, int depth) throws Exception {
        HashMap<String, String> headers = new HashMap<>();
        if (depth < 0) {
            headers.put("Depth", "infinity");
        } else {
            headers.put("Depth", "" + depth);
        }

        return this.makeDavRequest("PROPFIND", path, headers);
    }

    /**
     * Returns the contents of a remote file.
     * @param path path to the remote file
     * @return file contents
     * @throws Exception 
     */
    public String getFileContents(String path) throws Exception {
        return this.makeDavRequest("GET", path, null).getResponseText();
    }

    /**
     * Downloads a remote file.
     * @param remotePath path to the remote file
     * @param localPath path to the local file (will be override) or to the destination directory. 
     * @return true if the operation succeeded, false otherwise
     * @throws Exception 
     */
    public boolean getFile(String remotePath, String localPath) throws Exception {
        File localFile = new File(localPath);
        if (!localFile.exists()) {
            return false;
        }

        WebDAVResponse response = this.makeDavRequest("GET", remotePath, null, true);

        if (response.isSuccess() && response.getStatusCode() == 200) {
            String fileName, destinationDir;
            if (localFile.isDirectory()) {
                fileName = remotePath.substring(remotePath.lastIndexOf("/") + 1);
                destinationDir = localPath;
            } else { //override!!
                fileName = localFile.getName();
                destinationDir = localFile.getParent();
            }

            if (!"".equals(fileName)) {
                File targetFile = new File(destinationDir + File.separator + fileName);
                FileUtils.writeByteArrayToFile(targetFile, response.getResponseBinary());
                return true;
            }
        }
        return false;
    }

    //  - TODO: get_directory_as_zip
    //  - TODO: put_file_contents
    /**
     * Upload a file.
     * @param remotePath path to the target file. A target directory can also be specified instead by appending a "/"
     * @param localPath path to the local file to upload
     * @return true if the operation succeeded, false otherwise
     * @throws Exception 
     */
    public boolean putFile(String remotePath, String localPath) throws Exception {
        File localFile = new File(localPath);
        if (!localFile.exists() || localFile.isDirectory()) {
            return false;
        }

        //TODO: FIRST CHECK IF DESTIANTION EXISTS
        try {
            FileInputStream fileInputStream = new FileInputStream(localFile);

            String remoteFileName = "";
            if (remotePath.endsWith("/")) { //is dir
                remoteFileName = localFile.getName();
            } else {
                remoteFileName = remotePath.substring(remotePath.lastIndexOf("/") + 1);
                remotePath = remotePath.substring(0, remotePath.lastIndexOf("/"));
            }

            remotePath = this.normalizePath(remotePath) + remoteFileName;

            HttpMethod method = this.getMethod("PUT", remotePath);

            //TODO: update the remote file to the same mtime as the local one, defaults to True
            //headers = {}
            //if kwargs.get('keep_mtime', True):
            //    headers['X-OC-MTIME'] = str(stat_result.st_mtime)
            RequestEntity requestEntity = new InputStreamRequestEntity(fileInputStream);
            ((PutMethod) method).setRequestEntity(requestEntity);

            client.executeMethod(method);

            fileInputStream.close();

            WebDAVResponse objResponse = this.processDavResponse(method, false);
            method.releaseConnection();
            return objResponse.isSuccess();
        } catch (IOException e) {
            return false;
        }
    }

    /**
    Upload a file.
    @param remotePath path to the target file. A target directory can also be specified instead by appending a "/"
    @param localPath path to the local file to upload
    @param createDirs if true, the required directories will be auto-created
    @return
    @throws Exception 
     */
    public boolean putFile(String remotePath, String localPath, boolean createDirs) throws Exception {
        if (createDirs) {
            this.mkdir(remotePath, true);
        }
        return this.putFile(remotePath, localPath);
    }

//  - 
//  - TODO: put_directory
    /**
     * Creates a remote directory.
     * @param path path to the remote directory to create
     * @return true if the operation succeeded, false otherwise
     * @throws Exception 
     */
    public boolean mkdir(String path) throws Exception {
        //Directories must end with "/"
        path = path.replaceAll("\\/$", "") + "/";
        return this.makeDavRequest("MKCOL", path, null).isSuccess();
    }

    /**
     * Creates a remote directory recursively.
     * @param path path to the remote directory to create
     * @param recursive if true, the required directories will be auto-created
     * @return true if the operation succeeded, false otherwise
     * @throws Exception 
     */
    public boolean mkdir(String path, boolean recursive) throws Exception {
        path = path.substring(0, path.lastIndexOf("/") +1);
        String[] dirs = path.split("\\/");
        String _remotePath = "/";
        boolean success=true;
        for (String dir : dirs) {
            if (!"".equals(dir)) {
                _remotePath += dir + "/";
                success = this.mkdir(_remotePath);
            }
        }
        return success;
    }

    /**
     * Deletes a remote file or directory.
     * @param remotePath path to the file or directory to delete
     * @return true if the operation succeeded, false otherwise
     * @throws Exception 
     */
    public boolean delete(String remotePath) throws Exception {
        return this.makeDavRequest("DELETE", remotePath, null).isSuccess();
    }

//  - TODO: move
//  - TODO: copy
//  - TODO: move
    /**
     * This function sends a new WebDAV request to the server.
     * @param methodName the method to execute (e.g. GET, PUT, PROPFIND)
     * @param path the destination for the request
     * @param headers (optional) some extra headers 
     * @return the response from the server
     */
    private WebDAVResponse makeDavRequest(String methodName, String path, HashMap<String, String> headers) {
        return this.makeDavRequest(methodName, path, headers, false);
    }

    /**
     * This function sends a new WebDAV request to the server.
     * @param methodName the method to execute (e.g. GET, PUT, PROPFIND)
     * @param path the destination for the request
     * @param headers (optional) some extra headers 
     * @param asStream determines whether the response body should be returned as an String or as a byte array.
     * @return the response from the server
     */
    private WebDAVResponse makeDavRequest(String methodName, String remote_path, HashMap<String, String> headers, boolean asStream) {
        try {
            remote_path = this.normalizePath(remote_path);
            HttpMethod method = this.getMethod(methodName, remote_path);

            if (headers != null && !headers.isEmpty()) {
                Iterator it = headers.keySet().iterator();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    String value = headers.get(key);
                    method.addRequestHeader(new Header(key, value));
                }
            }

            client.executeMethod(method);
            WebDAVResponse objResponse = this.processDavResponse(method, asStream);
            method.releaseConnection();
            return objResponse;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * This function returns a new instance of WebDAV method.
     * @param method the method to execute (e.g. GET, PUT, PROPFIND)
     * @param path the destination for the request
     * @return the new instance of HTTPMethod
     */
    private HttpMethod getMethod(String method, String path) throws IOException {
        switch (method) {
            case "PROPFIND":
                return new PropFindMethod(path);
            case "GET":
                return new GetMethod(path);
            case "PUT":
                return new PutMethod(path);
            case "DELETE":
                return new DeleteMethod(path);
            case "MKCOL":
                return new MkColMethod(path);
        }
        return null;
    }

    /**
     * Adapt the remote path.
     * @param path the remote path
     * @return a valid remote path
     */
    private String normalizePath(String path) {
        //Remove starting "/"
        path = path.replaceAll("^\\/", "");
        //Ensures that path ends with "/"
        path = path.replaceAll("\\/$", "") + "/";
        //Appends the prefix (default "")
        return (this.davPrefix + "/" + path).replaceAll("\\/\\/", "\\/");
    }

    /**
     * Process the response from the server.
     * @param method the HTTPMethod used for the request.
     * @param getBinaryResponse determines whether the response content should be returned as a String or as an array of bytes.
     * @return 
     */
    private WebDAVResponse processDavResponse(HttpMethod method, boolean getBinaryResponse) {
        List<Integer> validCodes = Arrays.asList(new Integer[]{200, 204, 207, 201});
        WebDAVResponse response = new WebDAVResponse();

        response.setSuccess(validCodes.contains(method.getStatusCode()));
        response.setStatusCode(method.getStatusCode());
        response.setStatusText(method.getStatusText());

        //If multistatus response
        if (response.getStatusCode() == 207) {
            try {
                DavMethod davMethod = (DavMethod) method;
                MultiStatus responses = davMethod.getResponseBodyAsMultiStatus();

                for (MultiStatusResponse msresponse : responses.getResponses()) {
                    response.addItem(this.processDavResponseElement(msresponse));
                }

            } catch (IOException ex) {
                response.setSuccess(false);
            } catch (DavException ex) {
                response.setSuccess(false);
            }
        } else {
            try {
                if (getBinaryResponse) {
                    response.setResponseBinary(method.getResponseBody());
                } else {
                    response.setResponseText(method.getResponseBodyAsString());
                }
            } catch (IOException ex) {
                response.setSuccess(false);
            }
        }

        return response;
    }

    private OCFile processDavResponseElement(MultiStatusResponse response) {
        String href = response.getHref();
        String fileType = "file";
        if (href.endsWith("/")) {
            fileType = "dir";
        }
        //TODO: READ ATTRIBUTES
        //file_attrs = {}
        //attrs = dav_response.find('{DAV:}propstat')
        //attrs = attrs.find('{DAV:}prop')
        //for attr in attrs:
        //    file_attrs[attr.tag] = attr.text

        return new OCFile(href, fileType, null);
    }

}
