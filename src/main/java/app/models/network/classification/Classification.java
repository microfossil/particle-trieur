/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.app.models.network.classification;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class Classification {

    //TODO put index of class as well
//    private int index = 0;
    private String code = "";
    private double value = 0.0;

    public Classification() {
        
    }
    
    public Classification(String code, double value) {
        this.code = code;
        this.value = value;
    }

//    public Classification(int index, String code, double value) {
//        this.index = index;
//        this.code = code;
//        this.value = value;
//    }

//    public int getIndex() { return index; }
//    public void setIndex(int value) { index = value; }

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

    @Override
    public int hashCode() {
        return this.code.hashCode();
    }    
}
