/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.controls.dialogs;

import particletrieur.App;

import java.awt.Desktop;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Service;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 *
 * @author chaos
 */
public class BasicDialogs {
    
//    public static void ShowAbout() {
//        Dialog dialog = new Dialog();
//        dialog.setTitle("About");
//        ImageView imageView = new ImageView(App.iconImage);
//        dialog.setGraphic(imageView);
//        ((Stage)dialog.getDialogPane().getScene().getWindow()).getIcons().add(App.iconImage);
//        dialog.setHeaderText("About Particle Trieur");
//        dialog.setContentText("Authors: Ross Marchant and Thibault de Garidel-Thoron\n"
//                + "Thanks: Martin Tetard\n\n"
//                + "Created as part of the ANR FIRST project.\n" + ""
//                + "(c) 2017-2019\n\n" +
//                "Version: " + MainController.class.getPackage().getImplementationVersion());
//        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
//        dialog.showAndWait();
//    }
    
    public static void ShowError(String header, String message) {
        AlertEx alert = new AlertEx(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(App.iconImage);
        alert.showEmbedded();
    }
    
    public static AlertEx ShowInfo(String header, String message) {
        AlertEx alert = new AlertEx(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(App.iconImage);
        alert.showEmbedded();
        return alert;
    }
    
    public static void ShowException(String content, Exception ex) {
        AlertEx alert = new AlertEx(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error has occurred");
        alert.setContentText(content + "\n\n" + ex.getMessage());
        ((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(App.iconImage);

        // Create expandable Exception.
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
        alert.getDialogPane().setPrefWidth(600);
        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().setExpanded(true);
        alert.showAndWait();
    }

    public static void ShowExceptionLog(String content) {
        AlertEx alert = new AlertEx(Alert.AlertType.INFORMATION);
        alert.setTitle("Log");
        alert.setHeaderText("Log of uncaught errors");
        alert.setContentText("All errors since program start are shown below:");
        ((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(App.iconImage);

//        Label label = new Label("The exception log is:");
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
//        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);
        expContent.setVgap(7);
        Button mailButton = new Button("Send as report...");
        mailButton.setOnAction(event -> {
            Desktop desktop = Desktop.getDesktop();
            try {
                String encoded = URLEncoder.encode(content, "UTF-8");
                URL url = new URL("mailto","",-1,"?to=ross.g.marchant@gmail.com&subject=Particle%20Trieur%20Exception&body="+encoded);
                desktop.mail(url.toURI());
            } catch (URISyntaxException ex1) {
                Logger.getLogger(BasicDialogs.class.getName()).log(Level.SEVERE, null, ex1);
            } catch (IOException ex1) {
                Logger.getLogger(BasicDialogs.class.getName()).log(Level.SEVERE, null, ex1);
            }
        });
        expContent.add(mailButton, 0, 2);
        alert.getDialogPane().setPrefWidth(800);
        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().setExpanded(true);
        alert.showEmbedded();
    }

    public static void ShowConfirmation(String header, String content, Runnable onOk, Runnable onCancel) {
        AlertEx alert = new AlertEx(Alert.AlertType.CONFIRMATION);
        alert.setTitle(header);
        alert.setContentText(content);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showEmbedded();
        alert.setResultConverter(button -> {
            if (button == ButtonType.OK && onOk != null) {
                onOk.run();
            }
            else if (button == ButtonType.CANCEL && onCancel != null) {
                onCancel.run();
            }
            return null;
        });
    }

    public static void ShowYesNo(String header, String content, Runnable onYes, Runnable onNo) {
        AlertEx alert = new AlertEx(Alert.AlertType.CONFIRMATION);
        alert.setTitle(header);
        alert.setContentText(content);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getButtonTypes().clear();
        alert.getButtonTypes().add(ButtonType.YES);
        alert.getButtonTypes().add(ButtonType.NO);
        alert.showEmbedded();
        alert.setResultConverter(button -> {
            if (button == ButtonType.YES && onYes != null) {
                onYes.run();
            }
            else if (button == ButtonType.NO && onNo != null) {
                onNo.run();
            }
            return null;
        });
    }

    public static ProgressDialog2 ProgressDialogWithCancel2(
            String title, 
            String header,
            Service service) {       
        ProgressDialog2 pd = new ProgressDialog2(service);
        //pd.closeOnServiceFinished = false;
        //pd.setGraphic(alert.getGraphic());
        pd.setTitle(title);
        pd.setHeaderText(header);


        //pd.setSucceededText(success);
        //pd.setCancelledText(cancelled);
        //pd.setFailedText(failed);
        ((Stage)pd.getDialogPane().getScene().getWindow()).getIcons().add(App.iconImage);
//        pd.initModality(Modality.APPLICATION_MODAL);
//        pd.initOwner(pane.getScene().getWindow());
        pd.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
//        pd.setOnCloseRequest(event -> {
//            pd.end();
//            App.getInstance().injector.getInstance(MainViewModel.class).setOperationInProgress(false);
////            pane.setDisable(false);
//        });
//        pd.setOnShowing(event -> {
//            App.getInstance().injector.getInstance(MainViewModel.class).setOperationInProgress(true);
////            pane.setDisable(true);
//        });
        return pd;
    }
}
