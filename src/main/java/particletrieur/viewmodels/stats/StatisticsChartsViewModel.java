package particletrieur.viewmodels.stats;

import particletrieur.controls.dialogs.DialogEx;
import particletrieur.models.Supervisor;
import particletrieur.services.StatisticsService;
import particletrieur.viewcontrollers.stats.StatisticsDialogs;
import com.google.inject.Inject;
import javafx.scene.control.Dialog;

import java.util.LinkedHashMap;

public class StatisticsChartsViewModel {

    @Inject
    Supervisor supervisor;

    public void showLabelCounts() {
        StatisticsService stats = new StatisticsService(supervisor.project);
        LinkedHashMap counts = stats.classCounts();
        DialogEx dialog = StatisticsDialogs.CategoryCountDialog(
                "Count per Label",
                "Number of images for each label (except unlabeled)",
                "Label",
                "Count",
                counts);
        dialog.showEmbedded();
    }

    public void showSampleCounts() {
        StatisticsService stats = new StatisticsService(supervisor.project);
        LinkedHashMap counts = stats.sampleCounts();
        DialogEx dialog = StatisticsDialogs.CategoryCountDialog(
                "Count per Sample",
                "Number of images for each sample",
                "Sample",
                "Count",
                counts);
        dialog.showEmbedded();
    }

    public void showIndex1Counts() {
        StatisticsService stats = new StatisticsService(supervisor.project);
        LinkedHashMap counts = stats.index1Counts();
        DialogEx dialog = StatisticsDialogs.IndexCountDialog(
                "Count per index 1 value",
                "Number of images for each index 1 value",
                "Index 1",
                "Count",
                counts);
        dialog.showEmbedded();
    }

    public void showIndex2Counts() {
        StatisticsService stats = new StatisticsService(supervisor.project);
        LinkedHashMap counts = stats.index2Counts();
        DialogEx dialog = StatisticsDialogs.IndexCountDialog(
                "Count per index 2 value",
                "Number of images for each index 2 value",
                "Sample",
                "Index 2",
                counts);
        dialog.showEmbedded();
    }

    public void showRelativeAbundance(int index) {
        StatisticsService stats = new StatisticsService(supervisor.project);
        LinkedHashMap types = stats.labelByIndex(index, true);
        DialogEx dialog = StatisticsDialogs.RelativeAbundanceDialog(
                "Label Frequency",
                String.format("Fraction of images for each label vs index %d", index),
                "Index",
                "Fraction",
                types);
        dialog.showEmbedded();
    }

    public void showRelativeAbundanceWithCoreID() {
        StatisticsService stats = new StatisticsService(supervisor.project);
        LinkedHashMap types = stats.labelBySample(false);
        DialogEx dialog = StatisticsDialogs.RelativeAbundanceWithCoreIDDialog(
                "Label Frequency",
                "Fraction of images in each class vs sample",
                "Sample",
                "Fraction",
                types);
        dialog.showEmbedded();
    }
}
