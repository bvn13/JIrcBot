package ru.bvn13.jircbot.listeners.quiz;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import ru.bvn13.fsm.Condition;
import ru.bvn13.fsm.Exceptions.FSMException;
import ru.bvn13.fsm.Exceptions.NotInitedException;
import ru.bvn13.fsm.FSM;
import ru.bvn13.fsm.State;

import java.util.*;

/**
 * Created by bvn13 on 26.01.2018.
 */
public class QuizDialog extends FSM {

    public static final String COMMAND = "?quiz ";

    private static final Integer TICK = 10;

    private String command;
    private String[] commands;
    private GenericMessageEvent event;

    private QuizEngine engine = new QuizEngine();
    private Map<String, Integer> stats = new HashMap<>();
    private Question question;

    private Timer timer;
    private Timer timerTicker;
    private TimerTask task;
    private TimerTask taskTicker;

    private Boolean timeIsGone = false;
    private Integer timeSeconds = 0;
    private Boolean isWin = false;

    private String error = "";

    protected static class TimerCheckerTask extends TimerTask {
        private QuizDialog dialog;
        public TimerCheckerTask(QuizDialog dialog) {
            this.dialog = dialog;
        }
        @Override
        public void run() {
            synchronized (dialog.timeIsGone) {
                dialog.timeIsGone = true;
            }
            synchronized (dialog.isWin) {
                if (!dialog.isWin) {
                    try {
                        dialog.next();
                    } catch (FSMException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    protected static class TimeTickerTask extends TimerTask {
        private QuizDialog dialog;
        public TimeTickerTask(QuizDialog dialog) {
            this.dialog = dialog;
        }
        @Override
        public void run() {
            synchronized (dialog.timeIsGone) {
                if (!dialog.timeIsGone) {
                    synchronized (dialog.timeSeconds) {
                        dialog.timeSeconds += TICK;
                        dialog.sendNotice("Прошло " + dialog.timeSeconds + " секунд...");

                    }
                } else {
                    dialog.timerTicker.cancel();
                }
            }
        }
    }


    public static QuizDialog createDialog() throws FSMException {

        QuizDialog dialog = new QuizDialog();

        dialog.initState(new State("init"));

        dialog.addTransition("init", new State("help-initing") {
            @Override
            public void process() {
                QuizDialog dialog = (QuizDialog) this.getFSM();
                try {
                    dialog.event.respond(dialog.helpMessage());
                    dialog.next();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Condition() {
            @Override
            public boolean check() {
                QuizDialog dialog = ((QuizDialog)this.getFSM());
                return !dialog.commands[0].equalsIgnoreCase("new");
            }
        });

        dialog.addTransition("help-initing", "init");

        dialog.addTransition("init", new State("asking") {
            @Override
            public void process() {
                QuizDialog dialog = (QuizDialog) this.getFSM();
                dialog.error = "";
                dialog.timeIsGone = false;
                dialog.isWin = false;
                try {
                    dialog.question = dialog.engine.getQuestion();

                    dialog.sendNotice("ВОПРОС: "+dialog.question.getQuestion());
                    dialog.sendNotice("ОТВЕТЫ: ");
                    int i = 0;
                    for (String answer : dialog.question.getAnswers()) {
                        i++;
                        dialog.sendNotice(""+i+": "+answer);
                    }
                    dialog.sendNotice("КАКОЙ ПРАВИЛЬНЫЙ (1-"+dialog.question.getAnswers().size()+")? Для ответа дано 60 секунд. Время пошло.");

                    if (dialog.timer != null) {
                        dialog.timer.cancel();
                    }
                    if (dialog.timerTicker != null) {
                        dialog.timerTicker.cancel();
                    }
                    dialog.timer = new Timer();
                    dialog.task = new TimerCheckerTask(dialog);
                    dialog.timer.schedule(dialog.task, 60*1000);

                    dialog.timeSeconds = 0;
                    dialog.timerTicker = new Timer();
                    dialog.taskTicker = new TimeTickerTask(dialog);
                    dialog.timerTicker.schedule(dialog.taskTicker, TICK*1000,TICK*1000);

                    dialog.next();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Condition() {
            @Override
            public boolean check() {
                QuizDialog dialog = ((QuizDialog)this.getFSM());
                return dialog.commands[0].equalsIgnoreCase("new");
            }
        });

        dialog.addTransition("asking", new State("error", true) {
            @Override
            public void process() {
                QuizDialog dialog = (QuizDialog) this.getFSM();
                dialog.sendNotice("Ошибка: "+dialog.error);
            }
        }, new Condition() {
            @Override
            public boolean check() {
                QuizDialog dialog = ((QuizDialog) this.getFSM());
                return !dialog.error.isEmpty();
            }
        });

        dialog.addTransition("asking", new State("waiting-for-answer") {

        },  new Condition() {
            @Override
            public boolean check() {
                QuizDialog dialog = ((QuizDialog) this.getFSM());
                return dialog.error.isEmpty();
            }
        });

        dialog.addTransition("waiting-for-answer", new State("failed", true) {
            @Override
            public void process() {
                QuizDialog dialog = (QuizDialog) this.getFSM();
                dialog.sendNotice("ВРЕМЯ ВЫШЛО! Правильный ответ: "+dialog.question.getCorrectAnswer());
            }
        }, new Condition() {
            @Override
            public boolean check() {
                QuizDialog dialog = ((QuizDialog)this.getFSM());
                synchronized (dialog.timeIsGone) {
                    return dialog.timeIsGone;
                }
            }
        });

        dialog.addTransition("waiting-for-answer", new State("check-answer") {
            @Override
            public void process() {
                QuizDialog dialog = (QuizDialog) this.getFSM();
                Integer answer = 0;
                try {
                    answer = Integer.parseInt(dialog.commands[0]);
                } catch (Exception e) {
                    dialog.event.respond("Выберите ответ: 1-" + dialog.question.getAnswers().size());
                    return;
                }
                synchronized (dialog.isWin) {
                    boolean isCorrect = false;
                    if (answer <= dialog.question.getAnswers().size() && dialog.question.getAnswers().get(answer - 1).equalsIgnoreCase(dialog.question.getCorrectAnswer())) {
                        isCorrect = true;
                        dialog.isWin = true;
                        dialog.timer.cancel();
                    }
                    dialog.event.respond("Ваш ответ " + answer + " - " + (isCorrect ? "ПРАВИЛЬНЫЙ!" : "Неправильный"));
                }
                try {
                    dialog.next();
                } catch (FSMException e) {
                    e.printStackTrace();
                }
            }
        }, new Condition() {
            @Override
            public boolean check() {
                QuizDialog dialog = (QuizDialog) this.getFSM();
                synchronized (dialog.timeIsGone) {
                    return !dialog.timeIsGone;
                }
            }
        });

        dialog.addTransition("check-answer", new State("win", true) {
            @Override
            public void process() {
                QuizDialog dialog = (QuizDialog) this.getFSM();
                dialog.timerTicker.cancel();
                if (!dialog.stats.containsKey(dialog.event.getUser().getNick())) {
                    dialog.stats.put(dialog.event.getUser().getNick(), 0);
                }
                Integer wins = dialog.stats.get(dialog.event.getUser().getNick());
                dialog.stats.put(dialog.event.getUser().getNick(), wins+1);
                dialog.event.respond("ПОЗДРАВЛЯЮ!");

                dialog.printTopWinners();
            }
        }, new Condition() {
            @Override
            public boolean check() {
                QuizDialog dialog = (QuizDialog) this.getFSM();
                synchronized (dialog.isWin) {
                    return dialog.isWin;
                }
            }
        });

        dialog.addTransition("check-answer", "failed", new Condition() {
            @Override
            public boolean check() {
                QuizDialog dialog = (QuizDialog) this.getFSM();
                dialog.timerTicker.cancel();
                synchronized (dialog.isWin) {
                    synchronized (dialog.timeIsGone) {
                        return dialog.timeIsGone && !dialog.isWin;
                    }
                }
            }
        });

        dialog.addTransition("check-answer", "waiting-for-answer", new Condition() {
            @Override
            public boolean check() {
                QuizDialog dialog = (QuizDialog) this.getFSM();
                synchronized (dialog.isWin) {
                    synchronized (dialog.timeIsGone) {
                        return !dialog.timeIsGone && !dialog.isWin;
                    }
                }
            }
        });

        return dialog;
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
                this.next();
            } catch (NotInitedException e) {
                e.printStackTrace();
            } catch (FSMException e) {
                e.printStackTrace();
            }
        } else {
            try {
                this.next();
            } catch (FSMException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean prepareCommand(String userMessage) {
        String message = userMessage.replace(COMMAND, "").trim();
        this.commands = message.split(" ", 2);

        if (commands.length == 0 || commands[0].isEmpty() || commands[0].trim().equalsIgnoreCase("stats")) {
            this.printTopWinners();
            return true;
        }

        if (commands.length == 0 || commands[0].isEmpty() || commands[0].trim().equalsIgnoreCase("help")) {
            event.respond(this.helpMessage());
            return true;
        }

        if (commands.length == 0 || commands[0].isEmpty() || commands[0].trim().equalsIgnoreCase("status")) {
            event.respond("STATE: "+this.getCurrentState().getName());
            return true;
        }

        this.command = message;

        return false;
    }

    private String helpMessage() {
        return  "Викторина: я задаю вопрос и жду минуту правильный ответ. Все общение - через команду "+COMMAND;
    }

    private void sendNotice(String str) {
        event.getBot().sendIRC().notice(((MessageEvent) event).getChannel().getName(), str);
    }

    private void printTopWinners() {
        this.sendNotice("ТОП ПОБЕДИТЕЛЕЙ: ");
        Map<String, Integer> winners = QuizDialog.sortByValue(this.stats);
        int i=0;
        for (Map.Entry<String, Integer> entry : winners.entrySet()) {
            if (i++ > 3) {
                break;
            }
            this.sendNotice(""+i+": "+entry.getKey()+", побед: "+entry.getValue());
        }
    }

    private static Map<String, Integer> sortByValue(Map<String, Integer> unsortMap) {

        // 1. Convert Map to List of Map
        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o2,
                               Map.Entry<String, Integer> o1) { // descending!
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        /*
        //classic iterator example
        for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext(); ) {
            Map.Entry<String, Integer> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }*/


        return sortedMap;
    }

}
