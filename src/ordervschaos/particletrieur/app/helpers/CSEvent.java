/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.helpers;

import java.util.Set;
import java.util.HashSet;
import java.util.function.Consumer;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class CSEvent<T> {

    private Set<Consumer<T>> listeners = new HashSet();

    public void addListener(Consumer<T> listener) {
        listeners.add(listener);
    }

    public void broadcast(T args) {
        listeners.forEach(x -> x.accept(args));
    }
    
    public void broadcast() {
        listeners.forEach(x -> x.accept(null));
    }
}
