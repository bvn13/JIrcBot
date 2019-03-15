package ru.bvn13.jircbot.documentation;

import lombok.Builder;
import lombok.Getter;
import org.modelmapper.internal.util.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public String getCommandNamesJoined() {
        return Lists.from(commandsDescription.keySet().iterator()).stream().sorted().collect(Collectors.joining(","));
    }

    public Optional<CommandDescription> getCommandDescriptionOpt(String command) {
        return Optional.ofNullable(commandsDescription.getOrDefault(command, null));
    }

    public List<String> getCommandNames() {
        return Lists.from(commandsDescription.keySet().iterator());
    }

    public CommandDescription getCommandDescription(String command) {
        return commandsDescription.getOrDefault(command, null);
    }
}
