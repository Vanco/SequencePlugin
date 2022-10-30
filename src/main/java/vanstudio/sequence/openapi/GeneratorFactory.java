package vanstudio.sequence.openapi;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageExtension;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UElement;
import vanstudio.sequence.openapi.model.CallStack;

public abstract class GeneratorFactory {

    private static final LanguageExtension<GeneratorFactory> EP_NAME = new LanguageExtension<>("SequenceDiagram.generator");

    @NotNull
    public static IGenerator createGenerator(Language language, SequenceParams params) {
        GeneratorFactory factory = EP_NAME.forLanguage(language);
        if (factory == null) {
            return new UnsupportedGenerator();
        }
        factory.loadParams(params);
        return factory.getGenerator(params);
    }

    @NotNull
    public static IGenerator createGenerator(Language language, SequenceParams params, int offset) {
        GeneratorFactory factory = EP_NAME.forLanguage(language);
        if (factory == null) {
            return new UnsupportedGenerator();
        }
        factory.loadParams(params);
        return factory.getGenerator(params, offset);
    }

    @NotNull
    public abstract IGenerator getGenerator(@NotNull SequenceParams params);

    @NotNull
    public abstract IGenerator getGenerator(@NotNull SequenceParams params, int offset);

    @NotNull
    public abstract SequenceParams loadParams(@NotNull SequenceParams params);


    static class UnsupportedGenerator implements IGenerator{

        @Override
        public CallStack generate(PsiElement psiElement, @Nullable CallStack parent) {
            return CallStack.EMPTY;
        }

        @Override
        public CallStack generate(UElement node, CallStack parent) {
            return CallStack.EMPTY;
        }
    }
}
