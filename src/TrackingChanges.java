/*Name: Shubham Phape
 * UTA ID: 1001773736*/

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TrackingChanges {


    public static void main(String[] args) throws IOException, InterruptedException {
        File f_a= new File("LocalDirectory/A");
        File f_b= new File("LocalDirectory/B");
        File f_c= new File("LocalDirectory/C");
        f_a.mkdirs();
        f_b.mkdirs();
        f_c.mkdirs();
        /*calling the watcher functions*/
        beepAfterInterval();

    }
/*function to update the update file folder */
    private static void update_file(String name, String folder_naam) throws IOException {
        String curr_path_for_identifier = "LocalDirectory/" + name;


                String folder_name = folder_naam;
                //UPdateing the changed files
                File source_syn = new File("server/" + folder_name);
                File target_syn = new File(curr_path_for_identifier + "/" + folder_name);

                //copy source to target using Files Class
                delete_before_replace(curr_path_for_identifier + "/" + folder_name);
                /*creating a folder*/
                if (target_syn.mkdir()) {
                    // creating the outter folder
                    try {
                        //coping the file directories
                        System.out.println(
                                "Updated diretory " + folder_name + " at: " + name);
                        copyDirectory(source_syn, target_syn);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
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

    /*function to delete*/
    private static void delete_before_replace(String target_path) throws IOException {
        //deleting the directory and its content
        //Refrences: https://mkyong.com/java/how-to-delete-directory-in-java/
        //REfrence starts here
        Files.walkFileTree(Paths.get(target_path), new SimpleFileVisitor<>() {
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
    }


    /*Reference: https://codippa.com/how-to-do-a-task-repeatedly-using-threads-in-java-how-to-create-a-timer-using-scheduledthreadpoolexecutor-in-java/*/
    public static void beepAfterInterval() {
//initialize the scheduler service
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//schedule a task to execute after every 5 seconds
        final ScheduledFuture beeper = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // this is just a sample. Any repeatitive task such as connection
                // health monitoring can be done here
                for (String i: new String[]{"A", "B", "C"}
                ) {
                    try {
                        update_stuff(i);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }, 0, 2, TimeUnit.SECONDS);
    }
/*iterating function to see all identifiers*/
    public static void update_stuff(String I) throws IOException {
        File f= new File("LocalDirectory/"+I);

        for (String each_file: f.list()
             ) {
            update_file(I, each_file);

        }
    }


}
