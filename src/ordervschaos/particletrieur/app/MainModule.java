package ordervschaos.particletrieur.app;

import ordervschaos.particletrieur.app.viewmanagers.UndoManager;
import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.viewmodels.*;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import ordervschaos.particletrieur.app.viewmodels.export.ExportViewModel;
import ordervschaos.particletrieur.app.viewmodels.network.CNNPredictionViewModel;
import ordervschaos.particletrieur.app.viewmodels.network.KNNPredictionViewModel;
import ordervschaos.particletrieur.app.viewmodels.particles.LabelsViewModel;
import ordervschaos.particletrieur.app.viewmodels.particles.ParticlesViewModel;
import ordervschaos.particletrieur.app.viewmodels.particles.TagsViewModel;
import ordervschaos.particletrieur.app.viewmodels.network.NetworkViewModel;
import ordervschaos.particletrieur.app.viewmodels.project.ProjectRepositoryViewModel;
import ordervschaos.particletrieur.app.viewmodels.stats.StatisticsChartsViewModel;
import ordervschaos.particletrieur.app.viewmodels.tools.ToolsViewModel;


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
