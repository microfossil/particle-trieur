package particletrieur.models.project;
import javafx.scene.control.TreeCell;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class TreeTaxon {

    public static String[] validGroups = new String [] {"Superdomain", "Kingdom", "Phylum", "Class", "Ordo", "Family", "Genus", "Species"};
    public static String[] groupCodes = new String[] {"dom", "kng", "phy", "cls", "ord", "fam", "gen", "sp"};

    public String group;
    public String name;
    public String groupCode;
    public LinkedHashMap<String, TreeTaxon> children;

    public TreeTaxon(String group, String name) {
        this.group = group;
        this.name = name;
        int idx = Arrays.asList(validGroups).indexOf(group);
        if (idx > -1) {
            this.name += " (" + groupCodes[idx] + ")";
        }
        children = new LinkedHashMap<>();
    }

    public TreeTaxon addChild(String group, String name) {
        if (children.containsKey(name)) {
            return children.get(name);
        }
        else {
            TreeTaxon t = new TreeTaxon(group, name);
            children.put(name, t);
            return t;
        }
    }

    public void addList(List<Pair<String, String>> list) {
        TreeTaxon t = this;
        for (Pair<String, String> item : list) {
            t = t.addChild(item.getLeft(), item.getRight());
        }
    }
}
