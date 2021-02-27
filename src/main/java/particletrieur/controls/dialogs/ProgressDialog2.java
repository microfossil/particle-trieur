/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.controls.dialogs;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
/**
 * Copyright (c) 2014, 2015 ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import java.awt.Desktop;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import particletrieur.controls.SymbolLabel;
//import org.kordamp.ikonli.javafx.FontIcon;

public class ProgressDialog2 extends DialogEx<Void> {
    
    public boolean closeOnServiceFinished = false;
    
    private String cancelledText = "The operation was cancelled";
    private String failedText = "The operation failed";
    private String succeededText = "The operation succeeded";
    
//    public FontIcon icon;
    public SymbolLabel icon;

    public String getCancelledText() {
        return cancelledText;
    }
    public void setCancelledText(String cancelledText) {
        this.cancelledText = cancelledText;
    }

    public String getFailedText() {
        return failedText;
    }
    public void setFailedText(String failedText) {
        this.failedText = failedText;
    }

    public String getSucceededText() {
        return succeededText;
    }
    public void setSucceededText(String succeededText) {
        this.succeededText = succeededText;
    }

    private Service<?> service;

    public ProgressDialog2(final Service<?> service) {
        if (service != null
                && (service.getState() == State.CANCELLED
                || service.getState() == State.FAILED
                || service.getState() == State.SUCCEEDED)) {
            return;
        }
        this.service = service;
        
//        icon = new FontIcon("fth-pie-graph");
        icon = new SymbolLabel("featherpiechart", 32);
//        icon.setIconSize(32);
//        icon.setIconColor(Color.SLATEGREY);
        icon.setSymbolColor("slategrey");
        setGraphic(icon);

//        rt = new ScaleTransition(Duration.millis(3000), icon);
//        //rt.setByAngle(360);
//        rt.setFromX(0.8);
//        rt.setFromY(0.8);
//        rt.setToX(1.0);
//        rt.setToY(1.0);
//        rt.setAutoReverse(true);
//        rt.setCycleCount(Animation.INDEFINITE);
//        rt.play();

        final DialogPane dialogPane = getDialogPane();
        
        final Label progressMessage = new Label();
        progressMessage.textProperty().bind(service.messageProperty());

        final WorkerProgressPane content = new WorkerProgressPane(this);
        content.setMaxWidth(Double.MAX_VALUE);
        content.setWorker(service);
        
        VBox vbox = new VBox(10, progressMessage, content);
        vbox.setMaxWidth(Double.MAX_VALUE);
        vbox.setMinWidth(300);
        vbox.setMinHeight(150);

        /**
         * The content Text cannot be set before the constructor and since we
         * set a Content Node, the contentText will not be shown. If we want to
         * let the user display a content text, we must recreate it.
         */
        Label contentText = new Label();
        contentText.setWrapText(true);
        vbox.getChildren().add(0, contentText);
        contentText.textProperty().bind(dialogPane.contentTextProperty());
        dialogPane.setContent(vbox);
        getDialogPane().setMinHeight(Region.USE_COMPUTED_SIZE);
    }
    
    public void start() {
        service.start();
    }
    
    public void end() {
        service.cancel();
    }

    
    /**************************************************************************
     * 
     * Support classes
     * 
     **************************************************************************/

    /**
     * The WorkerProgressPane takes a {@link Dialog} and a {@link Worker}
     * and links them together so the dialog is shown or hidden depending
     * on the state of the worker.  The WorkerProgressPane also includes
     * a progress bar that is automatically bound to the progress property
     * of the worker.  The way in which the WorkerProgressPane shows and
     * hides its worker's dialog is consistent with the expected behavior
     * for {@link #showWorkerProgress(Worker)}.
     */
    private static class WorkerProgressPane extends Region {
        private Worker<?> worker;

        private boolean dialogVisible = false;
        private boolean cancelDialogShow = false;

        private ChangeListener<Worker.State> stateListener = new ChangeListener<Worker.State>() {
            @Override public void changed(ObservableValue<? extends State> observable, State old, State value) {
                switch(value) {
                    case CANCELLED:
//                        dialog.rt.stop();
                        getChildren().clear();
                        dialog.setContentText(dialog.getCancelledText());
                        dialog.getDialogPane().getButtonTypes().clear();
                        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
                        end();
                        break;
                    case FAILED:
//                        dialog.rt.stop();
                        getChildren().clear();
                        dialog.icon = new SymbolLabel("featherxcircle", 32);
                        dialog.icon.setSymbolColor("red");

//                        dialog.icon = new FontIcon();
//                        dialog.icon.setIconLiteral("fth-circle-cross");
//                        dialog.icon.setIconSize(32);
//                        dialog.icon.setIconColor(Color.RED);
                        dialog.setGraphic(dialog.icon);
                        dialog.setContentText(dialog.getFailedText());
                        dialog.getDialogPane().getButtonTypes().clear();
                        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
                        
                        // Create expandable Exception.
                        Exception ex = new Exception(worker.getException());
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        ex.printStackTrace(pw);
                        String exceptionText = sw.toString();

                        Label label = new Label("The exception stacktrace was:");
                        TextArea textArea = new TextArea(exceptionText);
                        textArea.setEditable(false);
                        textArea.setWrapText(true);
                        textArea.setMaxWidth(Double.MAX_VALUE);
                        textArea.setMaxHeight(Double.MAX_VALUE);
                        GridPane.setVgrow(textArea, Priority.ALWAYS);
                        GridPane.setHgrow(textArea, Priority.ALWAYS);
                        GridPane expContent = new GridPane();
                        expContent.setMaxWidth(Double.MAX_VALUE);
                        expContent.add(label, 0, 0);
                        expContent.add(textArea, 0, 1);
                        expContent.setVgap(7);
                        Button mailButton = new Button("Send bug report...");
                        mailButton.setOnAction(event -> {
                            Desktop desktop = Desktop.getDesktop(); 
                            try {                
                                String encoded = URLEncoder.encode(exceptionText, "UTF-8");
                                URL url = new URL("mailto","",-1,"?to=ross.g.marchant@gmail.com&subject=Particle%20Trieur%20Exception&body="+encoded);
                                desktop.mail(url.toURI());
                            } catch (URISyntaxException ex1) {
                                Logger.getLogger(BasicDialogs.class.getName()).log(Level.SEVERE, null, ex1);
                            } catch (IOException ex1) {
                                Logger.getLogger(BasicDialogs.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                        });
                        expContent.add(mailButton, 0, 2);
                        dialog.getDialogPane().setPrefWidth(600);
                        dialog.getDialogPane().setExpandableContent(expContent);
                        end();
                        break;
                        
                    case SUCCEEDED:          
//                        dialog.rt.stop();
                        if(!dialogVisible) {
                            cancelDialogShow = true;
                            end();
                        } else if(old == State.SCHEDULED || old == State.RUNNING) {
                            getChildren().clear();
                            dialog.icon = new SymbolLabel("feathercheckcircle", 32);
                            dialog.icon.setSymbolColor("green");
//                            dialog.icon = new FontIcon();
//                            dialog.icon.setIconLiteral("fth-circle-check");
//                            dialog.icon.setIconSize(32);
//                            dialog.icon.setIconColor(Color.GREEN);
                            dialog.setGraphic(dialog.icon);
                            dialog.setContentText(dialog.getSucceededText());
                            dialog.getDialogPane().getButtonTypes().clear();
                            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);  
                            end();
                        }
                        break;
                        
                    case SCHEDULED:
                        begin();
                        break;
                    default: //no-op     
                }
            }
        };

        public final void setWorker(final Worker<?> newWorker) { 
            if (newWorker != worker) {
                if (worker != null) {
                    worker.stateProperty().removeListener(stateListener);
                    end();
                }

                worker = newWorker;

                if (newWorker != null) {
                    newWorker.stateProperty().addListener(stateListener);
                    if (newWorker.getState() == Worker.State.RUNNING || newWorker.getState() == Worker.State.SCHEDULED) {
                        // It is already running
                        begin();
                    }
                }
            }
        }

        // If the progress indicator changes, then we need to re-initialize
        // If the worker changes, we need to re-initialize
        private final ProgressDialog2 dialog;
        private final ProgressBar progressBar;
        
        public WorkerProgressPane(ProgressDialog2 dialog) {
            this.dialog = dialog;
            
            this.progressBar = new ProgressBar();
            progressBar.setMaxWidth(Double.MAX_VALUE);
            getChildren().add(progressBar);
            
            if (worker != null) {
                progressBar.progressProperty().bind(worker.progressProperty());
            }
        }

        private void begin() {
            // Platform.runLater needs to be used to show the dialog because
            // the call begin() is going to be occurring when the worker is
            // notifying state listeners about changes.  If Platform.runLater
            // is not used, the call to show() will cause the worker to get
            // blocked during notification and it will prevent the worker
            // from performing any additional notification for state changes.
            //
            // Sine the dialog is hidden as a result of a change in worker
            // state, calling show() without wrapping it in Platform.runLater
            // will cause the progress dialog to run forever when the dialog
            // is attached to workers that start out with a state of READY.
            //
            // This also creates a case where the worker's state can change
            // to finished before the dialog is shown, resulting in an
            // an attempt to hide the dialog before it is shown.  It's
            // necessary to track whether or not this occurs, so flags are
            // set to indicate if the dialog is visible and if if the call
            // to show should still be allowed.
            cancelDialogShow = false;

            Platform.runLater(() -> {
                if(!cancelDialogShow) {
                    progressBar.progressProperty().bind(worker.progressProperty());
                    dialogVisible = true;
                    dialog.showEmbedded();
                }
            });
        }

        private void end() {
            progressBar.progressProperty().unbind();
            if (dialog.closeOnServiceFinished) {
                dialogVisible = false;
                DialogUtils.forcefullyHideDialog(dialog);
            }
        }

        @Override protected void layoutChildren() {
            if (progressBar != null) {
                Insets insets = getInsets();
                double w = getWidth() - insets.getLeft() - insets.getRight();
                double h = getHeight() - insets.getTop() - insets.getBottom();

                double prefH = progressBar.prefHeight(-1);
                double x = insets.getLeft() + (w - w) / 2.0;
                double y = insets.getTop() + (h - prefH) / 2.0;

                progressBar.resizeRelocate(x, y, w, prefH);
            }
        }
    }
}

