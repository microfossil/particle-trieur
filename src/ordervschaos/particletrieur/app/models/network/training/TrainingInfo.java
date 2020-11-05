package ordervschaos.particletrieur.app.models.network.training;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

@XmlRootElement(name = "network")
@XmlAccessorType(XmlAccessType.NONE)
public class TrainingInfo {

    @XmlElement
    public String name = "";
    @XmlElement
    public String description = "";
    @XmlElement
    public LocalDateTime date;
    @XmlElement
    public String path;
    @XmlElement
    public ArrayList<String> labels = new ArrayList<>();
    @XmlElement
    public HashMap<String, String> params = new HashMap<>();

    public TrainingInfo() {
        
    }
}
