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
import particletrieur.models.taxonomy.WormsTaxon;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WormsService {

//    public static EcoTaxaTaxon getTaxonById(int id) throws IOException {
//        HttpGet httpReq = new HttpGet("https://ecotaxa.obs-vlfr.fr/api/taxon/" + id);
//        System.out.println("\nRequested URI is :\n"+httpReq.getURI());
//        HttpClient httpClient = HttpClients.createDefault();
//        HttpResponse response = httpClient.execute(httpReq);
//        //Check if getting data successfully or not. If fails, print error then exit from the method
////        if(!Utils.isSuccessRequestPrnError(response, "Request data failed:"))
////            return;
//        //Get the response which is in the first index of JSON Array
//        String jsonMsg = EntityUtils.toString(response.getEntity());
////        JSONObject jsonArray = new JSONObject(jsonMsg);
////        String jsonResp = jsonArray.get(0).toString();
//        //print the response HTTP code 200
//        //System.out.println(jsonResp);
//
//        System.out.println("Request is processed successfully.");
//        //Jackson ObjectMapper reads HistoricalPricingEvents class and histocial pricing events JSON response
//        //to create an object representing the parsed JSON.
//        EcoTaxaTaxon taxon = new ObjectMapper().readerFor(EcoTaxaTaxon.class).readValue(jsonMsg);
//
//        return taxon;
//    }

    public static List<WormsTaxon> searchTaxons(String searchText) throws IOException, URISyntaxException {
        URI uri = new URI(
                "https",
                "www.marinespecies.org",
                String.format("/rest/AphiaRecordsByName/%s", searchText),
                "like=true&marine_only=false&offset=1",
                null
        );
        HttpGet httpReq = new HttpGet(uri.toASCIIString());
        System.out.println("\nRequested URI is :\n"+httpReq.getURI());
        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse response = httpClient.execute(httpReq);
        //Check if getting data successfully or not. If fails, print error then exit from the method
//        if(!Utils.isSuccessRequestPrnError(response, "Request data failed:"))
//            return;
        if (response.getStatusLine().getStatusCode() != 200) {
            return new ArrayList<WormsTaxon>();
        }
        String jsonMsg = EntityUtils.toString(response.getEntity());
        WormsTaxon[] results = new ObjectMapper().readerFor(WormsTaxon[].class).readValue(jsonMsg);
        return Arrays.stream(results).filter(w -> w.url != null).collect(Collectors.toList());
    }

    public static Service<List<WormsTaxon>> searchTaxonsService(String searchText) {
        Service<List<WormsTaxon>> service = new Service<List<WormsTaxon>>() {
            @Override
            protected Task<List<WormsTaxon>> createTask() {
                return new Task<List<WormsTaxon>>() {
                    @Override
                    protected List<WormsTaxon> call() throws IOException, URISyntaxException {
                        return searchTaxons(searchText);
                    };
                };
            }
        };
        return service;
    }

    public static Service<WormsTaxon> getTaxonByIdService(int id) {
        Service<WormsTaxon> service = new Service<WormsTaxon>() {
            @Override
            protected Task<WormsTaxon> createTask() {
                return new Task<WormsTaxon>() {
                    @Override
                    protected WormsTaxon call() throws IOException {
                        return null;
//                        return getTaxonById(id);
                    };
                };
            }
        };
        return service;
    }
}
