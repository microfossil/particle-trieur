package main.java.app.services.network;

import main.java.app.models.network.training.ModelDefaults;

import java.util.ArrayList;
import java.util.List;

public class TrainingNetworkDescriptionService {

    public static void addDefaultInputSize(ModelDefaults info, int height, int width, int channels) {
        info.imageHeight = height;
        info.imageWidth = width;
        info.imageChannels = channels;
    }

    public static List<ModelDefaults> getDescriptions() {

        ArrayList<ModelDefaults> networkInfos = new ArrayList<>();

        ModelDefaults cnn = new ModelDefaults();
        cnn.name = "ResNet50 TL (fast)";
        cnn.networkType = "resnet50_tl";
        cnn.description = "Transfer learning using ResNet50 and ImageNet weights." +
                "\nRecommended image input is size 224 x 224 colour." +
                "\nSuitable for training without a GPU.";
        addDefaultInputSize(cnn, 224, 224, 3);
        networkInfos.add(cnn);

        cnn = new ModelDefaults();
        cnn.name = "ResNet50 Cyclic TL (fast)";
        cnn.networkType = "resnet50_cyclic_tl";
        cnn.description = "Transfer learning using ResNet50 encapsulated with cyclic layers." +
                "\nRecommended image input is size 224 x 224 colour." +
                "\nSuitable for training without a GPU.";
        addDefaultInputSize(cnn, 224, 224, 3);
        networkInfos.add(cnn);

        cnn = new ModelDefaults();
        cnn.name = "ResNet50 Cyclic Gain TL (fast)";
        cnn.networkType = "resnet50_cyclic_gain_tl";
        cnn.description = "Transfer learning using ResNet50 encapsulated with cyclic and gain layers." +
                "\nRecommended image input is size 224 x 224 colour." +
                "\nSuitable for training without a GPU.";
        addDefaultInputSize(cnn, 224, 224, 3);
        networkInfos.add(cnn);

        cnn = new ModelDefaults();
        cnn.name = "Base Cyclic 4";
        cnn.networkType = "base_cyclic";
        cnn.description = "Sequential network with 4 filters and  cyclic layers for better rotation invariance." +
                "\nDesigned for particle classification at CEREGE." +
                "\nRecommended image input is size 128 x 128 greyscale.";
        cnn.numFilters = 4;
        addDefaultInputSize(cnn, 128, 128, 1);
        networkInfos.add(cnn);

        cnn = new ModelDefaults();
        cnn.name = "Base Cyclic 8";
        cnn.networkType = "base_cyclic";
        cnn.description = "Sequential network with 8 filters and cyclic layers for better rotation invariance." +
                "\nDesigned for particle classification at CEREGE." +
                "\nRecommended image input is size 128 x 128 greyscale.";
        cnn.numFilters = 8;
        addDefaultInputSize(cnn, 128, 128, 1);
        networkInfos.add(cnn);

        cnn = new ModelDefaults();
        cnn.name = "Base Cyclic 16";
        cnn.networkType = "base_cyclic";
        cnn.description = "Sequential network with 16 filters and cyclic layers for better rotation invariance." +
                "\nDesigned for particle classification at CEREGE." +
                "\nRecommended image input is size 128 x 128 greyscale.";
        cnn.numFilters = 16;
        addDefaultInputSize(cnn, 128, 128, 1);
        networkInfos.add(cnn);

        cnn = new ModelDefaults();
        cnn.name = "ResNet Cyclic 4";
        cnn.networkType = "resnet_cyclic";
        cnn.description = "Base cyclic network with 4 filters and  the addition of residual connections." +
                "\nDesigned for particle classification at CEREGE." +
                "\nRecommended image input is size 128 x 128 greyscale.";
        cnn.numFilters = 4;
        addDefaultInputSize(cnn, 128, 128, 1);
        networkInfos.add(cnn);

        cnn = new ModelDefaults();
        cnn.name = "ResNet Cyclic 8";
        cnn.networkType = "resnet_cyclic";
        cnn.description = "Base cyclic network with 8 filters and  the addition of residual connections." +
                "\nDesigned for particle classification at CEREGE." +
                "\nRecommended image input is size 128 x 128 greyscale.";
        cnn.numFilters = 8;
        addDefaultInputSize(cnn, 128, 128, 1);
        networkInfos.add(cnn);

        cnn = new ModelDefaults();
        cnn.name = "ResNet Cyclic 16";
        cnn.networkType = "resnet_cyclic";
        cnn.description = "Base cyclic network with 16 filters and the addition of residual connections." +
                "\nDesigned for particle classification at CEREGE." +
                "\nRecommended image input is size 128 x 128 greyscale.";
        cnn.numFilters = 16;
        addDefaultInputSize(cnn, 128, 128, 1);
        networkInfos.add(cnn);

        cnn = new ModelDefaults();
        cnn.name = "ResNet18";
        cnn.networkType = "resnet18";
        cnn.description = "Residual network (ResNet) with 18 layers.";
        addDefaultInputSize(cnn, 224, 224, 3);
        networkInfos.add(cnn);

        cnn = new ModelDefaults();
        cnn.name = "ResNet34";
        cnn.networkType = "resnet34";
        cnn.description = "Residual network (ResNet) with 34 layers.";
        addDefaultInputSize(cnn, 224, 224, 3);
        networkInfos.add(cnn);

        cnn = new ModelDefaults();
        cnn.name = "ResNet50";
        cnn.networkType = "resnet50";
        cnn.description = "Residual network (ResNet) with 50 layers.";
        addDefaultInputSize(cnn, 224, 224, 3);
        networkInfos.add(cnn);

        cnn = new ModelDefaults();
        cnn.name = "SEResNet18";
        cnn.networkType = "seresnet18";
        cnn.description = "Squeeze Excitation Residual network (ResNet) with 18 layers.";
        addDefaultInputSize(cnn, 224, 224, 3);
        networkInfos.add(cnn);

        cnn = new ModelDefaults();
        cnn.name = "SEResNet34";
        cnn.networkType = "seresnet34";
        cnn.description = "Squeeze Excitation Residual network (ResNet) with 34 layers.";
        addDefaultInputSize(cnn, 224, 224, 3);
        networkInfos.add(cnn);

        cnn = new ModelDefaults();
        cnn.name = "SEResNet50";
        cnn.networkType = "seresnet50";
        cnn.description = "Squeeze Excitation Residual network (ResNet) with 50 layers.";
        addDefaultInputSize(cnn, 224, 224, 3);
        networkInfos.add(cnn);

        cnn = new ModelDefaults();
        cnn.name = "VGG16";
        cnn.networkType = "vgg16";
        cnn.description = "VGG network with 16 layers";
        addDefaultInputSize(cnn, 224, 224, 3);
        networkInfos.add(cnn);

        cnn = new ModelDefaults();
        cnn.name = "VGG19";
        cnn.networkType = "vgg19";
        cnn.description = "VGG network with 19 layers";
        addDefaultInputSize(cnn, 224, 224, 3);
        networkInfos.add(cnn);

        return networkInfos;
    }
}
