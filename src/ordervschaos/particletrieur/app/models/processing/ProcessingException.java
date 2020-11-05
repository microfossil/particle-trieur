/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.models.processing;

/**
 *
 * @author rossm
 */
public class ProcessingException extends Exception {
    
    public ProcessingException(){
        super();
    }

    public ProcessingException(String message){
        super(message);
    }    
}
