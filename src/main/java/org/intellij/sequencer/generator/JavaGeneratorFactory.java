package org.intellij.sequencer.generator;

import org.intellij.sequencer.openapi.GeneratorFactory;
import org.intellij.sequencer.openapi.IGenerator;
import org.intellij.sequencer.openapi.SequenceParams;
import org.jetbrains.annotations.NotNull;

public class JavaGeneratorFactory extends GeneratorFactory {
    @NotNull
    @Override
    public IGenerator getGenerator(@NotNull SequenceParams params) {
        return new SequenceGenerator(params);
    }

    @Override
    public IGenerator getGenerator(@NotNull SequenceParams params, int offset, int depth) {
        return new SequenceGenerator(params, offset, depth);
    }
}
