package org.intellij.sequencer.diagram;

import org.apache.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Diagram {
    private static final Logger LOGGER = Logger.getLogger(Diagram.class);

    private List<DisplayObject> _objectLifeLines = new ArrayList<>();
    private List<DisplayLink> _links = new ArrayList<>();

    public Diagram() {
    }

    public void build(String queryString) {
        _objectLifeLines = new ArrayList<>();
        _links = new ArrayList<>();

        Parser p = new Parser();
        try {
            p.parse(queryString);
        } catch(IOException ioe) {
            LOGGER.error("IOException", ioe);
            return;
        }

        List<ObjectInfo> theObjects = p.getObjects();
        for (ObjectInfo objectInfo : theObjects) {
            _objectLifeLines.add(new DisplayObject(objectInfo));
        }

        List<Link> theDisplayLinks = p.getLinks();
        int seq = 0;
        for (Link link : theDisplayLinks) {
            int fromSeq = link.getFrom().getSeq();
            int toSeq = link.getTo().getSeq();
            DisplayObject fromObj = _objectLifeLines.get(fromSeq);
            DisplayObject toObj = _objectLifeLines.get(toSeq);
            DisplayLink displayLink = null;
            if (link instanceof Call) {
                if (fromSeq == toSeq)
                    displayLink = new DisplaySelfCall(link, fromObj, toObj, seq);
                else
                    displayLink = new DisplayCall(link, fromObj, toObj, seq);
            } else if (link instanceof CallReturn) {
                if (fromSeq == toSeq)
                    displayLink = new DisplaySelfCallReturn(link, fromObj, toObj, seq);
                else
                    displayLink = new DisplayCallReturn(link, fromObj, toObj, seq);
            } else {
                LOGGER.error("Unknown link: " + link);
            }
            if (displayLink != null) {
                _links.add(displayLink);
                ++seq;
            }
        }

        for (ObjectInfo info : theObjects) {
            DisplayObject displayInfo = _objectLifeLines.get(info.getSeq());
            for (MethodInfo methodInfo : info.getMethods()) {
                int startSeq = methodInfo.getStartSeq();
                int endSeq = methodInfo.getEndSeq();
                if ((startSeq < _links.size()) && (endSeq < _links.size())) {
                    DisplayMethod methodBox = new DisplayMethod(info, methodInfo,
                            _links.get(startSeq), _links.get(endSeq));
                    displayInfo.addMethod(methodBox);
                }
            }
        }
    }

    public Dimension layoutObjects(Graphics2D g2, int inset) {
        int x = inset;
        int y = inset;
        for (DisplayObject displayObject : _objectLifeLines) {
            displayObject.setX(x);
            displayObject.setY(y);
            displayObject.initializeGraphics(g2);
            x += displayObject.getWidth() + inset;
        }

        int maxX = 200;
        for(int i = 0; i < _objectLifeLines.size(); ++i) {
            DisplayObject obj = _objectLifeLines.get(i);
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("Laying out " + obj);
            for (DisplayLink call : obj.getCalls()) {
                int availableGap;
                if (call.isSelfCall()) {
                    if (i == _objectLifeLines.size() - 1) {
                        int width = obj.getWidth();
                        if (width < call.getTextWidth())
                            obj.setWidth(obj.getTextWidth() + call.getTextWidth());
                        continue;
                    } else {
                        availableGap = obj.calcCurrentGap(
                                _objectLifeLines.get(i + 1), call.getSeq());
                    }
                } else {
                    availableGap = obj.calcCurrentGap(call.getTo(), call.getSeq());
                }

                if (availableGap < call.getTextWidth()) {
                    int offset = call.getTextWidth() - availableGap;
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("gap too small by " + offset);
                    int startJ = i + 1;
                    if (call.getTo().getSeq() < startJ)
                        startJ = call.getTo().getSeq() + 1;
                    for (int j = startJ; j < _objectLifeLines.size(); ++j) {
                        (_objectLifeLines.get(j)).translate(offset);
                    }
                }
            }
            maxX = obj.getX() + 2 * obj.getWidth() + inset;
        }

        if(_objectLifeLines.isEmpty())
            y = 100;
        else
            y += (_objectLifeLines.get(0)).getHeight();
        for (DisplayLink link : _links) {
            link.setY(y);
            link.initTwo();
            y += link.getTextHeight() + link.getLinkHeight();
        }
        y += 10;
        culculateFullSize(y);
        return new Dimension(maxX, y);
    }

    private void culculateFullSize(int height) {
        for(int i = 0; i < _objectLifeLines.size(); i++) {
            DisplayObject displayObject = _objectLifeLines.get(i);
            displayObject.setFullHeight(height);
            if(i + 1 == _objectLifeLines.size())
                displayObject.setFullWidth(displayObject.getWidth());
            else {
                DisplayObject nextDisplayObject = _objectLifeLines.get(i + 1);
                displayObject.setFullWidth(nextDisplayObject.getCenterX() - displayObject.getX());
            }
        }
    }

    public Dimension getPreferredHeaderSize() {
        int maxHeight = 0, width = 0;
        for (DisplayObject displayObjectInfo : _objectLifeLines) {
            int preferredHeight = displayObjectInfo.getPreferredHeaderHeight();
            if (maxHeight < preferredHeight)
                maxHeight = preferredHeight;
            width += displayObjectInfo.getPreferredHeaderWidth();
        }
        return new Dimension(width, maxHeight);
    }

    public ScreenObject findScreenObjectByXY(int x, int y) {
        DisplayMethod selectedMethodBox = null;
        for (DisplayObject displayObject : _objectLifeLines) {
            if (displayObject.isInRange(x, y))
                return displayObject;
            DisplayMethod methodBox = displayObject.findMethod(x, y);
            if (methodBox != null) {
                if (selectedMethodBox == null || selectedMethodBox.getX() < methodBox.getX()) {
                    selectedMethodBox = methodBox;
                }
            }
        }
        if(selectedMethodBox == null) {
            for (DisplayLink displayLink : _links) {
                if (displayLink.isReturnLink())
                    continue;
                if (displayLink.isInRange(x, y))
                    return displayLink;
            }
        }
        return selectedMethodBox;
    }

    public void paint(Graphics2D g2) {
        int max = _objectLifeLines.size();
        for (DisplayObject displayObject : _objectLifeLines) {
            displayObject.paint(g2);
        }
    }

    public void paintHeader(Graphics2D g2) {
        int max = _objectLifeLines.size();
        for (DisplayObject displayObject : _objectLifeLines) {
            displayObject.paintHeader(g2);
        }
    }

    public boolean isSingleObject() {
        return _objectLifeLines.size() == 1;
    }
}
