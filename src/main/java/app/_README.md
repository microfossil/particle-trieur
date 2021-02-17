# Software Pattern Explanation

This software tries to follow a MVVM (model-view-viewmodel) design pattern, but with a few little tweaks.

The typically MVVM design features three components:

- Model: Classes that contain variables and lists to hold data, along side functions to modify this data
- View: The user interface
- ViewModel: A class that bridges the view to the viewmodel. Typically the view _binds_ to objects exposed via the viewmodel.

Typically the viewmodel and view have a one-on-one correspondance

JavaFX is a MVC (model-view-controller) paradigm where each view has a controller object that is responsible for both updating the view as well as interfacing with the models.

In this project we have four types:

- Model: As before, but they also may contain JavaFX observable properties.
- View: FXML file laying out the GUI elements
- ViewController: One-on-one relationship with a view. It sets up bindings and calls commands
- ViewModel: Contains commands that might be called by the views as well as any related logic.

Because parts of the viewmodels may be used by other viewmodels, it was decided to split the viewmodels into functional classes, rather than have a one-on-one correspondance with a view.

## Services and Managers

There are two other class types - services and managers.

Services typically provide functions that perform some operation on the data using a wide range of objects as input. These are typically long running, and so encapsulate this functionality into a JavaFX _Service_ instance.

Managers watch for GUI or other changes and then perform an operation and expose the results. For example, when a new image is selected the similarity calculation will be run.

