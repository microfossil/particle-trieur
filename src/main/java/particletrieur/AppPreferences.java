/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur;

import particletrieur.controls.BasicDialogs;

import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class AppPreferences {
    
    //Preferences
    public static Preferences appPrefs =  Preferences.userRoot().node("particleTrieur2");

    //Needs checking in here for valid path
    public String getProjectPath() { return appPrefs.get("projectPath", System.getProperty("user.home")); }
    public void setProjectPath(String path) { appPrefs.put("projectPath", path); }
    
    public String getExportPath() { return appPrefs.get("exportPath", System.getProperty("user.home")); }
    public void setExportPath(String path) { appPrefs.put("exportPath", path); }
    
    public String getImagePath() { return appPrefs.get("imagePath", System.getProperty("user.home")); }
    public void setImagePath(String path) { appPrefs.put("imagePath", path); }
    
    public String getSegmenterPath() { return appPrefs.get("segmenterPath", System.getProperty("user.home")); }
    public void setSegmenterPath(String path) { appPrefs.put("segmenterPath", path); }

    public String getNetworkPath() { return appPrefs.get("networkPath", System.getProperty("user.home")); }
    public void setNetworkPath(String path) { appPrefs.put("networkPath", path); }

    public String getTrainingPath() { return appPrefs.get("trainingPath", System.getProperty("user.home")); }
    public void setTrainingPath(String path) { appPrefs.put("trainingPath", path); }

    public String getFlowcamPath() { return appPrefs.get("flowcamPath", System.getProperty("user.home")); }
    public void setFlowcamPath(String path) { appPrefs.put("flowcamPath", path); }
    
    public String getParseString() { return appPrefs.get("parseString", ""); }
    public void setParseString(String path) { appPrefs.put("parseString", path); }
    
    public String getUsername() { return appPrefs.get("username", System.getProperty("user.name")); }
    public void setUsername(String path) { appPrefs.put("username", path); }

    public String getPythonPath() { return appPrefs.get("pythonPath", ""); }
    public void setPythonPath(String path) { appPrefs.put("pythonPath", path); }

    public String getLastVersion() { return appPrefs.get("lastVersion", "0"); }
    public void setLastVersion(String version) { appPrefs.put("lastVersion", version); }

    public String getRecentProject() { return appPrefs.get("recentProject", ""); }

    public ArrayList<String> getRecentProjects() {
        String[] projects = new String[5];
        projects[0] = appPrefs.get("recentProject", "");
        projects[1] = appPrefs.get("recentProject2", "");
        projects[2] = appPrefs.get("recentProject3", "");
        projects[3] = appPrefs.get("recentProject4", "");
        projects[4] = appPrefs.get("recentProject5", "");

        ArrayList<String> projectList = new ArrayList<>();
        for (String project : projects) {
            //if (!project.equals("")) {
                projectList.add(project);
            //}
        }

        return projectList;
    }

    public void setRecentProject(String path) {
        ArrayList<String> recentProjects = getRecentProjects();
        int idx = recentProjects.indexOf(path);
        if (idx != -1) recentProjects.remove(idx);
        recentProjects.add(0, path);
        setRecentProjects(recentProjects);
    }

    public void setRecentProjects(ArrayList<String> paths) {
        String[] keys = { "recentProject", "recentProject2", "recentProject3", "recentProject4", "recentProject5" };
        int i = 0;
        for ( ; i < Math.min(paths.size(),5); i++) {
            appPrefs.put(keys[i], paths.get(i));
        }
        for ( ; i < 5; i++) {
            appPrefs.put(keys[i], "");
        }
    }

    //Save all
    public void save() {
        try {
            appPrefs.flush();
        } catch (BackingStoreException ex) {
            BasicDialogs.ShowException(
                    "The project was saved, but there was a problem storing program preferences.\n"
                    + "This will not affect anything. Continue as normal.", ex);
        }
    }
}
