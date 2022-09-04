package org.intellij.sequencer.diagram;

import com.intellij.openapi.diagnostic.Logger;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Diagram {
    private static final Logger LOGGER = Logger.getInstance(Diagram.class);

    private final List<DisplayObject> _objectLifeLines = Collections.synchronizedList(new ArrayList<>());
    private final List<DisplayLink> _links = Collections.synchronizedList(new ArrayList<>());

    public Diagram() {
    }

    public void build(String queryString) {
        _objectLifeLines.clear();
        _links.clear();

        Parser p = new Parser();
        try {
            p.parse(queryString);
        } catch (IOException ioe) {
            LOGGER.error("IOException", ioe);
            return;
        }

        List<ObjectInfo> theObjects = p.getObjects();
        for (ObjectInfo objectInfo : theObjects) {
            _objectLifeLines.add(new DisplayObject(objectInfo));
        }

        List<Link> theDisplayLinks = p.getLinks();
        int seq = 0;
        synchronized (_objectLifeLines) {
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
        }

        synchronized (_objectLifeLines) {
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

        int maxWidth = 200;
        synchronized (_objectLifeLines) {
            for (int i = 0; i < _objectLifeLines.size(); ++i) {
                DisplayObject obj = _objectLifeLines.get(i);
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("Laying out " + obj);
                for (DisplayLink call : obj.getCalls()) {
                    int availableGap;
                    if (call.isSelfCall()) {
                        if (i == _objectLifeLines.size() - 1) {
                            int width = obj.getWidth();
                            if (width < call.getTextWidth())
                                obj.setWidth(width / 2 + call.getTextWidth());
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
                maxWidth = obj.getX() + obj.getWidth() + inset;
            }

            if (_objectLifeLines.isEmpty())
                y = 100;
            else
                y += (_objectLifeLines.get(0)).getHeight();
        }
        synchronized (_links) {
            for (DisplayLink link : _links) {
                link.setY(y);
                link.initTwo();
                y += link.getTextHeight() + link.getLinkHeight();
            }
        }
        int maxHeight = y + inset;

        calculateFullSize(y);
        return new Dimension(maxWidth, maxHeight);
    }

    private void calculateFullSize(int height) {
        synchronized (_objectLifeLines) {
            for (int i = 0; i < _objectLifeLines.size(); i++) {
                DisplayObject displayObject = _objectLifeLines.get(i);
                displayObject.setFullHeight(height);
                if (i + 1 == _objectLifeLines.size()) {
                    displayObject.setFullWidth(displayObject.getWidth());
                } else {
                    DisplayObject nextDisplayObject = _objectLifeLines.get(i + 1);
                    displayObject.setFullWidth(nextDisplayObject.getCenterX() - displayObject.getX());
                }
            }
        }
    }

    public Dimension getPreferredHeaderSize() {
        int maxHeight = 0, width = 0;
        synchronized (_objectLifeLines) {
            for (DisplayObject displayObjectInfo : _objectLifeLines) {
                int preferredHeight = displayObjectInfo.getPreferredHeaderHeight();
                if (maxHeight < preferredHeight)
                    maxHeight = preferredHeight;
                width = displayObjectInfo.getPreferredHeaderWidth();
            }
        }
        return new Dimension(width, maxHeight);
    }

    public ScreenObject findScreenObjectByXY(int x, int y) {
        DisplayMethod selectedMethodBox = null;
        synchronized (_objectLifeLines) {
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
        }
        if (selectedMethodBox == null) {
            synchronized (_links) {
                for (DisplayLink displayLink : _links) {
                    if (displayLink.isReturnLink())
                        continue;
                    if (displayLink.isInRange(x, y))
                        return displayLink;
                }
            }
        }
        return selectedMethodBox;
    }

    public void paint(Graphics2D g2) {
        synchronized (_objectLifeLines) {
            for (DisplayObject displayObject : _objectLifeLines) {
                displayObject.paint(g2);
            }
        }
    }

    public void paintHeader(Graphics2D g2) {
        synchronized (_objectLifeLines) {
            for (DisplayObject displayObject : _objectLifeLines) {
                displayObject.paintHeader(g2);
            }
        }
    }

    /**
     * `Actor` + One `DisplayObject` lifeline .
     * @return
     */
    public boolean isSingleObject() {
        return _objectLifeLines.size() <= 2;
    }

    public boolean isEmpty() {
        return _objectLifeLines.isEmpty();
    }

    public boolean nonEmpty() {
        return !isEmpty();
    }
}
