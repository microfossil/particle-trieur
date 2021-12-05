package particletrieur.models.taxonomy;

public class RappTaxon {

    public int id;
    public String type;
    public String group;
    public String name;

    public RappTaxon(int id, String type, String group, String name) {
        this.id = id;
        this.type = type;
        this.group = group;
        this.name = name;
    }
}
