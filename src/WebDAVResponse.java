

import java.io.InputStream;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author rhernandez
 */
public class WebDAVResponse {

    private int statusCode = 400;
    private String statusText;
    private String responseText;
    private byte[] responseBinary;
    private InputStream responseStream;
    private boolean success = false;
    private ArrayList<OCFile> items;

    public WebDAVResponse() {
        
    }

    public WebDAVResponse(int code, String text, String responseString) {
        statusCode = code;
        statusText = text;
        responseText = responseString;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public InputStream getResponseStream() {
        return responseStream;
    }

    public void setResponseStream(InputStream responseStream) {
        this.responseStream = responseStream;
    }

    public byte[] getResponseBinary() {
        return responseBinary;
    }

    public void setResponseBinary(byte[] responseBinary) {
        this.responseBinary = responseBinary;
    }
    
    public ArrayList<OCFile> getItems() {
        return items;
    }

    public void setItems(ArrayList<OCFile> items) {
        this.items = items;
    }

    public void addItem(OCFile item) {
        if(this.items == null){
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }
    
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        String result = "status code : " + statusCode + "\n"
                + "status text : " + statusText + "\n"
                + "   response : " + responseText + "\n"
                + "    success : " + success;
        return result;
    }
}
