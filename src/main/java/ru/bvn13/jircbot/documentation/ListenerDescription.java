package ru.bvn13.jircbot.documentation;

import lombok.Builder;
import lombok.Getter;
import org.modelmapper.internal.util.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bvn13 on 28.10.2018.
 */
public class ListenerDescription {

    @Builder
    public static class CommandDescription {

        @Getter
        private String command = "";
        @Getter
        private String description = "";
        @Getter
        private String example = "";

    }

    @Getter
    private String moduleName = "";
    @Getter
    private String moduleDescription = "";

    private Map<String, CommandDescription> commandsDescription = new HashMap<>();


    private ListenerDescription() {}

    public static ListenerDescription create() {
        return new ListenerDescription();
    }

    public ListenerDescription setModuleName(String moduleName) {
        this.moduleName = moduleName;
        return this;
    }

    public ListenerDescription setModuleDescription(String moduleDescription) {
        this.moduleDescription = moduleDescription;
        return this;
    }

    public ListenerDescription addCommand(CommandDescription description) {
        commandsDescription.put(description.getCommand(), description);
        return this;
    }

    public List<String> getCommandNames() {
        List<String> names = Lists.from(commandsDescription.keySet().iterator());
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    public CommandDescription getCommandDescription(String command) {
        return commandsDescription.getOrDefault(command, null);
    }

}
