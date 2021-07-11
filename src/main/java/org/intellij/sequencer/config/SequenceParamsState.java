package org.intellij.sequencer.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "sequenceParams", storages = {@Storage("sequencePlugin.xml")})
public class SequenceParamsState implements PersistentStateComponent<SequenceParamsState> {
    public int callDepth = 5;
    public boolean projectClassesOnly = true;
    public boolean noGetterSetters = true;
    public boolean noPrivateMethods = false;
    public boolean noConstructors = false;
    public boolean smartInterface = false;

    public SequenceParamsState() {
    }

    public static @NotNull SequenceParamsState getInstance() {
        return ServiceManager.getService(SequenceParamsState.class);
    }

    @Override
    public @Nullable SequenceParamsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull SequenceParamsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
