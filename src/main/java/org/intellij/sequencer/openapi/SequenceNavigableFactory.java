package org.intellij.sequencer.openapi;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageExtensionPoint;
import com.intellij.openapi.components.ComponentManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import org.intellij.sequencer.generator.EmptySequenceNavigable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SequenceNavigableFactory {
    private static final ExtensionPointName<LanguageExtensionPoint<SequenceNavigable>> EP_NAME = ExtensionPointName.create("SequenceDiagram.sequenceNavigable");

    public static final SequenceNavigableFactory INSTANCE = new SequenceNavigableFactory();

    private SequenceNavigableFactory() {
    }

    public SequenceNavigable forLanguage(ComponentManager componentManager, Language language) {

        @NotNull List<LanguageExtensionPoint<SequenceNavigable>> extensionList = EP_NAME.getExtensionsIfPointIsRegistered(componentManager);
        return extensionList.stream()
                .filter(it -> it.getKey().equals(language.getID()))
                .map(it -> it.createInstance(componentManager))
                .findFirst()
                .orElse(new EmptySequenceNavigable());

    }

}
