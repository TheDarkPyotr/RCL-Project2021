import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Project{

    private String nome;
    private ArrayList<String> userList;
    private ArrayList<Card> cardList;
    private String multicastIP;

    public Project(String name, String authorName){

        this.nome=name;
        userList = new ArrayList<>();
        userList.add(authorName);
    }

    public String getProjectName(){

        return this.nome;
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



}
