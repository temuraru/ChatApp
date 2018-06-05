package com.temuraru;


import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.Socket;

public class ClientMain extends JFrame {

    public static void main(String[] args) throws IOException {
        int port = 8867;

        int newClientId = Server.getNewClientId();
        Server server = new Server(port);
        System.out.println("Connecting to server on port: "+port+" as client #"+newClientId+"!");

        Socket clientSocket = new Socket("127.0.0.1", port);
//        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
//        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        ClientHandler clientHandler = new ClientHandler(server, clientSocket, newClientId);
        clientHandler.setCurrentGroup(Server.getMainGroup());

        runGui(clientHandler);
    }

    private static void runGui(ClientHandler clientHandler) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {

        // create a jtextarea
        JTextArea textArea = new JTextArea();

        // add text to it; we want to make it scroll
        textArea.setText("xx\nxx\nxxx\nxx\nxx\nxx\n");

        // create a scrollpane, givin it the textarea as a constructor argument
        JScrollPane scrollPane = new JScrollPane(textArea);

        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(false);


        // now add the scrollpane to the jframe's content pane, specifically
        // placing it in the center of the jframe's borderlayout
        JFrame frame = new JFrame("ChatApp - Client");
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        // make it easy to close the application
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // set the frame size (you'll usually want to call frame.pack())
//        frame.setSize(new Dimension(640, 480));
        frame.setSize(640, 480);
        frame.getContentPane().setSize(new Dimension(640, 480));

        // center the frame
        frame.setLocationRelativeTo(null);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
//                setEnabled(false);
    }

}
