package ordervschaos.particletrieur.app.models.network.classification;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "tensor")
@XmlAccessorType(XmlAccessType.NONE)
public class TensorInfo {

    @XmlElement
    public String name = "";
    @XmlElement
    public String description = "";
    @XmlElement
    public String operation ="";
    @XmlElement
    public int height = 0;
    @XmlElement
    public int width = 0;
    @XmlElement
    public int channels = 0;

    public TensorInfo() {
        
    }
}
