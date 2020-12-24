//Generated by GuiGenie - Copyright (c) 2004 Mario Awad.
//Home Page http://guigenie.cjb.net - Check often for new versions!

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import javax.swing.*;
import javax.swing.event.*;

public class ClientGUI extends JPanel {

    /*
    *
    * */
    private JButton register_button;
    private JButton login_button;
    private JTextField username_field;
    private JLabel jcomp4;
    private JPasswordField password_field;
    private JLabel jcomp6;
    private JMenuBar jcomp7;
    private String username;
    private String password;
    private JFrame mainWindow;

    private JButton showProjectMenuButton;
    private JButton createNewProjectButton;
    private JLabel welcomeLabel;
    private JMenuBar menuBar;

    //Register component
    private JButton registerButton;
    private JLabel usernameRegisterLabel;
    private JLabel passwordRegisterLabel;
    private JPasswordField passwordRegisterField;
    private JTextField usernameRegisterField;

    //Show online user DIALOG
    private JList showOnlineUserList;

    //Show all user DIALOG
    private JList showAllUserList;

    //Select project list DIALOG
    private JComboBox projectListItem;
    private JLabel projectListLabel;

    //Chat dialog
    private JTextArea chatbox;
    private JTextField messageField;
    private JLabel messageLabel;
    private JButton sendMessageButton;
    private JLabel projectChatLabel;

    private String authenticatedUsername;
    private String authenticatedPasswrod;

    //Frame for new project
    private JButton createProjectButton;
    private JTextField projectTitleField;
    private JLabel projectTitleLabel;


    public ClientGUI(final Client clientProcessor, int connectionError) {

        mainWindow = new JFrame("WORTH Meme Management");
        mainWindow.setSize(8, 6);
        mainWindow.setLocation(550, 150);

        if(connectionError==1){
            JOptionPane.showMessageDialog(null, "Impossibile connettersi al server", "ERRORE INTERNO", JOptionPane.ERROR_MESSAGE);

        }

        //Pannello con le operazioni e informazioni di login



        mainWindow.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        mainWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //WQClientController.client.logout();
                mainWindow.dispose();
                System.exit(0);
            }
        });
        mainWindow.setVisible(true);
        mainWindow.pack();
        mainWindow.setMinimumSize(mainWindow.getSize());
        clientLoginPanel(clientProcessor);


    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);

        Client clientProcessor = null;
        int connectionError = 0;

        try{
        clientProcessor = new Client(port);

        }catch (IOException ex){

            connectionError = 1;
        }

        ClientGUI loginWindow = new ClientGUI(clientProcessor, connectionError);
    }


    public void clientDashboardPanel(final Client clientProcessor){

        final JDialog dashWindow = new JDialog(mainWindow, "WORTH - Dashboard", true);
        dashWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //construct preComponents
        JMenu accountMenu = new JMenu ("Account");
        JMenuItem logoutItem = new JMenuItem ("Logout");
        accountMenu.add (logoutItem);

        JMenu helpMenu = new JMenu ("Help");
        JMenuItem contentsItem = new JMenuItem ("Contents");
        helpMenu.add (contentsItem);
        JMenuItem aboutItem = new JMenuItem ("About");
        helpMenu.add (aboutItem);

        JMenu userMenu = new JMenu ("Worth users");
        JMenuItem showOnlineUsersItem = new JMenuItem ("Show online users");
        userMenu.add (showOnlineUsersItem);
        JMenuItem showAllUsers = new JMenuItem ("Show all users");
        userMenu.add (showAllUsers);

        JMenu chatMenu = new JMenu ("Worth chat");
        JMenuItem projectChat = new JMenuItem ("Project chat");
        chatMenu.add (projectChat);



        //construct components
        showProjectMenuButton = new JButton ("Show a menu of a single project");
        createNewProjectButton = new JButton ("Create a new project");
        welcomeLabel = new JLabel ("Hey, welcome " + authenticatedUsername + "!");
        menuBar = new JMenuBar();
        menuBar.add (accountMenu);
        menuBar.add (helpMenu);
        menuBar.add(userMenu);
        menuBar.add(chatMenu);

        //adjust size and set layout
        dashWindow.setPreferredSize (new Dimension (450, 318));
        dashWindow.setLayout (null);

        //add components
        dashWindow.add (showProjectMenuButton);
        dashWindow.add (createNewProjectButton);
        dashWindow.add (welcomeLabel);
        dashWindow.add (menuBar);




        projectChat.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {


                JDialog selectProjectFrame = new JDialog(dashWindow,"Worth online user list",true);

                //construct preComponents
                String[] jcomp1Items = {"Item 1", "Item 2", "Item 3"};

                //construct components
                projectListItem = new JComboBox (jcomp1Items);
                projectListLabel = new JLabel ("Seleziona progetto");

                //adjust size and set layout
                selectProjectFrame.getContentPane().setPreferredSize (new Dimension (206, 136));
                selectProjectFrame.getContentPane().setLayout (null);

                //add components
                selectProjectFrame.getContentPane().add (projectListItem);
                selectProjectFrame.getContentPane().add (projectListLabel);

                //set component bounds (only needed by Absolute Positioning)
                projectListItem.setBounds (15, 60, 170, 35);
                projectListLabel.setBounds (15, 25, 175, 25);



                projectListItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        String selectedValue = projectListItem.getSelectedItem().toString();

                        JDialog showChatProjectDialog = new JDialog(dashWindow,"Project " +selectedValue + "chat ",true);
                        showChatProjectDialog.setDefaultCloseOperation (JFrame.HIDE_ON_CLOSE);



                        chatbox = new JTextArea (5, 5);
                        messageField = new JTextField (5);
                        messageLabel = new JLabel ("Messaggio da [UTENTE]");
                        sendMessageButton = new JButton ("Invia messaggio");
                        projectChatLabel = new JLabel ("Chat del progetto");

                        //set components properties
                        chatbox.setEnabled (false);

                        //adjust size and set layout

                        showChatProjectDialog.getContentPane().setPreferredSize (new Dimension (614, 431));
                        showChatProjectDialog.setLayout (null);

                        //add components
                        showChatProjectDialog.getContentPane().add (chatbox);
                        showChatProjectDialog.getContentPane().add (messageField);
                        showChatProjectDialog.getContentPane().add (messageLabel);
                        showChatProjectDialog.getContentPane().add (sendMessageButton);
                        showChatProjectDialog.getContentPane().add (projectChatLabel);

                        //set component bounds (only needed by Absolute Positioning)
                        chatbox.setBounds (15, 35, 585, 265);
                        messageField.setBounds (15, 340, 585, 25);
                        messageLabel.setBounds (15, 310, 615, 25);
                        sendMessageButton.setBounds (435, 375, 165, 35);
                        projectChatLabel.setBounds (15, 10, 585, 30);

                        sendMessageButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent ae) {

                                String message = messageField.getText();
                                messageField.setText("");
                                chatbox.append("\nLuca: " + message);

                            }});

                                showChatProjectDialog.setDefaultCloseOperation (JFrame.HIDE_ON_CLOSE);
                        showChatProjectDialog.pack();
                        showChatProjectDialog.setLocationRelativeTo(null);
                        showChatProjectDialog.setVisible (true);

                    }
                });


                selectProjectFrame.setDefaultCloseOperation (JFrame.HIDE_ON_CLOSE);
                selectProjectFrame.pack();
                selectProjectFrame.setLocationRelativeTo(null);
                selectProjectFrame.setVisible (true);




            }
        });

        showOnlineUsersItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {

                JDialog showOnlineUserFrame = new JDialog(dashWindow,"Worth online user list",true);
                showOnlineUserFrame.setDefaultCloseOperation (JFrame.HIDE_ON_CLOSE);


                    //construct preComponents

                    String[] onlineUserList = clientProcessor.listOnlineUsers();

                    //construct components
                showOnlineUserList = new JList(onlineUserList);

                    //adjust size and set layout
                showOnlineUserFrame.getContentPane().setPreferredSize(new Dimension (206, 200));
                showOnlineUserFrame.getContentPane().setLayout (null);

                    //add components
                showOnlineUserFrame.getContentPane().add(showOnlineUserList);
                JScrollPane scrPane = new JScrollPane();
                showOnlineUserFrame.getContentPane().add(scrPane);

                    //set component bounds (only needed by Absolute Positioning)
                showOnlineUserList.setBounds (15, 20, 180, 155);

                showOnlineUserFrame.pack();
                showOnlineUserFrame.setLocationRelativeTo(null);
                showOnlineUserFrame.setVisible (true);


            }
        });

        logoutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {

                try {
                    if(clientProcessor.logout(authenticatedUsername) == 1)
                        JOptionPane.showMessageDialog(null, "Errore interno durante logout", "Errore", JOptionPane.ERROR_MESSAGE);
                    else System.out.println("[CLIENT] Logout success, bye!\n");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                dashWindow.setVisible(false);
                mainWindow.getDefaultCloseOperation();
                System.exit(0);


            }
        });


        createNewProjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                final JDialog newProjectWindow = new JDialog(dashWindow, "Creazione nuovo progetto", true);
                newProjectWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

                //construct components
                createProjectButton = new JButton ("Crea progetto");
                projectTitleField = new JTextField (8);
                projectTitleLabel = new JLabel ("Titolo progetto");

                createProjectButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        projectTitleField.getText();
                        if(projectTitleField.getText().isEmpty() || projectTitleField.getText().isBlank())
                            JOptionPane.showMessageDialog(null, "Titolo progetto vuoto!", "Errore", JOptionPane.ERROR_MESSAGE);
                        else {
                            int serverResponse = clientProcessor.createProject(projectTitleField.getText());
                            switch (serverResponse){

                                case 0:
                                    JOptionPane.showMessageDialog(null, "Progetto creato correttamente!", "Ok", JOptionPane.DEFAULT_OPTION);
                                    break;
                                case 1:
                                    JOptionPane.showMessageDialog(null, "Progetto esistente!", "Errore", JOptionPane.ERROR_MESSAGE);
                                    break;
                                case 2:
                                    JOptionPane.showMessageDialog(null, "Errore interno: impossibile creare progetto!", "Errore", JOptionPane.ERROR_MESSAGE);
                                    break;

                            }
                        }

                    }
                });

                //adjust size and set layout
                newProjectWindow.getContentPane().setPreferredSize (new Dimension (426, 176));
                newProjectWindow.getContentPane().setLayout (null);

                //add components
                newProjectWindow.getContentPane().add (createProjectButton);
                newProjectWindow.getContentPane().add (projectTitleField);
                newProjectWindow.getContentPane().add (projectTitleLabel);

                //set component bounds (only needed by Absolute Positioning)
                createProjectButton.setBounds (220, 105, 165, 40);
                projectTitleField.setBounds (20, 50, 375, 25);
                projectTitleLabel.setBounds (20, 25, 215, 25);

                newProjectWindow.setLocationRelativeTo(null);
                newProjectWindow.pack();
                newProjectWindow.setVisible(true);


            }});



        showProjectMenuButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {



                final JDialog selectProjectFrame = new JDialog(dashWindow,"Worth online user list",true);
                selectProjectFrame.setLocationRelativeTo(null);

                    //construct preComponents
                    String[] jcomp1Items = clientProcessor.listProjects(authenticatedUsername);
                    //String[] jcomp1Items = {"Item 1", "Item 2", "Item 3"};

                    //construct components
                    projectListItem = new JComboBox (jcomp1Items);
                    projectListLabel = new JLabel ("Seleziona progetto");

                    //adjust size and set layout
                    selectProjectFrame.getContentPane().setPreferredSize (new Dimension (206, 136));
                    selectProjectFrame.getContentPane().setLayout (null);

                    //add components
                    selectProjectFrame.getContentPane().add (projectListItem);
                    selectProjectFrame.getContentPane().add (projectListLabel);

                    //set component bounds (only needed by Absolute Positioning)
                    projectListItem.setBounds (15, 60, 170, 35);
                    projectListLabel.setBounds (15, 25, 175, 25);



                projectListItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        String selectedProject = projectListItem.getSelectedItem().toString();

                        selectProjectFrame.setVisible(false);

                        JDialog frame = new JDialog(mainWindow, "Dashboard progetto " + selectedProject, true);
                        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                        JTabbedPane tabbedPane = new JTabbedPane();

                        JLabel cardTitleLabel = new JLabel("Titolo card");
                        JTextField cardTitleField = new JTextField(5);
                        JLabel cardDescLabel = new JLabel("Descrizione card");
                        JTextArea cardDescField = new JTextArea(5, 5);
                        JButton sendRequestButton = new JButton("Send request");





                        //set component bounds (only needed by Absolute Positioning)
                        cardTitleLabel.setBounds (60, 190, 100, 25);
                        cardTitleField .setBounds (60, 220, 385, 25);
                        cardDescLabel.setBounds (60, 260, 160, 25);
                        cardDescField .setBounds (60, 285, 380, 75);
                        sendRequestButton.setBounds (620, 320, 195, 45);

                            //Create the "cards".
                        JPanel card1 = new JPanel();

                        JPanel card2 = new JPanel();

                        JPanel card3 = new JPanel();

                        JPanel card4 = new JPanel();

                        JPanel card5 = new JPanel();

                        String project = "DA PENSARCI";
                        //ClientMenuGUI manageProject = new ClientMenuGUI(clientProcessor, project);
                        card1.add (new ClientMenuGUI(clientProcessor, selectedProject).ClientRemoveCardGUI());
                        card2.add (new ClientMenuGUI(clientProcessor, selectedProject).ClientAddCardGUI());
                        card3.add (new ClientMenuGUI(clientProcessor, selectedProject).ClientShowCardsGUI(mainWindow));
                        card4.add (new ClientMenuGUI(clientProcessor, selectedProject).ClientMoveCardGUI());
                        card5.add (new ClientMenuGUI(clientProcessor, selectedProject).ClientShowCardHistoryGUI(dashWindow));

                        tabbedPane.addTab("Cancel project", card1);
                        tabbedPane.addTab("Add card", card2);
                        tabbedPane.addTab("Show card", card3);
                        tabbedPane.addTab("Move card", card4);
                        tabbedPane.addTab("Show card history", card5);


                        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);


                            //Display the window.
                        frame.setPreferredSize (new Dimension (925, 318));
                        frame.pack();
                        frame.setLocationRelativeTo(null);
                        frame.setVisible(true);
                    }


                    });


                selectProjectFrame.setDefaultCloseOperation (JFrame.HIDE_ON_CLOSE);
                selectProjectFrame.pack();
                selectProjectFrame.setVisible (true);


                }
            });



        showAllUsers.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {

                JDialog showAllUserFrame = new JDialog(dashWindow,"Worth user list",true);
                showAllUserFrame.setDefaultCloseOperation (JFrame.HIDE_ON_CLOSE);


                //construct preComponents
               String[] allUserList = clientProcessor.listUsers();//{"DarkPyotr", "TuMa", "Mr.Radix", "MicioBaubau"};
                clientProcessor.listUsers();
                //construct components
                showAllUserList = new JList (allUserList);

                //adjust size and set layout
                showAllUserFrame.getContentPane().setPreferredSize(new Dimension (206, 200));
                showAllUserFrame.getContentPane().setLayout (null);

                //add components
                showAllUserFrame.getContentPane().add(showAllUserList);
                JScrollPane scrPane = new JScrollPane();
                showAllUserFrame.getContentPane().add(scrPane);

                //set component bounds (only needed by Absolute Positioning)
                showAllUserList.setBounds (15, 20, 180, 155);

                showAllUserFrame.pack();
                showAllUserFrame.setLocationRelativeTo(null);
                showAllUserFrame.setVisible (true);


            }
        });






        //set component bounds (only needed by Absolute Positioning)
        showProjectMenuButton.setBounds (95, 130, 290, 40);
        createNewProjectButton.setBounds (95, 210, 290, 40);
        welcomeLabel.setBounds (150, 45, 175, 29);
        menuBar.setBounds (0, 0, 535, 25);


        //dashWindow.getContentPane().add(new ClientMenuGUI());
        dashWindow.setLocationRelativeTo(null);
        dashWindow.pack();
        dashWindow.setVisible(true);


    }

    public void clientRegisterPanel(final Client clientProcessor){

        final JDialog registerWindow = new JDialog(mainWindow, "WORTH - Registrazione", true);
        registerWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        registerWindow.setLocationRelativeTo(null);

            //construct components
            registerButton = new JButton ("Registrami");
            usernameRegisterLabel = new JLabel ("Username");
            passwordRegisterLabel = new JLabel ("Password");
            passwordRegisterField = new JPasswordField (5);
            usernameRegisterField = new JTextField (5);

            //adjust size and set layout
            registerWindow.setPreferredSize (new Dimension (403, 293));
            registerWindow.setLayout (null);

            //add components

            registerWindow.getContentPane().add(registerButton);
            registerWindow.getContentPane().add (usernameRegisterLabel);
            registerWindow.getContentPane().add (passwordRegisterLabel);
            registerWindow.getContentPane().add (passwordRegisterField);
            registerWindow.getContentPane().add (usernameRegisterField);

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String registerUsername = usernameRegisterField.getText();
                String registerPassword = new String(passwordRegisterField.getPassword());

                if (registerUsername.isEmpty())
                    JOptionPane.showMessageDialog(null, "Errore: campo username vuoto", "Errore", JOptionPane.ERROR_MESSAGE);
                else if (registerPassword.isEmpty())
                    JOptionPane.showMessageDialog(null, "Errore: campo password vuoto", "Errore", JOptionPane.ERROR_MESSAGE);
                else {

                    //Richiesta di registrazione tramite RMI:
                    //Codifica degli stati
                    // 0 -- SUCCESS
                    // 1 -- DUPLICATED USERNAME
                    // 2 -- SAME USERNAME AND PASSWORD

                    Integer serverResponse = clientProcessor.registerRequest(registerUsername, registerPassword);

                    switch(serverResponse){

                        case 0:
                            registerWindow.setVisible(false);
                            JOptionPane.showMessageDialog(null, "Sei registrato con successo!", "Successo", JOptionPane.INFORMATION_MESSAGE);
                            break;

                        case 1:
                            JOptionPane.showMessageDialog(null, "Errore: username non disponibile", "Errore", JOptionPane.ERROR_MESSAGE);
                            break;

                        case 2:
                            registerWindow.setVisible(false);
                            JOptionPane.showMessageDialog(null, "Errore: password e username coincidono!", "Errore", JOptionPane.ERROR_MESSAGE);
                            break;

                    }



                }

            }
        });

            //set component bounds (only needed by Absolute Positioning)
            registerButton.setBounds (120, 190, 165, 40);
            usernameRegisterLabel.setBounds (55, 20, 100, 25);
            passwordRegisterLabel.setBounds (55, 90, 100, 25);
            passwordRegisterField.setBounds (55, 120, 275, 25);
            usernameRegisterField.setBounds (55, 50, 270, 25);



        //registerWindow.getContentPane().add(new ClientRegisterGUI());

        registerWindow.pack();
        registerWindow.setVisible(true);

    }


    public void clientLoginPanel(final Client clientProcessor){

        final JDialog login = new JDialog(mainWindow, "WORTH Login", true);


        //construct preComponents
        JMenu infoMenu = new JMenu("Info");
        JMenuItem option_1Item = new JMenuItem("Option 1");
        infoMenu.add(option_1Item);
        JMenuItem option_2Item = new JMenuItem("Option 2");
        infoMenu.add(option_2Item);
        JMenu helpMenu = new JMenu("Help");
        JMenuItem contentsItem = new JMenuItem("Contents");
        helpMenu.add(contentsItem);
        JMenuItem aboutItem = new JMenuItem("About");
        helpMenu.add(aboutItem);


        //construct components
        register_button = new JButton("Register");
        login_button = new JButton("Login");
        username_field = new JTextField(5);
        jcomp4 = new JLabel("Username");
        password_field = new JPasswordField(5);
        jcomp6 = new JLabel("Password");
        jcomp7 = new JMenuBar();
        jcomp7.add(infoMenu);
        jcomp7.add(helpMenu);


        //Pressing enter from keyboard, need to think about it
        password_field.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode()==KeyEvent.VK_ENTER){

                }
            }
        });


        login_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                username = username_field.getText();
                password = new String(password_field.getPassword());

                if (username.isEmpty())
                    JOptionPane.showMessageDialog(null, "Errore: campo username vuoto", "Errore", JOptionPane.ERROR_MESSAGE);
                else if (password.isEmpty())
                    JOptionPane.showMessageDialog(null, "Errore: campo password vuoto", "Errore", JOptionPane.ERROR_MESSAGE);
                else {
                    try {
                        String response = clientProcessor.login(username,password);
                        System.out.println(response); //restituisce stringa con utenti e stato (online/offline)

                        if (response.compareTo("USERNAME-ERROR") == 0)
                            JOptionPane.showMessageDialog(null, "Errore: username non valido", "Errore", JOptionPane.ERROR_MESSAGE);
                        else if (response.compareTo("PASSWORD-ERROR") == 0)
                            JOptionPane.showMessageDialog(null, "Errore: password errata", "Errore", JOptionPane.ERROR_MESSAGE);
                        else {

                            authenticatedUsername = username;
                            authenticatedPasswrod = password;
                            login.setVisible(false);
                            clientDashboardPanel(clientProcessor);
                        }


                    } catch (IOException | NotBoundException e) {
                        e.printStackTrace();
                    }



                }

            }
        });


        register_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {

                clientRegisterPanel(clientProcessor);


            }
        });

        //adjust size and set layout
        login.setPreferredSize(new Dimension(437, 279));
        login.setLayout(null);
        login.setBackground(Color.gray);

        //add components
        login.getContentPane().add(register_button);
        login.getContentPane().add(login_button);
        login.getContentPane().add(username_field);
        login.getContentPane().add(jcomp4);
        login.getContentPane().add(password_field);
        login.getContentPane().add(jcomp6);
        login.getContentPane().add(jcomp7);

        //set component bounds (only needed by Absolute Positioning)
        register_button.setBounds(165, 180, 140, 20);
        login_button.setBounds(165, 150, 140, 20);
        username_field.setBounds(165, 60, 135, 25);
        jcomp4.setBounds(80, 60, 100, 25);
        password_field.setBounds(165, 90, 135, 25);
        jcomp6.setBounds(80, 90, 100, 25);
        jcomp7.setBounds(0, 0, 440, 15);

        login.setDefaultCloseOperation (JFrame.HIDE_ON_CLOSE);
        login.pack();
        login.setLocationRelativeTo(null);
        login.setVisible(true);






    }

}
