package particletrieur.models.taxonomy;

public class EcoTaxaTaxon {

    public int id;
    public int renm_id;
    public String name;
    public String type;
    public int nb_objects;
    public int nb_children_objects;
    public String display_name;
    public String[] lineage;
    public int[] id_lineage;
    public int[] children;

    public EcoTaxaTaxon() {

    }
}
