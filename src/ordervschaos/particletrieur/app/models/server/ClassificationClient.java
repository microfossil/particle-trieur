/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.models.server;

import ordervschaos.particletrieur.app.controls.BasicDialogs;
import ordervschaos.particletrieur.app.models.network.classification.ClassificationSet;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;

/**
 *
 * @author chaos
 */
public class ClassificationClient {

    //public static ClassificationXML Post(InputStream imageStream) throws IOException {
    public static ClassificationSet Post(byte [] imageStream) throws IOException {
        
        CloseableHttpClient httpClient = HttpClients.createDefault();
        ClassificationSet xmlResponse = null;
        
        try {
            HttpPost httppost = new HttpPost("http://localhost:9000");            
            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addBinaryBody("image", imageStream)
                    .setMode(HttpMultipartMode.STRICT)
                    .build();

            httppost.setEntity(reqEntity);
            //System.out.println("Is chucked" + reqEntity.isChunked());

            System.out.println("Executing request: " + httppost.getRequestLine());
            CloseableHttpResponse response = httpClient.execute(httppost);
            
            try {
                JAXBContext context = JAXBContext.newInstance(ClassificationSet.class);
                Unmarshaller um = context.createUnmarshaller();            
                xmlResponse = (ClassificationSet) um.unmarshal(response.getEntity().getContent());  
            }
            catch (JAXBException ex) {
                BasicDialogs.ShowException("There was an rrror parsing the classification set.", ex);
            }
            
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                System.out.println(EntityUtils.toString(response.getEntity()));
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }
        return xmlResponse;
    }
}
