package org.intellij.sequencer.ui;

import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.metal.MetalTabbedPaneUI;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class PlasticTabbedPaneUI  extends MetalTabbedPaneUI {
    public static final String MARK_CONTENT_BORDERS_KEY =
        "markContentBorders";


    // State ******************************************************************

    /**
     * Indicates that content borders shall be marked.
     */
    private static final boolean MARK_CONTENT_BORDERS =
        System.getProperty(MARK_CONTENT_BORDERS_KEY, "").
            equalsIgnoreCase("true");

    /**
     * Describes if tabs are painted with or without icons.
     */
    private static boolean isTabIconsEnabled = true;

    /**
     * Used in mark content borders mode to paint the content border
     * that may be otherwise difficult to see.
     */
    private static Color MARK_CONTENT_BORDER_COLOR = JBColor.MAGENTA;

    /**
     * Describes if we paint no content border or not; is false by default.
     * You can disable the content border by setting the client property
     * Options.NO_CONTENT_BORDER_KEY to Boolean.TRUE;
     * <p>
     * Overrides any ClearLook considerations.
     */
    private Boolean noContentBorder;

    /**
     * Describes if we paint tabs in an embedded style that is with
     * less decoration; this is false by default.
     * You can enable the embedded tabs style by setting the client property
     * Options.EMBEDDED_TABS_KEY to Boolean.TRUE.
     * <p>
     * Overrides any ClearLook considerations.
     */
    private Boolean embeddedTabs;

    /**
     * Describes that ClearLook suggests to hide the content border.
     * Will be overriden if the developer sets a content border client property.
     */
    private boolean clearLookSuggestsNoContentBorder = false;

    /**
     * Holds the renderer that is used to render the tabs.
     */
    private AbstractRenderer renderer;
    private static final String EMBEDDED_TABS = "jgoodies.embeddedTabs";
    private static final String NO_CONTENT_BORDER = "jgoodies.noContentBorder";


    /**
     * Creates the <code>PlasticTabbedPaneUI</code>.
     *
     * @see javax.swing.plaf.ComponentUI#createUI(javax.swing.JComponent)
     */
    public static ComponentUI createUI(JComponent tabPane) {
        return new PlasticTabbedPaneUI();
    }

    /**
     * Installs the UI.
     *
     * @see javax.swing.plaf.ComponentUI#installUI(JComponent)
     */
    public void installUI(JComponent c) {
        super.installUI(c);
        embeddedTabs    = (Boolean) c.getClientProperty(EMBEDDED_TABS);
        noContentBorder = (Boolean) c.getClientProperty(NO_CONTENT_BORDER);
        renderer = createRenderer(tabPane);
    }

    /**
     * Uninstalls the UI.
     * @see javax.swing.plaf.ComponentUI#uninstallUI(JComponent)
     */
    public void uninstallUI(JComponent c) {
        renderer = null;
        super.uninstallUI(c);
    }

    /**
     * Checks and answers if content border will be painted.
     * This is controlled by the component's client property
     * Options.NO_CONTENT_BORDER or Options.EMBEDDED.
     */
    private boolean hasNoContentBorder() {
        return noContentBorder == null
                ? clearLookSuggestsNoContentBorder()
                : noContentBorder;
    }

    /**
     * Checks and answers if tabs are painted with minimal decoration.
     */
    private boolean hasEmbeddedTabs() {
        return embeddedTabs == null
                ? false
                : embeddedTabs;
    }

    /**
     * Checks and answers if ClearLook suggests to hide the content border.
     */
    private boolean clearLookSuggestsNoContentBorder() {
        return clearLookSuggestsNoContentBorder;
    }

    /**
     * Creates the renderer used to layout and paint the tabs.
     * @param tabbedPane               the UIs component
     * @return AbstractRenderer     the renderer that will be used to paint
     */
    private AbstractRenderer createRenderer(JTabbedPane tabbedPane) {
        return hasEmbeddedTabs()
                ? AbstractRenderer.createEmbeddedRenderer(tabbedPane)
                : AbstractRenderer.createRenderer(tabPane);
    }

    /**
     * Checks if ClearLook indicates that the current component context
     * uses a border that is considered as visual clutter.
     * In this case replaces the border.
     *
     * @param tabbedPane    the tabbed pane component
     */
    private void checkBorderReplacement(JTabbedPane tabbedPane) {
        Border newBorder = null;//ClearLookManager.replaceBorder(tabbedPane);
        // Override the ClearLook suggestion if my parent is a split pane
        Container parent = tabbedPane.getParent();
        if (parent != null && (parent instanceof JSplitPane)) {
            newBorder = null;
        }
        clearLookSuggestsNoContentBorder = newBorder != null;
    }

    /**
     * Creates and answer a handler that listens to property changes.
     * Unlike the superclass BasicTabbedPane, the PlasticTabbedPaneUI
     * uses an extended Handler.
     *
     * @see javax.swing.plaf.basic.BasicTabbedPaneUI#createPropertyChangeListener()
     */
    protected PropertyChangeListener createPropertyChangeListener() {
        return new MyPropertyChangeHandler();
    }

    protected ChangeListener createChangeListener() {
//        return scrollableTabLayoutEnabled()
//                    ? super.createChangeListener()
//                    : new TabSelectionHandler();
        return new TabSelectionHandler();
    }

    /*
     * Private helper method for the next three methods.
     */
    private void doLayout() {
         TabbedPaneLayout layout = (TabbedPaneLayout)tabPane.getLayout();
         layout.calculateLayoutInfo();
         tabPane.repaint();
    }

     /**
      * Updates the renderer and layout. This message is sent by
      * my PropertyChangeHandler whenever the tab placement changes.
      */
     private void tabPlacementChanged() {
         renderer = createRenderer(tabPane);
         doLayout();
     }

    /**
     * Updates the embedded tabs property. This message is sent by
     * my PropertyChangeHandler whenever the embedded tabs property changes.
     */
    private void embeddedTabsPropertyChanged(Boolean newValue) {
        embeddedTabs = newValue;
        renderer = createRenderer(tabPane);
        doLayout();
    }

     /**
      * Updates the no content border property. This message is sent
      * by my PropertyChangeHandler whenever the noContentBorder
      * property changes.
      */
     private void noContentBorderPropertyChanged(Boolean newValue) {
         noContentBorder = newValue;
         tabPane.repaint();
     }

    private void ensureCurrentLayout() {
        if (!tabPane.isValid()) {
            tabPane.validate();
        }
    /* If tabPane doesn't have a peer yet, the validate() call will
     * silently fail.  We handle that by forcing a layout if tabPane
     * is still invalid.  See bug 4237677.
     */
        if (!tabPane.isValid()) {
            TabbedPaneLayout layout = (TabbedPaneLayout)tabPane.getLayout();
            layout.calculateLayoutInfo();
        }
    }

    /**
     * Paints the tabbed pane; checks if we replace borders, first.
     */
    public void paint(Graphics g, JComponent c) {
//        if (scrollableTabLayoutEnabled()) {
//            super.paint(g, c);
//            return;
//        }

        int selectedIndex = tabPane.getSelectedIndex();
        int tabPlacement  = tabPane.getTabPlacement();
        int tabCount      = tabPane.getTabCount();

        ensureCurrentLayout();

        Rectangle iconRect = new Rectangle();
        Rectangle textRect = new Rectangle();
        Rectangle clipRect = g.getClipBounds();

        // Paint tabRuns of tabs from back to front
        for (int i = runCount - 1; i >= 0; i--) {
            int start = tabRuns[i];
            int next  = tabRuns[(i == runCount - 1) ? 0 : i + 1];
            int end   = (next != 0 ? next - 1 : tabCount - 1);
            for (int j = end; j >= start; j--) {
                if (rects[j].intersects(clipRect)) {
                    paintTab(g, tabPlacement, rects, j, iconRect, textRect);
                }
            }
        }
        // Always paint selected tab
        // since it may overlap other tabs
        if (selectedIndex >= 0 /*&& isTabInFirstRun(selectedIndex)*/) {
            if (selectedIndex >= rects.length) {
                System.out.println("Caution");

            }
            if (rects[selectedIndex].intersects(clipRect)) {
                paintTab(g, tabPlacement, rects, selectedIndex, iconRect, textRect);
            }
        }
        // Paint content border
        paintContentBorder(g, tabPlacement, selectedIndex);
    }

    /*
     * Copied here from super(super)class to avoid labels being centered on
     * vertical tab runs if they consist of icon and text
     */
    protected void layoutLabel(
        int tabPlacement,
        FontMetrics metrics,
        int tabIndex,
        String title,
        Icon icon,
        Rectangle tabRect,
        Rectangle iconRect,
        Rectangle textRect,
        boolean isSelected) {
        textRect.x = textRect.y = iconRect.x = iconRect.y = 0;
        Rectangle calcRectangle = new Rectangle(tabRect);
        if (isSelected) {
        	Insets calcInsets = getSelectedTabPadInsets(tabPlacement);
        	calcRectangle.x += calcInsets.left;
        	calcRectangle.y += calcInsets.top;
        	calcRectangle.width -= calcInsets.left + calcInsets.right ;
        	calcRectangle.height -= calcInsets.bottom + calcInsets.top;
        }
		int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
		int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
        if ((tabPlacement == RIGHT || tabPlacement == LEFT) && icon != null && title != null && !title.equals("")) {
            SwingUtilities.layoutCompoundLabel(
                    tabPane,
                metrics,
                title,
                icon,
                SwingUtilities.CENTER,
                SwingUtilities.LEFT,
                SwingUtilities.CENTER,
                SwingUtilities.TRAILING,
                calcRectangle,
                iconRect,
                textRect,
                textIconGap);
            xNudge += 4;
        } else {
            SwingUtilities.layoutCompoundLabel(
                    tabPane,
                metrics,
                title,
                icon,
                SwingUtilities.CENTER,
                SwingUtilities.CENTER,
                SwingUtilities.CENTER,
                SwingUtilities.TRAILING,
                calcRectangle,
                iconRect,
                textRect,
                textIconGap);
                iconRect.y += calcRectangle.height %2;
        }

        iconRect.x += xNudge;
        iconRect.y += yNudge;
        textRect.x += xNudge;
        textRect.y += yNudge;
    }

    /**
     * Answers the icon for the tab with the specified index.
     * In case, we have globally switched of the use tab icons,
     * we answer <code>null</code> if and only if we have a title.
     */
    protected Icon getIconForTab(int tabIndex) {
        String title = tabPane.getTitleAt(tabIndex);
        boolean hasTitle = (title != null) && (title.length() > 0);
        return !isTabIconsEnabled  && hasTitle
                    ? null
                    : super.getIconForTab(tabIndex);
    }

    /**
     * Checks and answers if the scrollable tab layout is enabled.
     *
     * @return true for scroll tab layout, false for wrap tab layout.
     */
//    private boolean scrollableTabLayoutEnabled() {
//        return tabPane.getTabLayoutPolicy() == 1;
//    }

    /**
     * Creates the layout manager used to set the tab's bounds.
     */
    protected LayoutManager createLayoutManager() {
//        if (scrollableTabLayoutEnabled()) {
//            return super.createLayoutManager();
//        }
        return new TabbedPaneLayout();
    }

    protected boolean isTabInFirstRun(int tabIndex) {
        return getRunForTab(tabPane.getTabCount(), tabIndex) == 0;
    }

    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        int    width  = tabPane.getWidth();
        int    height = tabPane.getHeight();
        Insets insets = tabPane.getInsets();

        int x = insets.left;
        int y = insets.top;
        int w = width - insets.right - insets.left;
        int h = height - insets.top - insets.bottom;

        switch (tabPlacement) {
            case LEFT :
                x += calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
                w -= (x - insets.left);
                break;
            case RIGHT :
                w -= calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
                break;
            case BOTTOM :
                h -= calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                break;
            case TOP :
            default :
                y += calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                h -= (y - insets.top);
        }
        // Fill region behind content area
        g.setColor(selectColor == null
                        ? tabPane.getBackground()
                        : selectColor);
        g.fillRect(x, y, w, h);

        Rectangle selRect;
        selRect = (selectedIndex < 0) ? null : getTabBounds(tabPane, selectedIndex);
        //pending: when 1.4 gets standard, code below will work
        //      selRect = (selectedIndex < 0) ? null : getTabBounds(selectedIndex, calcRect);
        boolean drawBroken = selectedIndex >= 0 && isTabInFirstRun(selectedIndex);
        boolean isContentBorderPainted = !hasNoContentBorder();
        // It sounds a bit odd to call paintContentBorder with
        // a parameter isContentBorderPainted set to false.
        // But in this case the part of the border touching the tab
        // area will still be painted so best let the renderer decide.
        renderer.paintContentBorderTopEdge   (g, x, y, w, h, drawBroken, selRect, isContentBorderPainted);
        renderer.paintContentBorderLeftEdge  (g, x, y, w, h, drawBroken, selRect, isContentBorderPainted);
        renderer.paintContentBorderBottomEdge(g, x, y, w, h, drawBroken, selRect, isContentBorderPainted);
        renderer.paintContentBorderRightEdge (g, x, y, w, h, drawBroken, selRect, isContentBorderPainted);
    }

    //
    // Here comes a number of methods that are just delegated to the
    // appropriate renderer
    //
    /**
     * Returns the insets (i.e. the width) of the content Border
     */
    protected Insets getContentBorderInsets(int tabPlacement) {
        return renderer.getContentBorderInsets(super.getContentBorderInsets(tabPlacement));
    }

    /**
     * Returns the amount by which the Tab Area is inset
     */
    protected Insets getTabAreaInsets(int tabPlacement) {
        return renderer.getTabAreaInsets(super.getTabAreaInsets(tabPlacement));
    }

    /**
     * Returns the amount by which the label should be shifted horizontally
     */
    protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
        return renderer.getTabLabelShiftX(tabIndex, isSelected);
    }

    /**
     * Returns the amount by which the label should be shifted vertically
     */
    protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
        return renderer.getTabLabelShiftY(tabIndex, isSelected);
    }

    /**
     * Returns the amount (in pixels) by which two runs should overlap
     */
    protected int getTabRunOverlay(int tabPlacement) {
        return renderer.getTabRunOverlay(tabRunOverlay);
    }

    /**
     * This boolean controls wheather the given run should be padded to
     * use up as much space as the others (with more tabs in them)
     */
    protected boolean shouldPadTabRun(int tabPlacement, int run) {
        return renderer.shouldPadTabRun(run, super.shouldPadTabRun(tabPlacement, run));
    }

    /**
     * Returns the amount by which the run number <code>run</code>
     * should be indented. Add six pixels for every run to make
     * diagonal lines align.
     */
    protected int getTabRunIndent(int tabPlacement, int run) {
        return renderer.getTabRunIndent(run);
    }

    /**
     * Returns the insets for this tab.
     */
    protected Insets getTabInsets(int tabPlacement, int tabIndex) {
        return renderer.getTabInsets(tabIndex, tabInsets);
    }

    /**
     * Returns the insets for selected tab.
     */
    protected Insets getSelectedTabPadInsets(int tabPlacement) {
        return renderer.getSelectedTabPadInsets();
    }

    /**
     * Draws the rectancle around the Tab label which indicates keyboard focus
     */
    protected void paintFocusIndicator(
        Graphics g,
        int tabPlacement,
        Rectangle[] rectangles,
        int tabIndex,
        Rectangle iconRect,
        Rectangle textRect,
        boolean isSelected) {
        renderer.paintFocusIndicator(g, rectangles, tabIndex, iconRect, textRect, isSelected);
    }

    /**
     * Fills the background of the given tab to make sure overlap of
     * tabs is handled correctly.
     * Note: that tab backgrounds seem to be painted somewhere else, too.
     */
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        renderer.paintTabBackground(g, tabIndex, x, y, w, h, isSelected);
    }

    /**
     * Paints the border for one tab. Gets the bounds of the tab as parameters.
     * Note that the result is not clipped so you can paint outside that
     * rectangle. Tabs painted later on have a chance to overwrite though.
     */
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        renderer.paintTabBorder(g, tabIndex, x, y, w, h, isSelected);
    }

    /**
     * Answers wheather tab runs should be rotated. If true, the layout mechanism
     * will move the run containing the selected tab so that it touches
     * the content pane.
     */
    protected boolean shouldRotateTabRuns(int tabPlacement) {
        return false;
    }

    /**
     * Catches and handles property change events. In addition to the super
     * class behavior we listen to changes of the ancestor, tab placement,
     * and JGoodies options for content border, and embedded tabs.
     */
    private class MyPropertyChangeHandler extends BasicTabbedPaneUI.PropertyChangeHandler {

        public void propertyChange(PropertyChangeEvent e) {
            super.propertyChange(e);

            String pName = e.getPropertyName();
            if (null == pName) {
                return;
            }
            if (pName.equals("ancestor")) {
                checkBorderReplacement(tabPane);
            }
            if (pName.equals("tabPlacement")) {
                tabPlacementChanged();
                return;
            }
            if (pName.equals(EMBEDDED_TABS)) {
                embeddedTabsPropertyChanged((Boolean) e.getNewValue());
                return;
            }
            if (pName.equals(NO_CONTENT_BORDER)) {
                noContentBorderPropertyChanged((Boolean) e.getNewValue());
                return;
            }
        }
    }

    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of BasicTabbedPaneUI.
     */
    public class TabSelectionHandler implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            JTabbedPane tabbedPane = (JTabbedPane)e.getSource();
            tabbedPane.revalidate();
            tabbedPane.repaint();
        }
    }

    /**
     * Does all the layout work. The result is stored in the container
     * class's instance variables. Mainly the rects[] vector.
     */
    private class TabbedPaneLayout extends BasicTabbedPaneUI.TabbedPaneLayout implements LayoutManager {

        protected void calculateTabRects(int tabPlacement, int tabCount) {
            FontMetrics metrics = getFontMetrics();
            Dimension size = tabPane.getSize();
            Insets insets = tabPane.getInsets();
            Insets theTabAreaInsets = getTabAreaInsets(tabPlacement);
            int fontHeight    = metrics.getHeight();
            int selectedIndex = tabPane.getSelectedIndex();
            int theTabRunOverlay;
            int i, j;
            int x, y;
            int returnAt;
            boolean verticalTabRuns = (tabPlacement == LEFT || tabPlacement == RIGHT);
            boolean leftToRight = tabPane.getComponentOrientation().isLeftToRight();

            //
            // Calculate bounds within which a tab run must fit
            //
            switch (tabPlacement) {
                case LEFT :
                    maxTabWidth = calculateMaxTabWidth(tabPlacement);
                    x = insets.left + theTabAreaInsets.left;
                    y = insets.top  + theTabAreaInsets.top;
                    returnAt = size.height - (insets.bottom + theTabAreaInsets.bottom);
                    break;
                case RIGHT :
                    maxTabWidth = calculateMaxTabWidth(tabPlacement);
                    x = size.width - insets.right - theTabAreaInsets.right - maxTabWidth;
                    y = insets.top + theTabAreaInsets.top;
                    returnAt = size.height - (insets.bottom + theTabAreaInsets.bottom);
                    break;
                case BOTTOM :
                    maxTabHeight = calculateMaxTabHeight(tabPlacement);
                    x = insets.left + theTabAreaInsets.left;
                    y = size.height - insets.bottom - theTabAreaInsets.bottom - maxTabHeight;
                    returnAt = size.width - (insets.right + theTabAreaInsets.right);
                    break;
                case TOP :
                default :
                    maxTabHeight = calculateMaxTabHeight(tabPlacement);
                    x = insets.left + theTabAreaInsets.left;
                    y = insets.top  + theTabAreaInsets.top;
                    returnAt = size.width - (insets.right + theTabAreaInsets.right);
                    break;
            }

            theTabRunOverlay = getTabRunOverlay(tabPlacement);

            runCount = 0;
            selectedRun = -1;
            //keeps track of where we are in the current run.
            //this helps not to rely on fragile positioning
            //informaion to find out wheter the active Tab
            //is the first in run
            int tabInRun = -1;
            // make a copy of returnAt for the current run and modify
            // that so returnAt may still be used later on
            int runReturnAt = returnAt;

            if (tabCount == 0) {
                return;
            }

            // Run through tabs and partition them into runs
            Rectangle rect;
            for (i = 0; i < tabCount; i++) {
                rect = rects[i];
                tabInRun++;

                if (!verticalTabRuns) {
                    // Tabs on TOP or BOTTOM....
                    if (i > 0) {
                        rect.x = rects[i - 1].x + rects[i - 1].width;
                    } else {
                        tabRuns[0] = 0;
                        runCount = 1;
                        maxTabWidth = 0;
                        rect.x = x;
                        //  tabInRun = 0;
                    }
                    rect.width  = calculateTabWidth(tabPlacement, i, metrics);
                    maxTabWidth = Math.max(maxTabWidth, rect.width);

                    // Never move a TAB down a run if it is the first in run.
                    // Even if there isn't enough room, moving it to a fresh
                    // line won't help.
                    //                    if (rect.x != 2 + insets.left && rect.x + rect.width > returnAt) {
                    // Never rely on phisical position information to determine
                    // logical position (if you can avoid it)
                    if (tabInRun != 0 && rect.x + rect.width > runReturnAt) {
                        if (runCount > tabRuns.length - 1) {
                            expandTabRunsArray();
                        }
                        // just created a new run, adjust some counters
                        tabInRun = 0;
                        tabRuns[runCount] = i;
                        runCount++;
                        rect.x = x;
                        runReturnAt = runReturnAt - 2 * getTabRunIndent(tabPlacement, runCount);
                    }
                    // Initialize y position in case there's just one run
                    rect.y = y;
                    rect.height = maxTabHeight /* - 2*/;

                } else {
                    // Tabs on LEFT or RIGHT...
                    if (i > 0) {
                        rect.y = rects[i - 1].y + rects[i - 1].height;
                    } else {
                        tabRuns[0] = 0;
                        runCount = 1;
                        maxTabHeight = 0;
                        rect.y = y;
                        //                        tabInRun = 0;
                    }
                    rect.height = calculateTabHeight(tabPlacement, i, fontHeight);
                    maxTabHeight = Math.max(maxTabHeight, rect.height);

                    // Never move a TAB over a run if it is the first in run.
                    // Even if there isn't enough room, moving it to a fresh
                    // run won't help.
                    //                    if (rect.y != 2 + insets.top && rect.y + rect.height > returnAt) {
                    if (tabInRun != 0 && rect.y + rect.height > runReturnAt) {
                        if (runCount > tabRuns.length - 1) {
                            expandTabRunsArray();
                        }
                        tabRuns[runCount] = i;
                        runCount++;
                        rect.y = y;
                        tabInRun = 0;
                        runReturnAt -= 2 * getTabRunIndent(tabPlacement, runCount);
                    }
                    // Initialize x position in case there's just one column
                    rect.x = x;
                    rect.width = maxTabWidth /* - 2*/;

                }
                if (i == selectedIndex) {
                    selectedRun = runCount - 1;
                }
            }

            if (runCount > 1) {
                // Re-distribute tabs in case last run has leftover space
                //last line flush left is OK
                //                normalizeTabRuns(tabPlacement, tabCount, verticalTabRuns? y : x, returnAt);
                //don't need to recalculate selectedRun if not changed
                //                selectedRun = getRunForTab(tabCount, selectedIndex);

                // Rotate run array so that selected run is first
                if (shouldRotateTabRuns(tabPlacement)) {
                    rotateTabRuns(tabPlacement, selectedRun);
                }
            }

            // Step through runs from back to front to calculate
            // tab y locations and to pad runs appropriately
            for (i = runCount - 1; i >= 0; i--) {
                int start = tabRuns[i];
                int next  = tabRuns[i == (runCount - 1) ? 0 : i + 1];
                int end   = (next != 0 ? next - 1 : tabCount - 1);
                int indent = getTabRunIndent(tabPlacement, i);
                if (!verticalTabRuns) {
                    for (j = start; j <= end; j++) {
                        rect = rects[j];
                        rect.y = y;
                        rect.x += indent;
                        // try to make tabRunIndent symmetric
                        //	rect.width -= 2* indent + 20;
                    }
                    if (shouldPadTabRun(tabPlacement, i)) {
                        padTabRun(tabPlacement, start, end, returnAt - 2 * indent);
                    }
                    if (tabPlacement == BOTTOM) {
                        y -= (maxTabHeight - theTabRunOverlay);
                    } else {
                        y += (maxTabHeight - theTabRunOverlay);
                    }
                } else {
                    for (j = start; j <= end; j++) {
                        rect = rects[j];
                        rect.x = x;
                        rect.y += indent;
                    }
                    if (shouldPadTabRun(tabPlacement, i)) {
                        padTabRun(tabPlacement, start, end, returnAt - 2 * indent);
                    }
                    if (tabPlacement == RIGHT) {
                        x -= (maxTabWidth - theTabRunOverlay);
                    } else {
                        x += (maxTabWidth - theTabRunOverlay);
                    }
                }
            }

            // Pad the selected tab so that it appears raised in front
            padSelectedTab(tabPlacement, selectedIndex);

            // if right to left and tab placement on the top or
            // the bottom, flip x positions and adjust by widths
            if (!leftToRight && !verticalTabRuns) {
                int rightMargin = size.width - (insets.right + theTabAreaInsets.right);
                for (i = 0; i < tabCount; i++) {
                    rects[i].x = rightMargin - rects[i].x - rects[i].width;
                }
            }
        }
    }

    /**
     * This is the abstract superclass for all TabbedPane renderers.
     * Those will be defined in the rest of this file
     */
    private static abstract class AbstractRenderer {

        protected static final Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);
        protected static final Insets NORTH_INSETS = new Insets(1, 0, 0, 0);
        protected static final Insets WEST_INSETS  = new Insets(0, 1, 0, 0);
        protected static final Insets SOUTH_INSETS = new Insets(0, 0, 1, 0);
        protected static final Insets EAST_INSETS  = new Insets(0, 0, 0, 1);

        protected final JTabbedPane tabPane;
        protected final int tabPlacement;
        protected Color shadowColor;
        protected Color darkShadow;
        protected Color selectColor;
        protected Color selectLight;
        protected Color selectHighlight;
        protected Color lightHighlight;
        protected Color focus;

        private AbstractRenderer(JTabbedPane tabPane) {
            initColors();
            this.tabPane = tabPane;
            this.tabPlacement = tabPane.getTabPlacement();
        }

        private static AbstractRenderer createRenderer(JTabbedPane tabPane) {
            switch (tabPane.getTabPlacement()) {
                case JTabbedPane.TOP :
                    return new TopRenderer(tabPane);
                case JTabbedPane.BOTTOM :
                    return new BottomRenderer(tabPane);
                case JTabbedPane.LEFT :
                    return new LeftRenderer(tabPane);
                case JTabbedPane.RIGHT :
                    return new RightRenderer(tabPane);
                default :
                    return new TopRenderer(tabPane);
            }
        }

        private static AbstractRenderer createEmbeddedRenderer(JTabbedPane tabPane) {
            switch (tabPane.getTabPlacement()) {
                case JTabbedPane.TOP :
                    return new TopEmbeddedRenderer(tabPane);
                case JTabbedPane.BOTTOM :
                    return new BottomEmbeddedRenderer(tabPane);
                case JTabbedPane.LEFT :
                    return new LeftEmbeddedRenderer(tabPane);
                case JTabbedPane.RIGHT :
                    return new RightEmbeddedRenderer(tabPane);
                default :
                    return new TopEmbeddedRenderer(tabPane);
            }
        }

        private void initColors() {
            shadowColor     = UIManager.getColor("TabbedPane.shadow");
            darkShadow      = UIManager.getColor("TabbedPane.darkShadow");
            selectColor     = UIManager.getColor("TabbedPane.selected");
            focus           = UIManager.getColor("TabbedPane.focus");
            selectHighlight = UIManager.getColor("TabbedPane.selectHighlight");
            lightHighlight  = UIManager.getColor("TabbedPane.highlight");
            selectLight =
                new Color(
                    (2 * selectColor.getRed()   + selectHighlight.getRed())   / 3,
                    (2 * selectColor.getGreen() + selectHighlight.getGreen()) / 3,
                    (2 * selectColor.getBlue()  + selectHighlight.getBlue())  / 3);
        }

        protected boolean isFirstDisplayedTab(int tabIndex, int position, int paneBorder) {
            return tabIndex == 0;
//            return (position - paneBorder) < 8;
        }

        protected Insets getTabAreaInsets(Insets defaultInsets) {
            return defaultInsets;
        }

        protected Insets getContentBorderInsets(Insets defaultInsets) {
            return defaultInsets;
        }

        /**
         * Returns the amount by which the label should be shifted horizontally
         */
        protected int getTabLabelShiftX(int tabIndex, boolean isSelected) {
            return 0;
        }

        /**
         * Returns the amount by which the label should be shifted vertically
         */
        protected int getTabLabelShiftY(int tabIndex, boolean isSelected) {
            return 0;
        }

        /**
         * Returns the amount of overlap for two Runs
         */
        protected int getTabRunOverlay(int tabRunOverlay) {
            return tabRunOverlay;
        }

        /**
         * Returns if a run should be padded with empty space
         * to take up as much room as the others
         */
        protected boolean shouldPadTabRun(int run, boolean aPriori) {
            return aPriori;
        }

        /**
         * Returns the amount by which the run number <code>run</code>
         * should be indented. Add a few pixels for every run to make
         * diagonal lines align.
         */
        protected int getTabRunIndent(int run) {
            return 0;
        }

        /**
         * Returns the insets for the given tab.
         */
        abstract protected Insets getTabInsets(int tabIndex, Insets tabInsets);

        /**
         * Draws the rectancle around the Tab label which indicates keyboard focus
         */
        abstract protected void paintFocusIndicator(
            Graphics g,
            Rectangle[] rects,
            int tabIndex,
            Rectangle iconRect,
            Rectangle textRect,
            boolean isSelected);

        /**
         * Fills the background of the given tab to make sure overlap of
         * tabs is handled correctly.
         */
        abstract protected void paintTabBackground(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected);

        /**
         * Paints the border around the given tab
         */
        abstract protected void paintTabBorder(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected);

        /**
         * Returns additional the insets for the selected tab. This allows to "raise"
         * The selected tab over the others
         */
        protected Insets getSelectedTabPadInsets() {
            return EMPTY_INSETS;
        }

        /**
         * Draws the top edge of the border around the content area
         * Draw unbroken line for tabs are not on TOP
         * override where appropriate
         */
        protected void paintContentBorderTopEdge(
            Graphics g,
            int x,
            int y,
            int w,
            int h,
            boolean drawBroken,
            Rectangle selRect,
            boolean isContentBorderPainted) {
            if (isContentBorderPainted) {
                g.setColor(MARK_CONTENT_BORDERS
                        ? MARK_CONTENT_BORDER_COLOR
                        : selectHighlight);
                g.fillRect(x, y, w - 1, 1);
            }
        }

        /**
         * Draws the bottom edge of the Border around the content area
         * Draw broken line if selected tab is visible and adjacent to content
         * and TabPlacement is same as painted edge
         */
        protected void paintContentBorderBottomEdge(
            Graphics g,
            int x,
            int y,
            int w,
            int h,
            boolean drawBroken,
            Rectangle selRect,
            boolean isContentBorderPainted) {
            if (isContentBorderPainted) {
                g.setColor(MARK_CONTENT_BORDERS
                        ? MARK_CONTENT_BORDER_COLOR
                        : darkShadow);
                g.fillRect(x, y + h - 1, w - 1, 1);
            }
        }

        /**
         * Draws the left edge of the Border around the content area
         * Draw broken line if selected tab is visible and adjacent to content
         * and TabPlacement is same as painted edge
         */
        protected void paintContentBorderLeftEdge(
            Graphics g,
            int x,
            int y,
            int w,
            int h,
            boolean drawBroken,
            Rectangle selRect,
            boolean isContentBorderPainted) {
            if (isContentBorderPainted) {
                g.setColor(MARK_CONTENT_BORDERS
                        ? MARK_CONTENT_BORDER_COLOR
                        : selectHighlight);
                g.fillRect(x, y, 1, h - 1);
            }
        }

        /**
         * Draws the right edge of the Border around the content area
         * Draw broken line if selected tab is visible and adjacent to content
         * and TabPlacement is same as painted edge
         */
        protected void paintContentBorderRightEdge(
            Graphics g,
            int x,
            int y,
            int w,
            int h,
            boolean drawBroken,
            Rectangle selRect,
            boolean isContentBorderPainted) {
            if (isContentBorderPainted) {
                g.setColor(MARK_CONTENT_BORDERS
                        ? MARK_CONTENT_BORDER_COLOR
                        : darkShadow);
               g.fillRect(x + w - 1, y, 1, h);
            }
        }
    }

    /**
     * The renderer for the case where tabs are displayed below the contents
     * and with minimal decoration.
     */
    private static class BottomEmbeddedRenderer extends AbstractRenderer {

        private BottomEmbeddedRenderer(JTabbedPane tabPane) {
            super(tabPane);
        }

        protected Insets getTabAreaInsets(Insets insets) {
            return EMPTY_INSETS;
        }

        protected Insets getContentBorderInsets(Insets defaultInsets) {
            return SOUTH_INSETS;
        }

        protected Insets getSelectedTabPadInsets() {
            return EMPTY_INSETS;
        }

        protected Insets getTabInsets(int tabIndex, Insets tabInsets) {
            return new Insets(tabInsets.top, tabInsets.left, tabInsets.bottom, tabInsets.right);
        }
        /**
         * minimal decoration is really minimal: noFocus
         */
        protected void paintFocusIndicator(
            Graphics g,
            Rectangle[] rects,
            int tabIndex,
            Rectangle iconRect,
            Rectangle textRect,
            boolean isSelected) {
            }

        protected void paintTabBackground(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected) {

            g.setColor(selectColor);
            g.fillRect(x, y, w + 1, h);
        }

        protected void paintTabBorder(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected) {

            int bottom = h;
            int right = w + 1;

            g.translate(x, y);
            if (isFirstDisplayedTab(tabIndex, x, tabPane.getBounds().x)) {
                if (isSelected) {
                    // selected and first in line
                    g.setColor(shadowColor);
                    g.fillRect(right, 0, 1, bottom - 1);
                    g.fillRect(right - 1, bottom - 1, 1, 1);
                    // it is open to discussion if the outer border of the tab
                    // should be painted because in the primary case it won't
                    // be visible anyway. uncomment the following two lines if wanted
                    //                    g.fillRect(0,bottom, right, 1);
                    //                    g.fillRect(-1,0,1,bottom;
                    g.setColor(selectHighlight);
                    g.fillRect(0, 0, 1, bottom);
                    g.fillRect(right - 1, 0, 1, bottom - 1);
                    g.fillRect(1, bottom - 1, right - 2, 1);
                } else {
                    //not selected and first in line
                }
            } else {
                if (isSelected) {
                    //selected and not first in line
                    g.setColor(shadowColor);
                    g.fillRect(0, 0, 1, bottom - 1);
                    g.fillRect(1, bottom - 1, 1, 1);
                    g.fillRect(right, 0, 1, bottom - 1);
                    g.fillRect(right - 1, bottom - 1, 1, 1);
                    // outside line:
                    //                    g.fillRect(2,bottom, right-3, 1);
                    g.setColor(selectHighlight);
                    g.fillRect(1, 0, 1, bottom - 1);
                    g.fillRect(right - 1, 0, 1, bottom - 1);
                    g.fillRect(2, bottom - 1, right - 3, 1);
                } else {
                    g.setColor(shadowColor);
                    g.fillRect(1, h / 2, 1, h - (h / 2));
                }
            }
            g.translate(-x, -y);
        }

        protected void paintContentBorderBottomEdge(
            Graphics g,
            int x,
            int y,
            int w,
            int h,
            boolean drawBroken,
            Rectangle selRect,
            boolean isContentBorderPainted) {

            g.setColor(shadowColor);
            g.fillRect(x, y + h - 1, w, 1);
        }

    }

    /**
     * The renderer for the case where Tabs are below the content and
     * decoration is standard
     */
    private static final class BottomRenderer extends AbstractRenderer {

        private BottomRenderer(JTabbedPane tabPane) {
            super(tabPane);
        }

        protected Insets getTabAreaInsets(Insets defaultInsets) {
            return new Insets(defaultInsets.top, defaultInsets.left + 5, defaultInsets.bottom, defaultInsets.right);
        }

		protected int getTabLabelShiftY(int tabIndex, boolean isSelected) {
			return isSelected? 0 : -1;
		}

        protected int getTabRunOverlay(int tabRunOverlay) {
            return tabRunOverlay - 2;
        }

        protected int getTabRunIndent(int run) {
            return 6 * run;
        }

        protected Insets getSelectedTabPadInsets() {
            return SOUTH_INSETS;
        }

        protected Insets getTabInsets(int tabIndex, Insets tabInsets) {
            return new Insets(tabInsets.top, tabInsets.left - 2, tabInsets.bottom, tabInsets.right - 2);
        }

        protected void paintFocusIndicator(
            Graphics g,
            Rectangle[] rects,
            int tabIndex,
            Rectangle iconRect,
            Rectangle textRect,
            boolean isSelected) {

            if (!tabPane.hasFocus() || !isSelected)
                return;
            Rectangle tabRect = rects[tabIndex];
            int top = tabRect.y;
            int left = tabRect.x + 6;
            int height = tabRect.height - 3;
            int width = tabRect.width - 12;
            g.setColor(focus);
            g.drawRect(left, top, width, height);
        }

        protected void paintTabBackground(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected) {

            g.setColor(selectColor);
            g.fillRect(x, y, w, h);
        }

        protected void paintTabBorder(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected) {

            int bottom = h - 1;
            int right = w + 4;

            g.translate(x - 3, y);

            // Paint Border
            g.setColor(selectHighlight);

            // Paint left
            g.fillRect(0, 0, 1, 2);
            g.drawLine(0, 2, 4, bottom - 4);
            g.fillRect(5, bottom - 3, 1, 2);
            g.fillRect(6, bottom - 1, 1, 1);

            // Paint bootom
            g.fillRect(7, bottom, 1, 1);
            g.setColor(darkShadow);
            g.fillRect(8, bottom, right - 13, 1);

            // Paint right
            g.drawLine(right + 1, 0, right - 3, bottom - 4);
            g.fillRect(right - 4, bottom - 3, 1, 2);
            g.fillRect(right - 5, bottom - 1, 1, 1);

            g.translate(-x + 3, -y);
        }

        protected void paintContentBorderBottomEdge(
            Graphics g,
            int x,
            int y,
            int w,
            int h,
            boolean drawBroken,
            Rectangle selRect,
            boolean isContentBorderPainted) {
            int bottom = y + h - 1;
            int right = x + w - 1;
            g.translate(x, bottom);
            if (drawBroken && selRect.x >= x && selRect.x <= x + w) {
                // Break line to show visual connection to selected tab
                g.setColor(darkShadow);
                g.fillRect(0, 0, selRect.x - x - 2, 1);
                if (selRect.x + selRect.width < x + w - 2) {
                    g.setColor(darkShadow);
                   g.fillRect(selRect.x + selRect.width + 2 - x, 0, right - selRect.x - selRect.width - 2, 1);
                }
            } else {
                g.setColor(darkShadow);
                g.fillRect(0, 0, w - 1, 1);
            }
            g.translate(-x, -bottom);
        }

    }

    /**
     * The renderer for tabs on the left with minimal decoration
     */
    private static class LeftEmbeddedRenderer extends AbstractRenderer {

        private LeftEmbeddedRenderer(JTabbedPane tabPane) {
            super(tabPane);
        }

        protected Insets getTabAreaInsets(Insets insets) {
            return EMPTY_INSETS;
        }

        protected Insets getContentBorderInsets(Insets defaultInsets) {
            return WEST_INSETS;
        }

        protected int getTabRunOverlay(int tabRunOverlay) {
            return 0;
        }

        protected boolean shouldPadTabRun(int run, boolean aPriori) {
            return false;
        }

        protected Insets getTabInsets(int tabIndex, Insets tabInsets) {
            return new Insets(tabInsets.top, tabInsets.left, tabInsets.bottom, tabInsets.right);
        }

        protected Insets getSelectedTabPadInsets() {
            return EMPTY_INSETS;
        }

        /**
         * minimal decoration is really minimal: no focus
         */
        protected void paintFocusIndicator(
            Graphics g,
            Rectangle[] rects,
            int tabIndex,
            Rectangle iconRect,
            Rectangle textRect,
            boolean isSelected) {
            }

        protected void paintTabBackground(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            g.setColor(selectColor);
            g.fillRect(x, y, w, h);
        }

        protected void paintTabBorder(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected) {

            int bottom = h;
            int right  = w;

            g.translate(x, y);

            if (isFirstDisplayedTab(tabIndex, y, tabPane.getBounds().y)) {
                if (isSelected) {
                    //selected and first in line
                    g.setColor(selectHighlight);
                    g.fillRect(0, 0, right, 1);
                    g.fillRect(0, 0, 1, bottom - 1);
                    g.fillRect(1, bottom - 1, right - 1, 1);
                    g.setColor(shadowColor);
                    g.fillRect(0, bottom - 1, 1, 1);
                    g.fillRect(1, bottom, right - 1, 1);
                    // outside line:
                    //                    g.fillRect(-1,0,1,bottom-1)
                } else {
                    //not selected but first in line
                }
            } else {
                if (isSelected) {
                    //selected but not first in line
                    g.setColor(selectHighlight);
                    g.fillRect(1, 1, right - 1, 1);
                    g.fillRect(0, 2, 1, bottom - 2);
                    g.fillRect(1, bottom - 1, right - 1, 1);
                    g.setColor(shadowColor);
                    g.fillRect(1, 0, right - 1, 1);
                    g.fillRect(0, 1, 1, 1);
                    g.fillRect(0, bottom - 1, 1, 1);
                    g.fillRect(1, bottom, right - 1, 1);
                    // outside line:
                    //                    g.fillRect(-1,2,1,bottom-3)
                } else {
                    g.setColor(shadowColor);
                    g.fillRect(0, 0, right / 3, 1);
                }
            }

            g.translate(-x, -y);
        }

        protected void paintContentBorderLeftEdge(
            Graphics g,
            int x,
            int y,
            int w,
            int h,
            boolean drawBroken,
            Rectangle selRect,
            boolean isContentBorderPainted) {
            g.setColor(shadowColor);
            g.fillRect(x, y, 1, h);
        }
    }

    /**
     * Renderer for tabs on the left with normal decoration
     */
    private static class LeftRenderer extends AbstractRenderer {

        private LeftRenderer(JTabbedPane tabPane) {
            super(tabPane);
        }

        protected Insets getTabAreaInsets(Insets defaultInsets) {
            return new Insets(defaultInsets.top + 4, defaultInsets.left, defaultInsets.bottom, defaultInsets.right);
        }

        protected int getTabLabelShiftX(int tabIndex, boolean isSelected) {
            return 1;
        }

        protected int getTabRunOverlay(int tabRunOverlay) {
            return 1;
        }

        protected boolean shouldPadTabRun(int run, boolean aPriori) {
            return false;
        }

        protected Insets getTabInsets(int tabIndex, Insets tabInsets) {
            return new Insets(tabInsets.top, tabInsets.left - 5, tabInsets.bottom + 1, tabInsets.right - 5);
        }

        protected Insets getSelectedTabPadInsets() {
            return WEST_INSETS;
        }

        protected void paintFocusIndicator(
            Graphics g,
            Rectangle[] rects,
            int tabIndex,
            Rectangle iconRect,
            Rectangle textRect,
            boolean isSelected) {

            if (!tabPane.hasFocus() || !isSelected)
                return;
            Rectangle tabRect = rects[tabIndex];
            int top = tabRect.y + 2;
            int left = tabRect.x + 3;
            int height = tabRect.height - 5;
            int width = tabRect.width - 6;
            g.setColor(focus);
            g.drawRect(left, top, width, height);
        }

        protected void paintTabBackground(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            if (!isSelected) {
                g.setColor(selectLight);
                g.fillRect(x + 1, y + 1, w - 1, h - 2);
            } else {
                g.setColor(selectColor);
                g.fillRect(x + 1, y + 1, w - 3, h - 2);
            }
        }

        protected void paintTabBorder(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected) {

            int bottom = h - 1;
            int left = 0;
            g.translate(x, y);

            // Paint Border
            g.setColor(selectHighlight);
            // Paint top
            g.fillRect(left + 2, 0, w - 2 - left, 1);

            // Paint left
            g.fillRect(left + 1, 1, 1, 1);
            g.fillRect(left, 2, 1, bottom - 3);
            g.setColor(darkShadow);
            g.fillRect(left + 1, bottom - 1, 1, 1);

            // Paint bottom
            g.fillRect(left + 2, bottom, w - 2 - left, 1);

            g.translate(-x, -y);
        }

        protected void paintContentBorderLeftEdge(
            Graphics g,
            int x,
            int y,
            int w,
            int h,
            boolean drawBroken,
            Rectangle selRect,
            boolean isContentBorderPainted) {
            g.setColor(selectHighlight);
            if (drawBroken && selRect.y >= y && selRect.y <= y + h) {
                // Break line to show visual connection to selected tab
                g.fillRect(x, y, 1, selRect.y + 1 - y);
                if (selRect.y + selRect.height < y + h - 2) {
                    g.fillRect(x, selRect.y + selRect.height - 1, 1, y + h - selRect.y - selRect.height);
                }
            } else {
                g.fillRect(x, y, 1, h - 1);
            }
        }

    }

    /**
     * The renderer for tabs on the right with minimal decoration
     */
    private static class RightEmbeddedRenderer extends AbstractRenderer {

        private RightEmbeddedRenderer(JTabbedPane tabPane) {
            super(tabPane);
        }

        protected Insets getTabAreaInsets(Insets insets) {
            return EMPTY_INSETS;
        }

        protected Insets getContentBorderInsets(Insets defaultInsets) {
            return EAST_INSETS;
        }

        protected int getTabRunIndent(int run) {
            return 4 * run;
        }

        protected int getTabRunOverlay(int tabRunOverlay) {
            return 0;
        }

        protected boolean shouldPadTabRun(int run, boolean aPriori) {
            return false;
        }

        protected Insets getTabInsets(int tabIndex, Insets tabInsets) {
            return new Insets(tabInsets.top, tabInsets.left, tabInsets.bottom, tabInsets.right);
        }

        protected Insets getSelectedTabPadInsets() {
            return EMPTY_INSETS;
        }

        /**
         * minmal decoration : no focus
         */
        protected void paintFocusIndicator(
            Graphics g,
            Rectangle[] rects,
            int tabIndex,
            Rectangle iconRect,
            Rectangle textRect,
            boolean isSelected) {
            }

        protected void paintTabBackground(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected) {

            g.setColor(selectColor);
            g.fillRect(x, y, w, h);
        }

        protected void paintTabBorder(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected) {

            int bottom = h;
            int right  = w - 1;

            g.translate(x + 1, y);

            if (isFirstDisplayedTab(tabIndex, y, tabPane.getBounds().y)) {
                if (isSelected) {
                    //selected and first in line
                    g.setColor(shadowColor);
                    //outside lines:
                    //                    g.fillRect(0,-1,right,1);
                    //                    g.fillRect(right,-1,1,bottom);
                    g.fillRect(right - 1, bottom - 1, 1, 1);
                    g.fillRect(0, bottom, right - 1, 1);
                    g.setColor(selectHighlight);
                    g.fillRect(0, 0, right - 1, 1);
                    g.fillRect(right - 1, 0, 1, bottom - 1);
                    g.fillRect(0, bottom - 1, right - 1, 1);
                }
            } else {
                if (isSelected) {
                    //selected but not first in line
                    g.setColor(shadowColor);
                    g.fillRect(0, -1, right - 1, 1);
                    g.fillRect(right - 1, 0, 1, 1);
                    //outside line:
                    //                    g.fillRect(right,0,1,bottom);
                    g.fillRect(right - 1, bottom - 1, 1, 1);
                    g.fillRect(0, bottom, right - 1, 1);
                    g.setColor(selectHighlight);
                    g.fillRect(0, 0, right - 1, 1);
                    g.fillRect(right - 1, 1, 1, bottom - 2);
                    g.fillRect(0, bottom - 1, right - 1, 1);
                } else {
                    //not selected and not first in line
                    g.setColor(shadowColor);
                    g.fillRect(2 * right / 3, 0, right / 3, 1);
                }
            }
            g.translate(-x - 1, -y);
        }

        protected void paintContentBorderRightEdge(
            Graphics g,
            int x,
            int y,
            int w,
            int h,
            boolean drawBroken,
            Rectangle selRect,
            boolean isContentBorderPainted) {
            g.setColor(shadowColor);
            g.fillRect(x + w - 1, y, 1, h);
        }

    }

    /**
     * renderer for tabs on the right with normal decoration
     */
    private static class RightRenderer extends AbstractRenderer {

        private RightRenderer(JTabbedPane tabPane) {
            super(tabPane);
        }

        protected int getTabLabelShiftX(int tabIndex, boolean isSelected) {
            return 1;
        }

        protected int getTabRunOverlay(int tabRunOverlay) {
            return 1;
        }

        protected boolean shouldPadTabRun(int run, boolean aPriori) {
            return false;
        }

        protected Insets getTabInsets(int tabIndex, Insets tabInsets) {
            return new Insets(tabInsets.top, tabInsets.left - 5, tabInsets.bottom + 1, tabInsets.right - 5);
        }

        protected Insets getSelectedTabPadInsets() {
            return EAST_INSETS;
        }

        protected void paintFocusIndicator(
            Graphics g,
            Rectangle[] rects,
            int tabIndex,
            Rectangle iconRect,
            Rectangle textRect,
            boolean isSelected) {

            if (!tabPane.hasFocus() || !isSelected)
                return;
            Rectangle tabRect = rects[tabIndex];
            int top = tabRect.y + 2;
            int left = tabRect.x + 3;
            int height = tabRect.height - 5;
            int width = tabRect.width - 6;
            g.setColor(focus);
            g.drawRect(left, top, width, height);
        }

        protected void paintTabBackground(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            if (!isSelected) {
                g.setColor(selectLight);
                ;
                g.fillRect(x, y, w, h);
            } else {
                g.setColor(selectColor);
                g.fillRect(x + 2, y, w - 2, h);
            }
        }

        protected void paintTabBorder(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected) {

            int bottom = h - 1;
            int right  = w;

            g.translate(x, y);

            // Paint Border

            g.setColor(selectHighlight);
            g.fillRect(0, 0, right - 1, 1);
            // Paint right
            g.setColor(darkShadow);
            g.fillRect(right - 1, 1, 1, 1);
            g.fillRect(right, 2, 1, bottom - 3);
            // Paint bottom
            g.fillRect(right - 1, bottom - 1, 1, 1);
            g.fillRect(0, bottom, right - 1, 1);

            g.translate(-x, -y);
        }

        protected void paintContentBorderRightEdge(
            Graphics g,
            int x,
            int y,
            int w,
            int h,
            boolean drawBroken,
            Rectangle selRect,
            boolean isContentBorderPainted) {
            g.setColor(darkShadow);
            if (drawBroken && selRect.y >= y && selRect.y <= y + h) {
                // Break line to show visual connection to selected tab
                g.fillRect(x + w - 1, y, 1, selRect.y - y);
                if (selRect.y + selRect.height < y + h - 2) {
                    g.fillRect(x + w - 1, selRect.y + selRect.height, 1, y + h - selRect.y - selRect.height);
                }
            } else {
                g.fillRect(x + w - 1, y, 1, h - 1);
            }
        }
    }

    /**
     * Renderer for tabs on top with minimal decoration
     */
    private static class TopEmbeddedRenderer extends AbstractRenderer {

        private TopEmbeddedRenderer(JTabbedPane tabPane) {
            super(tabPane);
        }

        protected Insets getTabAreaInsets(Insets insets) {
            return EMPTY_INSETS;
        }

        protected Insets getContentBorderInsets(Insets defaultInsets) {
            return NORTH_INSETS;
        }

        protected Insets getTabInsets(int tabIndex, Insets tabInsets) {
            return new Insets(tabInsets.top, tabInsets.left + 1, tabInsets.bottom, tabInsets.right);
        }

        protected Insets getSelectedTabPadInsets() {
            return EMPTY_INSETS;
        }

        /**
         * Minimal decoration: no focus
         */
        protected void paintFocusIndicator(
            Graphics g,
            Rectangle[] rects,
            int tabIndex,
            Rectangle iconRect,
            Rectangle textRect,
            boolean isSelected) {
            }

        protected void paintTabBackground(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected) {

            g.setColor(selectColor);
            g.fillRect(x, y, w, h);
        }

        protected void paintTabBorder(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected) {

            g.translate(x, y);

            int right  = w;
            int bottom = h;

            if (isFirstDisplayedTab(tabIndex, x, tabPane.getBounds().x)) {
                if (isSelected) {
                    g.setColor(selectHighlight);
                    //left
                    g.fillRect(0, 0, 1, bottom);
                    //top
                    g.fillRect(0, 0, right - 1, 1);
                    //right
                    g.fillRect(right - 1, 0, 1, bottom);
                    g.setColor(shadowColor);
                    //topright corner
                    g.fillRect(right - 1, 0, 1, 1);
                    //right
                    g.fillRect(right, 1, 1, bottom);
                } else {}
            } else {
                if (isSelected) {
                    g.setColor(selectHighlight);
                    //left
                    g.fillRect(1, 1, 1, bottom - 1);
                    //top
                    g.fillRect(2, 0, right - 3, 1);
                    //right
                    g.fillRect(right - 1, 1, 1, bottom - 1);
                    g.setColor(shadowColor);
                    //left
                    g.fillRect(0, 1, 1, bottom - 1);
                    //topleft corner
                    g.fillRect(1, 0, 1, 1);
                    //topright corner
                    g.fillRect(right - 1, 0, 1, 1);
                    //right
                    g.fillRect(right, 1, 1, bottom);
                } else {
                    g.setColor(shadowColor);
                    g.fillRect(0, 0, 1, bottom +2 - bottom / 2);
                }
            }
            g.translate(-x, -y);
        }

        protected void paintContentBorderTopEdge(
            Graphics g,
            int x,
            int y,
            int w,
            int h,
            boolean drawBroken,
            Rectangle selRect,
            boolean isContentBorderPainted) {
            g.setColor(shadowColor);
            g.fillRect(x, y, w, 1);
        }

    }

    /**
     * the renderer for tabs on top with normal decoration
     */
    private static class TopRenderer extends AbstractRenderer {

        private TopRenderer(JTabbedPane tabPane) {
            super(tabPane);
        }

        protected Insets getTabAreaInsets(Insets defaultInsets) {
            return new Insets(defaultInsets.top, defaultInsets.left + 4, defaultInsets.bottom, defaultInsets.right);
        }

		protected int getTabLabelShiftY(int tabIndex, boolean isSelected) {
			return isSelected? -1 : 0;
		}

         protected int getTabRunOverlay(int tabRunOverlay) {
            return tabRunOverlay - 2;
        }

        protected int getTabRunIndent(int run) {
            return 6 * run;
        }

        protected Insets getSelectedTabPadInsets() {
            return NORTH_INSETS;
        }

        protected Insets getTabInsets(int tabIndex, Insets tabInsets) {
            return new Insets(tabInsets.top-1, tabInsets.left - 4, tabInsets.bottom, tabInsets.right - 4);
        }

        protected void paintFocusIndicator(
            Graphics g,
            Rectangle[] rects,
            int tabIndex,
            Rectangle iconRect,
            Rectangle textRect,
            boolean isSelected) {

            if (!tabPane.hasFocus() || !isSelected)
                return;
            Rectangle tabRect = rects[tabIndex];
            int top = tabRect.y +1 ;
            int left = tabRect.x + 4;
            int height = tabRect.height - 3;
            int width = tabRect.width - 9;
            g.setColor(focus);
            g.drawRect(left, top, width, height);
        }

        protected void paintTabBackground(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected) {

            int sel = (isSelected) ? 0 : 1;
            g.setColor(selectColor);
            g.fillRect(x, y + sel, w, h / 2);
            g.fillRect(x - 1, y + sel + h / 2, w + 2, h - h / 2);
        }

        protected void paintTabBorder(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected) {

            g.translate(x - 4, y);

            int top = 0;
            int right = w + 6;

            // Paint Border
            g.setColor(selectHighlight);

            // Paint left
            g.drawLine(1, h - 1, 4, top + 4);
            g.fillRect(5, top + 2, 1, 2);
            g.fillRect(6, top + 1, 1, 1);

            // Paint top
            g.fillRect(7, top, right - 12, 1);

            // Paint right
            g.setColor(darkShadow);
            g.drawLine(right, h - 1, right - 3, top + 4);
            g.fillRect(right - 4, top + 2, 1, 2);
            g.fillRect(right - 5, top + 1, 1, 1);

            g.translate(-x + 4, -y);
        }

        protected void paintContentBorderTopEdge(
            Graphics g,
            int x,
            int y,
            int w,
            int h,
            boolean drawBroken,
            Rectangle selRect,
            boolean isContentBorderPainted) {
            int right = x + w - 1;
            int top = y;
            g.setColor(selectHighlight);

            if (drawBroken && selRect.x >= x && selRect.x <= x + w) {
                // Break line to show visual connection to selected tab
                g.fillRect(x, top, selRect.x - 2 - x, 1);
                if (selRect.x + selRect.width < x + w - 2) {
                    g.fillRect(selRect.x + selRect.width + 2, top, right - 2 - selRect.x - selRect.width, 1);
                } else {
                    g.fillRect(x + w - 2, top, 1, 1);
                }
            } else {
                g.fillRect(x, top, w - 1, 1);
            }
        }
    }
}
