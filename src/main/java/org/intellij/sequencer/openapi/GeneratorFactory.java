package org.intellij.sequencer.openapi;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageExtension;
import org.jetbrains.annotations.NotNull;

public abstract class GeneratorFactory {

    private static final LanguageExtension<GeneratorFactory> EP_NAME = new LanguageExtension<>("SequenceDiagram.generator");

    @NotNull
    public static IGenerator createGenerator(Language language, SequenceParams params) {
        GeneratorFactory factory = EP_NAME.forLanguage(language);
        factory.loadParams(params);
        return factory.getGenerator(params);
    }

    @NotNull
    public static IGenerator createGenerator(Language language, SequenceParams params, int offset) {
        GeneratorFactory factory = EP_NAME.forLanguage(language);
        factory.loadParams(params);
        return factory.getGenerator(params, offset);
    }

    @NotNull
    public abstract IGenerator getGenerator(@NotNull SequenceParams params);

    @NotNull
    public abstract IGenerator getGenerator(@NotNull SequenceParams params, int offset);

    @NotNull
    public abstract SequenceParams loadParams(@NotNull SequenceParams params);

}
