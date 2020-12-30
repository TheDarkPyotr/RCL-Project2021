import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.event.*;

public class ClientMenuGUI extends JPanel {

    private Client client;
    private String project;
    private JLabel jcomp1;
    private JButton jcomp2;
    private JLabel jcomp3;
    private JLabel jcomp4;
    private JLabel jcomp5;
    private JPanel removeCardComponent;
    private JPanel addCardComponent;

    private JLabel jcomp11;
    private JTextField jcomp21;
    private JLabel jcomp31;
    private JTextArea jcomp41;
    private JButton jcomp51;

    private JLabel cardListLabel;
    private JList cardList;

    private JLabel showCardDescLabel;
    private JTextArea showCardDescArea;

    private JComboBox cardBox;
    private JComboBox stateBox;
    private JLabel selectCardLabel;
    private JLabel changeCardStateLabel;
    private JButton saveCardStateButton;

    private JButton showHistoryButton;
    private JList historyList;




    public ClientMenuGUI(Client clientProcessor, String project){

        this.client = clientProcessor;
        this.project = project;

    }
    public ClientMenuGUI ClientRemoveCardGUI(final JDialog dashWindow) {

        removeCardComponent = new JPanel();
        //construct components
       String[] projectMembers = client.showMembers(project);

        jcomp1 = new JLabel ("Eliminazione progetto " + project);
        jcomp2 = new JButton ("Elimina");
        jcomp3 = new JLabel ("Totale cards: " );
        jcomp4 = new JLabel ("Totale membri:"+ projectMembers.length);
        jcomp5 = new JLabel ("Sei sicuro di voler elimina il progetto " + project +  "?");

        //adjust size and set layout
        setPreferredSize (new Dimension (905, 241));
        setLayout (null);

        //add components
        add (jcomp1);
        add (jcomp2);
        add (jcomp3);
        add (jcomp4);
        add (jcomp5);

        jcomp2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                if(client.cancelProject(project) == 1) JOptionPane.showMessageDialog(null, "Errore: Cards non ancora ultimate", "Errore: impossibile eliminare " + project, JOptionPane.ERROR_MESSAGE);
                else {
                    JOptionPane.showMessageDialog(null, "Progetto eliminato", "Progetto eliminato con successo" + project, JOptionPane.ERROR_MESSAGE);
                dashWindow.setVisible(false);
                }
            }
        });

        //set component bounds (only needed by Absolute Positioning)
        jcomp1.setBounds (15, 15, 800, 25);
        jcomp2.setBounds (15, 160, 165, 40);
        jcomp3.setBounds (15, 50, 100, 25);
        jcomp4.setBounds (15, 75, 115, 25);
        jcomp5.setBounds (15, 100, 800, 25);

        return this;


    }


    public ClientMenuGUI ClientAddCardGUI(JPanel cardListPanel, JPanel moveCardPanel, JPanel historyPanel, JFrame mainWindow, JDialog dashWindow, JDialog projectWindow){


        jcomp11 = new JLabel ("Titolo card");
        jcomp21 = new JTextField (5);
        jcomp31 = new JLabel ("Descrizione card");
        jcomp41 = new JTextArea();
        jcomp51 = new JButton ("Invia richiesta");
        JScrollBar scrollBar = new JScrollBar(Scrollbar.VERTICAL);

        //adjust size and set layout
        setPreferredSize (new Dimension (908, 193));
        setLayout (null);

        //add components
        add (jcomp11);
        add (jcomp21);
        add (jcomp31);
        add (jcomp41);
        add (jcomp51);
        add(scrollBar);

        jcomp51.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                String cardName = jcomp21.getText();
                String cardDesc = jcomp41.getText();

                if(cardName.isEmpty())   JOptionPane.showMessageDialog(null, "Titolo card vuoto!", "Errore", JOptionPane.ERROR_MESSAGE);
                    else if(cardDesc.isEmpty())   JOptionPane.showMessageDialog(null, "Descrizione card assente!", "Errore", JOptionPane.ERROR_MESSAGE);
                    else {
                        int response = client.addCard(project, cardName, cardDesc);
                        if(response == 0){
                            JOptionPane.showMessageDialog(null, "Card inserita con successo", "OK", JOptionPane.ERROR_MESSAGE);

                            //Update show cards
                            cardListPanel.removeAll();
                            cardListPanel.add (new ClientMenuGUI(client, project).ClientShowCardsGUI(mainWindow));

                            //Update move cards
                            moveCardPanel.removeAll();
                            moveCardPanel.add (new ClientMenuGUI(client, project).ClientMoveCardGUI(projectWindow));

                            //Update card history
                            historyPanel.removeAll();
                            historyPanel.add (new ClientMenuGUI(client, project).ClientShowCardHistoryGUI(dashWindow,projectWindow));



                        } else if(response == 2) {
                            JOptionPane.showMessageDialog(null, "Progetto eliminato!", "ERRORE", JOptionPane.ERROR_MESSAGE);
                            projectWindow.setVisible(false);
                        }
                            else JOptionPane.showMessageDialog(null, "Card giá esistente!", "ERRORE", JOptionPane.ERROR_MESSAGE);

                        }
                }
        });

        //set component bounds (only needed by Absolute Positioning)
        jcomp11.setBounds (50, 20, 100, 25);
        jcomp21.setBounds (50, 45, 275, 25);
        jcomp31.setBounds (50, 85, 600, 25);
        jcomp41.setBounds (50, 110, 515, 65);
        jcomp51.setBounds (635, 125, 145, 40);

        return this;

    }



    public ClientMenuGUI ClientShowCardsGUI(final JFrame mainWindow){


            //construct preComponents
            String[] cardListComponent = client.showCards(project);

            //String[] cardListComponent = {"ToDoMEME - Todo", "Progetto RCL - Done", "Lol - Done"};

            //construct components
            cardListLabel = new JLabel ("Lista cards");
            if(cardListComponent[0].compareTo("NO-CARDS-ERROR") == 0) cardListComponent[0] = "";
            cardList = new JList (cardListComponent );

            //adjust size and set layout
            setPreferredSize (new Dimension (908, 236));
            setLayout (null);


        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                JList theList = (JList) mouseEvent.getSource();
                if (mouseEvent.getClickCount() == 2) {
                    int index = theList.locationToIndex(mouseEvent.getPoint());
                    if (index >= 0) {
                        Object o = theList.getModel().getElementAt(index);
                        if (o.toString().compareTo("") != 0) {
                            String[] cardInfo = client.showCard(project, o.toString());


                            final JDialog showCardDesc = new JDialog(mainWindow, "Descrizione card " + o.toString(), true);


                            //construct components
                            showCardDescLabel = new JLabel("Stato card: " + cardInfo[0]);
                            showCardDescArea = new JTextArea(5, 5);

                            //adjust size and set layout
                            showCardDesc.getContentPane().setPreferredSize(new Dimension(347, 236));
                            showCardDesc.getContentPane().setLayout(null);

                            //add components
                            showCardDesc.getContentPane().add(showCardDescLabel);
                            showCardDesc.getContentPane().add(showCardDescArea);

                            //set component bounds (only needed by Absolute Positioning)
                            showCardDescLabel.setBounds(15, 10, 300, 25);
                            showCardDescArea.setBounds(15, 40, 315, 175);
                            showCardDescArea.setEnabled(false);
                            showCardDescArea.setText(cardInfo[1]);

                            showCardDesc.setLocationRelativeTo(null);

                            showCardDesc.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                            showCardDesc.pack();
                            showCardDesc.setVisible(true);


                        }
                    }
                }
            }
        };
        cardList.addMouseListener(mouseListener);

            //add components
            add (cardListLabel);
            add (cardList);

            //set component bounds (only needed by Absolute Positioning)
            cardListLabel.setBounds (15, 10, 100, 25);
            cardList.setBounds (15, 35, 865, 160);



        return this;
    }

    public ClientMenuGUI ClientMoveCardGUI(JDialog projectWindow){


            //construct preComponents
            String[] jcomp1Items = client.showCards(project);
            String[] jcomp2Items = {"Todo", "In progress", "To be revised", "Done"};

            //construct components
            if(jcomp1Items[0].compareTo("NO-CARDS-ERROR") == 0) jcomp1Items[0] = "";
            cardBox = new JComboBox (jcomp1Items);
            stateBox = new JComboBox (jcomp2Items);
            selectCardLabel = new JLabel ("Seleziona card");
            changeCardStateLabel = new JLabel ("Nuova lista");
            saveCardStateButton = new JButton ("Salva modifiche");

            //adjust size and set layout
            setPreferredSize (new Dimension (908, 186));
            setLayout (null);

            //add components
            add (cardBox);
            add (stateBox);
            add (selectCardLabel);
            add (changeCardStateLabel);
            add(saveCardStateButton);

            saveCardStateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {

                    if (cardBox.getSelectedItem().toString().compareTo("") != 0) {
                        Integer response = client.moveCard(project, cardBox.getSelectedItem().toString(), stateBox.getSelectedItem().toString());
                        switch (response) {
                            case 0:
                                JOptionPane.showMessageDialog(null, "Modifica salvata con successo", "OK", JOptionPane.ERROR_MESSAGE);
                                break;
                            case 1:
                                JOptionPane.showMessageDialog(null, "Card giá in stato " + stateBox.getSelectedItem().toString(), "ERRORE", JOptionPane.ERROR_MESSAGE);
                                break;
                            case 2:
                                JOptionPane.showMessageDialog(null, "Stato non congruo", "ERRORE", JOptionPane.ERROR_MESSAGE);
                                break;
                            case 3:
                                JOptionPane.showMessageDialog(null, "Errore interno, riprova piú tardi", "INTERNAL SERVER ERROR", JOptionPane.ERROR_MESSAGE);
                                break;
                            case 4:
                                JOptionPane.showMessageDialog(null, "Progetto eliminato da un altro utente!", "Impossibile completare l´operazione", JOptionPane.ERROR_MESSAGE);
                                projectWindow.setVisible(false);
                                break;
                        }
                    }
                }
            });

            //set component bounds (only needed by Absolute Positioning)
            cardBox.setBounds (30, 60, 280, 25);
            stateBox.setBounds (30, 120, 280, 25);
            selectCardLabel.setBounds (30, 35, 140, 25);
            changeCardStateLabel.setBounds (30, 95, 100, 25);
            saveCardStateButton.setBounds (700, 115, 165, 35);

        return this;
    }

    public ClientMenuGUI ClientShowCardHistoryGUI(final JDialog mainWindow, JDialog projectWindow){

            //construct preComponents
            String[] jcomp1Items = client.showCards(project);
            if(jcomp1Items[0].compareTo("NO-CARDS-ERROR") == 0) jcomp1Items[0] = "";
            //construct components
            cardBox = new JComboBox (jcomp1Items);
            selectCardLabel = new JLabel ("Seleziona card");
            showHistoryButton = new JButton ("Mostra history");

            //adjust size and set layout
            setPreferredSize (new Dimension (908, 186));
            setLayout (null);

            //add components
            add (cardBox);
            add (selectCardLabel);
            add (showHistoryButton);

            //set component bounds (only needed by Absolute Positioning)
            cardBox.setBounds (30, 60, 280, 25);
            selectCardLabel.setBounds (30, 35, 140, 25);
            showHistoryButton.setBounds (30, 120, 165, 35);


        showHistoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {

                String selectedCard = cardBox.getSelectedItem().toString();
                if(selectedCard.compareTo("") != 0){
                JDialog cardHistoryDialog = new JDialog (mainWindow,"History card " + selectedCard, true);
                cardHistoryDialog.setDefaultCloseOperation (JFrame.HIDE_ON_CLOSE);
                    //construct preComponents
                    String[] jcomp1Items = client.getCardHistory(project,selectedCard);
                    if(jcomp1Items == null) {
                        JOptionPane.showMessageDialog(null, "Progetto eliminato da un altro utente!", "Impossibile completare l´operazione", JOptionPane.ERROR_MESSAGE);
                        projectWindow.setVisible(false);
                    } else {
                            //{"Todo - 12/12/2020 09:45 ", "Toberevised - 12/11/2020 11:45", "Done - 25/12/2020 11:55 "};

                    //construct components
                    historyList = new JList (jcomp1Items);

                    //adjust size and set layout
                    cardHistoryDialog.setPreferredSize (new Dimension (614, 431));
                    cardHistoryDialog.setLayout (null);

                    //add components
                    cardHistoryDialog.getContentPane().add (historyList);

                    //set component bounds (only needed by Absolute Positioning)
                    historyList.setBounds (10, 15, 570, 390);



                cardHistoryDialog.pack();
                cardHistoryDialog.setVisible (true);

            }}}});

        return this;
    }
}


