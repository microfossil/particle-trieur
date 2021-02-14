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
public class Similarity {

    public boolean isOriginal;
    public int index;
    public double score;
    public String value;

    public Similarity(boolean isOriginal, int index, double score, String value) {
        this.isOriginal = isOriginal;
        this.index = index;
        this.score = score;
        this.value = value;
    }
}