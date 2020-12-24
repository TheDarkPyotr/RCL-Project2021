import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
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
    public ClientMenuGUI ClientRemoveCardGUI() {

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

        //set component bounds (only needed by Absolute Positioning)
        jcomp1.setBounds (15, 15, 800, 25);
        jcomp2.setBounds (15, 160, 165, 40);
        jcomp3.setBounds (15, 50, 100, 25);
        jcomp4.setBounds (15, 75, 115, 25);
        jcomp5.setBounds (15, 100, 800, 25);

        return this;


    }


    public ClientMenuGUI ClientAddCardGUI(){


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
            String[] cardListComponent = {"ToDoMEME - Todo", "Progetto RCL - Done", "Lol - Done"};

            //construct components
            cardListLabel = new JLabel ("Lista cards");
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



                        final JDialog showCardDesc = new JDialog(mainWindow, "Descrizione card" + o.toString(), true);


                            //construct components
                        showCardDescLabel = new JLabel ("Descrizione card " + o.toString());
                        showCardDescArea = new JTextArea (5, 5);

                        //adjust size and set layout
                        showCardDesc.setPreferredSize (new Dimension (347, 236));
                        showCardDesc.setLayout (null);

                            //add components
                        showCardDesc.getContentPane().add (showCardDescLabel);
                        showCardDesc.getContentPane().add (showCardDescArea);

                            //set component bounds (only needed by Absolute Positioning)
                        showCardDescLabel.setBounds (15, 10, 300, 25);
                        showCardDescArea.setBounds (15, 40, 315, 175);
                        showCardDescArea.setEnabled(false);
                        showCardDescArea.setText("qui va la descrizione della card");

                        showCardDesc.setLocationRelativeTo(null);

                        showCardDesc.setDefaultCloseOperation (JFrame.HIDE_ON_CLOSE);
                        showCardDesc.pack();
                        showCardDesc.setVisible (true);



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

    public ClientMenuGUI ClientMoveCardGUI(){


            //construct preComponents
            String[] jcomp1Items = {"Meme - Todo", "RCL - Done", "SERVER - Todo"};
            String[] jcomp2Items = {"Todo", "In progress", "To be revised", "Done"};

            //construct components
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

            //set component bounds (only needed by Absolute Positioning)
            cardBox.setBounds (30, 60, 280, 25);
            stateBox.setBounds (30, 120, 280, 25);
            selectCardLabel.setBounds (30, 35, 140, 25);
            changeCardStateLabel.setBounds (30, 95, 100, 25);
            saveCardStateButton.setBounds (700, 115, 165, 35);

        return this;
    }

    public ClientMenuGUI ClientShowCardHistoryGUI(final JDialog mainWindow){

            //construct preComponents
            String[] jcomp1Items = {"Meme - Todo", "RCL - Done", "SERVER - Todo"};

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

        cardBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String selectedValue = cardBox.getSelectedItem().toString();
                JOptionPane.showMessageDialog(null, "Hai selezionato " + selectedValue, "Errore", JOptionPane.ERROR_MESSAGE);


            }
        });

        showHistoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {


                JDialog cardHistoryDialog = new JDialog (mainWindow,"MyPanel", true);
                cardHistoryDialog.setDefaultCloseOperation (JFrame.HIDE_ON_CLOSE);
                    //construct preComponents
                    String[] jcomp1Items = {"Todo - 12/12/2020 09:45 ", "Toberevised - 12/11/2020 11:45", "Done - 25/12/2020 11:55 "};

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

            }});

        return this;
    }
}
