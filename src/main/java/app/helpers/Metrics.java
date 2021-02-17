package main.java.app.helpers;

public class Metrics {

    public static double vectorCosineSimilarity(float [] a, float [] b) {
        double totala = 0;
        double totalb = 0;
        double diff = 0;
        for (int i = 0; i < a.length; i++) {
//            if (a[i] != 0.0 && b[i] != 0.0) {
                diff += a[i] * b[i];
                totala += Math.pow(a[i], 2);
                totalb += Math.pow(b[i], 2);
//            }
        }
        double den = Math.sqrt(totala * totalb);
        if (den == 0) return 0;
        else return diff / den;
    }
}
