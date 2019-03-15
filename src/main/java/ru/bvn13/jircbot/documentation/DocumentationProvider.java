package ru.bvn13.jircbot.documentation;

import lombok.Getter;
import org.modelmapper.internal.util.Lists;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Created by bvn13 on 28.10.2018.
 */
@Component
public class DocumentationProvider {

    @Getter
    private Map<String, DescriptionProvided> descriptors = new HashMap<>();

    public void register(DescriptionProvided descriptionProvided) {
        descriptors.put(descriptionProvided.getDescription().getModuleName(), descriptionProvided);
    }

    public List<String> getModuleNames() {
        List<String> names = Lists.from(descriptors.keySet().iterator());
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    public DescriptionProvided getModuleDescriptor(String moduleName) {
        return descriptors.getOrDefault(moduleName, null);
    }

    public Optional<ListenerDescription.CommandDescription> findByCommand(final String command) {
        AtomicReference<ListenerDescription.CommandDescription> description = new AtomicReference<>(null);

        descriptors.forEach((name, dp) -> {
            dp.getDescription().getCommandDescriptionOpt(command).ifPresent(description::set);
        });

        return Optional.ofNullable(description.get());
    }

    public String getAllCommands() {
        return descriptors.values().stream().map(dp -> dp.getDescription().getCommandNamesJoined()).filter(s -> s != null && !s.isEmpty()).collect(Collectors.joining(","));
    }

}
