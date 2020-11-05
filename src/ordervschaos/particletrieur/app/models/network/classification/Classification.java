/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.models.network.classification;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class Classification {
    
    private String code = "";
    private double value = 0.0;

    public Classification() {
        
    }
    
    public Classification(String code, double value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

//    @Override
//    public boolean equals(Object obj) {
//        if ((obj instanceof Classification)
//                && ((Classification) obj).getCode().equals(this.code))
//        {
//            return true;
//        }
//        else {
//            return false;
//        }
//    }

    @Override
    public int hashCode() {
        return this.code.hashCode();
    }    
}
