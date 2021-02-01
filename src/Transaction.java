/*Name: Shubham Phape
 * UTA ID: 1001773736*/
import java.io.Serializable;

public class Transaction implements Serializable {
    /*variable names*/
    private int operation_id;
    private String operation_name;
    private String display_message;
    private String create_foldername;
    private String path1;
    private String path2;
    private String clientname;
    private String Parent;
    public String getPath1() {
        return path1;
    }

    public void setPath1(String path1) {
        this.path1 = path1;
    }

    public String getPath2() {
        return path2;
    }

    public void setPath2(String path2) {
        this.path2 = path2;
    }


    public int getOperation_id() {
        return operation_id;
    }

    public void setOperation_id(int operation_id) {
        this.operation_id = operation_id;
    }

    public String getOperation_name() {
        return operation_name;
    }

    public void setOperation_name(String operation_name) {
        this.operation_name = operation_name;
    }

    public String getDisplay_message() {
        return display_message;
    }

    public void setDisplay_message(String display_message) {
        this.display_message = display_message;
    }

    public String getCreate_foldername() {
        return create_foldername;
    }

    public void setCreate_foldername(String create_foldername) {
        this.create_foldername = create_foldername;
    }


    public String getClientname() {
        return clientname;
    }

    public void setClientname(String clientname) {
        this.clientname = clientname;
    }

    public String getParent() {
        return Parent;
    }

    public void setParent(String parent) {
        Parent = parent;
    }
}
