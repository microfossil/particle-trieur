package main.java.app.models.network.training;

public class GPUStatus {

    public int power;
    public int maxPower;
    public int memory;
    public int maxMemory;
    public int usagePercentage;

    public double powerPercentage;
    public double memoryPercentage;

    public GPUStatus(int power, int maxPower, int memory, int maxMemory, int usagePercentage) {
        this.power = power;
        this.maxPower = maxPower;
        this.memory = memory;
        this.maxMemory = maxMemory;
        this.usagePercentage = usagePercentage;

        this.powerPercentage = (double) power / maxPower * 100;
        this.memoryPercentage = (double) memory / maxMemory * 100;
    }
}
