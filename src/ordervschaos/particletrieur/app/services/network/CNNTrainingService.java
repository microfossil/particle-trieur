package ordervschaos.particletrieur.app.services.network;

import ordervschaos.particletrieur.app.App;
import ordervschaos.particletrieur.app.AppPreferences;
import ordervschaos.particletrieur.app.controls.BasicDialogs;
import ordervschaos.particletrieur.app.models.network.training.TrainingLaunchInfo;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.commons.lang3.SystemUtils;
import org.tensorflow.Session;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class CNNTrainingService {

    Process process = null;
    private static AppPreferences appPrefs = new AppPreferences();

    public CNNTrainingService() {

    }

    private static void watch(final Process process) {
        new Thread(() -> {
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            try {
                while ((line = input.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public String getLaunchInfoScript(TrainingLaunchInfo info) {
        StringBuilder sb = new StringBuilder();
//        String basePath = (new File(getAnacondaInstallationLocation())).getParent();
//        sb.append("import sys\nprint('Python %s on %s' % (sys.version, sys.platform))\n");
//        sb.append("sys.path.extend([r'" + basePath + "'])\n");

        sb.append("from miso.training.model_params import default_params\n" +
                "from miso.training.model_trainer import train_image_classification_model\n" +
                "from miso.data.download import download_images\n" +
                "\n" +
                "params = default_params()\n\n");

        //Network
        sb.append(String.format("params['type'] = '%s'\n", info.networkType));
        sb.append(String.format("params['name'] = '%s'\n", info.name));
        sb.append(String.format("params['description'] = %s\n", info.description.equals("") ? "None" : info.description));
        sb.append(String.format("params['filters'] = %d\n", info.numFilters));

        //Input
        sb.append(String.format("params['input_source'] = r'%s'\n", info.inputSource));
        sb.append(String.format("params['data_min_count'] = %d\n", info.minCountPerClass));
        sb.append(String.format("params['data_split'] = %f\n", info.trainTestSplit).replace(',','.'));
        sb.append(String.format("params['data_map_others'] = %s\n", info.mapOthers ? "True" : "False"));
        sb.append(String.format("params['use_mmap'] = %s\n", info.useMemoryMapping ? "True" : "False"));

        //Output
        sb.append(String.format("params['output_dir'] = r'%s'\n", info.outputDirectory));
        sb.append(String.format("params['save_model'] = %s\n", info.saveModel ? "'frozen'" : "None"));
        sb.append(String.format("params['save_mislabeled'] = %s\n", info.saveMislabeled ? "True" : "False"));

        //Image
        sb.append(String.format("params['img_height'] = %d\n", info.imageHeight));
        sb.append(String.format("params['img_width'] = %d\n", info.imageWidth));
        sb.append(String.format("params['img_channels'] = %d\n", info.imageChannels));

        //Augmentation
        sb.append(String.format("params['use_augmentation'] = %s\n", info.useAugmentation ? "True" : "False"));

        //Training
        sb.append(String.format("params['use_class_weights'] = %s\n", info.useClassWeights ? "True" : "False"));
        sb.append(String.format("params['alr_epochs'] = %d\n", info.alrEpochs));
        sb.append(String.format("params['batch_size'] = %d\n", info.batchSize));

        //Run
        sb.append("\nmodel, vector_model, data_source, result = train_image_classification_model(params)\n");

        return sb.toString();
    }

    public String getFlowcamScript(String input, String species, String output) {
        StringBuilder sb = new StringBuilder();
        sb.append("from miso.data.flowcam import process\n");
        sb.append(String.format("process(r\"%s\", r\"%s\", r\"%s\")", input, species, output));
        return sb.toString();
    }

    public static void executeInTerminal(String command) throws IOException, InterruptedException {
        final String[] wrappedCommand;
        if (SystemUtils.IS_OS_WINDOWS) {
            wrappedCommand = new String[]{ "cmd", "/c", "start", "/wait", "cmd.exe", "/K", command };
        }
        else if (SystemUtils.IS_OS_MAC_OSX) {
            wrappedCommand = new String[]{"osascript",
                    "-e", "tell application \"Terminal\" to activate",
                    "-e", "tell application \"Terminal\" to do script \"" + command + ";exit\""};
        }
        else if (SystemUtils.IS_OS_LINUX) {
            wrappedCommand = new String[]{
                    "x-terminal-emulator",
                    "-e",
                    "bash",
                    "-l",
                    "-c",
                    command + "; echo ~~~ Script complete, press any key to close ~~~; read line"};
        }
        else {
            throw new RuntimeException("Unsupported OS");
        }
        Process process = Runtime.getRuntime().exec(wrappedCommand);
        System.out.println(command);
//        Process process = new ProcessBuilder(wrappedCommand)
//                .redirectErrorStream(true)
//                .start();
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                System.out.println(line); // Your superior logging approach here
//            }
//        }
//        return process.waitFor();
    }

    public static String getAnacondaInstallationLocation() {
        String windowsA = System.getProperty("user.home") + "\\Anaconda3\\envs\\miso\\python.exe";
        String windowsB = System.getProperty("user.home") + "\\AppData\\Local\\Continuum\\Anaconda3\\envs\\miso\\python.exe";
        String macA = System.getProperty("user.home") + "/anaconda3/envs/miso/bin/python";
        String macB = System.getProperty("user.home") + "/opt/anaconda3/envs/miso/bin/python";
        String linuxA = System.getProperty("user.home") + "/anaconda3/envs/miso/bin/python";
        String linuxB = "/usr/local/anaconda3/envs/miso/bin/python";
        String commonA = appPrefs.getPythonPath();
        if (SystemUtils.IS_OS_WINDOWS) {
            if (!commonA.equals("") && new File(commonA).exists()) return commonA;
            else if (new File(windowsA).exists()) return windowsA;
            else if (new File(windowsB).exists()) return windowsB;
        }
        else if (SystemUtils.IS_OS_MAC_OSX) {
            if (!commonA.equals("") && new File(commonA).exists()) return commonA;
            if (new File(macA).exists()) return macA;
            else if (new File(macB).exists()) return macB;
        }
        else if (SystemUtils.IS_OS_LINUX) {
            if (!commonA.equals("") && new File(commonA).exists()) return commonA;
            else if (new File(linuxA).exists()) return linuxA;
            else if (new File(linuxB).exists()) return linuxB;
        }
        return null;
    }

    public void showAnacondaNotFoundError() {
        if (SystemUtils.IS_OS_WINDOWS) {
            BasicDialogs.ShowError("Error", "Python not found, please check your installation or update the location\n" +
                    "\n" +
                    "1) %HOMEPATH%\\Anaconda3\\\n" +
                    "2) %HOMEPATH%\\AppData\\Local\\Continuum\\Anaconda3\\\n" +
                    "3) " + appPrefs.getPythonPath());
        }
        else if (SystemUtils.IS_OS_MAC_OSX) {
            BasicDialogs.ShowError("Error", "Python not found, please check your installation or update the location\n" +
                    "\n" +
                    "1) ./anaconda3/\n" +
                    "2) ./opt/anaconda3/\n" +
                    "3) " + appPrefs.getPythonPath());
        }
        else if (SystemUtils.IS_OS_LINUX) {
            BasicDialogs.ShowError("Error", "Python not found, please check your installation or update the location\n" +
                    "\n" +
                    "1) ~/anaconda3/envs/miso/bin/python\n" +
                    "2) ~/.conda/envs/miso/bin/python\n" +
                    "3) " + appPrefs.getPythonPath());
        }
        else {
            BasicDialogs.ShowError("Error", "Your operating system is neither Windows, Mac or Linux.");
        }
    }

    public void launch(TrainingLaunchInfo info) {
        try {
            String script = getLaunchInfoScript(info);
            File temp = File.createTempFile("script", ".py");
            BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
            writer.write(script);
            writer.close();

            String pythonPath = getAnacondaInstallationLocation();
            if (pythonPath == null) {
                showAnacondaNotFoundError();
                return;
            }
            String basePath = (new File(pythonPath)).getParent();
            String anacondaPath = (new File(pythonPath)).getParentFile().getParentFile().getParentFile().getPath();
            if (SystemUtils.IS_OS_WINDOWS) {
                executeInTerminal("call \"" + anacondaPath + "\\Scripts\\activate.bat\" miso && " + "cd /d \"" + basePath + "\" && python -W ignore -u " + String.format("\"%s\"", temp.getAbsolutePath()));
            }
            else if (SystemUtils.IS_OS_MAC_OSX) {
                executeInTerminal("conda activate miso && cd \"" + basePath + "\" && python -u " + String.format("\"%s\"", temp.getAbsolutePath()));
            }
            else if (SystemUtils.IS_OS_LINUX) {
                executeInTerminal(pythonPath + " -u " + String.format("\"%s\"", temp.getAbsolutePath()));
            }
            else {
                throw new RuntimeException("Unsupported OS");
            }
        }
        catch (Exception ex) {
            BasicDialogs.ShowException("Error launching training", ex);
        }
    }

    public void launchFlowcam(String input, String species, String output) {
        try {
            String script = getFlowcamScript(input, species, output);
            File temp = File.createTempFile("script", ".py");
            BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
            writer.write(script);
            writer.close();

//            String pythonPath = getAnacondaInstallationLocation();
//            if (pythonPath == null) {
//                showAnacondaNotFoundError();
//                return;
//            }
//            String command = String.format("-i \"%s\" -s \"%s\" -o \"%s\"", input, species, output);
//            String basePath = (new File(pythonPath)).getParent();
//            String anacondaPath = (new File(pythonPath)).getParentFile().getParentFile().getParentFile().getPath();
//            if (SystemUtils.IS_OS_WINDOWS) {
//                executeInTerminal("call \"" + anacondaPath + "\\Scripts\\activate.bat\" miso && " + "cd /d \"" + basePath + "\" && python -u -m miso.data.flowcam " + command);
//            }
//            else if (SystemUtils.IS_OS_MAC_OSX) {
//                executeInTerminal("conda activate miso && cd \"" + basePath + "\" && python -u miso.data.flowcam " + command);
//            }
//            else if (SystemUtils.IS_OS_LINUX) {
//                executeInTerminal(pythonPath + " -u " + String.format("\"%s\"", temp.getAbsolutePath()));
//            }
//            else {
//                throw new RuntimeException("Unsupported OS");
//            }
            String pythonPath = getAnacondaInstallationLocation();
            if (pythonPath == null) {
                showAnacondaNotFoundError();
                return;
            }
            String basePath = (new File(pythonPath)).getParent();
            String anacondaPath = (new File(pythonPath)).getParentFile().getParentFile().getParentFile().getPath();
            if (SystemUtils.IS_OS_WINDOWS) {
                executeInTerminal("call \"" + anacondaPath + "\\Scripts\\activate.bat\" miso && " + "cd /d \"" + basePath + "\" && python -W ignore -u " + String.format("\"%s\"", temp.getAbsolutePath()));
            }
            else if (SystemUtils.IS_OS_MAC_OSX) {
                executeInTerminal("conda activate miso && cd \"" + basePath + "\" && python -u " + String.format("\"%s\"", temp.getAbsolutePath()));
            }
            else if (SystemUtils.IS_OS_LINUX) {
                executeInTerminal(pythonPath + " -u " + String.format("\"%s\"", temp.getAbsolutePath()));
            }
            else {
                throw new RuntimeException("Unsupported OS");
            }
        }
        catch (Exception ex) {
            BasicDialogs.ShowException("Error launching training", ex);
        }
    }

    public void updateMISO() {
        try {
            String pythonPath = getAnacondaInstallationLocation();
            if (pythonPath == null) {
                showAnacondaNotFoundError();
                return;
            }
            executeInTerminal(pythonPath + " -m pip install -U git+http://www.github.com/microfossil/particle-classification");

//            if (SystemUtils.IS_OS_WINDOWS) {
//                executeInTerminal(pythonPath + " -m pip install -U git+http://www.github.com/microfossil/particle-classification");
//            }
//            else if (SystemUtils.IS_OS_MAC_OSX) {
//                executeInTerminal(pythonPath + " -m pip install -U git+http://www.github.com/microfossil/particle-classification");
//            }
//            else if (SystemUtils.IS_OS_LINUX) {
//                executeInTerminal(pythonPath + " -m pip install -U git+http://www.github.com/microfossil/particle-classification");
//            }
        }
        catch (Exception ex) {
            BasicDialogs.ShowException("Error updating MISO library", ex);
        }
    }

    public Service<Void> createLaunchService(TrainingLaunchInfo info) {
        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        String script = getLaunchInfoScript(info);
                        File temp = File.createTempFile("script", ".py");
                        BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
                        writer.write(script);
                        writer.close();
                        ArrayList<String> commands = new ArrayList<>();
                        return null;
                    }
                };
                return task;
            }
        };
        return service;
    }

    public void terminate() {
        if (process != null) {
            process.destroyForcibly();
        }
    }
}
