package particletrieur.services.network;

import particletrieur.models.network.training.ModelDefaults;

import java.util.ArrayList;
import java.util.List;

public class TrainingNetworkDescriptionService {

    public static void addDefaultInputSize(ModelDefaults info, int height, int width, int channels) {
        info.imageHeight = height;
        info.imageWidth = width;
        info.imageChannels = channels;
    }

    public static List<ModelDefaults> getCustomNetworks() {

        ArrayList<ModelDefaults> networkInfos = new ArrayList<>();
        ModelDefaults cnn;

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

        return networkInfos;
    }

    public static List<ModelDefaults> getDescriptions() {

        ArrayList<ModelDefaults> networkInfos = new ArrayList<>();
        ModelDefaults cnn;

        cnn = new ModelDefaults();
        cnn.name = "ResNet50";
        cnn.networkType = "resnet50";
        cnn.description = "Residual network (ResNet) with 50 layers.";
        addDefaultInputSize(cnn, 224, 224, 3);
        networkInfos.add(cnn);

        ModelDefaults resnet50v2 = new ModelDefaults();
        resnet50v2.name = "ResNet50V2";
        resnet50v2.networkType = "resnet50v2";
        resnet50v2.description = "Residual network (ResNet) V2 with 50 layers.";
        addDefaultInputSize(resnet50v2, 224, 224, 3);
        networkInfos.add(resnet50v2);

        // ResNet101
        ModelDefaults resnet101 = new ModelDefaults();
        resnet101.name = "ResNet101";
        resnet101.networkType = "resnet101";
        resnet101.description = "Residual network (ResNet) with 101 layers.";
        addDefaultInputSize(resnet101, 224, 224, 3);
        networkInfos.add(resnet101);

        // ResNet101V2
        ModelDefaults resnet101v2 = new ModelDefaults();
        resnet101v2.name = "ResNet101V2";
        resnet101v2.networkType = "resnet101v2";
        resnet101v2.description = "Residual network (ResNet) V2 with 101 layers.";
        addDefaultInputSize(resnet101v2, 224, 224, 3);
        networkInfos.add(resnet101v2);

        // ResNet152
        ModelDefaults resnet152 = new ModelDefaults();
        resnet152.name = "ResNet152";
        resnet152.networkType = "resnet152";
        resnet152.description = "Residual network (ResNet) with 152 layers.";
        addDefaultInputSize(resnet152, 224, 224, 3);
        networkInfos.add(resnet152);

        // ResNet152V2
        ModelDefaults resnet152v2 = new ModelDefaults();
        resnet152v2.name = "ResNet152V2";
        resnet152v2.networkType = "resnet152v2";
        resnet152v2.description = "Residual network (ResNet) V2 with 152 layers.";
        addDefaultInputSize(resnet152v2, 224, 224, 3);
        networkInfos.add(resnet152v2);

        // InceptionResNetV2
        ModelDefaults inceptionResNetV2 = new ModelDefaults();
        inceptionResNetV2.name = "InceptionResNetV2";
        inceptionResNetV2.networkType = "inceptionresnetV2";
        inceptionResNetV2.description = "Inception-ResNet V2, a convolutional neural network.";
        addDefaultInputSize(inceptionResNetV2, 299, 299, 3);
        networkInfos.add(inceptionResNetV2);

        // MobileNet
        ModelDefaults mobileNet = new ModelDefaults();
        mobileNet.name = "MobileNet";
        mobileNet.networkType = "mobilenet";
        mobileNet.description = "MobileNet, a network optimized for mobile and embedded vision applications.";
        addDefaultInputSize(mobileNet, 224, 224, 3);
        networkInfos.add(mobileNet);

        // MobileNetV2
        ModelDefaults mobileNetV2 = new ModelDefaults();
        mobileNetV2.name = "MobileNetV2";
        mobileNetV2.networkType = "mobilenetV2";
        mobileNetV2.description = "MobileNetV2 improves the state of the art performance of mobile models.";
        addDefaultInputSize(mobileNetV2, 224, 224, 3);
        networkInfos.add(mobileNetV2);

        // DenseNet121
        ModelDefaults denseNet121 = new ModelDefaults();
        denseNet121.name = "DenseNet121";
        denseNet121.networkType = "densenet121";
        denseNet121.description = "Densely connected convolutional network (DenseNet) with 121 layers.";
        addDefaultInputSize(denseNet121, 224, 224, 3);
        networkInfos.add(denseNet121);

        // DenseNet169
        ModelDefaults denseNet169 = new ModelDefaults();
        denseNet169.name = "DenseNet169";
        denseNet169.networkType = "densenet169";
        denseNet169.description = "Densely connected convolutional network (DenseNet) with 169 layers.";
        addDefaultInputSize(denseNet169, 224, 224, 3);
        networkInfos.add(denseNet169);

        // DenseNet201
        ModelDefaults denseNet201 = new ModelDefaults();
        denseNet201.name = "DenseNet201";
        denseNet201.networkType = "densenet201";
        denseNet201.description = "Densely connected convolutional network (DenseNet) with 201 layers.";
        addDefaultInputSize(denseNet201, 224, 224, 3);
        networkInfos.add(denseNet201);

        // NASNetMobile
        ModelDefaults nasNetMobile = new ModelDefaults();
        nasNetMobile.name = "NASNetMobile";
        nasNetMobile.networkType = "nasnetmobile";
        nasNetMobile.description = "Neural Architecture Search Network (NASNet) optimized for mobile.";
        addDefaultInputSize(nasNetMobile, 224, 224, 3);
        networkInfos.add(nasNetMobile);

        // NASNetLarge
        ModelDefaults nasNetLarge = new ModelDefaults();
        nasNetLarge.name = "NASNetLarge";
        nasNetLarge.networkType = "nasnetlarge";
        nasNetLarge.description = "Neural Architecture Search Network (NASNet) large model.";
        addDefaultInputSize(nasNetLarge, 331, 331, 3);
        networkInfos.add(nasNetLarge);

        // EfficientNetB0 to EfficientNetB7
        String[] efficientNetModels = {"B0", "B1", "B2", "B3", "B4", "B5", "B6", "B7"};
        int[] efficientNetSizes = {224, 240, 260, 300, 380, 456, 528, 600};
        for (int i = 0; i < efficientNetModels.length; i++) {
            ModelDefaults efficientNet = new ModelDefaults();
            efficientNet.name = "EfficientNet" + efficientNetModels[i];
            efficientNet.networkType = "efficientnetb" + i;
            efficientNet.description = "EfficientNet" + efficientNetModels[i] + ", scaling up networks in a more structured manner.";
            addDefaultInputSize(efficientNet, efficientNetSizes[i], efficientNetSizes[i], 3);
            networkInfos.add(efficientNet);
        }

        // ConvNeXt Tiny to XLarge
        String[] convNextModels = {"Tiny", "Small", "Base", "Large", "XLarge"};
        for (String model : convNextModels) {
            ModelDefaults convNext = new ModelDefaults();
            convNext.name = "ConvNeXt" + model;
            convNext.networkType = "convnext" + model.toLowerCase();
            convNext.description = "ConvNeXt " + model + ", a convolutional network design for visual recognition.";
            addDefaultInputSize(convNext, 224, 224, 3);
            networkInfos.add(convNext);
        }

        return networkInfos;
    }
}
