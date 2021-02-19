package main.java.app;

import main.java.app.viewmanagers.UndoManager;
import main.java.app.models.Supervisor;
import main.java.app.viewmodels.*;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import main.java.app.viewmodels.export.ExportViewModel;
import main.java.app.viewmodels.network.CNNPredictionViewModel;
import main.java.app.viewmodels.network.KNNPredictionViewModel;
import main.java.app.viewmodels.particles.LabelsViewModel;
import main.java.app.viewmodels.particles.ParticlesViewModel;
import main.java.app.viewmodels.particles.TagsViewModel;
import main.java.app.viewmodels.network.NetworkViewModel;
import main.java.app.viewmodels.project.ProjectRepositoryViewModel;
import main.java.app.viewmodels.stats.StatisticsChartsViewModel;
import main.java.app.viewmodels.tools.ToolsViewModel;


public class MainModule extends AbstractModule {
    
    @Override
    protected void configure() {

        //Supervisor contains references to the models
        bind(Supervisor.class).in(Singleton.class);

        //Main
        bind(MainViewModel.class).in(Singleton.class);
        bind(SelectionViewModel.class).in(Singleton.class);

        //Classification
        bind(NetworkViewModel.class).in(Singleton.class);
        bind(CNNPredictionViewModel.class).in(Singleton.class);
        bind(KNNPredictionViewModel.class).in(Singleton.class);

        //Labelling
        bind(ParticlesViewModel.class).in(Singleton.class);
        bind(LabelsViewModel.class).in(Singleton.class);
        bind(TagsViewModel.class).in(Singleton.class);

        //Other
        bind(ExportViewModel.class).in(Singleton.class);
        bind(ToolsViewModel.class).in(Singleton.class);
        bind(ProjectRepositoryViewModel.class).in(Singleton.class);
        bind(StatisticsChartsViewModel.class).in(Singleton.class);

        //Undo
        bind(UndoManager.class).in(Singleton.class);
    }
}
