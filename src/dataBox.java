/*Name: Shubham Phape
 * UTA ID: 1001773736*/


import java.io.Serializable;
import java.util.List;

public class dataBox implements Serializable {
/*-----------------type of message directory------------
* 1) Initial: means its the first message form the user to establish connection*/
    //variables
    private String client_name;
    private String type_of_message;
    /*variable to indicate which directory the clients currently in*/
    private String current_path;
    /*variable to save list of file contents*/
    private List<String> list_of_files;
    /*variable to report status of operation as message*/
    private String operation_report;
    /*variable for delete path*/
    private String delete_path;
    /*Variables for rename and move operations*/
    private String source_path;
    private String destination_path;
    private String identifier;
    private String folder_name;
    private String rename_this;

    public String getRename_this() {
        return rename_this;
    }

    public void setRename_this(String rename_this) {
        this.rename_this = rename_this;
    }

    public String getRename_to_this() {
        return rename_to_this;
    }

    public void setRename_to_this(String rename_to_this) {
        this.rename_to_this = rename_to_this;
    }

    private String rename_to_this;
    public String getSync_item() {
        return sync_item;
    }

    public void setSync_item(String sync_item) {
        this.sync_item = sync_item;
    }

    private String sync_item;

    /*Auto-generated Getters and setters*/
    public String getClient_name() {
        return client_name;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    public String getType_of_message() {
        return type_of_message;
    }

    public void setType_of_message(String type_of_message) {
        this.type_of_message = type_of_message;
    }

    public String getOperation_report() {
        return operation_report;
    }

    public void setOperation_report(String operation_report) {
        this.operation_report = operation_report;
    }

    public String getCurrent_path() {
        return current_path;
    }

    public void setCurrent_path(String current_path) {
        this.current_path = current_path;
    }

    public List<String> getList_of_files() {
        return list_of_files;
    }

    public void setList_of_files(List<String> list_of_files) {
        this.list_of_files = list_of_files;
    }


    public String getDelete_path() {
        return delete_path;
    }

    public void setDelete_path(String delete_path) {
        this.delete_path = delete_path;
    }

    public String getSource_path() {
        return source_path;
    }

    public void setSource_path(String source_path) {
        this.source_path = source_path;
    }

    public String getDestination_path() {
        return destination_path;
    }

    public void setDestination_path(String destination_path) {
        this.destination_path = destination_path;
    }
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getFolder_name() {
        return folder_name;
    }

    public void setFolder_name(String folder_name) {
        this.folder_name = folder_name;
    }
}



