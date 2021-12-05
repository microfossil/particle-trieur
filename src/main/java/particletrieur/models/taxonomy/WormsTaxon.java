package particletrieur.models.taxonomy;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

// import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
// import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
/* ObjectMapper om = new ObjectMapper();
Root root = om.readValue(myJsonString), Root.class); */
public class WormsTaxon{
    @JsonProperty("AphiaID")
    public int aphiaID;
    public String url;
    public String scientificname;
    public String authority;
    public String status;
    public Object unacceptreason;
    public int taxonRankID;
    public String rank;
    public int valid_AphiaID;
    public String valid_name;
    public String valid_authority;
    public int parentNameUsageID;
    public String kingdom;
    public String phylum;
    @JsonProperty("class")
    public String _class;
    public String order;
    public String family;
    public String genus;
    public String citation;
    public String lsid;
    public int isMarine;
    public Object isBrackish;
    public Object isFreshwater;
    public int isTerrestrial;
    public Object isExtinct;
    public String match_type;
    public Date modified;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("id: %s\n", aphiaID));
        sb.append(String.format("rank: %s\n", rank));
        sb.append(String.format("status: %s\n", status));
        sb.append(String.format("authority: %s\n", authority));
        sb.append(String.format("kingdom: %s\n", kingdom));
        sb.append(String.format("phylum: %s\n", phylum));
        sb.append(String.format("class: %s\n", _class));
        sb.append(String.format("order: %s\n", order));
        sb.append(String.format("family: %s\n", family));
        sb.append(String.format("genus: %s\n", genus));
        return sb.toString();
    }
}
