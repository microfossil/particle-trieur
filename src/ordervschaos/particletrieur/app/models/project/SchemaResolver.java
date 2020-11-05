package ordervschaos.particletrieur.app.models.project;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;

public class SchemaResolver extends SchemaOutputResolver {
    public Result createOutput(String uri, String suggestedFileName) throws IOException {
        Result result = new StreamResult(System.out);
        result.setSystemId("System.out");
        return result;
    }
}
