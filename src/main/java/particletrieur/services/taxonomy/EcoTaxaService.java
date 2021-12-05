package particletrieur.services.taxonomy;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import particletrieur.models.taxonomy.EcoTaxaSearchResult;
import particletrieur.models.taxonomy.EcoTaxaTaxon;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class EcoTaxaService {

    public static EcoTaxaTaxon getTaxonById(int id) throws IOException {
        HttpGet httpReq = new HttpGet("https://ecotaxa.obs-vlfr.fr/api/taxon/" + id);
        System.out.println("\nRequested URI is :\n"+httpReq.getURI());
        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse response = httpClient.execute(httpReq);
        //Check if getting data successfully or not. If fails, print error then exit from the method
//        if(!Utils.isSuccessRequestPrnError(response, "Request data failed:"))
//            return;
        //Get the response which is in the first index of JSON Array
        String jsonMsg = EntityUtils.toString(response.getEntity());
//        JSONObject jsonArray = new JSONObject(jsonMsg);
//        String jsonResp = jsonArray.get(0).toString();
        //print the response HTTP code 200
        //System.out.println(jsonResp);

        System.out.println("Request is processed successfully.");
        //Jackson ObjectMapper reads HistoricalPricingEvents class and histocial pricing events JSON response
        //to create an object representing the parsed JSON.
        EcoTaxaTaxon taxon = new ObjectMapper().readerFor(EcoTaxaTaxon.class).readValue(jsonMsg);

        return taxon;
    }

    public static List<EcoTaxaSearchResult> searchTaxons(String searchText) throws IOException {
        HttpGet httpReq = new HttpGet("https://ecotaxa.obs-vlfr.fr/api/taxon_set/search?query=" + searchText);
        System.out.println("\nRequested URI is :\n"+httpReq.getURI());
        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse response = httpClient.execute(httpReq);
        //Check if getting data successfully or not. If fails, print error then exit from the method
//        if(!Utils.isSuccessRequestPrnError(response, "Request data failed:"))
//            return;
        String jsonMsg = EntityUtils.toString(response.getEntity());
        EcoTaxaSearchResult[] results = new ObjectMapper().readerFor(EcoTaxaSearchResult[].class).readValue(jsonMsg);
        return Arrays.asList(results);
    }

    public static Service<List<EcoTaxaSearchResult>> searchTaxonsService(String searchText) {
        Service<List<EcoTaxaSearchResult>> service = new Service<List<EcoTaxaSearchResult>>() {
            @Override
            protected Task<List<EcoTaxaSearchResult>> createTask() {
                return new Task<List<EcoTaxaSearchResult>>() {
                    @Override
                    protected List<EcoTaxaSearchResult> call() throws IOException {
                        return searchTaxons(searchText);
                    };
                };
            }
        };
        return service;
    }

    public static Service<EcoTaxaTaxon> getTaxonByIdService(int id) {
        Service<EcoTaxaTaxon> service = new Service<EcoTaxaTaxon>() {
            @Override
            protected Task<EcoTaxaTaxon> createTask() {
                return new Task<EcoTaxaTaxon>() {
                    @Override
                    protected EcoTaxaTaxon call() throws IOException {
                        return getTaxonById(id);
                    };
                };
            }
        };
        return service;
    }
}
