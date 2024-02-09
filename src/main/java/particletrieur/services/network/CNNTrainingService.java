package particletrieur.services.network;

import particletrieur.AppPreferences;
import particletrieur.controls.dialogs.BasicDialogs;
import particletrieur.models.network.training.GPUStatus;
import particletrieur.models.network.training.CNNTrainingScript;
import org.apache.commons.lang3.SystemUtils;

import javax.script.ScriptException;
import java.io.*;

public class CNNTrainingService {

    Process process = null;
    private static AppPreferences appPrefs = new AppPreferences();

    public static String ENV = "miso";
    public static String PACKAGE = "miso==3.1.16";

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

    public String getFlowcamScript(String input, String output, String campaign, String species) {
        StringBuilder sb = new StringBuilder();
        sb.append("from miso.utils.flowcam import process_dir\n");
        sb.append(String.format("process_dir(r\"%s\", r\"%s\", r\"%s\")", input, output, campaign, species));
        return sb.toString();
    }

    public static void executeInTerminal(String command) throws IOException, InterruptedException, ScriptException {
        final String[] wrappedCommand;
        if (SystemUtils.IS_OS_WINDOWS) {
            wrappedCommand = new String[]{"cmd", "/c", "start", "/wait", "cmd.exe", "/K", command};
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            command = command.replace("\"", "\\\"");
            wrappedCommand = new String[]{"osascript",
                    "-e", "tell application \"Terminal\" to activate",
                    "-e", "tell application \"Terminal\" to do script \"" + command + ";exit\""};
//            StringBuilder sb = new StringBuilder();
//            for (String str : wrappedCommand) {
//                sb.append(str);
//                sb.append(" ");
//            }
//            System.out.println(sb);
//            String script = "tell application \"Terminal\"\n" +
//                    "activate\n" +
//                    "do script \"" + command + ";exit\"\n" +
//                    "end tell";
//            ScriptEngineManager mgr = new ScriptEngineManager();
//            ScriptEngine engine = mgr.getEngineByName("AppleScriptEngine");
//            engine.eval(script);
        } else if (SystemUtils.IS_OS_LINUX) {
            wrappedCommand = new String[]{
                    "x-terminal-emulator",
                    "-e",
                    "bash",
                    "-l",
                    "-c",
                    command + "; echo ~~~ Script complete, press enter key to close ~~~; read line"};
        } else {
            throw new RuntimeException("Unsupported OS");
        }
//        System.out.println(command);
//        Process process = Runtime.getRuntime().exec(wrappedCommand);

        Process process = new ProcessBuilder(wrappedCommand)
                .redirectErrorStream(true)
                .start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // Your superior logging approach here
            }
        }
        process.waitFor();
//        return process.waitFor();
    }

    public static String executeInTerminalAndReturnOutput(String command) throws IOException {
//        final String[] wrappedCommand;
//        if (SystemUtils.IS_OS_WINDOWS) {
//            wrappedCommand = new String[]{ "cmd", "/c", "start", "/wait", "cmd.exe", "/K", command };
//        }
//        else if (SystemUtils.IS_OS_MAC_OSX) {
//            wrappedCommand = new String[]{"osascript",
//                    "-e", "tell application \"Terminal\" to activate",
//                    "-e", "tell application \"Terminal\" to do script \"" + command + ";exit\""};
//        }
//        else if (SystemUtils.IS_OS_LINUX) {
//            wrappedCommand = new String[]{
//                    "x-terminal-emulator",
//                    "-e",
//                    "bash",
//                    "-l",
//                    "-c",
//                    command + "; echo ~~~ Script complete, press any key to close ~~~; read line"};
//        }
//        else {
//            throw new RuntimeException("Unsupported OS");
//        }
//        System.out.println(command);

        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
        try {
            return streamToString(process.getInputStream());
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public static String streamToString(InputStream stream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = stream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }

    public static GPUStatus getNVIDIAStatus() {
        String command;
        if (SystemUtils.IS_OS_WINDOWS) {
            command = "\"c:\\Program Files\\NVIDIA Corporation\\NVSMI\\nvidia-smi.exe\"";
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            command = "nvidia-smi";
        } else if (SystemUtils.IS_OS_LINUX) {
            command = "nvidia-smi";
        } else {
            throw new RuntimeException("Unsupported OS");
        }

        try {
            String result = executeInTerminalAndReturnOutput(command);
            String[] lines = result.split("\r\n|\n|\r");
            String[] parts = lines[9].split("\\s+");
            GPUStatus status = new GPUStatus(
                    Integer.parseInt(parts[4].substring(0, parts[4].length() - 1)),
                    Integer.parseInt(parts[6].substring(0, parts[6].length() - 1)),
                    Integer.parseInt(parts[8].substring(0, parts[8].length() - 3)),
                    Integer.parseInt(parts[10].substring(0, parts[10].length() - 3)),
                    Integer.parseInt(parts[12].substring(0, parts[12].length()-1))
            );
            return status;
        }
        catch (IOException | ArrayIndexOutOfBoundsException ex) {
            return null;
        }
    }

    public static String getAnacondaInstallationLocation() {
        String windowsA = System.getProperty("user.home") + "\\Anaconda3\\envs\\" + ENV + "\\python.exe";
        String windowsB = System.getProperty("user.home") + "\\AppData\\Local\\Continuum\\Anaconda3\\envs\\" + ENV + "\\python.exe";
        String macA = System.getProperty("user.home") + "/anaconda3/envs/" + ENV + "/bin/python";
        String macB = System.getProperty("user.home") + "/opt/anaconda3/envs/" + ENV + "/bin/python";
        String linuxA = System.getProperty("user.home") + "/anaconda3/envs/" + ENV + "/bin/python";
        String linuxB = "/usr/local/anaconda3/envs/" + ENV + "/bin/python";
        String commonA = appPrefs.getPythonPath();
        if (SystemUtils.IS_OS_WINDOWS) {
            if (commonA != null && !commonA.equals("") && new File(commonA).exists()) return commonA;
            else if (new File(windowsA).exists()) return windowsA;
            else if (new File(windowsB).exists()) return windowsB;
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            if (commonA != null && !commonA.equals("") && new File(commonA).exists()) return commonA;
            else if (new File(macA).exists()) return macA;
            else if (new File(macB).exists()) return macB;
        } else if (SystemUtils.IS_OS_LINUX) {
            if (commonA != null && !commonA.equals("") && new File(commonA).exists()) return commonA;
            else if (new File(linuxA).exists()) return linuxA;
            else if (new File(linuxB).exists()) return linuxB;
        }
        return null;
    }

    public void showAnacondaNotFoundError() {
        if (SystemUtils.IS_OS_WINDOWS) {
            BasicDialogs.ShowError("Error", "Python not found, please check your installation or update the location\n" +
                    "\n" +
                    "1) %HOMEPATH%\\Anaconda3\\" + ENV + "\\python.exe\n" +
                    "2) %HOMEPATH%\\AppData\\Local\\Continuum\\Anaconda3\\envs\\" + ENV + "\\python.exe\n" +
                    "3) " + appPrefs.getPythonPath());
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            BasicDialogs.ShowError("Error", "Python not found, please check your installation or update the location\n" +
                    "\n" +
                    "1) ./anaconda3/envs/" + ENV + "/bin/python\n" +
                    "2) ./opt/anaconda3/" + ENV + "/bin/python\n" +
                    "3) " + appPrefs.getPythonPath());
        } else if (SystemUtils.IS_OS_LINUX) {
            BasicDialogs.ShowError("Error", "Python not found, please check your installation or update the location\n" +
                    "\n" +
                    "1) ~/anaconda3/envs/" + ENV + "/bin/python\n" +
                    "2) ~/.conda/envs/" + ENV + "/bin/python\n" +
                    "3) " + appPrefs.getPythonPath());
        } else {
            BasicDialogs.ShowError("Error", "Your operating system is neither Windows, Mac or Linux.");
        }
    }

    public void executePythonCommand(String command) throws IOException, InterruptedException, RuntimeException, ScriptException {
        String pythonPath = getAnacondaInstallationLocation();
        if (pythonPath == null) {
            showAnacondaNotFoundError();
            return;
        }
        String basePath = (new File(pythonPath)).getParent();
        String anacondaPath = (new File(pythonPath)).getParentFile().getParentFile().getParentFile().getPath();
        String terminalCommand;
        if (SystemUtils.IS_OS_WINDOWS) {
            terminalCommand = "call \"" + anacondaPath + "\\Scripts\\activate.bat\" " + ENV + " && " + "cd /d \"" + basePath + "\" && python -W ignore -u " + command;
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            terminalCommand = "conda activate " + ENV + " && cd \"" + basePath + "\" && python -u " + command;
        } else if (SystemUtils.IS_OS_LINUX) {
            terminalCommand = pythonPath + " -u " + command;
//            terminalCommand = "conda activate " + ENV + " && cd \"" + basePath + "\" && python -u " + command;
        } else {
            throw new RuntimeException("Unsupported OS");
        }
        executeInTerminal(terminalCommand);
    }

    public void executePythonCommands(String[] commands) throws IOException, InterruptedException, RuntimeException, ScriptException {
        String pythonPath = getAnacondaInstallationLocation();
        if (pythonPath == null) {
            showAnacondaNotFoundError();
            return;
        }
        String basePath = (new File(pythonPath)).getParent();
        String anacondaPath = (new File(pythonPath)).getParentFile().getParentFile().getParentFile().getPath();
        StringBuilder terminalCommand;
        if (SystemUtils.IS_OS_WINDOWS) {
            terminalCommand = new StringBuilder("call \"" + anacondaPath + "\\Scripts\\activate.bat\" " + ENV + " && " + "cd /d \"" + basePath + "\"");
            for (String command : commands) {
                terminalCommand.append(" && python -W ignore -u ").append(command);
            }
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            terminalCommand = new StringBuilder("conda activate " + ENV + " && cd \"" + basePath + "\"");
            for (String command : commands) {
                terminalCommand.append(" && python -u ").append(command);
            }
        } else if (SystemUtils.IS_OS_LINUX) {
            terminalCommand = new StringBuilder(":");
            for (String command : commands) {
                terminalCommand.append(" && ").append(pythonPath).append(" -u ").append(command);
            }
//            terminalCommand = new StringBuilder("conda activate " + ENV + " && cd \"" + basePath + "\"");
//            for (String command : commands) {
//                terminalCommand.append(" && python -u ").append(command);
//            }
        } else {
            throw new RuntimeException("Unsupported OS");
        }
        executeInTerminal(terminalCommand.toString());
    }

    public void launchTraining(CNNTrainingScript info) {
        try {
            String script = info.getScript();
            File temp = File.createTempFile("miso_", ".py");
            BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
            writer.write(script);
            writer.close();
//            executePythonCommand("\"" + temp.getAbsolutePath() + "\"");
            executePythonCommands(new String[] {
                    " -m pip install -U " + PACKAGE,
                    "\"" + temp.getAbsolutePath() + "\""
            });
        } catch (Exception ex) {
            BasicDialogs.ShowException("Error launching training", ex);
        }
    }

    public void launchFlowcam(String input, String output, String campaign, String species) {
        try {
            String script = getFlowcamScript(input, output, campaign, species);
            File temp = File.createTempFile("flowcam_", ".py");
            BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
            writer.write(script);
            writer.close();
            executePythonCommand("\"" + temp.getAbsolutePath() + "\"");
        } catch (Exception ex) {
            BasicDialogs.ShowException("Error launching flowcam", ex);
        }
    }

    public void updateMISO() {
        try {
            executePythonCommand(" -m pip install -U " + PACKAGE);
        }
        catch (Exception ex) {
            BasicDialogs.ShowException("Error updating MISO library", ex);
        }
    }

//    public Service<Void> createLaunchService(MISOTrainingScript info) {
//        Service<Void> service = new Service<Void>() {
//            @Override
//            protected Task<Void> createTask() {
//                Task<Void> task = new Task<Void>() {
//                    @Override
//                    protected Void call() throws Exception {
//                        String script = info.getScript();
//                        File temp = File.createTempFile("script", ".py");
//                        BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
//                        writer.write(script);
//                        writer.close();
//                        ArrayList<String> commands = new ArrayList<>();
//                        return null;
//                    }
//                };
//                return task;
//            }
//        };
//        return service;
//    }
//
//    public void terminate() {
//        if (process != null) {
//            process.destroyForcibly();
//        }
//    }
}
