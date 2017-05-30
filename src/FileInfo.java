/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.HashMap;

/**
 *
 * @author rhernandez
 */
public class FileInfo {

    private String path;
    private String name;
    private String fileType;
    private HashMap<String, String> attributes;

    public FileInfo(String path, String fileType, HashMap<String, String> attributes) {
        this.path = path.replaceAll("\\/$", "");
        this.name = path.substring(path.lastIndexOf("/")+1);
        this.fileType = fileType;
        if (attributes == null) {
            this.attributes = new HashMap<>();
        } else {
            this.attributes = attributes;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<String, String> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(String name, String value) {
        if(this.attributes == null){
            this.attributes = new HashMap<> ();
        }
        this.attributes.put(name, value);
    }
    
}
