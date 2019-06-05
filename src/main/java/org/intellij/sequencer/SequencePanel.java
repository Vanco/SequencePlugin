package org.intellij.sequencer;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.components.JBScrollBar;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.UIUtil;
import com.zenuml.dsl.SequenceGeneratorV1;
import icons.SequencePluginIcons;
import org.intellij.sequencer.diagram.*;
import org.intellij.sequencer.generator.CallStack;
import org.intellij.sequencer.generator.SequenceGenerator;
import org.intellij.sequencer.generator.SequenceParams;
import org.intellij.sequencer.generator.filters.ImplementClassFilter;
import org.intellij.sequencer.generator.filters.SingleClassFilter;
import org.intellij.sequencer.generator.filters.SingleMethodFilter;
import org.intellij.sequencer.ui.MyButtonlessScrollBarUI;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class SequencePanel extends JPanel {
    private static final Logger LOGGER = Logger.getInstance(SequencePanel.class.getName());

    private Display _display;
    private Model _model;
    private final SequencePlugin _plugin;
    private SequenceParams _sequenceParams;
    private PsiMethod _psiMethod;
    private String _titleName;
    private JScrollPane _jScrollPane;

    public SequencePanel(SequencePlugin plugin, PsiMethod psiMethod, SequenceParams sequenceParams) {
        super(new BorderLayout());
        _plugin = plugin;
        _psiMethod = psiMethod;
        _sequenceParams = sequenceParams;

        _model = new Model();
        _display = new Display(_model, new SequenceListenerImpl());

        DefaultActionGroup actionGroup = new DefaultActionGroup("SequencerActionGroup", false);
        actionGroup.add(new CloseAction());
        actionGroup.add(new ReGenerateAction());
        actionGroup.add(new ExportAction());
        actionGroup.add(new ExportTextAction());

        ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar actionToolbar = actionManager.createActionToolbar("SequencerToolbar", actionGroup, false);
        add(actionToolbar.getComponent(), BorderLayout.WEST);

        MyButton birdViewButton = new MyButton(SequencePluginIcons.PREVIEW_ICON_13);
        birdViewButton.setToolTipText("Bird view");
        birdViewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showBirdView();
            }
        });

        _jScrollPane = new JBScrollPane(_display);
        _jScrollPane.setVerticalScrollBar(new MyScrollBar(Adjustable.VERTICAL));
        _jScrollPane.setHorizontalScrollBar(new MyScrollBar(Adjustable.HORIZONTAL));
        _jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        _jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        _jScrollPane.setCorner(JScrollPane.LOWER_RIGHT_CORNER, birdViewButton);
        add(_jScrollPane, BorderLayout.CENTER);
    }

    public Model getModel() {
        return _model;
    }

    private void generate(String query) {
//        LOGGER.debug("sequence = " + query);
        _model.setText(query, this);
        _display.invalidate();
    }

    public void generate() {
        if (_psiMethod == null || !_psiMethod.isValid()) { // || !_psiMethod.isPhysical()
            _psiMethod = null;
            return;
        }
        SequenceGenerator generator = new SequenceGenerator(_sequenceParams);
        SequenceGeneratorV1 sequenceGeneratorV1=new SequenceGeneratorV1(_sequenceParams);
        sequenceGeneratorV1.generate(_psiMethod);
        try {
            new File("/Users/dengzhiguo/workspace/test.zenuml").createNewFile();
            FileOutputStream file=new FileOutputStream(new File("/Users/dengzhiguo/workspace/test.zenuml"));
            file.write(sequenceGeneratorV1.toDsl().getBytes());
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        final CallStack callStack = generator.generate(_psiMethod);
        _titleName = callStack.getMethod().getTitleName();
        generate(callStack.generateSequence());
    }

    public void generateTextFile(File selectedFile) throws IOException {
        if (_psiMethod == null || !_psiMethod.isValid()) { // || !_psiMethod.isPhysical()
            _psiMethod = null;
            return;
        }
        SequenceGenerator generator = new SequenceGenerator(_sequenceParams);
        final CallStack callStack = generator.generate(_psiMethod);

        Files.write(selectedFile.toPath(),
                callStack.generateText().getBytes(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

    }

    private void showBirdView() {
        PreviewFrame frame = new PreviewFrame(_jScrollPane, _display);
        frame.setVisible(true);
    }

    public String getTitleName() {
        return _titleName;
    }

    private void gotoSourceCode(ScreenObject screenObject) {
        if (screenObject instanceof DisplayObject) {
            DisplayObject displayObject = (DisplayObject) screenObject;
            gotoClass(displayObject.getObjectInfo());
        } else if (screenObject instanceof DisplayMethod) {
            DisplayMethod displayMethod = (DisplayMethod) screenObject;
            gotoMethod(displayMethod.getMethodInfo());
        } else if (screenObject instanceof DisplayLink) {
            DisplayLink displayLink = (DisplayLink) screenObject;
            gotoCall(displayLink.getLink().getCallerMethodInfo(),
                    displayLink.getLink().getMethodInfo());
        }
    }

    private void gotoClass(ObjectInfo objectInfo) {
        _plugin.openClassInEditor(objectInfo.getFullName());
    }

    private void gotoMethod(MethodInfo methodInfo) {
        String className = methodInfo.getObjectInfo().getFullName();
        String methodName = methodInfo.getRealName();
        List argTypes = methodInfo.getArgTypes();
        _plugin.openMethodInEditor(className, methodName, argTypes);
    }

    private void gotoCall(MethodInfo fromMethodInfo, MethodInfo toMethodInfo) {
        if (fromMethodInfo == null || toMethodInfo == null)
            return;
        _plugin.openMethodCallInEditor(
                _sequenceParams.getMethodFilter(),
                fromMethodInfo.getObjectInfo().getFullName(),
                fromMethodInfo.getRealName(),
                fromMethodInfo.getArgTypes(),
                toMethodInfo.getObjectInfo().getFullName(),
                toMethodInfo.getRealName(),
                toMethodInfo.getArgTypes(),
                toMethodInfo.getNumbering().getTopLevel()
        );
    }

    private class CloseAction extends AnAction {
        public CloseAction() {
            super("Close", "Close sequence", SequencePluginIcons.CLOSE_ICON);
        }

        public void actionPerformed(AnActionEvent event) {
            _plugin.closeSequence(SequencePanel.this);
        }
    }

    private class ReGenerateAction extends AnAction {
        public ReGenerateAction() {
            super("ReGenerate", "Regenerate diagram", SequencePluginIcons.REFRESH_ICON);
        }

        public void actionPerformed(AnActionEvent anActionEvent) {
            generate();

        }
    }

    private class ExportAction extends AnAction {
        public ExportAction() {
            super("Export", "Export image to file", SequencePluginIcons.EXPORT_ICON);
        }

        public void actionPerformed(AnActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
            fileChooser.setFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith("png");
                }

                public String getDescription() {
                    return "PNG Images";
                }
            });
            try {
                if (fileChooser.showSaveDialog(SequencePanel.this) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    if (!selectedFile.getName().endsWith("png"))
                        selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".png");
                    _display.saveImageToFile(selectedFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(SequencePanel.this, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class ExportTextAction extends AnAction {

        public ExportTextAction() {
            super("ExportTextFile", "Export call stack as text file", SequencePluginIcons.EXPORT_TEXT_ICON);
        }
        @Override
        public void actionPerformed(AnActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
            fileChooser.setFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith("txt");
                }

                public String getDescription() {
                    return "Text File";
                }
            });
            try {
                if (fileChooser.showSaveDialog(SequencePanel.this) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    if (!selectedFile.getName().endsWith("txt"))
                        selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".txt");

                    generateTextFile(selectedFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(SequencePanel.this, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class GotoSourceAction extends AnAction {
        private ScreenObject _screenObject;

        public GotoSourceAction(ScreenObject screenObject) {
            super("Go to Source");
            _screenObject = screenObject;
        }

        public void actionPerformed(AnActionEvent anActionEvent) {
            gotoSourceCode(_screenObject);
        }
    }

    private class RemoveClassAction extends AnAction {
        private ObjectInfo _objectInfo;

        public RemoveClassAction(ObjectInfo objectInfo) {
            super("Remove Class '" + objectInfo.getName() + "'");
            _objectInfo = objectInfo;
        }

        public void actionPerformed(AnActionEvent anActionEvent) {
            _sequenceParams.getMethodFilter().addFilter(new SingleClassFilter(_objectInfo.getFullName()));
            generate();
        }
    }

    private class RemoveMethodAction extends AnAction {
        private MethodInfo _methodInfo;

        public RemoveMethodAction(MethodInfo methodInfo) {
            super("Remove Method '" + methodInfo.getRealName() + "()'");
            _methodInfo = methodInfo;
        }

        public void actionPerformed(AnActionEvent anActionEvent) {
            _sequenceParams.getMethodFilter().addFilter(new SingleMethodFilter(
                    _methodInfo.getObjectInfo().getFullName(),
                    _methodInfo.getRealName(),
                    _methodInfo.getArgTypes()
            ));
            generate();

        }
    }

    private class ExpendInterfaceAction extends AnAction {
        private String face;
        private String impl;

        public ExpendInterfaceAction(String face, String impl) {
            super(impl);
            this.face = face;
            this.impl = impl;
        }

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            _sequenceParams.getInterfaceImplFilter().put(
                    face,
                    new ImplementClassFilter(impl)
            );
            generate();
        }
    }

    private class SequenceListenerImpl implements SequenceListener {

        public void selectedScreenObject(ScreenObject screenObject) {
            gotoSourceCode(screenObject);
        }

        public void displayMenuForScreenObject(ScreenObject screenObject, int x, int y) {
            DefaultActionGroup actionGroup = new DefaultActionGroup("SequencePopup", true);
            actionGroup.add(new GotoSourceAction(screenObject));
            if (screenObject instanceof DisplayObject) {
                DisplayObject displayObject = (DisplayObject) screenObject;
                if (displayObject.getObjectInfo().hasAttribute(Info.INTERFACE_ATTRIBUTE) && !_sequenceParams.isSmartInterface()) {
                    String className = displayObject.getObjectInfo().getFullName();
                    List<String> impls = _plugin.findImplementations(className);
                    actionGroup.addSeparator();
                    for (String impl : impls) {
                        actionGroup.add(new ExpendInterfaceAction(className,impl));
                    }
                    actionGroup.addSeparator();
                }
               actionGroup.add(new RemoveClassAction(displayObject.getObjectInfo()));
            } else if (screenObject instanceof DisplayMethod) {
                DisplayMethod displayMethod = (DisplayMethod) screenObject;
                if (displayMethod.getObjectInfo().hasAttribute(Info.INTERFACE_ATTRIBUTE) && !_sequenceParams.isSmartInterface()) {

                    String className = displayMethod.getObjectInfo().getFullName();
                    String methodName = displayMethod.getMethodInfo().getRealName();
                    List argTypes = displayMethod.getMethodInfo().getArgTypes();
                    List<String> impls = _plugin.findImplementations(className, methodName, argTypes);

                    actionGroup.addSeparator();
                    for (String impl : impls) {
                        actionGroup.add(new ExpendInterfaceAction(className,impl));
                    }
                    actionGroup.addSeparator();

                }
                actionGroup.add(new RemoveMethodAction(displayMethod.getMethodInfo()));
            } else if (screenObject instanceof DisplayLink) {
                DisplayLink displayLink = (DisplayLink) screenObject;
                if (!displayLink.isReturnLink())
                    actionGroup.add(new RemoveMethodAction(displayLink.getLink().getMethodInfo()));
            }
            ActionPopupMenu actionPopupMenu = ActionManager.getInstance().
                    createActionPopupMenu("SequenceDiagram.Popup", actionGroup);
            Component invoker = screenObject instanceof DisplayObject ? _display.getHeader() : _display;
            actionPopupMenu.getComponent().show(invoker, x, y);
        }
    }

    private class MyScrollBar extends JBScrollBar {
        public MyScrollBar(int orientation) {
            super(orientation);
        }

        @Override
        public void updateUI() {
            setUI(MyButtonlessScrollBarUI.createNormal());
        }


    }

    private class MyButton extends JButton {

        public MyButton(Icon icon) {
            super(icon);
            init();
        }

        private void init() {
            setUI(new BasicButtonUI());
            setBackground(UIUtil.getLabelBackground());
//            setContentAreaFilled(false);
            setBorder(BorderFactory.createEmptyBorder());
            setBorderPainted(false);
            setFocusable(false);
            setRequestFocusEnabled(false);
        }

        @Override
        public void updateUI() {
            init();
        }
    }

}
