/*Name: Shubham Phape
 * UTA ID: 1001773736*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

public class Server extends JFrame {

/*UI Components*/

    private JTextArea operations_log;
    private JButton startServerButton;
    private JButton stopServerButton;
    private JPanel jpanelserver;
    private JTextArea activeusers_ta;
    private JList jList_operation;
    private JButton undoOperationButton;
    private JLabel hoverlable_2;
    private JLabel hoverlabel_1;
    private JLabel to;

    public void setActiveusers_ta(JTextArea activeusers_ta) {
        this.activeusers_ta = activeusers_ta;
    }



    /*--------------------------------------Global varaibles-----------------------------------------------------------*/
    public static String server_directory_path="server";
    public static String recyclebin= "trash";
    /*list to keep track of online clients*/
    static List<String> online_clients =new ArrayList<>();
    /*List to keep the Clienthandler objegt of each client*/
    static ArrayList<ClientHandler> Client_handler_list = new ArrayList<>();
    /*Hashmap to keep track of the identifiers assigned*/
    static HashMap<String, String> identifier_map = new HashMap<String, String>();
    /*List to keep track of the operation transaction objects*/
    static ArrayList<Transaction> operations_objectList= new ArrayList<>();
    /*----------------------------------------------Main Method-------------------------------------------------------------*/
    public static void main(String[] args) {
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Server().setVisible(true);
            }
        });



    }

    /*----------------------------------------------Server class Constructor for initializations---------------------------*/
    public Server() throws HeadlessException {

        //setting the dimensions and making the panel visible
        setSize(650, 550);
        setContentPane(jpanelserver);
        setVisible(true);

        //Firstly this constructor is called and we create a Server directory.
        /*We are creating the parent server dir here. It returns true only if the directory is created. and false it it already exists*/
        /*-------------Citation:------------------*/
        /*https://stackoverflow.com/questions/3634853/how-to-create-a-directory-in-java/3634879*/
        if (new File(server_directory_path).mkdirs()) {
            System.out.println("Server directory created");
            new File(server_directory_path).mkdirs();
        } else {
            System.out.println("Server directory already exists");
        }

        /*----------------When the START SERVER button server is pressed.--------------------*/
        startServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //creating new thread to run the server operations
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //calling the function to start the socketserver to establish and accept connections.

                            start_server();

                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        /*------------------When the STOP SERVER button server is pressed.------------------*/
        stopServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.exit(0);
            }
        });
        /*------------------When the Undo operation button is pressed.------------------*/
        undoOperationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int this_id = jList_operation.getSelectedIndex();
                String this_undoValue = (String) jList_operation.getSelectedValue();

                undo_Operation(this_id, this_undoValue);

            }
        });
        /*reference: http://www.java2s.com/Code/JavaAPI/javax.swing/JListaddMouseListenerMouseListenerlis.htm*/
        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                JList theList = (JList) e.getSource();
                if (e.getClickCount() == 2) {
                    int index = theList.locationToIndex(e.getPoint());

                    if (index >= 0) {
                        Object o = theList.getModel().getElementAt(index);
                        Transaction thisselectedoperationis = getselectedobject(o.toString());
                        if (thisselectedoperationis.getOperation_name().equalsIgnoreCase("create"))
                        {
                            hoverlabel_1.setText(thisselectedoperationis.getPath1());
                            hoverlable_2.setText("");
                            to.setText("");
                        }
                        else if (thisselectedoperationis.getOperation_name().equalsIgnoreCase("move")){
                            hoverlabel_1.setText(thisselectedoperationis.getPath2());
                            to.setText("to");
                            hoverlable_2.setText(thisselectedoperationis.getPath1());
                        }
                        else if (thisselectedoperationis.getOperation_name().equalsIgnoreCase("delete")){
                            hoverlabel_1.setText(thisselectedoperationis.getPath1());
                            hoverlable_2.setText("");
                            to.setText("");

                        }
                        else if (thisselectedoperationis.getOperation_name().equalsIgnoreCase("rename")){
                            hoverlabel_1.setText(thisselectedoperationis.getPath2());
                            to.setText("to");
                            hoverlable_2.setText(thisselectedoperationis.getPath1());
                        }

                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                System.out.println("clicked");
            }
        };
        jList_operation.addMouseListener(mouseListener);


    }

    /*this method if the opertion is to undo*/
    private void undo_Operation(int this_id, String this_undoValue) {

        Transaction selected_object = getselectedobject(this_undoValue);
        operations_log.append(String.valueOf(selected_object.getOperation_id())+" : "+selected_object.getOperation_name()+"\n");
        String type_ofoperation_toundo = selected_object.getOperation_name();
        if (selected_object.getParent().equalsIgnoreCase("no")){
            //operation has invalid parent
            JOptionPane.showMessageDialog(jpanelserver, "This Operations Parent or the directory itself has been\nhas been affected.\nTry to UNDO parent operation.", "Invalid Operation",
                    JOptionPane.WARNING_MESSAGE);
        }
        else{
            /*Selecting the type of operation*/
            /*------------------------------CREATE_--------------------------------*/
            if (type_ofoperation_toundo.equalsIgnoreCase("create")){
                //this is create operation to UNDO so delete the folder
                /*actually deleteing the folder*/
                try {

                    delete_folder_CR(selected_object);
                    //remover the object from the Jlist as well as the operationsobjectslist


                    //remove_jlistelement(selected_object.getDisplay_message());
                    operations_objectList.remove(selected_object);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            /*------------------------------MOVE_--------------------------------*/
            else if (type_ofoperation_toundo.equalsIgnoreCase("move")){
                Path path1 = Paths.get(selected_object.getPath1());
                Path path2 = Paths.get(selected_object.getPath2());
                System.out.println(selected_object.getPath1());
                File tocraetefolderfirst = new File(selected_object.getPath2());
                if (tocraetefolderfirst.mkdir()){
                    try {
                        //re moving actual paths
                        move(path1, path2, selected_object);
                        remove_jlistelement(selected_object.getDisplay_message());
                    /*for (String item: path1.toFile().list()
                         ) {
                        Transaction subfolder= getselectedobject(selected_object.getClientname()+
                                " : "+"created "+ item);
                        subfolder.setPath1(selected_object.getPath2()+"/"+item);
                    }*/
                        operations_objectList.remove(selected_object);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            /*------------------------------Delete_--------------------------------*/
            else if (type_ofoperation_toundo.equalsIgnoreCase("delete")){
                Path path1 = Paths.get(selected_object.getPath1());
                Path path2 = Paths.get(selected_object.getPath2());
                System.out.println(selected_object.getPath1());
                File tobringbackfolderfirst = new File(selected_object.getPath1());
                if (tobringbackfolderfirst.mkdir()){
                    try {
                        //re moving actual paths
                        move(path2, path1,selected_object);
                        remove_jlistelement(selected_object.getDisplay_message());
                    /*for (String item: path1.toFile().list()
                         ) {
                        Transaction subfolder= getselectedobject(selected_object.getClientname()+
                                " : "+"created "+ item);
                        subfolder.setPath1(selected_object.getPath2()+"/"+item);
                    }*/
                        operations_objectList.remove(selected_object);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }
            /*------------------------------rename_--------------------------------*/
            else if (type_ofoperation_toundo.equalsIgnoreCase("rename")){
                File src = new File(selected_object.getPath1());
                File dest = new File(selected_object.getPath2());
                if(src.renameTo(dest)){
                    remove_jlistelement(selected_object.getDisplay_message());
                    operations_objectList.remove(selected_object);
                }
            }
        }

    }
    private static Transaction getselectedobject(String displaymessage){
        Transaction thisobject = new Transaction();
        for(int i = 0; i < operations_objectList.size(); i= i+1 ){
            Transaction selected_object = operations_objectList.get(i);
            if (selected_object.getDisplay_message().equalsIgnoreCase(displaymessage)){
                return selected_object;
            }
        }
        return thisobject;
    }
    private void remove_jlistelement(String displaymessage){
        /*variable for the size of the Jlist*/
        int size_ofjlist = jList_operation.getModel().getSize();
        /*string array t store the previous content*/
        List<String>  operatiosname = new ArrayList<>();

        for (int i = 0; i < size_ofjlist ; i++) {
            String word = (String.valueOf(jList_operation.getModel().getElementAt(i)));
            if (!word.equalsIgnoreCase(displaymessage)){
                operatiosname.add( word);

            }

        }
        /*Reference:https://stackoverflow.com/questions/5374311/convert-arrayliststring-to-string-array*/
        String[] stockArr = new String[operatiosname.size()];
        stockArr = operatiosname.toArray(stockArr);
        jList_operation.setModel(new DefaultComboBoxModel(stockArr));
    }

    /*--------------------------------Function to start the server for listening to client connections----------------*/
    private void start_server() {
        try {

            //setting server to listen at port
            ServerSocket ss = new ServerSocket(5000);

            /*displaying the sucessfull running of server-----(SERVER OPERATION)*/
            operations_log.append("Server is now up and running!!\n");

            /*---REFERENCES---*/
            /*MUlti thread client connection from:*/
            /*https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/*/
            /*// running infinite loop for getting the client messages*/
            while (true)
            {
                Socket s = null;

                try
                {
                    // socket object to receive incoming client requests
                    s = ss.accept();

                    // obtaining input and out streams
                    ObjectInputStream objectInputStream = new ObjectInputStream(s.getInputStream());
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(s.getOutputStream());

                    //reading the received object
                    dataBox r_data = (dataBox) objectInputStream.readObject();
                    operations_log.append("------------------NEW CONNECTION-----------------\n");
                    /*Displaying that connection has been accepted.-----(SERVER OPERATION LOG)*/
                    operations_log.append("Accepted Socket Connection from "+r_data.getClient_name()+"\n");




                    /*Checkin for the duplicate user*/
                    if(check_duplicate(r_data)){
                        /*It means no match found for duplicate user*/
//                        We can Establish tjis client
                        //adding client name to online client list
                        online_clients.add(r_data.getClient_name());
                        /*making a instance of clienthandler object for this client*/
                        ClientHandler Client_object = new ClientHandler(s, r_data.getClient_name(), activeusers_ta,operations_log,jList_operation, objectInputStream, objectOutputStream);

                        // create a new thread object
                        Thread t = new Thread(Client_object);
                        operations_log.append(r_data.getClient_name()+" just Succesfully joined the server.\n");
                        //Displaying the message of client addition -----(SERVER OPERATION LOG)
                        display_activeuserlist();

                        //adding clienthandler object to the object list of handlers
                        Client_handler_list.add(Client_object);
                        //displaying file logs
                        //creating user directory for the first time user connect if it does not exists
                        String client_path = Server.server_directory_path+"/"+r_data.getClient_name();
                        if (new File(client_path).mkdirs()){
                            File trash = new File("trash/"+r_data.getClient_name());
                            trash.mkdirs    ();
                            System.out.println("Client directory created");
                            operations_log.append(r_data.getClient_name()+" home directory created.\n");
                            /*Sending response to client as his Home directory*/
                            /*sending datBox object*/
                            dataBox send_response = new dataBox();
                            /*Checking if the type of message received was Initial*/
                            if (r_data.getType_of_message().equalsIgnoreCase("Initial")){
                                /*If its the first message from the user*/
                                /*Checking for the identifier is taken*/
                                if (identifier_map.containsKey(r_data.getIdentifier())){
                                    /*it means identifier is not taken*/
                                    /*setting idenifier od databox to "Z" means it is alredy taken choose again*/
                                    send_response.setIdentifier("Z");
                                }
                                else{
                                /*It means identifier is not taken*/
                                    String temppp= r_data.getIdentifier();
                                    send_response.setIdentifier(temppp);
                                    //operations_log.append("Identifir: "+r_data.getIdentifier()+"\n");
                                    identifier_map.put(temppp,r_data.getClient_name());
                                }
                                /*setting the object to send */
                                //operations_log.append(Server.identifier_map.toString());
                                send_response.setClient_name(r_data.getClient_name());
                                send_response.setType_of_message("Initial");
                                send_response.setCurrent_path(server_directory_path+"/"+ r_data.getClient_name());
                                objectOutputStream.writeObject(send_response);
                                objectOutputStream.flush();
                            }
                        }
                        operations_log.append("-----------------------------------\n");

                        // Invoking the start() method
                        t.start();
                    }
                    else {
                        /*There is another client with same user name online right now
                        * So we reject the connection*/
                        /*sending datBox object*/
                        operations_log.append("!!DUPLICATE USERNAME !!!!\nRejected Connection\n");
                        operations_log.append("-----------------------------------\n");
                        dataBox send_response = new dataBox();
                        send_response.setClient_name(r_data.getClient_name());
                        send_response.setType_of_message("reject");
                        send_response.setOperation_report("Connection Rejected. Due to duplicate username");
                        objectOutputStream.writeObject(send_response);
                        objectOutputStream.flush();
                        s.close();

                    }

                }
                catch (Exception e){
                    //CLosing the coonection in case of mishap
                    s.close();
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    /*Function to check the duplicate username with the online client*/
    private boolean check_duplicate(dataBox r_data) {
        //checking for 3 clients
        if (online_clients.size() >= 3 ){
            return false;
        }

        for (String user : online_clients){
            /*Checking if the username matches with the current online users*/
            if (user.equalsIgnoreCase(r_data.getClient_name())){
                /*if it matches we return false to terminate the connection*/
                return false;
            }
        }
        return true;
    }

/*Function to display the active users*/
    public void display_activeuserlist() {
        if (SwingUtilities.isEventDispatchThread()){
            activeusers_ta.setText(String.join("\n", online_clients));
        }
        else{
            /*Reference*/
            /*https://stackoverflow.com/questions/50397355/cant-update-text-area-in-swing*/
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                   /* adding the updated list to the UI*/
                    activeusers_ta.setText(String.join("\n", online_clients));
                }
            });
        }


    }
/*function to dlete folder*/
    public void delete_folder_CR(Transaction pathto_delete) throws IOException {
        //deleting the directory and its content
        //Refrences: https://mkyong.com/java/how-to-delete-directory-in-java/
        //REfrence starts here
        Path traget_path = Paths.get(pathto_delete.getPath1());
        Files.walkFileTree(traget_path, new SimpleFileVisitor<>(){
            // delete directories or folders

            @Override
            public FileVisitResult postVisitDirectory(Path dir,
                                                      IOException exc)
                    throws IOException {
                //Actually dleleting the directory
                Files.delete(dir);
                String thispath = dir.toString();
                String currentFolder = thispath.substring(thispath.lastIndexOf("/")+1);
                System.out.printf("Directory is deleted : %s%n", dir.getFileName());
                /*Transaction okthisubfolder = getselectedobject(pathto_delete.getClientname()+ " : "+"created "+ dir.getFileName());
                operations_objectList.get(okthisubfolder.getOperation_id()).setParent("yes");
                System.out.println(currentFolder+"MAde this operation Yes again\n");*/

                remove_jlistelement(pathto_delete.getClientname()+ " : "+"created "+ dir.getFileName());
                return FileVisitResult.CONTINUE;
            }

            // delete files
            @Override
            public FileVisitResult visitFile(Path file,
                                             BasicFileAttributes attrs)
                    throws IOException {
                //deleting the inside file in the directory
                Files.delete(file);
                System.out.printf("File is deleted : %s%n", file);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    /*--------------------------Function to move the directroy*/
    /*Class and function to move files
     * Referenced from:https://stackoverflow.com/questions/15137849/java-using-nio-files-copy-to-move-directory*/
    /*Reference starts here*/
    public static void move(Path source, Path target, Transaction pathto_delete) throws IOException {
        /*function to move the directory */
        class FileMover extends SimpleFileVisitor<Path> {
            private Path source;
            private Path target;

            //constructor to initialize class parameters
            private FileMover(Path source, Path target) {
                this.source = source;
                this.target = target;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                Files.move(file, target.resolve(source.relativize(file)),
                        StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                Path newDir = target.resolve(source.relativize(dir));
                try {
                    Files.copy(dir, newDir,
                            StandardCopyOption.COPY_ATTRIBUTES,
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (DirectoryNotEmptyException e) {
                    // ignore and skip
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                Path newDir = target.resolve(source.relativize(dir));
                Transaction okthisubfolder = getselectedobject(pathto_delete.getClientname()+ " : "+"created "+ dir.getFileName());
                operations_objectList.get(okthisubfolder.getOperation_id()).setParent("yes");
                System.out.println(dir.getFileName()+" Made this operation Yes again\n");
                FileTime time = Files.getLastModifiedTime(dir);
                Files.setLastModifiedTime(newDir, time);
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        }

        FileMover fm = new FileMover(source, target);
        EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        /*Iterating through the file structure*/
        Files.walkFileTree(source, opts, Integer.MAX_VALUE, fm);
    }
    /*Refrence ends here*/
}
