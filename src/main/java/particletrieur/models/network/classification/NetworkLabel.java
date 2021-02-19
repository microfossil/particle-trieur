package particletrieur.models.network.classification;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "label")
public class NetworkLabel {
    public String code;
    public int count;
    public double precision;
    public double recall;
    public double f1score;
    public double support;
}
