package org.intellij.sequencer.diagram;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Numbering {
    private List _numbers;

    private Numbering(List numbers) {
        _numbers = numbers;
    }

    public Numbering(Numbering fromNumbering) {
        _numbers = fromNumbering != null? new ArrayList(fromNumbering._numbers): new ArrayList();
    }

    public int level() {
        return _numbers.size();
    }

    public void addNewLevel() {
        _numbers.add(new Integer(1));
    }

    public void incrementLevel(int level) {
        _numbers.set(level, new Integer(((Integer)_numbers.get(level)).intValue() + 1));
    }

    public String getName() {
        StringBuffer buffer = new StringBuffer();
        for(Iterator iterator = _numbers.iterator(); iterator.hasNext();) {
            Integer number = (Integer)iterator.next();
            buffer.append(number);
            if(iterator.hasNext())
                buffer.append('.');
        }
        return buffer.toString();
    }

    public int getTopLevel() {
        if(_numbers.isEmpty())
            return 0;
        return ((Integer)_numbers.get(_numbers.size() - 1)).intValue();
    }

    public Numbering getPreviousNumbering() {
        if(_numbers.size() < 2)
            return null;
        List numbers = new ArrayList(_numbers);
        numbers.remove(_numbers.size() - 1);
        return new Numbering(numbers);
    }

    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof Numbering)) return false;

        final Numbering numbering = (Numbering)o;

        if(!_numbers.equals(numbering._numbers)) return false;

        return true;
    }

    public int hashCode() {
        return _numbers.hashCode();
    }
}
