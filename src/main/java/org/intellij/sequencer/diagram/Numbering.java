package org.intellij.sequencer.diagram;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Numbering {
    private List<Integer> _numbers;

    private Numbering(List<Integer> numbers) {
        _numbers = numbers;
    }

    public Numbering(Numbering fromNumbering) {
        _numbers = fromNumbering != null? new ArrayList<>(fromNumbering._numbers): new ArrayList<>();
    }

    public int level() {
        return _numbers.size();
    }

    public void addNewLevel() {
        _numbers.add(1);
    }

    public void incrementLevel(int level) {
        _numbers.set(level, _numbers.get(level) + 1);
    }

    public String getName() {
        StringBuilder buffer = new StringBuilder();
        for(Iterator<Integer> iterator = _numbers.iterator(); iterator.hasNext();) {
            Integer number = iterator.next();
            buffer.append(number);
            if(iterator.hasNext())
                buffer.append('.');
        }
        return buffer.toString();
    }

    public int getTopLevel() {
        if(_numbers.isEmpty())
            return 0;
        return _numbers.get(_numbers.size() - 1);
    }

    public Numbering getPreviousNumbering() {
        if(_numbers.size() < 2)
            return null;
        List<Integer> numbers = new ArrayList<>(_numbers);
        numbers.remove(_numbers.size() - 1);
        return new Numbering(numbers);
    }

    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof Numbering)) return false;

        final Numbering numbering = (Numbering)o;

        return _numbers.equals(numbering._numbers);
    }

    public int hashCode() {
        return _numbers.hashCode();
    }
}
