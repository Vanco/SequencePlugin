package vanstudio.sequence.diagram;

import java.util.EventListener;

public interface ModelTextListener extends EventListener {

    public void modelTextChanged(ModelTextEvent mte);
}

