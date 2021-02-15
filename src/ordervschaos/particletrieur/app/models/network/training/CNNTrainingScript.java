package ordervschaos.particletrieur.app.models.network.training;

import java.io.IOException;
import java.nio.file.Files;

public class CNNTrainingScript {

    //Name
    public String name = "";
    public String description = "";

    //Dataset
    public String datasetSource = "";
    public int datasetMinCount = 10;
    public boolean datasetMapOthers = false;
    public double datasetValSplit = 0.2;
    public int datasetRandomSeed = 0;
    public boolean datasetUseMemmap = false;

    //CNN
    public String cnnId = "base_cyclic";
    public int[] cnnImgShape = new int[]{224, 224, 3};
    public String cnnImgType = "rgb";
    public int cnnFilters = 4;
    public int cnnBlocks = -1;
    public int cnnDense = -1;
    public boolean cnnUseBatchNorm = true;
    public String cnnGlobalPooling = "avg";
    public String cnnActivation = "relu";
    public boolean cnnUseAsoftmax = false;

    //Training
    public int trainingBatchSize = 32;
    public int trainingMaxEpochs = 10000;
    public int trainingAlrEpochs = 10;
    public int trainingAlrDrops = 4;
    public boolean trainingMonitorValLoss = false;
    public boolean trainingUseClassWeights = true;
    public boolean trainingUseClassUndersampling = false;
    public boolean trainingUseAugmentation = true;

    //Augmentation
    public int[] augmentationRotation = new int[]{0, 360};
    public double[] augmentationGain = new double[]{0.8, 1.0, 1.2};
    public double[] augmentationGamma = new double[]{0.5, 1.0, 2.0};
    public double[] augmentationBias = new double[]{};
    public double[] augmentationZoom = new double[]{0.9, 1, 1.1};
    public double[] augmentationGaussianNoise = new double[]{};
    public int[] augmentationRandomCrop = new int[]{};
    public int[] augmentationOrigImgShape = new int[]{224, 224, 3};

    //Output
    public String outputDirectory = null;
    public boolean outputSaveModel = true;
    public boolean outputSaveMislabeled = true;


    public String getScript() throws IOException {
        String script =
                "import os\n" +
                        "os.environ[\"TF_CPP_MIN_LOG_LEVEL\"] = \"3\"\n" +
                        "\n" +
                        "from miso.training.parameters import MisoParameters\n" +
                        "from miso.training.trainer import train_image_classification_model\n" +
                        "\n" +
                        "tp = MisoParameters()\n" +
                        "\n" +
                        "# -----------------------------------------------------------------------------\n" +
                        "# Name \n" +
                        "# -----------------------------------------------------------------------------\n" +
                        "# Name of this training run (leave as \"\" to auto-generate\n" +
                        "tp.name = " + quote(name) + "\n" +
                        "# Description of this training run (leave as \"\" to auto-generate\n" +
                        "tp.description = " + convert(description) + "\n" +
                        "# -----------------------------------------------------------------------------\n" +
                        "# Dataset\n" +
                        "# -----------------------------------------------------------------------------\n" +
                        "# Source directory (local folder or download link to dataset)\n" +
                        "tp.dataset.source = " + quote(datasetSource) + "\n" +
                        "# Minimum number of images to include in a class\n" +
                        "tp.dataset.min_count = " + convert(datasetMinCount) + "\n" +
                        "# Whether to map the images in class with not enough examples to an \"others\" class\n" +
                        "tp.dataset.map_others = " + convert(datasetMapOthers) + "\n" +
                        "# Fraction of dataset used for validation\n" +
                        "tp.dataset.val_split = " + convert(datasetValSplit) + "\n" +
                        "# Random seed used to split the dataset into train and validation\n" +
                        "tp.dataset.random_seed = " + convert(datasetRandomSeed) + "\n" +
                        "# Set to a local directory to stored the loaded dataset on disk instead of in memory\n" +
                        "tp.dataset.memmap_directory = " + getMemmapDirectory(datasetUseMemmap) + "\n" +
                        "\n" +
                        "# -----------------------------------------------------------------------------\n" +
                        "# CNN\n" +
                        "# -----------------------------------------------------------------------------\n" +
                        "# CNN type\n" +
                        "# Transfer learning:\n" +
                        "# - resnet50_tl\n" +
                        "# - resnet50_cyclic_tl\n" +
                        "# - resnet50_cyclic_gain_tl\n" +
                        "# Full network (custom)\n" +
                        "# - base_cyclic\n" +
                        "# - resnet_cyclic\n" +
                        "# Full network (keras applications / qubvel image_classifiers)\n" +
                        "# - resnet[18,34,50]\n" +
                        "# - vgg[16,19]\n" +
                        "# - efficientnetB[0-7]\n" +
                        "tp.cnn.id = " + quote(cnnId) + "\n" +
                        "# Input image shape, set to None to use default size ([128, 128, 1] for custom, [224, 224, 3] for others)\n" +
                        "tp.cnn.img_shape = " + convert(cnnImgShape) + "\n" +
                        "# Input image colour space [greyscale/rgb]\n" +
                        "tp.cnn.img_type = " + convert(cnnImgType) + "\n" +
                        "# Number of filters in first block (custom networks)\n" +
                        "tp.cnn.filters = " + convert(cnnFilters) + "\n" +
                        "# Number of blocks (custom networks), set to None for automatic selection\n" +
                        "tp.cnn.blocks = " + convert(cnnBlocks) + "\n" +
                        "# Size of dense layers (custom networks / transfer learning) as a list, e.g. [512, 512] for two dense layers size 512\n" +
                        "tp.cnn.dense = " + convert(cnnDense) + "\n" +
                        "# Whether to use batch normalisation\n" +
                        "tp.cnn.use_batch_norm = " + convert(cnnUseBatchNorm) + "\n" +
                        "# Type of pooling [avg, max, none]\n" +
                        "tp.cnn.global_pooling = " + convert(cnnGlobalPooling) + "\n" +
                        "# Type of activation\n" +
                        "tp.cnn.activation = " + convert(cnnActivation) + "\n" +
                        "# Use A-Softmax\n" +
                        "tp.cnn.use_asoftmax = " + convert(cnnUseAsoftmax) + "\n" +
                        "\n" +
                        "# -----------------------------------------------------------------------------\n" +
                        "# Training\n" +
                        "# -----------------------------------------------------------------------------\n" +
                        "# Number of images for each training step\n" +
                        "tp.training.batch_size = " + convert(trainingBatchSize) + "\n" +
                        "# Number of epochs after which training is stopped regardless\n" +
                        "tp.training.max_epochs = " + convert(trainingMaxEpochs) + "\n" +
                        "# Number of epochs to monitor for no improvement by the adaptive learning rate scheduler.\n" +
                        "# After no improvement for this many epochs, the learning rate is dropped by half\n" +
                        "tp.training.alr_epochs = " + convert(trainingAlrEpochs) + "\n" +
                        "# Number of learning rate drops after which training is suspended\n" +
                        "tp.training.alr_drops = " + convert(trainingAlrDrops) + "\n" +
                        "# Monitor the validation loss instead?\n" +
                        "tp.training.monitor_val_loss = " + convert(trainingMonitorValLoss) + "\n" +
                        "# Use class weighting?\n" +
                        "tp.training.use_class_weights = " + convert(trainingUseClassWeights) + "\n" +
                        "# Use class balancing via random over sampling? (Overrides class weights)\n" +
                        "tp.training.use_class_undersampling = " + convert(trainingUseClassUndersampling) + "\n" +
                        "# Use train time augmentation?\n" +
                        "tp.training.use_augmentation = " + convert(trainingUseAugmentation) + "\n" +
                        "\n" +
                        "# -----------------------------------------------------------------------------\n" +
                        "# Augmentation\n" +
                        "# -----------------------------------------------------------------------------\n" +
                        "# Setting depends on the size of list passed:\n" +
                        "# - length 2, e.g. [low, high] = random value between low and high\n" +
                        "# - length 3 or more, e.g. [a, b, c] = choose a random value from this list\n" +
                        "# Rotation\n" +
                        "tp.augmentation.rotation = " + convert(augmentationRotation) + "\n" +
                        "# Gain: I' = I * gain\n" +
                        "tp.augmentation.gain = " + convert(augmentationGain) + "\n" +
                        "# Gamma: I' = I ^ gamma\n" +
                        "tp.augmentation.gamma = " + convert(augmentationGamma) + "\n" +
                        "# Bias: I' = I + bias\n" +
                        "tp.augmentation.bias = " + convert(augmentationBias) + "\n" +
                        "# Zoom: I'[x,y] = I[x/zoom, y/zoom]\n" +
                        "tp.augmentation.zoom = " + convert(augmentationZoom) + "\n" +
                        "# Gaussian noise std deviation\n" +
                        "tp.augmentation.gaussian_noise = " + convert(augmentationGaussianNoise) + "\n" +
                        "# The parameters for the following are not random\n" +
                        "# Random crop, e.g. [224, 224, 3]\n" +
                        "# If random crop is used, you MUST set the original image size that the crop is taken from\n" +
                        "tp.augmentation.random_crop = " + convert(augmentationRandomCrop) + "\n" +
                        "tp.augmentation.orig_img_shape = " + convert(augmentationOrigImgShape) + "\n" +
                        "\n" +
                        "# -----------------------------------------------------------------------------\n" +
                        "# Output\n" +
                        "# -----------------------------------------------------------------------------\n" +
                        "# Directory to save output\n" +
                        "tp.output.save_dir = " + quote(outputDirectory) + "\n" +
                        "# Save model?\n" +
                        "tp.output.save_model = " + convert(outputSaveModel) + "\n" +
                        "# Save the mislabelled image analysis?\n" +
                        "tp.output.save_mislabeled = " + convert(outputSaveMislabeled) + "\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "# Train the model!!!\n" +
                        "import time\n" +
                        "import tempfile\n" +
                        "from datetime import datetime\n" +
                        "import numpy as np\n" +
                        "from miso.utils import singleton\n" +
                        "# Guard for windows\n" +
                        "if __name__ == \"__main__\":\n" +
                        "    # Only one script at a time\n" +
                        "    start = time.time()\n" +
                        "    done = False\n" +
                        "    while not done:\n" +
                        "        try:\n" +
                        "            fn = os.path.join(tempfile.gettempdir(), \"miso.lock\")\n" +
                        "            with open(fn, 'w+') as fh:\n" +
                        "                fh.write('miso')\n" +
                        "            try:\n" +
                        "                os.chmod(fn, 0o777)\n" +
                        "            except OSError:\n" +
                        "                pass\n" +
                        "            lock = singleton.SingleInstance(lockfile=fn)\n" +
                        "            print()\n" +
                        "            train_image_classification_model(tp)\n" +
                        "            done = True\n" +
                        "        except singleton.SingleInstanceException:\n" +
                        "            print(\"{}: Another script is already running, trying again in 10 seconds. ({}s waiting)\\r\".format(datetime.now(), np.round(time.time() - start)), end='')\n" +
                        "            time.sleep(10)";
        return script;
    }

    public String quote(String str) {
        return "r\"" + str + "\"";
    }

    public String convert(String str) {
        if (str.equals("")) {
            return "None";
        } else {
            return "\"" + str + "\"";
        }
    }

    public String convert(int i) {
        if (i == -1) {
            return "None";
        } else {
            return Integer.toString(i);
        }
    }

    public String convert(boolean b) {
        if (b) {
            return "True";
        } else {
            return "False";
        }
    }

    public String convert(double d) {
        if (d == -1) {
            return "None";
        } else {
            return Double.toString(d);
        }
    }

    public String convert(int[] arr) {
        if (arr.length == 0) {
            return "None";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < arr.length; i++) {
                sb.append(arr[i]);
                if (i < arr.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        }
    }

    public String convert(double[] arr) {
        if (arr.length == 0) {
            return "None";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < arr.length; i++) {
                sb.append(arr[i]);
                if (i < arr.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        }
    }

    public String getMemmapDirectory(boolean b) throws IOException {
        if (!b) {
            return "None";
        } else {
            return Files.createTempDirectory("miso_").toString();
        }
    }
}
