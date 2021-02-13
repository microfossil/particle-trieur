/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.viewcontrollers.tag;

import ordervschaos.particletrieur.app.models.project.Tag;
import ordervschaos.particletrieur.app.AbstractDialogController;
import ordervschaos.particletrieur.app.FxmlLocation;
import ordervschaos.particletrieur.app.viewmodels.particles.TagsViewModel;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
@FxmlLocation("views/tag/EditTagView.fxml")
public class EditTagViewController extends AbstractDialogController implements Initializable {

    @FXML public TextField textFieldCode;
    @FXML public TextField textFieldName;
    @FXML public TextArea textAreaDescription;

    private Tag tag;
    private TagsViewModel tagsViewModel;

    @Inject
    public EditTagViewController(TagsViewModel tagsViewModel) {
        this.tagsViewModel = tagsViewModel;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }    
    
    public void setData(Tag tag) {
        this.tag = tag;
        textFieldCode.setText(tag.getCode());
        textFieldName.setText(tag.getName());
        textAreaDescription.setText(tag.getDescription());
    }  
    
    public Tag getData() {
        if (textFieldCode.getText().equals("")) return null;
        Tag newTag = new Tag(
                textFieldCode.getText(), 
                textFieldName.getText(),
                textAreaDescription.getText());
        return newTag;
    }

    @Override
    public void processDialogResult(ButtonType buttonType) {
        if (buttonType == ButtonType.OK) {
            Tag newTag = getData();
            if (newTag != null) {
                if (this.tag == null) {
                    tagsViewModel.addTag(newTag);
                } else {
                    tagsViewModel.updateTag(tag.getCode(), newTag);
                }
            }
        }
    }

    @Override
    public String getHeader() {
        return "Edit Tag";
    }

    @Override
    public String getSymbol() {
        return "featheredit3";
    }

    @Override
    public ButtonType[] getButtonTypes() {
        return new ButtonType[] {ButtonType.OK, ButtonType.CANCEL};
    }
}
