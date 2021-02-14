package ordervschaos.particletrieur.app.services;

import ordervschaos.particletrieur.app.models.network.classification.NetworkInfo;
import ordervschaos.particletrieur.app.models.network.classification.TensorInfo;
import ordervschaos.particletrieur.app.services.network.FCNNSegmenterService;
import ordervschaos.particletrieur.app.helpers.AutoCancellingServiceRunner;
import ordervschaos.particletrieur.app.models.processing.*;
import ordervschaos.particletrieur.app.models.processing.processors.MorphologyProcessor;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.opencv.core.Mat;

public class ImageProcessingService {

    //TODO this is not a service??
    //TODO the process stuff should be in another class
    //TODO the processAsync should be in a manager
    private FCNNSegmenterService fcnnSegmenterService;
    private AutoCancellingServiceRunner<ParticleImage> processingServiceRunner;

    private BooleanProperty running = new SimpleBooleanProperty(false);
    public boolean isRunning() {
        return running.get();
    }
    public BooleanProperty runningProperty() {
        return running;
    }
    public void setRunning(boolean running) {
        this.running.set(running);
    }

    public ImageProcessingService(FCNNSegmenterService fcnnSegmenterService) {
        this.fcnnSegmenterService = fcnnSegmenterService;
    }

    public ParticleImage process(Mat mat, ProcessingInfo def) {
        ParticleImage image = ParticleImage.create(mat, def.getImageType(), def.isConvertToGreyscale());
        //return image;

        if (def.isRemoveWhiteBorder()) image.removeWhiteBorder();
        if (def.isRemoveBlackBorder()) image.removeBlackBorder();
        if (def.isMakeSquare()) image.makeSquare();

        image.rescale(255);

        Mask mask = Mask.create(image.workingImage, image.type);
        if (def.isSegmentationEnhanceEdges()) mask.enhanceEdges();
        if (def.isSegmentationRescale()) mask.rescale();
        switch (def.getSegmentationMethod()) {
            case INTENSITY:
                mask.segmentFixedIntensity(def.getSegmentationThreshold());
                break;
            case OTSU:
                mask.segmentOtsuIntensity(def.getSegmentationThreshold());
                break;
            case CNN:
                mask.segmentCNN(def.getSegmentationThreshold(), fcnnSegmenterService);
        }
        mask.largestRegion().calculateParameters();
        mask.forDisplay();

        image.setMask(mask);

        if (def.isNormalise()) image.normalise(def.getNormalisationParameter(), false);
        if (def.isRemoveBackground()) image.removeBackground(def.getBackgroundRemovalMargin());
        if (def.getCentre()) image.adjustFromMask(def.isRotateToMajorAxis());

        return image;
    }

    public ParticleImage processForNetwork(Mat mat, NetworkInfo def) {
        TensorInfo input = def.inputs.get(0);
        boolean isSingleChannel = input.channels == 1 ? true : false;

        ParticleImage image = ParticleImage.create(mat, ImageType.GREY, isSingleChannel);
        image.rescale(255).makeSquare().resize(input.width, input.height);

        return image;
    }

    public ParticleImage processForNetwork(Mat mat, ProcessingInfo def, NetworkInfo net) {
        ParticleImage image = process(mat, def);

        TensorInfo input = net.inputs.get(0);

        if (!def.isMakeSquare()) image.makeSquare();
        image.convertChannels(input.channels).resize(input.width, input.height);

        return image;
    }

    public void processAsyncAndReleaseMat(Mat mat, ProcessingInfo def, ObjectProperty<ParticleImage> imageProperty)
    {
        if (processingServiceRunner == null) processingServiceRunner = new AutoCancellingServiceRunner<>("processing");
        Service<ParticleImage> service = new Service<ParticleImage>() {
            @Override
            protected Task<ParticleImage> createTask() {
                Task<ParticleImage> task = new Task<ParticleImage>() {
                    @Override
                    protected ParticleImage call() throws Exception {
                        ParticleImage image = process(mat, def);
                        mat.release();
                        image.morphology = MorphologyProcessor.calculateMorphology(image);
                        return image;
                    }
                };
                return task;
            }
        };
        service.setOnSucceeded(event -> {
            ParticleImage image = imageProperty.get();
            if (image != null) image.release();
            imageProperty.set(service.getValue());
            setRunning(false);
        });
        service.setOnFailed(event -> {
            service.getException().printStackTrace();
            setRunning(false);
        });
        setRunning(true);
        processingServiceRunner.run(service);
    }
}
