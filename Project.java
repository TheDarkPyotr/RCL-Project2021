import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Project{

    private String nome;
    private ArrayList<String> userList;
    private ArrayList<Card> toDoList;
    private ArrayList<Card> inProgressList;
    private ArrayList<Card> toBeRevisedList;
    private ArrayList<Card> doneList;
    private String multicastIP;

    private Object listLock = null;


    public Project(String name, String authorName, String multicastIP){

        this.nome=name;
        userList = new ArrayList<>();
        userList.add(authorName);
        listLock = new Object();
        toDoList = new ArrayList<>();
        inProgressList = new ArrayList<>();
        toBeRevisedList = new ArrayList<>();
        doneList = new ArrayList<>();
        this.multicastIP = multicastIP;
        System.out.println("[SERVER] Project " + name +  " multicast IP assigned " + multicastIP);

    }

    public Project(String name, String multicastIP){

        this.nome=name;
        userList = new ArrayList<>();
        listLock = new Object();
        toDoList = new ArrayList<>();
        inProgressList = new ArrayList<>();
        toBeRevisedList = new ArrayList<>();
        doneList = new ArrayList<>();
        this.multicastIP = multicastIP;


    }

    public String toString() {
        return this.nome + "#" + this.userList.toString() + "#" + this.toDoList + "#" + this.inProgressList + "#" + this.toBeRevisedList + "#" + this.doneList + "#" + this.multicastIP;
    }

    public String getMulticastIP(){

        return this.multicastIP;
    }

    public String getProjectName(){

        return this.nome;
    }

    public ArrayList<Card> getToDoList(){
        return this.toDoList;
    }

    public ArrayList<Card> getInProgressList(){
        return this.inProgressList;
    }

    public ArrayList<Card> getToBeRevisedList(){
        return this.toBeRevisedList;
    }

    public ArrayList<Card> getDoneList(){
        return this.doneList;
    }
    public ArrayList<String> getMemberList(){

        return userList;
    }

    public boolean equals(Project o) {
      if(this.nome.compareTo((o.getProjectName())) == 0) return true;
      return false;
    }

    public boolean equals(Object o) {
        if(!(o instanceof Project))
            return false;
        Project other = (Project) o;
        return other.getProjectName().equals(this.nome);
    }

    public boolean done(){

        synchronized (listLock){
            if(toDoList.size() + toBeRevisedList.size() + inProgressList.size() == 0) return true;
        }

        return false;
    }

    public boolean addCard(String cardName, String descrizione){

        synchronized (listLock){
            Card newCard = new Card(cardName,descrizione);
            if(toDoList.contains(newCard) || inProgressList.contains(newCard) || toBeRevisedList.contains(newCard) || doneList.contains(newCard)) return false;
            else {
                toDoList.add(newCard);
                return true;
            }
        }
    }


   public void addToDoList(Card card){

        this.toDoList.add(card);
   }

    public void addInProgressList(Card card){

        this.inProgressList.add(card);
    }


    public void addToBeRevisedList(Card card){

        this.toBeRevisedList.add(card);
    }


    public void addDoneList(Card card){

        this.doneList.add(card);
    }


    public boolean addMember(String memberName){

            if(userList.contains(memberName)) return false;
            return userList.add(memberName);

    }

    public  ArrayList<String> showCards(){

        ArrayList<String> cardList = new ArrayList<>();
        synchronized (listLock){
            for (Card c: toDoList) cardList.add(c.getTitolo());
            for (Card c: inProgressList) cardList.add(c.getTitolo());
            for (Card c: toBeRevisedList) cardList.add(c.getTitolo());
            for (Card c: doneList) cardList.add(c.getTitolo());

            return cardList;

        }
    }

    public Integer moveCard(String cardName, String destinationList){
        /* STATUS CODEX
            return 0 --> ok
            return 1 --> initial and destination list equals
            return 2 --> error
            return 3 --> internal error


         */

        synchronized (listLock){

            ArrayList<String> cardInfo = showCard(cardName);
            if(cardInfo.size() == 0) return 3;
            else {

                switch (destinationList){

                    case  "In progress":
                        if(cardInfo.get(0).compareTo("In progress") == 0) return 1;
                        else if(cardInfo.get(0).compareTo("To do") == 0){
                            int cardIndex = toDoList.indexOf(new Card(cardName, ""));
                            Card tempCard = toDoList.get(cardIndex);
                            tempCard.updateHistory(destinationList);
                            inProgressList.add(tempCard);
                            toDoList.remove(cardIndex);
                            return 0;
                        }
                        else if(cardInfo.get(0).compareTo("To be revised") == 0){
                            int cardIndex = toBeRevisedList.indexOf(new Card(cardName, ""));
                            Card tempCard = toBeRevisedList.get(cardIndex);
                            tempCard.updateHistory(destinationList);
                            inProgressList.add(tempCard);
                            toBeRevisedList.remove(cardIndex);
                            return 0;
                        }

                        break;

                    case  "To be revised":
                        if(cardInfo.get(0).compareTo("To be revised") == 0) return 1;
                        else if(cardInfo.get(0).compareTo("In progress") == 0){
                            int cardIndex = inProgressList.indexOf(new Card(cardName, ""));
                            Card tempCard = inProgressList.get(cardIndex);
                            tempCard.updateHistory(destinationList);
                            toBeRevisedList.add(tempCard);
                            inProgressList.remove(cardIndex);
                            return 0;
                        }
                        break;

                    case  "Done":
                        if(cardInfo.get(0).compareTo("Done") == 0) return 1;
                        else if(cardInfo.get(0).compareTo("In progress") == 0){
                            int cardIndex = inProgressList.indexOf(new Card(cardName, ""));
                            Card tempCard = inProgressList.get(cardIndex);
                            tempCard.updateHistory(destinationList);
                            doneList.add(tempCard);
                            inProgressList.remove(cardIndex);
                            return 0;
                        }
                        else if(cardInfo.get(0).compareTo("To be revised") == 0){
                            int cardIndex = toBeRevisedList.indexOf(new Card(cardName, ""));
                            Card tempCard = toBeRevisedList.get(cardIndex);
                            tempCard.updateHistory(destinationList);
                            doneList.add(tempCard);
                            toBeRevisedList.remove(cardIndex);
                            return 0;
                        }

                        break;
                }


            }
            return 2;

        }

    }

    public ArrayList<String> getCardHistory(String cardName){

        Card findCard = new Card(cardName, "");
        synchronized (listLock){

            for (Card c: toDoList){
                if(c.equals(findCard)) return c.getHistory();
            }

            for (Card c: inProgressList){
                if(c.equals(findCard)) return c.getHistory();
            }
            for (Card c: toBeRevisedList){
                if(c.equals(findCard)) return c.getHistory();
            }
            for (Card c: doneList){
                if(c.equals(findCard)) return c.getHistory();
            }

            return null;


        }

    }


    public  ArrayList<String> showCard(String cardName){

        ArrayList<String> cardInfo = new ArrayList<>();
        Card findCard = new Card(cardName, "");
        synchronized (listLock){
            for (Card c: toDoList){
                    if(c.equals(findCard)) {
                        cardInfo.add("To do");
                        cardInfo.add(c.getDescrizione());
                  }
            }

            for (Card c: inProgressList){
                if(c.equals(findCard)) {
                    cardInfo.add("In progress");
                    cardInfo.add(c.getDescrizione());
                }
            }
            for (Card c: toBeRevisedList){
                if(c.equals(findCard)) {
                    cardInfo.add("To be revised");
                    cardInfo.add(c.getDescrizione());
                }
            }
            for (Card c: doneList){
                if(c.equals(findCard)) {
                    cardInfo.add("Done");
                    cardInfo.add(c.getDescrizione());
                }
            }

            return cardInfo;

        }
    }



}
