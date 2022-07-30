package org.intellij.sequencer.openapi;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageExtension;
import com.intellij.psi.PsiElement;
import org.intellij.sequencer.openapi.model.CallStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GeneratorFactory {

    private static final LanguageExtension<GeneratorFactory> EP_NAME = new LanguageExtension<>("SequenceDiagram.generator");

    @NotNull
    public static IGenerator createGenerator(Language language, SequenceParams params) {
        GeneratorFactory factory = EP_NAME.forLanguage(language);
        return  factory != null ? factory.getGenerator(params) : new DefaultGenerator();
    }

    @NotNull
    public static IGenerator createGenerator(Language language, SequenceParams params, int offset, int depth) {
        GeneratorFactory factory = EP_NAME.forLanguage(language);
        return  factory != null ? factory.getGenerator(params, offset, depth) : new DefaultGenerator();
    }

    @NotNull
    public abstract IGenerator getGenerator(@NotNull SequenceParams params);

    public abstract IGenerator getGenerator(@NotNull SequenceParams params, int offset, int depth);

    private static class DefaultGenerator implements IGenerator {
        @Override
        public CallStack generate(PsiElement psiElement, @Nullable CallStack parent) {
            return parent;
        }
    }
}
