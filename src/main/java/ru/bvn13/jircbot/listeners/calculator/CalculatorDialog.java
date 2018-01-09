package ru.bvn13.jircbot.listeners.calculator;

import lombok.Data;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.pircbotx.hooks.types.GenericMessageEvent;
import ru.bvn13.fsm.Condition;
import ru.bvn13.fsm.Exceptions.FSMException;
import ru.bvn13.fsm.Exceptions.NotInitedException;
import ru.bvn13.fsm.FSM;
import ru.bvn13.fsm.State;

/**
 * Created by bvn13 on 03.01.2018.
 */
@Data
public class CalculatorDialog extends FSM {

    public static final String COMMAND = "?calc ";

    private String command;
    private String[] commands;
    private GenericMessageEvent event;

    private String expression;
    private ExpressionBuilder expressionBuilder;
    private Expression exp = null;

    public static CalculatorDialog createDialog() throws FSMException {

        CalculatorDialog dialog = new CalculatorDialog();

        dialog.initState(new State("init") {
            @Override
            public void process() {
                CalculatorDialog dialog = ((CalculatorDialog)this.getFSM());
                dialog.expression = dialog.command;
                dialog.expressionBuilder = new ExpressionBuilder(dialog.expression);
                dialog.event.respond("inited, expression: "+dialog.expression);
            }
        });

        dialog.addTransition("init", new State("entering-vars") {
            @Override
            public void beforeEvent() {
                CalculatorDialog dialog = ((CalculatorDialog)this.getFSM());
                dialog.event.respond("please enter variables (comma separated)");
            }
            @Override
            public void process() {
                CalculatorDialog dialog = ((CalculatorDialog)this.getFSM());
                dialog.enteringVars();
            }
        });

        dialog.addTransition("entering-vars", new State("settings-vars") {
            @Override
            public void beforeEvent() {
                CalculatorDialog dialog = ((CalculatorDialog)this.getFSM());
                dialog.event.respond("please set variable");
            }
            @Override
            public void process() {
                CalculatorDialog dialog = ((CalculatorDialog)this.getFSM());
                dialog.settingVars();
            }
        }, new Condition() {
            @Override
            public boolean check() {
                CalculatorDialog dialog = ((CalculatorDialog)this.getFSM());
                return dialog.commands[0].equalsIgnoreCase("vars");
            }
        });

        dialog.addTransition("settings-vars", "settings-vars", new Condition() {
            @Override
            public boolean check() {
                CalculatorDialog dialog = ((CalculatorDialog)this.getFSM());
                return !dialog.commands[0].equalsIgnoreCase("done");
            }
        });

        dialog.addTransition("settings-vars", new State("calculating", true) {
            @Override
            public void process() {
                CalculatorDialog dialog = ((CalculatorDialog)this.getFSM());
                dialog.calculating();
            }
        }, new Condition() {
            @Override
            public boolean check() {
                CalculatorDialog dialog = ((CalculatorDialog)this.getFSM());
                return dialog.commands[0].equalsIgnoreCase("done");
            }
        });

        dialog.addTransition("entering-vars", "calculating", new Condition() {
            @Override
            public boolean check() {
                CalculatorDialog dialog = ((CalculatorDialog)this.getFSM());
                return dialog.commands[0].equalsIgnoreCase("done");
            }
        });

        dialog.addTransition("entering-vars", new State("helping-entering-vars") {
            @Override
            public void process() {
                CalculatorDialog dialog = ((CalculatorDialog)this.getFSM());
                dialog.event.respond(dialog.helpMessage());
                //dialog.event.respond("Please set vars");
                try {
                    dialog.next();
                } catch (FSMException e) {
                    e.printStackTrace();
                }
            }
        }, new Condition() {
            @Override
            public boolean check() {
                CalculatorDialog dialog = ((CalculatorDialog)this.getFSM());
                return !dialog.commands[0].equalsIgnoreCase("done")
                        && !dialog.commands[0].equalsIgnoreCase("vars");
            }
        });

        dialog.addTransition("helping-entering-vars", "entering-vars");

        return dialog;

    }

    private void enteringVars() {
        exp = expressionBuilder.variables(commands[1].trim()).build();
        event.respond("VARIABLES: "+commands[1].trim());
    }

    private void settingVars() {
        String[] variableData = commands[1].trim().split("=", 2);
        if (variableData.length < 2 || variableData[0].isEmpty() || variableData[1].isEmpty()) {
            event.respond("FORMAT: variable = value");
        } else {
            if (exp == null) {
                //currentEvent.respond("Variables are not set!");
                exp = expressionBuilder.build();
            } else {
                Double value = Double.parseDouble(variableData[1].trim());
                exp = exp.setVariable(variableData[0].trim(), value);
                event.respond(String.format("VARIABLE SET: %s = %f", variableData[0].trim(), value));
            }
        }
    }

    private void calculating() {
        if (exp == null) {
            exp = expressionBuilder.build();
        }
        Double result = exp.evaluate();
        event.respond(String.format("%s = %f", expression, result));
        expressionBuilder = null;
        exp = null;
        return;
    }

    public void setEvent(GenericMessageEvent event) {
        this.event = event;
    }


    public void processCommand(String userMessage) {

        if (prepareCommand(userMessage)) {
            return;
        }

        if (this.getCurrentState()==null || this.getCurrentState().isFinish()) {
            try {
                this.init();
            } catch (NotInitedException e) {
                e.printStackTrace();
            }
        }

        try {
            this.next();
        } catch (FSMException e) {
            e.printStackTrace();
        }

    }



    private boolean prepareCommand(String userMessage) {
        String message = userMessage.replace(COMMAND, "").trim();
        this.commands = message.split(" ", 2);

        if (commands.length == 0 || commands[0].isEmpty() || commands[0].trim().equalsIgnoreCase("help")) {
            event.respond(this.helpMessage());
            return true;
        }

        if (this.checkComand(commands[0].trim())) {
            return false;
        }

        this.command = message;
        return false;
    }

    private String helpMessage() {
        return  "CALCULATOR (powered by Exp4J) | "+
                "Commands: | "+
                "vars - set variables names delimetered by comma, i.e.: a, b | "+
                "set - set variable, i.e.: a = 2 | "+
                "done - evaluate expression";
    }

    private Boolean checkComand(String command) {
        return command.equalsIgnoreCase("vars")
                || command.equalsIgnoreCase("set")
                || command.equalsIgnoreCase("done");
    }

}
