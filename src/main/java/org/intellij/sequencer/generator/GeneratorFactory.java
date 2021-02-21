package org.intellij.sequencer.generator;

import com.intellij.lang.Language;
import org.jetbrains.kotlin.idea.KotlinLanguage;

public class GeneratorFactory {

    public static IGenerator createGenerator(Language language, SequenceParams params) {
        if (language.is(KotlinLanguage.INSTANCE)) {
            return new KtSequenceGenerator(params);
        }
        return  new SequenceGenerator(params);
    }
}
