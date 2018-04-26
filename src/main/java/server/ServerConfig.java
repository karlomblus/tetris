package server;

import java.util.Properties;

public class ServerConfig {

    Properties configFile;

    public ServerConfig() {
        configFile = new java.util.Properties();
        try {
            configFile.load(this.getClass().getClassLoader().
                    getResourceAsStream("config.txt"));
        } catch (Exception eta) {
            eta.printStackTrace();
        }
    }




    public String getProperty(String key) {
        return getProperty(key, null);
    }

    public String getProperty(String key, String defaultkey) {
        String value = this.configFile.getProperty(key);
        if (value == null) {
            if (defaultkey!=null) {ServerMain.debug(1,"ServerConfing using default for: " + key+ " ("+defaultkey+")");}
            return defaultkey;
        }
        return value;
    }

}
