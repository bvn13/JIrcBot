package ru.bvn13.jircbot.config;

import lombok.Getter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import ru.bvn13.jircbot.model.Config;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Configuration
public class JircBotConfiguration {

    private static Logger logger = LoggerFactory.getLogger(JircBotConfiguration.class);


    @Value("${config}")
    private String configFileName;

    @Getter
    private List<Config> connections = new ArrayList<>();


    @PostConstruct
    public void readConfigFile() {
        logger.debug("Start reading configuration file: "+this.configFileName);

        JSONParser parser = new JSONParser();
        Reader reader = null;
        try {
            reader = new FileReader(this.configFileName);
            Object jsonObj = parser.parse(reader);
            JSONObject jsonObject = (JSONObject) jsonObj;

            logger.debug("CONFIG VERSION: "+jsonObject.get("version"));

            JSONArray settings = (JSONArray) jsonObject.get("connections");

            Iterator<JSONObject> iterator = settings.iterator();
            while (iterator.hasNext()) {
                Config config = this.parseConfig(iterator.next());
                if (config.getEnabled())
                    this.connections.add(config);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new InternalError("Config file not found: "+this.configFileName);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new InternalError("Wrong config file format. JSON is expected.");
        } catch (IOException e) {
            e.printStackTrace();
            throw new InternalError("Config is not readable.");
        }

    }

    private Config parseConfig(JSONObject data) {
        Config config = new Config();
        config.setEnabled((Boolean)data.get("enabled"));
        config.setServer((String)data.get("server"));
        config.setPort(Integer.parseInt(data.get("port").toString()));
        config.setChannelName((String)data.get("channelName"));
        config.setBotName((String)data.get("botName"));
        return config;
    }

}
