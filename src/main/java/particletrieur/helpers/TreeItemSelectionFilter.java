package particletrieur.helpers;

import javafx.scene.control.TreeItem;

//https://stackoverflow.com/questions/31267157/treeview-certain-treeitems-are-not-allowed-to-be-selected/58936433#58936433
public interface TreeItemSelectionFilter<S> {

    public boolean isSelectable(TreeItem<S> treeItem);
}
