package org.intellij.sequencer.diagram;

import org.apache.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Diagram {
    private static final Logger LOGGER = Logger.getLogger(Diagram.class);

    private List _objectLifeLines = new ArrayList();
    private List _links = new ArrayList();

    public Diagram() {
    }

    public void build(String queryString) {
        _objectLifeLines = new ArrayList();
        _links = new ArrayList();

        Parser p = new Parser();
        try {
            p.parse(queryString);
        } catch(IOException ioe) {
            LOGGER.error("IOException", ioe);
            return;
        }

        List theObjects = p.getObjects();
        for(Iterator it = theObjects.iterator(); it.hasNext();) {
            ObjectInfo objectInfo = (ObjectInfo)it.next();
            _objectLifeLines.add(new DisplayObject(objectInfo));
        }

        List theDisplayLinks = p.getLinks();
        int seq = 0;
        for(Iterator it = theDisplayLinks.iterator(); it.hasNext();) {
            Link link = (Link)it.next();
            int fromSeq = link.getFrom().getSeq();
            int toSeq = link.getTo().getSeq();
            DisplayObject fromObj = (DisplayObject)_objectLifeLines.get(fromSeq);
            DisplayObject toObj = (DisplayObject)_objectLifeLines.get(toSeq);
            DisplayLink displayLink = null;
            if(link instanceof Call) {
                if(fromSeq == toSeq)
                    displayLink = new DisplaySelfCall(link, fromObj, toObj, seq);
                else
                    displayLink = new DisplayCall(link, fromObj, toObj, seq);
            } else if(link instanceof CallReturn) {
                if(fromSeq == toSeq)
                    displayLink = new DisplaySelfCallReturn(link, fromObj, toObj, seq);
                else
                    displayLink = new DisplayCallReturn(link, fromObj, toObj, seq);
            } else {
                LOGGER.error("Unknown link: " + link);
            }
            if(displayLink != null) {
                _links.add(displayLink);
                ++seq;
            }
        }

        for(Iterator it = theObjects.iterator(); it.hasNext();) {
            ObjectInfo info = (ObjectInfo)it.next();
            DisplayObject displayInfo = (DisplayObject)_objectLifeLines.get(info.getSeq());
            for(Iterator methodsIt = info.getMethods().iterator(); methodsIt.hasNext();) {
                MethodInfo methodInfo = (MethodInfo)methodsIt.next();
                int startSeq = methodInfo.getStartSeq();
                int endSeq = methodInfo.getEndSeq();
                if((startSeq < _links.size()) && (endSeq < _links.size())) {
                    DisplayMethod methodBox = new DisplayMethod(info, methodInfo,
                          (DisplayLink)_links.get(startSeq), (DisplayLink)_links.get(endSeq));
                    displayInfo.addMethod(methodBox);
                }
            }
        }
    }

    public Dimension layoutObjects(Graphics2D g2, int inset) {
        int x = inset;
        int y = inset;
        for(int i = 0; i < _objectLifeLines.size(); i++) {
            DisplayObject displayObject = (DisplayObject)_objectLifeLines.get(i);
            displayObject.setX(x);
            displayObject.setY(y);
            displayObject.initializeGraphics(g2);
            x += displayObject.getWidth() + inset;
        }

        int maxX = 200;
        for(int i = 0; i < _objectLifeLines.size(); ++i) {
            DisplayObject obj = (DisplayObject)_objectLifeLines.get(i);
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("Laying out " + obj);
            for(Iterator it = obj.getCalls().iterator(); it.hasNext();) {
                DisplayLink call = (DisplayLink)it.next();
                int availableGap = -1;
                if(call.isSelfCall()) {
                    if(i == _objectLifeLines.size() - 1) {
                        int width = obj.getWidth();
                        if(width < call.getTextWidth())
                            obj.setWidth(obj.getTextWidth() + call.getTextWidth());
                        continue;
                    } else {
                        availableGap = obj.calcCurrentGap(
                              (DisplayObject)_objectLifeLines.get(i + 1), call.getSeq());
                    }
                } else {
                    availableGap = obj.calcCurrentGap(call.getTo(), call.getSeq());
                }

                if(availableGap < call.getTextWidth()) {
                    int offset = call.getTextWidth() - availableGap;
                    if(LOGGER.isDebugEnabled())
                        LOGGER.debug("gap too small by " + offset);
                    int startJ = i + 1;
                    if(call.getTo().getSeq() < startJ)
                        startJ = call.getTo().getSeq() + 1;
                    for(int j = startJ; j < _objectLifeLines.size(); ++j) {
                        ((DisplayObject)_objectLifeLines.get(j)).translate(offset);
                    }
                }
            }
            maxX = obj.getX() + 2 * obj.getWidth() + inset;
        }

        if(_objectLifeLines.isEmpty())
            y = 100;
        else
            y += ((DisplayObject)_objectLifeLines.get(0)).getHeight();
        for(Iterator it = _links.iterator(); it.hasNext();) {
            DisplayLink link = (DisplayLink)it.next();
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
            DisplayObject displayObject = (DisplayObject)_objectLifeLines.get(i);
            displayObject.setFullHeight(height);
            if(i + 1 == _objectLifeLines.size())
                displayObject.setFullWidth(displayObject.getWidth());
            else {
                DisplayObject nextDisplayObject = (DisplayObject)_objectLifeLines.get(i + 1);
                displayObject.setFullWidth(nextDisplayObject.getCenterX() - displayObject.getX());
            }
        }
    }

    public Dimension getPreferredHeaderSize() {
        int maxHeight = 0, width = 0;
        for(Iterator iterator = _objectLifeLines.iterator(); iterator.hasNext();) {
            DisplayObject displayObjectInfo = (DisplayObject)iterator.next();
            int preferredHeight = displayObjectInfo.getPreferredHeaderHeight();
            if(maxHeight < preferredHeight)
                maxHeight = preferredHeight;
            width += displayObjectInfo.getPreferredHeaderWidth();
        }
        return new Dimension(width, maxHeight);
    }

    public ScreenObject findScreenObjectByXY(int x, int y) {
        DisplayMethod selectedMethodBox = null;
        for(Iterator iterator = _objectLifeLines.iterator(); iterator.hasNext();) {
            DisplayObject displayObject = (DisplayObject)iterator.next();
            if(displayObject.isInRange(x, y))
                return displayObject;
            DisplayMethod methodBox = displayObject.findMethod(x, y);
            if(methodBox != null) {
                if(selectedMethodBox == null || selectedMethodBox.getX() < methodBox.getX()) {
                    selectedMethodBox = methodBox;
                }
            }
        }
        if(selectedMethodBox == null) {
            for(Iterator iterator = _links.iterator(); iterator.hasNext();) {
                DisplayLink displayLink = (DisplayLink)iterator.next();
                if(displayLink.isReturnLink())
                    continue;
                if(displayLink.isInRange(x, y))
                    return displayLink;
            }
        }
        return selectedMethodBox;
    }

    public void paint(Graphics2D g2) {
        int max = _objectLifeLines.size();
        for(int i = 0; i < max; ++i) {
            DisplayObject displayObject = (DisplayObject)_objectLifeLines.get(i);
            displayObject.paint(g2);
        }
    }

    public void paintHeader(Graphics2D g2) {
        int max = _objectLifeLines.size();
        for(int i = 0; i < max; ++i) {
            DisplayObject displayObject = (DisplayObject)_objectLifeLines.get(i);
            displayObject.paintHeader(g2);
        }
    }

    public boolean isSingleObject() {
        return _objectLifeLines.size() == 1;
    }
}
