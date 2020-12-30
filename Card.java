import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class Card {

    private String titolo;
    private String descrizione;
    private ArrayList<String> history;


    public Card(String titolo, String descrizione){

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        this.titolo = titolo;
        this.descrizione = descrizione;
        history = new ArrayList<>();
        history.add("To do " + formatter.format(date));

    }

    public Card(){ } //for JSON deserialization

    @JsonIgnoreProperties
    @JsonIgnore
    public String getLastHistory(){

        String lastStatus = history.get(history.size()-1);
        if(lastStatus.contains("To do")) return "To do";
        if(lastStatus.contains("In progress")) return "In progress";
        if(lastStatus.contains("To be revised")) return "To be revised";
        if(lastStatus.contains("Done")) return "Done";
        return null;
    }


    public String getTitolo(){
        return this.titolo;
    }

    public String getDescrizione(){

        return this.descrizione;
    }

    public ArrayList<String> getHistory(){
        return this.history;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(titolo, card.titolo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(titolo, descrizione);
    }



    public void updateHistory(String status){
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        history.add(status + " " + formatter.format(date));

    }
}
