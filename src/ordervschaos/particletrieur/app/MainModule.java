/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app;

import ordervschaos.particletrieur.app.viewmanagers.UndoManager;
import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.viewmodels.*;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 *
 * @author rossm
 */
public class MainModule extends AbstractModule {
    
    @Override
    protected void configure() {

        //Supervisor contains references to the models
        bind(Supervisor.class).in(Singleton.class);

        //Main
        bind(MainViewModel.class).in(Singleton.class);
        bind(SelectionViewModel.class).in(Singleton.class);

        //Classification
        bind(CNNVectorViewModel.class).in(Singleton.class);
        bind(PredictionViewModel.class).in(Singleton.class);

        //Labelling
        bind(ParticlesViewModel.class).in(Singleton.class);
        bind(LabelsViewModel.class).in(Singleton.class);
        bind(TagsViewModel.class).in(Singleton.class);

        //Other
        bind(ExportViewModel.class).in(Singleton.class);
        bind(FindDuplicatesViewModel.class).in(Singleton.class);
        bind(ProjectRepositoryViewModel.class).in(Singleton.class);
        bind(StatisticsChartsViewModel.class).in(Singleton.class);

        //Undo
        bind(UndoManager.class).in(Singleton.class);
    }
}
