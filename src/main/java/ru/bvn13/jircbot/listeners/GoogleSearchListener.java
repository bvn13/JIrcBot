package ru.bvn13.jircbot.listeners;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.CustomsearchRequestInitializer;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;
import ru.bvn13.jircbot.config.JircBotConfiguration;
import ru.bvn13.jircbot.model.GoogleSearchSettings;

import static ru.bvn13.jircbot.config.JircBotConfiguration.KEY_GOOGLE_SEARCH;

public class GoogleSearchListener extends ListenerAdapter {

    private static final String COMMAND = "?search ";

    private JircBotConfiguration config;

    public GoogleSearchListener(JircBotConfiguration config) {
        this.config = config;
    }


    @Override
    public void onGenericMessage(final GenericMessageEvent event) throws Exception {

        if (event.getUser().getUserId().equals(event.getBot().getUserBot().getUserId())) {
            return;
        }

        if (!event.getMessage().startsWith(COMMAND.trim())) {
            return;
        }

        Customsearch.Cse.List list = null;

        try {
            GoogleSearchSettings settings = (GoogleSearchSettings) this.config.getListenersSettings().get(KEY_GOOGLE_SEARCH);

            String query = event.getMessage().substring(COMMAND.length()).trim(); //The query to search
            String uuid = settings.getUuid(); //Your search engine
            String appKey = settings.getAppKey(); //Your application key

            //Instance Customsearch
            Customsearch customSearch = new Customsearch.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), null)
                    .setApplicationName("JIrcBot")
                    .setGoogleClientRequestInitializer(new CustomsearchRequestInitializer(uuid))
                    .build();

            //Set search parameter
            list = customSearch.cse().list(query).setCx(uuid);
        } catch (Exception ex) {
            ex.printStackTrace();
            event.respond("ERROR is occured while initialization, sorry.");
            return;
        }

        //Execute search
        Search result = null;
        try {
            result = list.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
            event.respond("ERROR is occured while searching, sorry.");
            return;
        }
        if (result.getItems() != null) {
            Result first = result.getItems().get(0);
            event.respond(String.format("FOUND: %s (%s)", first.getTitle(), first.getLink()));
        } else {
            event.respond("NOT FOUND");
        }

    }

}
