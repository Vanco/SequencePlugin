package org.intellij.sequencer.ext.kotlin;

import org.intellij.sequencer.openapi.SequenceParams;
import org.intellij.sequencer.openapi.GeneratorFactory;
import org.intellij.sequencer.openapi.IGenerator;
import org.jetbrains.annotations.NotNull;

public class KtGeneratorFactory extends GeneratorFactory {
    @NotNull
    @Override
    public IGenerator getGenerator(@NotNull SequenceParams params) {
        return new KtSequenceGenerator(params);
    }

    @Override
    public IGenerator getGenerator(@NotNull SequenceParams params, int offset, int depth) {
        return new KtSequenceGenerator(params, offset, depth);
    }
}
