package particletrieur.models.ecotaxa;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

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
