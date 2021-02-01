/*Name: Shubham Phape
 * UTA ID: 1001773736*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;


public class Client extends JFrame {

    private JPanel jpanel1;
    private JTextArea client_display_TA;
    private JLabel user_greeting;
    private JButton btn_create_dir;
    private JButton deleteDirectoryButton;
    private JButton moveDirectoryButton;
    private JButton renameButton;
    private JButton disconnectButton;
    private JButton listOfContentsButton;
    private JButton goto_btn;
    private JComboBox synchronize_optionbox;
    private JButton syncronize_btn;
    private JButton refresh;
    private JButton desynchronizeButton;
    private JTextArea textArea1;
    private JList syn_list;
    private JLabel identifier_text;

    JTextField source = new JTextField(20);
    JTextField destination = new JTextField(20);

    JPanel myPanel = new JPanel();

    /*variable to save the username of this  running client*/
    private String user_name;
    private String current_path_in;
    private String user_homedirectory;
    private String identifier_chosen;
    private String LD_path;

    //Each client has this UI
    public Client() throws HeadlessException {
        /*Creating a Jframe object*/
        JFrame jframe = new JFrame("Client");

        user_name = null;
        String message = "Enter your username.";

        /*this warning message snippet and illegal characters snippet loop
         * It checks if the username is not empty or has only letters
         * Reference :https://stackoverflow.com/questions/34391402/show-input-dialog-box-after-error-message-dialog-box*/
        do {
            user_name =
                    JOptionPane.showInputDialog(jframe, message);
            message = "<html><b style='color:red'>Enter Your Name:</b><br>"
                    + "Use letters only.";
        } while (user_name != null && !user_name.matches("[a-zA-Z]+"));
        /*Making the user to choose the Identifier between A, B, C*/
        /*Refrenced from : http://www.java2s.com/Tutorial/Java/0240__Swing/Todisplaysadialogwithalistofchoicesinadropdownlistbox.htm*/
        /*Variabel to store the possible choices*/
        String[] choices = {"A", "B", "C"};
        /*displaying the option in  dialog box to choose the identifier*/
        identifier_chosen = (String) JOptionPane.showInputDialog(null, "Choose Identifier",
                "Identifier choice", JOptionPane.QUESTION_MESSAGE, null, // Use
                // default
                // icon
                choices, // Array of choices
                choices[0]); // Initial choice
        if (!user_name.isEmpty()) {
            //username entered is not empty
            if (server_connect()) {
                user_greeting.setText("Welcome, " + user_name);
                //setting the dimensions and making the panel visible
                setSize(700, 500);
                setContentPane(jpanel1);
                //making the client frame visible
                client_display_TA.setSize(500, 400);

                setVisible(true);


            }
        }



        /*-----------------Disconnect BUtton-----------------------------*/
        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                /*client wants to disconnect from the server and leave*/
                try {
                    dataBox send_operation = new dataBox();
                    send_operation.setClient_name(user_name);
                    /*Setting the kind of operation*/
                    send_operation.setType_of_message("disconnect");
                    send_operation.setIdentifier(identifier_chosen);
                    dataOutputStream.writeObject(send_operation);
                    dataOutputStream.flush();
                    sc.close();
                    System.exit(0);
                    new Client().setVisible(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        /*-----------------Create Directory Button-----------------------------*/
        btn_create_dir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                /*Client wants to create a directory in particular folder*/
                dataBox send_operation = new dataBox();
                send_operation.setClient_name(user_name);
                /*Setting the kind of operation*/
                send_operation.setType_of_message("create");

                /*creating an input box for taking the new directory path as input*/
                /*REFERENCES: https://mkyong.com/swing/java-swing-joptionpane-showinputdialog-example/*/
                String new_dir_path =
                        JOptionPane.showInputDialog(jframe, "input path", current_path_in);

                Path test_path = Paths.get(new_dir_path);

                /*testing if the user is authorized or not*/
                if (valid_path(test_path)) {
                    /*the client is authorized to do the operation
                     * */
                    try {
                        send_operation.setFolder_name(new_dir_path.substring(new_dir_path.lastIndexOf("/")+1));
                        send_operation.setCurrent_path(new_dir_path);
//                        Sending data to server.
                        dataOutputStream.writeObject(send_operation);
                        dataOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    /*client is trying to acces unauthorized path or unknown path*/
                    JOptionPane.showMessageDialog(jframe, "Your are not authorized to access this path.", "Unauthorized Path",
                            JOptionPane.WARNING_MESSAGE);
                }

            }
        });

        /*-----------------Move Directory BUtton-----------------------------*/
        moveDirectoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                /*Client wants to move a directory in particular folder*/
                dataBox send_operation = new dataBox();
                send_operation.setClient_name(user_name);

                /*Setting the kind of operation*/
                send_operation.setType_of_message("move");
                /*Making the dialog box for input path and destination */
                /*REFERENCES: https://stackoverflow.com/questions/41904362/multiple-joptionpane-input-dialogs*/
                //resetting the UI
                source.setText("");
                destination.setText("");
                myPanel.setLayout(new GridLayout(0, 2, 2, 2));
                /*adding the textarea and label to the panel*/
                myPanel.add(new JLabel("From(Source):input Path"));
                myPanel.add(source);
                /*adding the textarea and label to the panel*/
                myPanel.add(new JLabel("To(Destination):input Path"));
                myPanel.add(destination);

                /*REFERENCES: https://stackoverflow.com/questions/41904362/multiple-joptionpane-input-dialogs*/
                int result =
                        JOptionPane.showConfirmDialog(jframe, myPanel, "MOVE", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                //check if the values are entered.
                if (result == JOptionPane.YES_OPTION && (!source.getText().isEmpty()) && (!destination.getText().isEmpty())) {
                    //getting the text inputted
                    String source_name = source.getText();
                    String destination_name = destination.getText();

                    Path source_path = Paths.get(source_name);
                    Path destination_path = Paths.get(source_name);

                    /*Checking it the paths are authorized for client*/
                    if ((valid_path(source_path)) && (valid_path(destination_path))) {
                        /*if to check ift the input source path exists or not*/
                        if ((Files.exists(source_path)) && (Files.exists(destination_path))) {
                            /*populating the object to be send*/
                            send_operation.setCurrent_path(current_path_in);
                            send_operation.setSource_path(source_name);
                            send_operation.setDestination_path(destination_name);

                            //resetting the UI
                            source.setText("");
                            destination.setText("");
                            //remove all components in panel.
                            myPanel.removeAll();
                            // refresh the panel.
                            myPanel.updateUI();
//                  sending the object operation to the server
                            try {
                                dataOutputStream.writeObject(send_operation);
                                dataOutputStream.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            /*the source or destnation path does not exists*/
                            JOptionPane.showMessageDialog(jframe, "Your Source path or Destination path input directory does not EXISTS", "Directory not found.",
                                    JOptionPane.WARNING_MESSAGE);
                        }

                    } else {
                        /*tIf user is not authorized for that path*/
                        JOptionPane.showMessageDialog(jframe, "You are not authorized to access these paths", "Not Authorized",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        });

        /*-----------------Rename Directory Button-----------------------------*/
        renameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                /*Client wants to rename a directory*/
                dataBox send_operation = new dataBox();
                send_operation.setClient_name(user_name);
                /*Setting the kind of operation*/
                send_operation.setType_of_message("rename");

                /*Making the dialog box for input path and destination */
                /*REFERENCES: https://stackoverflow.com/questions/41904362/multiple-joptionpane-input-dialogs*/
                //resetting the UI
                source.setText("");
                destination.setText("");
                myPanel.setLayout(new GridLayout(0, 2, 2, 2));
                /*adding the textarea and label to the panel*/
                myPanel.add(new JLabel("Rename this(FOLDER NAME): "));
                myPanel.add(source);
                /*adding the textarea and label to the panel*/
                myPanel.add(new JLabel("To(FOLDER NAME):"));
                myPanel.add(destination);

                /*REFERENCES: https://stackoverflow.com/questions/41904362/multiple-joptionpane-input-dialogs*/
                int result =
                        JOptionPane.showConfirmDialog(jframe, myPanel, "Rename", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                //check if the values are entered.
                if (result == JOptionPane.YES_OPTION && (!source.getText().isEmpty()) && (!destination.getText().isEmpty())) {
                    //getting the text inputted
                    String source_name = source.getText();
                    String destination_name = destination.getText();

                    //aappending the name to the path
                    String source_path = current_path_in + "/" + source_name;
                    String destination_path = current_path_in + "/" + destination_name;
                    /*if to check ift the input source path exists or not*/
                    if (Files.exists(Paths.get(source_path))) {
                        /*populating the object to be send*/
                        send_operation.setCurrent_path(current_path_in);
                        send_operation.setSource_path(source_path);
                        send_operation.setDestination_path(destination_path);

                        send_operation.setRename_this(source_name);
                        send_operation.setRename_to_this(destination_name);
                        send_operation.setFolder_name(destination_name);
                        //resetting the UI
                        source.setText("");
                        destination.setText("");
                        //remove all components in panel.
                        myPanel.removeAll();
                        // refresh the panel.
                        myPanel.updateUI();
//                  sending the object operation to the server
                        try {
                            dataOutputStream.writeObject(send_operation);
                            dataOutputStream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        /*the path source unput does not exists*/
                        JOptionPane.showMessageDialog(jframe, "Your Source path input directory does not EXISTS", "Directory not found.",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        });

        /*-----------------list the Directory content BUtton-----------------------------*/
        listOfContentsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                /*Client wants to list the content of directory*/
                dataBox send_operation = new dataBox();
                send_operation.setClient_name(user_name);
                /*Setting the kind of operation*/
                send_operation.setType_of_message("list");

                try {
                    //saving which folders content is to be shown
                    send_operation.setCurrent_path(current_path_in);
                    dataOutputStream.writeObject(send_operation);
                    dataOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

        /*----------------- delete the directory BUtton-----------------------------*/
        deleteDirectoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                /*Client wants to delete the particular directory*/
                dataBox send_operation = new dataBox();
                send_operation.setClient_name(user_name);
                /*Setting the kind of operation*/
                send_operation.setType_of_message("delete");

                /*REFERENCES: https://mkyong.com/swing/java-swing-joptionpane-showinputdialog-example/*/
                String delete_dir_name =
                        JOptionPane.showInputDialog(jframe, "Delete : input FOLDER NAME");
                String targetdir_path = current_path_in + "/" + delete_dir_name;
                Path inputpath = Paths.get(targetdir_path);
                /*testing if the user is authorized or not*/
                if (valid_path(inputpath)) {
                    /*the client is authorized to do the operation
                     * */
                    if (Files.exists(inputpath)) {
                        // we are deleting the directory as ut is authorized and exists...
                        try {
                            send_operation.setCurrent_path(current_path_in);
                            send_operation.setDelete_path(targetdir_path);
                            send_operation.setFolder_name(delete_dir_name);
                            //sending the object to server
                            dataOutputStream.writeObject(send_operation);
                            dataOutputStream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        //incorrect file
                        client_display_TA.append("path does not exits\n");
                    }
                } else {
                    /*client is trying to access
                     unauthorized path or unknown path*/
                    JOptionPane.showMessageDialog(jframe, "Your are not authorized to delete this path.", "Unauthorized Path",
                            JOptionPane.WARNING_MESSAGE);
                }

            }
        });
        /*-----------------Go to the directory BUtton-----------------------------*/
        goto_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                /*creating an input box for taking the new directory path as input*/
                /*REFERENCES: https://mkyong.com/swing/java-swing-joptionpane-showinputdialog-example/*/
                String new_dir_path =
                        JOptionPane.showInputDialog(jframe, "input the desired path", current_path_in);
                Path inputpath = Paths.get(new_dir_path);
                /*testing if the user is authorized or not*/
                if (valid_path(inputpath)) {
                    /*the client is authorized to do the operation
                     * */
                    if (Files.exists(inputpath)) {
                        // ...
                        current_path_in = new_dir_path;
                        client_display_TA.append("You are currently in path\n" + current_path_in + "\n");
                    } else {
                        client_display_TA.append("path does not exits\n");
                    }

                } else {
                    /*client is trying to acces unauthorized path or unknown path*/
                    JOptionPane.showMessageDialog(jframe, "Your are not authorized to access this path.", "Unauthorized Path",
                            JOptionPane.WARNING_MESSAGE);
                }

            }
        });
        /*-----------------Refresh directory BUtton-----------------------------*/
        refresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                /*Client wants to refresh  the driectory options available*/
                dataBox send_operation = new dataBox();
                send_operation.setClient_name(user_name);
                /*Setting the kind of operation*/
                send_operation.setType_of_message("refresh");

                //sending the object operation to the server
                try {
                    dataOutputStream.writeObject(send_operation);
                    dataOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        /*-----------------Synchronize directory  directory BUtton-----------------------------*/
        syncronize_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                /*Client wants to synchronie the particular directory*/
                dataBox send_operation = new dataBox();
                send_operation.setClient_name(user_name);
                /*Setting the kind of operation*/
                send_operation.setType_of_message("synchronize");
                update_synclist();
                /*getting the selected message from the drop down menu*/
                String pathtoSyn = (String) synchronize_optionbox.getSelectedItem();
                if (!pathtoSyn.isEmpty()) {
                    File source_syn = new File("server/" + pathtoSyn);
                    File target_syn = new File(LD_path + "/" + pathtoSyn);
                    //copy source to target using Files Class
                    if (target_syn.mkdirs()) {
                        // creating the outter folder
                        try {
                            update_synclist();
                            //coping the file directories

                            copyDirectory(source_syn, target_syn);
                            send_operation.setSync_item(pathtoSyn);
                            send_operation.setIdentifier(identifier_chosen);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        });
        /*-----------------Desynchronize directory BUtton-----------------------------*/
        desynchronizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                /*Client wants to desynchronize the particular directory*/
                dataBox send_operation = new dataBox();
                send_operation.setClient_name(user_name);
                /*Setting the kind of operation*/
                send_operation.setType_of_message("desynchronize");
                /*Variable to save the target dir to desync*/
                String desync_target = (String) syn_list.getSelectedValue();
                if (!desync_target.isEmpty()) {
                    delete_dire_desync(LD_path + "/" + desync_target);
                    //deleting the directory
                    send_operation.setSync_item(desync_target);
                    send_operation.setIdentifier(identifier_chosen);

                }
                //updating the list
                update_synclist();
            }
        });
        /*functions finished*/
    }

    /*function to check the validity of path*/
    private boolean valid_path(Path test_path) {
        if (test_path.startsWith(user_homedirectory)) {
            return true;
        }
        return false;
    }

    /*Function to copy files
     * Reference: https://javarevisited.blogspot.com/2016/02/how-to-copy-non-empty-directory-in-java.html*/
    public static void copyDirectory(File sourceDir, File targetDir)
            throws IOException {
        if (sourceDir.isDirectory()) {
            copyDirectoryRecursively(sourceDir, targetDir);
        } else {
            Files.copy(sourceDir.toPath(), targetDir.toPath());
        }
    }

    /*--------------------------Function to copy files------------------------------------
     * Reference: https://javarevisited.blogspot.com/2016/02/how-to-copy-non-empty-directory-in-java.html*/
    // recursive method to copy directory and sub-diretory in Java
    private static void copyDirectoryRecursively(File source, File target)
            throws IOException {
        if (!target.exists()) {
            target.mkdir();
        }

        for (String child : source.list()) {
            copyDirectory(new File(source, child), new File(target, child));
        }
    }
    /*function to copy file ends here*/

    /*-----------------Function to update the syncronization list--------------*/
    private void update_synclist() {
        //setting the display
        File f = new File(LD_path);
        syn_list.setModel(new DefaultComboBoxModel(f.list()));
    }

    /*------------------Delete directory-----------------------------------
     * */
    private void delete_dire_desync(String target) {
        Path traget_path = Paths.get(target);

        //deleting the directory and its content
        //Refrences: https://mkyong.com/java/how-to-delete-directory-in-java/
        //REfrence starts here
        try {
            Files.walkFileTree(traget_path, new SimpleFileVisitor<>() {
                // delete directories or folders
                @Override
                public FileVisitResult postVisitDirectory(Path dir,
                                                          IOException exc)
                        throws IOException {
                    //Actually dleleting the directory
                    Files.delete(dir);

                    return FileVisitResult.CONTINUE;
                }

                // delete files
                @Override
                public FileVisitResult visitFile(Path file,
                                                 BasicFileAttributes attrs)
                        throws IOException {
                    //deleting the inside file in the directory
                    Files.delete(file);

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*---------------------Variable declaration section here---------these variables are local to client*/
    private ObjectInputStream dataInputStream;
    private ObjectOutputStream dataOutputStream;
    Socket sc;

    /*---------------------------------method to connect to server-------------------------------*/
    private boolean server_connect() {

        try {
            //establishing coonection
            sc = new Socket("localhost", 5000);

            //initialoizing the output stream to send the data
            dataOutputStream = new ObjectOutputStream(sc.getOutputStream());

            //creating and initializing the message object to be sent
            dataBox send_box = new dataBox();
            send_box.setClient_name(user_name.trim());
            /*setting the type of message :- "Initial"*/
            send_box.setType_of_message("Initial");
            /*Setting the identifier*/
            send_box.setIdentifier(identifier_chosen);
            //writing the object in output stream to send to the server
            dataOutputStream.writeObject(send_box);
            dataOutputStream.flush();

            client_display_TA.append("Succesfully Connected to the Server!\n");
            /*Calling the function to continuos listen of server response*/
            keep_listening();
            /*boolean indicates the success of connection*/
            return true;

        } catch (IOException e) {
            //showing error message that the connection establishment has failed
            JOptionPane.showMessageDialog(this, "Connection failed to connect", "Connection Error",
                    JOptionPane.WARNING_MESSAGE);
            e.printStackTrace();
            System.exit(0);
        }

        return false;
    }

    /*-----------------------------------MAIN method-------------------------------------------*/
    public static void main(String[] args) {
        new Client();

    }

    /*----------------------------------Continuous Listening method----------------------------*/
    public void keep_listening() {
        /*This method keeps listening to the socket to check if there is message from server any time after the connection
         * is established and the connection is disconnected. We create a separate thread for it.*/
        /*Reference: https://stackoverflow.com/questions/28148060/multi-threaded-java-tcp-client*/
        Thread listen_toreponse = new Thread(new Runnable() {
            @Override
            public void run() {
                /*creating an inputstream to listen to the server*/
                try {
                    dataInputStream = new ObjectInputStream(sc.getInputStream());


                } catch (IOException e) {
                    e.printStackTrace();
                }

                /*to continuously listen we create a infinite while loop*/
                while (true) {
                    try {
                        //reading the data object received
                        dataBox received_dataO = (dataBox) dataInputStream.readObject();
                        /*---------------------------------------Initial Message Response------------------------------------------*/
                        if (received_dataO.getType_of_message().equalsIgnoreCase("Initial")) {
                            /*this response to initial message*/
                            client_display_TA.append("Home path: " + received_dataO.getCurrent_path() + "\n");
                            //setting the directory path locally in the client
                            current_path_in = received_dataO.getCurrent_path();
                            user_homedirectory = current_path_in;

                            /*checking for the identifier response*/
                            String t = String.valueOf(received_dataO.getIdentifier());
                            if (t.equalsIgnoreCase("Z")) {
                                /*it means the identifier is already taken*/
                                /*showing a dialog box to take the empty identifier */
                                JOptionPane.showMessageDialog(jpanel1, "Identifier already in use.\nCannot Synchronize to local directory.", "Identifier already in use",
                                        JOptionPane.WARNING_MESSAGE);
                                LD_path = "";
                                identifier_text.setText("Identifier already taken");
                            } else {
                                /*It means the identifier is correct */
                                identifier_text.setText("Identifier: " + identifier_chosen);
                                /*setting the local directory path with select6ed identifier*/
                                LD_path = "LocalDirectory/" + identifier_chosen;
                                if (new File(LD_path).mkdirs()) {
                                    /*local directory does not existed and created*/
                                    client_display_TA.append("\nLocal Directory created with Identifier: " + identifier_chosen + "\n");
                                } else {
                                    /*local directory already exists and is to be syncronied*/
                                    client_display_TA.append("\nLocal Directory already exists and is Syncronized\n");
                                }

                            }
                        }
                        /*--------------------------------------Create Message Response-------------------------------------------*/
                        else if (received_dataO.getType_of_message().equalsIgnoreCase("create")) {
                            /*this is response after create operation*/
                            current_path_in = received_dataO.getCurrent_path();
                            client_display_TA.append("------------------------------------------\n");
                            client_display_TA.append(received_dataO.getOperation_report() + "You are currently in path:\n" + current_path_in + "\n");
                            client_display_TA.append("------------------------------------------\n");
                        }
                        /*--------------------------------------List the directory content Response-------------------------------*/
                        else if (received_dataO.getType_of_message().equalsIgnoreCase("list")) {
                            /*this is response after list operation*/
                            current_path_in = received_dataO.getCurrent_path();
                            client_display_TA.append("------------------------------------------\n");
                            client_display_TA.append("You are currently in path:\n" + current_path_in + "\n");
                            client_display_TA.append(received_dataO.getOperation_report());
                            List<String> filescontent = received_dataO.getList_of_files();
                            for (String file_i : filescontent) {
                                client_display_TA.append(file_i + "\n");
                            }
                            client_display_TA.append("------------------------------------------\n");
                        }
                        /*----------------------------------Delete Directory Response-----------------------------------------------*/
                        else if (received_dataO.getType_of_message().equalsIgnoreCase("delete")) {
                            /*this is response after create operation*/
                            current_path_in = received_dataO.getCurrent_path();
                            client_display_TA.append("------------------------------------------\n");
                            client_display_TA.append(received_dataO.getOperation_report() + "You are currently in path:\n" + current_path_in + "\n");
                            client_display_TA.append("------------------------------------------\n");
                        }
                        /*-----------------------------------RENAME Directory Response----------------------------------------------*/
                        else if (received_dataO.getType_of_message().equalsIgnoreCase("rename")) {
                            /*this is response after create operation*/
                            current_path_in = received_dataO.getCurrent_path();
                            client_display_TA.append("---------------------Rename Operation---------------------\n");
                            client_display_TA.append(received_dataO.getOperation_report() + "You are currently in path:\n" + current_path_in + "\n");
                            client_display_TA.append("------------------------------------------\n");
                        }
                        /*-----------------------------------move Directory Response----------------------------------------------*/
                        else if (received_dataO.getType_of_message().equalsIgnoreCase("move")) {
                            /*this is response after create operation*/
                            current_path_in = received_dataO.getCurrent_path();
                            client_display_TA.append("------------------------------------------\n");
                            client_display_TA.append(received_dataO.getOperation_report() + "You are currently in path:\n" + current_path_in + "\n");
                            client_display_TA.append("------------------------------------------\n");
                        }
                        /*-------------------------------------Reject Response--------------------------------------------*/
                        else if (received_dataO.getType_of_message().equalsIgnoreCase("reject")) {
                            /*this is response after create operation*/
                            String failure_message = received_dataO.getOperation_report();
                            JOptionPane.showMessageDialog(jpanel1, "Same username already Exists", "Duplicate user.",
                                    JOptionPane.WARNING_MESSAGE);
                            System.exit(0);

                        }
                        /*-------------------------------------REFRESH Response--------------------------------------------*/
                        else if (received_dataO.getType_of_message().equalsIgnoreCase("refresh")) {
                            /*this is response after refresh operation*/
                            String[] options_values = received_dataO.getList_of_files().toArray(new String[0]);
                            synchronize_optionbox.setModel(new DefaultComboBoxModel(options_values));
                            update_synclist();
                        }
                        /*-------------------------------------Synchronize Response--------------------------------------------*/
                        else if (received_dataO.getType_of_message().equalsIgnoreCase("synchronize")) {
                            /*this is response after synchronize operation*/


                        }
                        /*-------------------------------------DeSynchronize Response--------------------------------------------*/
                        else if (received_dataO.getType_of_message().equalsIgnoreCase("desynchronize")) {
                            /*this is response after desynchronize operation*/

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //starting the thread
        listen_toreponse.start();
    }

}
