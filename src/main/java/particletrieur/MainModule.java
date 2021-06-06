package particletrieur;

import particletrieur.services.network.ForaminiferaSegmenterService;
import particletrieur.services.network.PlanktonSegmenterService;
import particletrieur.viewmanagers.UndoManager;
import particletrieur.models.Supervisor;
import particletrieur.viewmodels.*;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import particletrieur.viewmodels.export.ExportViewModel;
import particletrieur.viewmodels.network.CNNPredictionViewModel;
import particletrieur.viewmodels.network.KNNPredictionViewModel;
import particletrieur.viewmodels.particles.LabelsViewModel;
import particletrieur.viewmodels.particles.ParticlesViewModel;
import particletrieur.viewmodels.particles.TagsViewModel;
import particletrieur.viewmodels.network.NetworkViewModel;
import particletrieur.viewmodels.project.ProjectRepositoryViewModel;
import particletrieur.viewmodels.stats.StatisticsChartsViewModel;
import particletrieur.viewmodels.tools.ToolsViewModel;


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

        //Services
        bind(ForaminiferaSegmenterService.class).in(Singleton.class);
        bind(PlanktonSegmenterService.class).in(Singleton.class);
    }
}
