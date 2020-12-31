package ordervschaos.particletrieur.app.models.network.training;

public class TrainingLaunchInfo {

    public String networkType = "basic_cyclic";
    public String name = "";
    public String description = "";

    public int imageHeight = 128;
    public int imageWidth = 128;
    public int imageChannels = 1;

    public int numFilters = 4;

    public int batchSize = 64;
    public int maxEpochs = 10000;
    public int alrEpochs = 40;
    public int alrDrops = 4;

    public int minCountPerClass = 10;
    public double trainTestSplit = 0.2;
    public boolean mapOthers = false;
    public boolean useMemoryMapping = false;
    public boolean saveTrainingSet = false;
    public String memoryMappingDirectory = "";

    public boolean useAugmentation = true;
    public boolean useClassWeights = true;

    public boolean saveModel = true;
    public boolean saveMislabeled = true;

    public String inputSource = "";
    public String outputDirectory = "";
}
