package ordervschaos.particletrieur.app.models.network;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

@XmlRootElement(name = "prepro")
@XmlAccessorType(XmlAccessType.NONE)
public class PreprocessingInfo {

    @XmlElement
    public String type = "";
    @XmlElement
    public String description = "";
    @XmlElementWrapper(name = "params")
    @XmlElements({ @XmlElement(name = "param", type = Number.class) })
    public ArrayList<Number> params = new ArrayList<>();

    public PreprocessingInfo() {
        
    }
}
