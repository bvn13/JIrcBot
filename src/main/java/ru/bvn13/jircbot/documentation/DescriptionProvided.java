package ru.bvn13.jircbot.documentation;

/**
 * Created by bvn13 on 28.10.2018.
 */
public interface DescriptionProvided {

    ListenerDescription getDescription();
    default void registerDescription(DocumentationProvider documentationProvider) {
        documentationProvider.register(this);
    }

}
