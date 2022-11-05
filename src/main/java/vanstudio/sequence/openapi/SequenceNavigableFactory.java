package vanstudio.sequence.openapi;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageExtensionPoint;
import com.intellij.openapi.components.ComponentManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
//import vanstudio.sequence.generator.EmptySequenceNavigable;
import org.jetbrains.annotations.NotNull;
import vanstudio.sequence.generator.JavaSequenceNavigable;

import java.util.List;

public class SequenceNavigableFactory {
    private static final ExtensionPointName<LanguageExtensionPoint<SequenceNavigable>> EP_NAME = ExtensionPointName.create("SequenceDiagram.sequenceNavigable");

    public static final SequenceNavigableFactory INSTANCE = new SequenceNavigableFactory();

    private SequenceNavigableFactory() {
    }

    public SequenceNavigable forLanguage(ComponentManager componentManager, Language language) {

        @NotNull List<LanguageExtensionPoint<SequenceNavigable>> extensionList = EP_NAME.getExtensionsIfPointIsRegistered(componentManager);

        return extensionList.stream()
                .filter(it -> language.isKindOf(it.getKey()))
                .map(it -> it.createInstance(componentManager))
                .findFirst()
                .orElse(new JavaSequenceNavigable((Project) componentManager)); // JAVA and UAST

    }

}
