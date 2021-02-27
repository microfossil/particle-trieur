package particletrieur.viewmodels.particles;

import particletrieur.AppPreferences;
import particletrieur.models.project.Project;
import particletrieur.models.Supervisor;
import particletrieur.models.project.Tag;
import particletrieur.controls.dialogs.BasicDialogs;
import com.google.inject.Inject;
import particletrieur.viewmodels.SelectionViewModel;

import java.util.List;

public class TagsViewModel {

    private static AppPreferences appPrefs = new AppPreferences();
    private SelectionViewModel selectionViewModel;
    private Supervisor supervisor;

    @Inject
    public TagsViewModel(SelectionViewModel selectionViewModel, Supervisor supervisor) {
        this.selectionViewModel = selectionViewModel;
        this.supervisor = supervisor;
    }

    public void setTag(String code) {
        try {
            supervisor.project.setParticleTag(selectionViewModel.getCurrentParticles(), code);
            selectionViewModel.currentParticleUpdatedEvent.broadcast(null);
        }
        catch (Project.TagDoesntExistException ex) {
            BasicDialogs.ShowError("Tag doesn't exist",
                    "The tag referred to by the button doesn't exist in the project's tag list.\n"
                            + "Please contact me regarding this error at ross.g.marchant@gmail.com");
        }
    }

    public boolean updateTag(String code, Tag updated) {
        try {
            supervisor.project.updateTag(code, updated);
            if(!code.equals(updated.getCode())) {
                BasicDialogs.ShowInfo("Code changed",
                        String.format("The tag code \"%s\" has been changed to \"%s\".\nAll particles with this tag have also been updated.", code, updated.getCode()));
            }
            return true;
        }
        catch (Project.TagAlreadyExistsException ex) {
            BasicDialogs.ShowError("Conflict",
                    String.format("Tried to modify tag code \"%s\" to \"%s\", but the code \"%s\" already exists.\n"
                                    + "Use the tag buttons if you wish to change tags.",
                            code, updated.getCode(), updated.getCode()));
            return false;
        }
    }

    public boolean addTag(Tag updated) {
        try {
            supervisor.project.addTag(updated);
            return true;
        }
        catch (Project.TagAlreadyExistsException ex) {
            BasicDialogs.ShowError("Conflict",
                    String.format("Tried to add a new tag with code \"%s\", but that code already exists.",
                            updated.getCode()));
            return false;
        }
    }

    public void deleteTag(Tag tag) {
        try {
            supervisor.project.deleteTag(tag);
            BasicDialogs.ShowInfo("Code changed",
                    String.format("The tag with code \"%s\" has been deleted.", tag.getCode()));
        }
        catch (Project.TaxonDoesntExistException ex) {
            BasicDialogs.ShowError("Error",
                    String.format("The tag code \"%s\" does not exist.", tag.getCode()));
        }
    }

    public void initialiseTags(List<Tag> tags) {
        supervisor.project.initialiseTags(tags);
    }

}
