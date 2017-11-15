package ru.bvn13.jircbot.listeners;

import org.pircbotx.dcc.ReceiveChat;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.WaitForQueue;
import org.pircbotx.hooks.events.IncomingChatRequestEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

public class TestListener extends ListenerAdapter {

    @Override
    public void onGenericMessage(final GenericMessageEvent event) throws Exception {
        //Hello world
        //This way to handle commands is useful for listeners that listen for multiple commands
        if (event.getMessage().startsWith("?hello"))
            event.respond("Hello World!");

        //If this isn't a waittest, ignore
        //This way to handle commands is useful for listers that only listen for one command
        if (!event.getMessage().startsWith("?waitTest start"))
            return;

        //WaitTest has started
        event.respond("Started...");
        WaitForQueue queue = new WaitForQueue(event.getBot());
        //Infinate loop since we might recieve messages that aren't WaitTest's.
        while (true) {
            //Use the waitFor() method to wait for a MessageEvent.
            //This will block (wait) until a message event comes in, ignoring
            //everything else
            MessageEvent currentEvent = queue.waitFor(MessageEvent.class);
            //Check if this message is the "ping" command
            if (currentEvent.getMessage().startsWith("?waitTest ping"))
                event.respond("pong");
                //Check if this message is the "end" command
            else if (currentEvent.getMessage().startsWith("?waitTest end")) {
                event.respond("Stopping");
                queue.close();
                //Very important that we end the infinate loop or else the test
                //will continue forever!
                return;
            }
        }
    }


    @Override
    public void onIncomingChatRequest(IncomingChatRequestEvent event) throws Exception {
        //Accept the incoming chat request. If it fails it will throw an exception
        ReceiveChat chat = event.accept();
        //Read lines from the server
        String line;
        while ((line = chat.readLine()) != null)
            if (line.equalsIgnoreCase("done")) {
                //Shut down the chat
                chat.close();
                break;
            } else {
                //Fun example
                int lineLength = line.length();
                chat.sendLine("Line '" + line + "' contains " + lineLength + " characters");
            }
    }

}
