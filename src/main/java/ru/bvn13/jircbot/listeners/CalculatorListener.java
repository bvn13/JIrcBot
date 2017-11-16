package ru.bvn13.jircbot.listeners;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.WaitForQueue;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;



public class CalculatorListener extends ListenerAdapter {

    private static final String COMMAND = "?calc ";


    @Override
    public void onGenericMessage(final GenericMessageEvent event) throws Exception {

        if (event.getUser().getNick().equals(event.getBot().getNick())) {
            return;
        }

        if (!event.getMessage().startsWith(COMMAND.trim())) {
            return;
        }

        String message = event.getMessage().replace(COMMAND, "").trim();
        String[] commands = message.split(" ", 2);

        if (commands.length == 0 || commands[0].isEmpty() || commands[0].trim().equalsIgnoreCase("help")) {
            event.respond(this.helpMessage());
            return;
        }

        if (this.checkComand(commands[0].trim())) {
            return;
        }

        String expressionString = message;
        event.respond("EXPRESSION: "+expressionString);
        ExpressionBuilder expressionBuilder = new ExpressionBuilder(expressionString);
        Expression exp = null;

        WaitForQueue queue = new WaitForQueue(event.getBot());
        while (true) {

            MessageEvent currentEvent = queue.waitFor(MessageEvent.class);

            if (currentEvent.getMessage().startsWith(COMMAND)) {
                message = currentEvent.getMessage().replace(COMMAND, "").trim();
                commands = message.split(" ", 2);
                if (commands.length == 0 || commands[0].isEmpty()) {
                    currentEvent.respond("Command is expected.");
                    currentEvent.respond(this.helpMessage());
                } else if (commands[0].trim().equalsIgnoreCase("vars")) {
                    exp = expressionBuilder.variables(commands[1].trim()).build();
                    currentEvent.respond("VARIABLES: "+commands[1].trim());
                } else if (commands[0].trim().equalsIgnoreCase("set")) {
                    String[] variableData = commands[1].trim().split("=", 2);
                    if (variableData.length < 2 || variableData[0].isEmpty() || variableData[1].isEmpty()) {
                        currentEvent.respond("FORMAT: variable = value");
                    } else {
                        if (exp == null) {
                            //currentEvent.respond("Variables are not set!");
                            exp = expressionBuilder.build();
                        } else {
                            Double value = Double.parseDouble(variableData[1].trim());
                            exp = exp.setVariable(variableData[0].trim(), value);
                            currentEvent.respond(String.format("VARIABLE SET: %s = %f", variableData[0].trim(), value));
                        }
                    }
                } else if (commands[0].trim().equalsIgnoreCase("done")) {
                    if (exp == null) {
                        exp = expressionBuilder.build();
                    }
                    Double result = exp.evaluate();
                    currentEvent.respond(String.format("%s = %f", expressionString, result));
                    expressionBuilder = null;
                    exp = null;
                    queue.close();
                    return;
                } else {
                    currentEvent.respond(this.helpMessage());
                }
            }

        }

    }


    private Boolean checkComand(String command) {
        return command.equalsIgnoreCase("vars")
                || command.equalsIgnoreCase("set")
                || command.equalsIgnoreCase("done");
    }

    private String helpMessage() {
        return  "CALCULATOR (powered by Exp4J) | "+
                "Commands: | "+
                "vars - set variables names delimetered by comma, i.e.: a, b | "+
                "set - set variable, i.e.: a = 2 | "+
                "done - evaluate expression";
    }

}
