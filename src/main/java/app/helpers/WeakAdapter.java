/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.app.helpers;

import java.util.ArrayList;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;

/**
 *
 * @author rossm
 */
public class WeakAdapter {
    
    ArrayList<Object> listenerRefs = new ArrayList<>();

    public WeakAdapter() {
    }

    public void dispose() {
        listenerRefs.clear();
    }

    public final <T> void remove(ChangeListener<T> listener) {
        listenerRefs.remove(listener);
    }

    public final <T> void addChangeListener(final ObservableValue observable, ChangeListener<T> listener) {
        listenerRefs.add(listener);
        observable.addListener(new WeakChangeListener<>(listener));
    }

    public final <T> WeakListChangeListener<T> addListChangeListener(ListChangeListener<T> listener) {
        listenerRefs.add(listener);
        return new WeakListChangeListener<>(listener);
    }

    public void addInvalidationListener(final Observable listened, InvalidationListener listener) {
        listenerRefs.add(listener);
        listened.addListener(new WeakInvalidationListener(listener));
    }

    public final void stringBind(final StringProperty propertyToUpdate, final StringExpression expressionToListen) {
        ChangeListener<String> listener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String name) {
                propertyToUpdate.set(name);
            }
        };
        listenerRefs.add(listener);
        expressionToListen.addListener(new WeakChangeListener<>(listener));
        listener.changed(null, null, expressionToListen.get());
    }

    public final void booleanBind(final BooleanProperty propertyToUpdate, final BooleanExpression expressionToListen) {
        ChangeListener<Boolean> listener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean name) {
                propertyToUpdate.set(name);
            }
        };
        listenerRefs.add(listener);
        expressionToListen.addListener(new WeakChangeListener<>(listener));
        propertyToUpdate.set(expressionToListen.get());
    }
}
