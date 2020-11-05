package ordervschaos.particletrieur.app.viewmodels;

import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.services.StatisticsService;
import ordervschaos.particletrieur.app.viewcontrollers.StatisticsDialogs;
import com.google.inject.Inject;
import javafx.scene.control.Dialog;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class StatisticsChartsViewModel {

    @Inject
    Supervisor supervisor;

    public void showLabelCounts() {
        StatisticsService stats = new StatisticsService(supervisor.project);
        LinkedHashMap counts = stats.classCounts();
        Dialog dialog = StatisticsDialogs.CategoryCountDialog(
                "Count per Label",
                "Number of images for each label (except unlabeled)",
                "Label",
                "Count",
                counts);
        dialog.showAndWait();
    }

    public void showSampleCounts() {
        StatisticsService stats = new StatisticsService(supervisor.project);
        LinkedHashMap counts = stats.sampleCounts();
        Dialog dialog = StatisticsDialogs.CategoryCountDialog(
                "Count per Sample",
                "Number of images for each sample",
                "Sample",
                "Count",
                counts);
        dialog.showAndWait();
    }

    public void showRelativeAbundance(int index) {
        StatisticsService stats = new StatisticsService(supervisor.project);
        LinkedHashMap types = stats.labelByIndex(index, true);
        Dialog dialog = StatisticsDialogs.RelativeAbundanceDialog(
                "Label Frequency",
                String.format("Fraction of images for each label vs index %d", index),
                "Index",
                "Fraction",
                types);
        dialog.showAndWait();
    }

    public void showRelativeAbundanceWithCoreID() {
        StatisticsService stats = new StatisticsService(supervisor.project);
        LinkedHashMap types = stats.labelBySample(false);
        Dialog dialog = StatisticsDialogs.RelativeAbundanceWithCoreIDDialog(
                "Label Frequency",
                "Fraction of images in each class vs sample",
                "Sample",
                "Fraction",
                types);
        dialog.showAndWait();
    }
}
