package org.intellij.sequencer;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.components.JBScrollBar;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.UIUtil;
import icons.SequencePluginIcons;
import org.intellij.sequencer.diagram.*;
import org.intellij.sequencer.generator.*;
import org.intellij.sequencer.generator.filters.*;
import org.intellij.sequencer.impl.EmptySequenceNavigable;
import org.intellij.sequencer.ui.MyButtonlessScrollBarUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtFunction;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Objects;

public class SequencePanel extends JPanel {
    //private static final Logger LOGGER = Logger.getInstance(SequencePanel.class.getName());

    private final Display _display;
    private final Model _model;
    private final SequenceNavigable navigable;
    private final SequenceParams _sequenceParams;
    private PsiElement psiElement;
    private String _titleName;
    private final JScrollPane _jScrollPane;

    public SequencePanel(SequenceNavigable navigable, PsiElement psiMethod, SequenceParams sequenceParams) {
        super(new BorderLayout());

        if (navigable == null) {
            this.navigable = new EmptySequenceNavigable();
        } else {
            this.navigable = navigable;
        }

        psiElement = psiMethod;
        _sequenceParams = sequenceParams;

        _model = new Model();
        _display = new Display(_model, new SequenceListenerImpl());

        DefaultActionGroup actionGroup = new DefaultActionGroup("SequencerActionGroup", false);
        actionGroup.add(new ReGenerateAction());
        actionGroup.add(new ExportAction());
        actionGroup.add(new SaveAsAction());
        actionGroup.add(new LoadAction());
        actionGroup.add(new ExportPumlAction());

        ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar actionToolbar = actionManager.createActionToolbar("SequencerToolbar", actionGroup, false);
        add(actionToolbar.getComponent(), BorderLayout.WEST);

        MyButton birdViewButton = new MyButton(SequencePluginIcons.PREVIEW_ICON);
        birdViewButton.setToolTipText("Bird view");
        birdViewButton.addActionListener(e -> showBirdView());

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
        if (psiElement == null || !psiElement.isValid() || !(psiElement instanceof PsiMethod || psiElement instanceof KtFunction)) {
            psiElement = null;
            return;
        }
        IGenerator generator = GeneratorFactory.createGenerator(psiElement.getLanguage(), _sequenceParams);

        final CallStack callStack = generator.generate(psiElement);
        _titleName = callStack.getMethod().getTitleName();
        generate(callStack.generateSequence());
    }

    public String generatePuml() {
        if (psiElement == null || !psiElement.isValid() || !(psiElement instanceof PsiMethod || psiElement instanceof KtFunction)) {
            psiElement = null;
            return "";
        }

        IGenerator generator = GeneratorFactory.createGenerator(psiElement.getLanguage(), _sequenceParams);

        final CallStack callStack = generator.generate(psiElement);

        return callStack.generatePuml();
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
        navigable.openClassInEditor(objectInfo.getFullName());
    }

    private void gotoMethod(MethodInfo methodInfo) {
        String className = methodInfo.getObjectInfo().getFullName();
        String methodName = methodInfo.getRealName();
        List<String> argTypes = methodInfo.getArgTypes();
        navigable.openMethodInEditor(className, methodName, argTypes);
    }

    private void gotoCall(MethodInfo fromMethodInfo, MethodInfo toMethodInfo) {
        if (toMethodInfo == null) {
            return;
        }

        // Only first call from Actor, the fromMethodInfo is null
        if (fromMethodInfo == null) {
            gotoMethod(toMethodInfo);
            return;
        }

        if (isLambdaCall(toMethodInfo)) {
            navigable.openLambdaExprInEditor(
                    fromMethodInfo.getObjectInfo().getFullName(),
                    fromMethodInfo.getRealName(),
                    fromMethodInfo.getArgTypes(),
                    toMethodInfo.getArgTypes(),
                    toMethodInfo.getReturnType()
            );
        } else if (isLambdaCall(fromMethodInfo)) {
            LambdaExprInfo lambdaExprInfo = (LambdaExprInfo) fromMethodInfo;
            navigable.openMethodCallInsideLambdaExprInEditor(
                    _sequenceParams.getMethodFilter(),
                    lambdaExprInfo.getObjectInfo().getFullName(),
                    lambdaExprInfo.getEnclosedMethodName(),
                    lambdaExprInfo.getEnclosedMethodArgTypes(),
                    lambdaExprInfo.getArgTypes(),
                    lambdaExprInfo.getReturnType(),
                    toMethodInfo.getObjectInfo().getFullName(),
                    toMethodInfo.getRealName(),
                    toMethodInfo.getArgTypes(),
                    toMethodInfo.getNumbering().getTopLevel()
            );
        } else if (fromMethodInfo.getObjectInfo().hasAttribute(Info.INTERFACE_ATTRIBUTE) && fromMethodInfo.hasAttribute(Info.ABSTRACT_ATTRIBUTE)) {
            gotoMethod(toMethodInfo);
        } else {
            navigable.openMethodCallInEditor(
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
    }

    private boolean isLambdaCall(MethodInfo methodInfo) {
        return Objects.equals(methodInfo.getRealName(), Constants.Lambda_Invoke);
    }

    private class ReGenerateAction extends AnAction {
        public ReGenerateAction() {
            super("ReGenerate", "Regenerate diagram", SequencePluginIcons.REFRESH_ICON);
        }

        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            generate();
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setEnabled(psiElement != null);
        }
    }

    private class ExportAction extends AnAction {
        public ExportAction() {
            super("Export Image", "Export image to file", SequencePluginIcons.EXPORT_ICON);
        }

        public void actionPerformed(@NotNull AnActionEvent event) {
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

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(_display.getDiagram().nonEmpty());
        }
    }

    private class LoadAction extends AnAction {
        public LoadAction() {
            super("Open Diagram", "Open SequenceDiagram text (.sdt) file", SequencePluginIcons.OPEN_ICON);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            final JFileChooser chooser = new JFileChooser();
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setDialogTitle("Open Diagram");
            chooser.setFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith("sdt");
                }

                public String getDescription() {
                    return "SequenceDiagram (.sdt) File";
                }
            });
            int returnVal = chooser.showOpenDialog(SequencePanel.this);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                _titleName = file.getName();
                _model.readFromFile(file);
            }

        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(psiElement == null);
        }
    }

    private class SaveAsAction extends AnAction {

        public SaveAsAction() {
            super("Save As ...", "Export Diagram to file", SequencePluginIcons.SAVE_ICON);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
            fileChooser.setFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith("sdt");
                }

                public String getDescription() {
                    return "SequenceDiagram (.sdt) File";
                }
            });
            try {
                if (fileChooser.showSaveDialog(SequencePanel.this) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    if (!selectedFile.getName().endsWith("sdt"))
                        selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".sdt");

                    _model.writeToFile(selectedFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(SequencePanel.this, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(_display.getDiagram().nonEmpty());
        }
    }

    private class ExportPumlAction extends AnAction {

        public ExportPumlAction() {
            super("Export PlantUML ...", "Export Diagram to PlantUML file", SequencePluginIcons.PUML_ICON);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
            fileChooser.setFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith("puml");
                }

                public String getDescription() {
                    return "PlantUML (.puml) File";
                }
            });
            try {
                if (fileChooser.showSaveDialog(SequencePanel.this) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    if (!selectedFile.getName().endsWith("puml"))
                        selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".puml");

                    String uml =  generatePuml();

                    FileUtil.writeToFile(selectedFile, uml);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(SequencePanel.this, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(_display.getDiagram().nonEmpty());
        }
    }



    private class GotoSourceAction extends AnAction {
        private final ScreenObject _screenObject;

        public GotoSourceAction(ScreenObject screenObject) {
            super("Go to Source");
            _screenObject = screenObject;
        }

        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            gotoSourceCode(_screenObject);
        }
    }

    private class RemoveClassAction extends AnAction {
        private final ObjectInfo _objectInfo;

        public RemoveClassAction(ObjectInfo objectInfo) {
            super("Remove Class '" + objectInfo.getName() + "'");
            _objectInfo = objectInfo;
        }

        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            _sequenceParams.getMethodFilter().addFilter(new SingleClassFilter(_objectInfo.getFullName()));
            generate();
        }
    }

    private class RemoveMethodAction extends AnAction {
        private final MethodInfo _methodInfo;

        public RemoveMethodAction(MethodInfo methodInfo) {
            super("Remove Method '" + methodInfo.getRealName() + "()'");
            _methodInfo = methodInfo;
        }

        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            _sequenceParams.getMethodFilter().addFilter(new SingleMethodFilter(
                    _methodInfo.getObjectInfo().getFullName(),
                    _methodInfo.getRealName(),
                    _methodInfo.getArgTypes()
            ));
            generate();

        }
    }

    private class ExpendInterfaceAction extends AnAction {
        private final String face;
        private final String impl;

        public ExpendInterfaceAction(String face, String impl) {
            super(impl);
            this.face = face;
            this.impl = impl;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
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
                    List<String> impls = navigable.findImplementations(className);
                    actionGroup.addSeparator();
                    for (String impl : impls) {
                        actionGroup.add(new ExpendInterfaceAction(className, impl));
                    }
                    actionGroup.addSeparator();
                }
                actionGroup.add(new RemoveClassAction(displayObject.getObjectInfo()));
            } else if (screenObject instanceof DisplayMethod) {
                DisplayMethod displayMethod = (DisplayMethod) screenObject;
                if (displayMethod.getObjectInfo().hasAttribute(Info.INTERFACE_ATTRIBUTE) && !_sequenceParams.isSmartInterface()) {

                    String className = displayMethod.getObjectInfo().getFullName();
                    String methodName = displayMethod.getMethodInfo().getRealName();
                    List<String> argTypes = displayMethod.getMethodInfo().getArgTypes();
                    List<String> impls = navigable.findImplementations(className, methodName, argTypes);

                    actionGroup.addSeparator();
                    for (String impl : impls) {
                        actionGroup.add(new ExpendInterfaceAction(className, impl));
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

    private static class MyScrollBar extends JBScrollBar {
        public MyScrollBar(int orientation) {
            super(orientation);
        }

        @Override
        public void updateUI() {
            setUI(MyButtonlessScrollBarUI.createNormal());
        }


    }

    private static class MyButton extends JButton {

        public MyButton(Icon icon) {
            super(icon);
            init();
        }

        private void init() {
            setUI(new BasicButtonUI());
            setBackground(UIUtil.getLabelBackground());
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
