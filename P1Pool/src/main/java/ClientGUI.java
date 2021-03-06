import sun.misc.Signal;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class ClientGUI extends JPanel {

    /*
    *
    * */
    private static int port = 2020;
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


    private String authenticatedUsername;
    private String authenticatedPasswrod;

    //Frame for new project
    private JButton createProjectButton;
    private JTextField projectTitleField;
    private JLabel projectTitleLabel;

    //Chat frame components
    private JTextArea messageListBoxArea;
    private JTextField newMessageField;
    private JButton sendMessageButton;
    private JLabel inviteUserLabel;
    private JComboBox memberListComboBox;
    private JButton addMemberButton;
    private JComboBox userListComboBox;
    private JLabel memberListLabel;




    public ClientGUI(final Client clientProcessor, int connectionError) {

        mainWindow = new JFrame("WORTH Windows Manager");
        URL iconURL = getClass().getResource("./w.png");
        ImageIcon img = new ImageIcon(iconURL);
        mainWindow.setIconImage(img.getImage());
        mainWindow.setSize(1, 1);
        mainWindow.setLocation(550, 150);
        
        defaultActionSet(clientProcessor, mainWindow);

        if(connectionError == 1){
            JOptionPane.showMessageDialog(null, "Impossibile connettersi al server", "ERRORE INTERNO", JOptionPane.ERROR_MESSAGE);
            System.exit(1);

        }

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
        mainWindow.setLocationRelativeTo(null);
        mainWindow.setResizable(false);
        mainWindow.pack();
        clientLoginPanel(clientProcessor);


    }

    public static void main(String[] args) {

        Client clientProcessor = null;
        int connectionError = 0;

        try{
            clientProcessor = new Client(port);

        }catch (IOException ex){

            connectionError = 1;
        }

        ClientGUI loginWindow = new ClientGUI(clientProcessor, connectionError);



    }

    public void defaultActionSet(Client clientProcessor, JFrame dashWindow) {
        Signal.handle(new Signal("INT"),  // SIGINT
                signal -> {

                    int status = 1;
                    try {
                        if(authenticatedUsername != null) status = clientProcessor.logout(authenticatedUsername, true);
                        else status = status = clientProcessor.logout(authenticatedUsername, false);


                        if (status == 1)
                            JOptionPane.showMessageDialog(null, "Errore interno durante logout", "Errore", JOptionPane.ERROR_MESSAGE);
                        else System.out.println("[CLIENT] Logout success, bye!\n");

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    dashWindow.setVisible(false);
                    mainWindow.getDefaultCloseOperation();
                    System.exit(0);

                });

    }


    public void clientDashboardPanel(final Client clientProcessor){

        final JDialog dashWindow = new JDialog(mainWindow, "WORTH - Dashboard", true);
        dashWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        dashWindow.setResizable(false);

        final String chatHistory =  "";


        //construct preComponents
        JMenu accountMenu = new JMenu ("Account");
        JMenuItem logoutItem = new JMenuItem ("Logout");
        accountMenu.add (logoutItem);


        JMenu userMenu = new JMenu ("Worth users");
        JMenuItem showOnlineUsersItem = new JMenuItem ("Mostra utenti online");
        userMenu.add (showOnlineUsersItem);
        JMenuItem showAllUsers = new JMenuItem ("Mostra utenti");
        userMenu.add (showAllUsers);

        JMenu chatMenu = new JMenu ("Worth chat");
        JMenuItem projectChat = new JMenuItem ("Project chat");
        chatMenu.add (projectChat);



        //construct components
        showProjectMenuButton = new JButton ("Mostra dashboard progetto");
        createNewProjectButton = new JButton ("Crea nuovo progetto");
        welcomeLabel = new JLabel ("Hey, benvenuto " + authenticatedUsername + "!");
        menuBar = new JMenuBar();
        menuBar.add (accountMenu);
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

                final String[] projectMulticastIIP = new String[1];

                final JDialog selectProjectFrame = new JDialog(dashWindow, "Seleziona progetto", true);
                selectProjectFrame.setLocationRelativeTo(null);

                //construct preComponents
                String[] projectWithMulticast = clientProcessor.listProjects(authenticatedUsername,true);
                String[] jcomp1Items = clientProcessor.listProjects(authenticatedUsername,false);
                //HashMap<String,Boolean> multicastGroupsIPJoined;

                if (jcomp1Items == null) {
                    JOptionPane.showMessageDialog(null, "Errore: nessun progetto trovato", "Errore", JOptionPane.ERROR_MESSAGE);
                    selectProjectFrame.setVisible(false);

                } else {

                    //construct components
                    projectListItem = new JComboBox(jcomp1Items);
                    projectListLabel = new JLabel("Seleziona progetto");

                    //adjust size and set layout
                    selectProjectFrame.getContentPane().setPreferredSize(new Dimension(206, 136));
                    selectProjectFrame.getContentPane().setLayout(null);

                    //add components
                    selectProjectFrame.getContentPane().add(projectListItem);
                    selectProjectFrame.getContentPane().add(projectListLabel);



                    //set component bounds (only needed by Absolute Positioning)
                    projectListItem.setBounds(15, 60, 170, 35);
                    projectListLabel.setBounds(15, 25, 175, 25);


                    projectListItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent ae) {

                            selectProjectFrame.setVisible(false);
                            String selectedProject = projectListItem.getSelectedItem().toString();
                            MulticastSocket receiver = null;

                            int projectIndex = 1;
                            for(int i = 0; i < projectWithMulticast.length; i+=2){
                                if(projectWithMulticast[i].compareTo(selectedProject) == 0) projectIndex = i+1;
                            }


                                receiver = clientProcessor.multicastAdd(projectWithMulticast[projectIndex]);
                                projectMulticastIIP[0] = projectWithMulticast[projectIndex];



                            JDialog showChatProjectDialog = new JDialog(mainWindow, "Project " + selectedProject + " chat ", true);
                            showChatProjectDialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

                            String[] userList = clientProcessor.listUsers();
                            String[] memberListComboBoxItems = new String[userList.length];
                            for (int i=0; i < userList.length; i++) {

                                String[] tokens = userList[i].split("[ (]+");
                                memberListComboBoxItems[i] = tokens[0];


                            }
                            String[] userListComboBoxItems = clientProcessor.showMembers(selectedProject);

                            //construct components
                            messageListBoxArea = new JTextArea(  17,51);
                            newMessageField = new JTextField (5);
                            sendMessageButton = new JButton ("Invia messaggio");
                            inviteUserLabel = new JLabel ("Invita utente");
                            memberListComboBox = new JComboBox (memberListComboBoxItems);
                            addMemberButton = new JButton ("Aggiungi utente");
                            userListComboBox = new JComboBox (userListComboBoxItems);
                            memberListLabel = new JLabel ("Membri progetto");
                            messageListBoxArea.append(clientProcessor.getChatHistory(selectedProject));


                            //Scrollable chatbox
                            JPanel chatboxPanel = new JPanel();
                            JScrollPane scroll = new JScrollPane (messageListBoxArea);
                            scroll.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
                            chatboxPanel.add(scroll);


                            //adjust size and set layout
                            showChatProjectDialog.getContentPane().setPreferredSize (new Dimension (675, 507));
                            showChatProjectDialog.getContentPane().setLayout (null);

                            //messageListBoxArea.setSize(400,400);

                            showChatProjectDialog.getContentPane().add(chatboxPanel);

                            //add components
                            //showChatProjectDialog.getContentPane().add (messageListBoxArea);
                            showChatProjectDialog.getContentPane().add (newMessageField);
                            showChatProjectDialog.getContentPane().add (sendMessageButton);
                            showChatProjectDialog.getContentPane().add (inviteUserLabel);
                            showChatProjectDialog.getContentPane().add (memberListComboBox);
                            showChatProjectDialog.getContentPane().add (addMemberButton);
                            showChatProjectDialog.getContentPane().add (userListComboBox);
                            showChatProjectDialog.getContentPane().add (memberListLabel);

                            ChatReceiverThread chatThread = null;

                            if(!clientProcessor.isListenerRegistered(selectedProject)) {

                                chatThread = new ChatReceiverThread(receiver, messageListBoxArea, clientProcessor, selectedProject);
                                clientProcessor.registerListener(selectedProject,chatThread);
                                chatThread.start();
                            } else clientProcessor.getListenerThread(selectedProject).updateBoxArea(messageListBoxArea);



                            //set component bounds (only needed by Absolute Positioning)
                            chatboxPanel.setBounds (40, 130, 585, 265);
                            newMessageField.setBounds (40, 405, 585, 30);
                            sendMessageButton.setBounds (360, 450, 265, 30);
                            inviteUserLabel.setBounds (445, 25, 100, 25);
                            memberListComboBox.setBounds (445, 50, 185, 30);
                            addMemberButton.setBounds (445, 85, 185, 25);
                            userListComboBox.setBounds (50, 55, 205, 30);
                            memberListLabel.setBounds (50, 25, 200, 25);
                            messageListBoxArea.setForeground(Color.black);

                            messageListBoxArea.setEnabled(false);

                            MulticastSocket finalReceiver = receiver;
                            sendMessageButton.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent ae) {

                                    String message = newMessageField.getText();
                                    newMessageField.setText("");
                                    InetAddress group = null;
                                    try {
                                            group = InetAddress.getByName(projectMulticastIIP[0]);

                                        byte[] data = (authenticatedUsername +  ": "+message).getBytes();
                                        DatagramPacket dpsend = new DatagramPacket(data, data.length, group, 6789);

                                        int ttl = 0;

                                            ttl = finalReceiver.getTimeToLive();
                                            finalReceiver.setTimeToLive(10);
                                            finalReceiver.send(dpsend);
                                            finalReceiver.setTimeToLive(ttl);

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });

                            addMemberButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent actionEvent) {
                                    String newMember = memberListComboBox.getSelectedItem().toString();
                                    Integer response = clientProcessor.addMember(selectedProject, newMember);

                                    if(response == 1) JOptionPane.showMessageDialog(null, "Membro giá parte del progetto", "Errore", JOptionPane.ERROR_MESSAGE);
                                    else if(response == 2){
                                        JOptionPane.showMessageDialog(null, "Progetto eliminato da un altro utente", "Errore", JOptionPane.ERROR_MESSAGE);
                                        showChatProjectDialog.setVisible(false);
                                    }
                                    else if(response == 0)  {
                                        InetAddress group = null;
                                        try {
                                            group = InetAddress.getByName(projectMulticastIIP[0]);
                                            String message = "ho aggiunto " + newMember  +  " al progetto!";
                                            byte[] data = (authenticatedUsername +  ": "+message).getBytes();
                                            DatagramPacket dpsend = new DatagramPacket(data, data.length, group, 6789);

                                            int ttl = 0;

                                            ttl = finalReceiver.getTimeToLive();
                                            finalReceiver.setTimeToLive(10);
                                            finalReceiver.send(dpsend);
                                            finalReceiver.setTimeToLive(ttl);

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                    else JOptionPane.showMessageDialog(null, "Errore sconosciuto, azione non eseguita!", "Errore", JOptionPane.ERROR_MESSAGE);


                                }
                            });

                            showChatProjectDialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                            showChatProjectDialog.pack();
                            showChatProjectDialog.setLocationRelativeTo(null);
                            showChatProjectDialog.setVisible(true);

                        }
                    });


                    selectProjectFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                    selectProjectFrame.pack();
                    selectProjectFrame.setVisible(true);

                }



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
                    if(clientProcessor.logout(authenticatedUsername, true) == 1)
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
                newProjectWindow.setResizable(false);

                //construct components
                createProjectButton = new JButton ("Crea progetto");
                projectTitleField = new JTextField (8);
                projectTitleLabel = new JLabel ("Titolo progetto");

                createProjectButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        projectTitleField.getText();
                        if(projectTitleField.getText().isEmpty() || projectTitleField.getText().isBlank() || projectTitleField.getText().contains("#"))
                            JOptionPane.showMessageDialog(null, "Titolo progetto vuoto!", "Errore", JOptionPane.ERROR_MESSAGE);
                        else {
                            int serverResponse = clientProcessor.createProject(projectTitleField.getText());
                            switch (serverResponse){

                                case 0:
                                    JOptionPane.showMessageDialog(null, "Progetto creato correttamente!", "Ok", JOptionPane.DEFAULT_OPTION);
                                    newProjectWindow.setVisible(false);
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


                final JDialog selectProjectFrame = new JDialog(dashWindow, "Seleziona progetto", true);
                selectProjectFrame.setLocationRelativeTo(null);
                selectProjectFrame.setResizable(false);

                //construct preComponents
                String[] jcomp1Items = clientProcessor.listProjects(authenticatedUsername,false);
                //String[] jcomp1Items = {"Item 1", "Item 2", "Item 3"};
                if (jcomp1Items==null) {
                    JOptionPane.showMessageDialog(null, "Errore: nessun progetto trovato", "Errore", JOptionPane.ERROR_MESSAGE);
                    selectProjectFrame.setVisible(false);

                } else {

                    //construct components
                    projectListItem = new JComboBox(jcomp1Items);
                    projectListLabel = new JLabel("Seleziona progetto");

                    //adjust size and set layout
                    selectProjectFrame.getContentPane().setPreferredSize(new Dimension(206, 136));
                    selectProjectFrame.getContentPane().setLayout(null);

                    //add components
                    selectProjectFrame.getContentPane().add(projectListItem);
                    selectProjectFrame.getContentPane().add(projectListLabel);

                    //set component bounds (only needed by Absolute Positioning)
                    projectListItem.setBounds(15, 60, 170, 35);
                    projectListLabel.setBounds(15, 25, 175, 25);


                    projectListItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent ae) {
                            String selectedProject = projectListItem.getSelectedItem().toString();

                            selectProjectFrame.setVisible(false);

                            JDialog frame = new JDialog(mainWindow, "Dashboard progetto " + selectedProject, true);
                            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                            frame.setResizable(false);
                            JTabbedPane tabbedPane = new JTabbedPane();

                            JLabel cardTitleLabel = new JLabel("Titolo card");
                            JTextField cardTitleField = new JTextField(5);
                            JLabel cardDescLabel = new JLabel("Descrizione card");
                            JTextArea cardDescField = new JTextArea(5, 5);
                            JButton sendRequestButton = new JButton("Send request");


                            //set component bounds (only needed by Absolute Positioning)
                            cardTitleLabel.setBounds(60, 190, 100, 25);
                            cardTitleField.setBounds(60, 220, 385, 25);
                            cardDescLabel.setBounds(60, 260, 160, 25);
                            cardDescField.setBounds(60, 285, 380, 75);
                            sendRequestButton.setBounds(620, 320, 195, 45);

                            //Create the "cards".
                            JPanel card1 = new JPanel();

                            JPanel card2 = new JPanel();

                            JPanel card3 = new JPanel();

                            JPanel card4 = new JPanel();

                            JPanel card5 = new JPanel();

                            ClientMenuGUI showCardPanel = new ClientMenuGUI(clientProcessor, selectedProject).ClientShowCardsGUI(mainWindow,frame);
                            card1.add(new ClientMenuGUI(clientProcessor, selectedProject).ClientRemoveCardGUI(frame));
                            card2.add(new ClientMenuGUI(clientProcessor, selectedProject).ClientAddCardGUI(card3,card4,card5, mainWindow,dashWindow, frame));
                            card3.add(showCardPanel);
                            card4.add(new ClientMenuGUI(clientProcessor, selectedProject).ClientMoveCardGUI(frame));
                            card5.add(new ClientMenuGUI(clientProcessor, selectedProject).ClientShowCardHistoryGUI(dashWindow, frame));


                            tabbedPane.addTab("Cancella progetto", card1);
                            tabbedPane.addTab("Aggiungi card", card2);
                            tabbedPane.addTab("Mostra card", card3);
                            tabbedPane.addTab("Sposta card", card4);
                            tabbedPane.addTab("Mostra cronologia card", card5);


                            frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);


                            //Display the window.
                            frame.setPreferredSize(new Dimension(925, 318));
                            frame.pack();
                            frame.setLocationRelativeTo(null);
                            frame.setVisible(true);
                        }


                    });


                    selectProjectFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                    selectProjectFrame.pack();
                    selectProjectFrame.setVisible(true);


                }
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

                if (registerUsername.isEmpty() || registerUsername.isBlank() || registerUsername.contains("#"))
                    JOptionPane.showMessageDialog(null, "Errore: campo username vuoto", "Errore", JOptionPane.ERROR_MESSAGE);
                else if (registerPassword.isEmpty() || registerPassword.isBlank() ||  registerPassword.contains("#"))
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
        login.setResizable(false);

        login.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {

                int status = 1;
                try {
                    if(authenticatedUsername != null) status = clientProcessor.logout(authenticatedUsername, true);
                    else status = status = clientProcessor.logout(authenticatedUsername, false);


                    if (status == 1)
                        JOptionPane.showMessageDialog(null, "Errore interno durante logout", "Errore", JOptionPane.ERROR_MESSAGE);
                    else System.out.println("[CLIENT] Logout success, bye!\n");

                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }

                mainWindow.dispose();
                System.exit(0);
            }
        });


        //construct preComponents
        JMenu infoMenu = new JMenu("Info");
        JMenuItem option_1Item = new JMenuItem("Relazione");
        infoMenu.add(option_1Item);
        JMenuItem option_2Item = new JMenuItem("GitHub");
        infoMenu.add(option_2Item);



        //construct components
        register_button = new JButton("Register");
        login_button = new JButton("Login");
        username_field = new JTextField(5);
        jcomp4 = new JLabel("Username");
        password_field = new JPasswordField(5);
        jcomp6 = new JLabel("Password");
        jcomp7 = new JMenuBar();
        jcomp7.add(infoMenu);

        option_1Item.addActionListener(new ActionListener() {
                                           @Override
                                           public void actionPerformed(ActionEvent actionEvent) {
                                               try{
                                                   String url = "https://github.com/TheDarkPyotr/RCL-Project2021/blob/main/Relazione-WORTH.pdf";
                                                   java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
                                               }
                                               catch (java.io.IOException e) {
                                                   System.out.println(e.getMessage());
                                               }
                                           }
                                       }
        );

        option_2Item.addActionListener(new ActionListener() {
                                           @Override
                                           public void actionPerformed(ActionEvent actionEvent) {
                                               try{
                                                   String url = "https://github.com/TheDarkPyotr/RCL-Project2021/";
                                                   java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
                                               }
                                               catch (java.io.IOException e) {
                                               }
                                           }
                                       }
        );


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

                if (username.isEmpty() || username.contains("#"))
                    JOptionPane.showMessageDialog(null, "Errore: campo username vuoto", "Errore", JOptionPane.ERROR_MESSAGE);
                else if (password.isEmpty() || password.contains("#"))
                    JOptionPane.showMessageDialog(null, "Errore: campo password vuoto", "Errore", JOptionPane.ERROR_MESSAGE);
                else {
                    try {
                        String response = clientProcessor.login(username,password);

                        if (response.compareTo("USERNAME-ERROR") == 0)
                            JOptionPane.showMessageDialog(null, "Errore: username non valido", "Errore", JOptionPane.ERROR_MESSAGE);
                        else if (response.compareTo("PASSWORD-ERROR") == 0)
                            JOptionPane.showMessageDialog(null, "Errore: password errata", "Errore", JOptionPane.ERROR_MESSAGE);
                        else if (response.compareTo("ONLINE-ERROR") == 0)
                            JOptionPane.showMessageDialog(null, "Errore: utente giá loggato", "Errore", JOptionPane.ERROR_MESSAGE);
                        else {

                            authenticatedUsername = username;
                            authenticatedPasswrod = password;
                            login.setVisible(false);
                            clientDashboardPanel(clientProcessor);
                        }


                    } catch (NotBoundException e) {
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

    private void appendToPane(JTextPane tp, String msg, Color c)
    {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);
        tp.replaceSelection(msg);
    }

}

