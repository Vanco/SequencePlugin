package org.intellij.sequencer;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.progress.util.AbstractProgressIndicatorExBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.components.JBScrollBar;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.concurrency.NonUrgentExecutor;
import com.intellij.util.ui.UIUtil;
import icons.SequencePluginIcons;
import org.intellij.sequencer.config.ConfigListener;
import org.intellij.sequencer.config.SequenceParamsState;
import org.intellij.sequencer.diagram.*;
import org.intellij.sequencer.formatter.MermaidFormatter;
import org.intellij.sequencer.formatter.PlantUMLFormatter;
import org.intellij.sequencer.formatter.SdtFormatter;
import org.intellij.sequencer.generator.filters.ImplementClassFilter;
import org.intellij.sequencer.generator.filters.SingleClassFilter;
import org.intellij.sequencer.generator.filters.SingleMethodFilter;
import org.intellij.sequencer.openapi.*;
import org.intellij.sequencer.openapi.model.CallStack;
import org.intellij.sequencer.ui.MyButtonlessScrollBarUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.CancellablePromise;
import org.jetbrains.kotlin.psi.KtFunction;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.intellij.sequencer.util.MyPsiUtil.getFileChooser;

public class SequencePanel extends JPanel implements ConfigListener {
    private static final Logger LOGGER = Logger.getInstance(SequencePanel.class);
    private final Project project;
    private final Display _display;
    private final Model _model;
    private final SequenceNavigable navigable;
    private final SequenceParams _sequenceParams;
    private PsiElement psiElement;
    private String _titleName;
    private final JScrollPane _jScrollPane;
    private final HashMap<String, Integer> navIndexMap = new HashMap<>();
    private GenerateFinishedListener finished = name -> {};

    public SequencePanel(Project project, PsiElement psiMethod) {
        super(new BorderLayout());
        this.project = project;

        navigable = SequenceNavigableFactory.INSTANCE.forLanguage(project, psiMethod.getLanguage());

        psiElement = psiMethod;
        _sequenceParams = new SequenceParams();


        _model = new Model();
        _display = new Display(_model, new SequenceListenerImpl());

        DefaultActionGroup actionGroup = new DefaultActionGroup("SequencerActionGroup", false);
        actionGroup.add(new ReGenerateAction());
        actionGroup.add(new SequenceParamsEditor());
        actionGroup.addSeparator();
        actionGroup.add(new SaveAsAction());
        actionGroup.add(new LoadAction());
        actionGroup.addSeparator();
        actionGroup.add(new ExportAction());
        actionGroup.add(new ExportPumlAction());

        ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar actionToolbar = actionManager.createActionToolbar("SequencerToolbar", actionGroup, false);
        add(actionToolbar.getComponent(), BorderLayout.WEST);
        actionToolbar.setTargetComponent(this);

        MyButton birdViewButton = new MyButton(AllIcons.General.InspectionsEye);
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

    public SequencePanel withFinishedListener(GenerateFinishedListener finished) {
        this.finished = finished;
        return this;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        SequenceParamsState.getInstance().addConfigListener(this);
    }

    @Override
    public void removeNotify() {
        SequenceParamsState.getInstance().removeConfigListener(this);
        super.removeNotify();
    }

    private void generate(String query) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("sequence = " + query);
        }
        _model.setText(query, this);
        _display.invalidate();
    }

    public void generate() {
        if (psiElement == null || !psiElement.isValid() || !(psiElement instanceof PsiMethod || psiElement instanceof KtFunction)) {
            psiElement = null;
            return;
        }

        IGenerator generator = GeneratorFactory.createGenerator(psiElement.getLanguage(), _sequenceParams);

        final BackgroundableProcessIndicator progressIndicator =
                new BackgroundableProcessIndicator(
                        project,
                        "Generate sequence...",
                        PerformInBackgroundOption.ALWAYS_BACKGROUND, // todo: remove this deprecated parameter 
                        "Stop",
                        "Stop",
                        false);
        ReadAction
                .nonBlocking(() -> {
                    final CallStack callStack = generator.generate(psiElement, null);
                    buildNaviIndex(callStack, "1");
                    _titleName = callStack.getMethod().getTitleName();
                    String format = new SdtFormatter().format(callStack);
                    generate(format);
                    progressIndicator.processFinish();
                    return _titleName;
                })
                .wrapProgress(progressIndicator)
                .finishOnUiThread(ModalityState.defaultModalityState(), title -> finished.onFinish(title))
                .inSmartMode(project)
                .submit(NonUrgentExecutor.getInstance());

    }

    private void buildNaviIndex(CallStack callStack, String level) {
        if (callStack.getMethod() == null) return;
        navIndexMap.put(level, callStack.getMethod().getOffset());
        int i = 1;
        for (CallStack call : callStack.getCalls()) {
            buildNaviIndex(call, level + "." + i++);
        }
    }

    public String generatePumlMmd(String ext) {
        if (psiElement == null || !psiElement.isValid() || !(psiElement instanceof PsiMethod || psiElement instanceof KtFunction)) {
            psiElement = null;
            return "";
        }

        IGenerator generator = GeneratorFactory.createGenerator(psiElement.getLanguage(), _sequenceParams);

        final CallStack callStack = generator.generate(psiElement, null);

        if ("mmd".equalsIgnoreCase(ext))
            return new MermaidFormatter().format(callStack);

        return new PlantUMLFormatter().format(callStack);
    }

    private void showBirdView() {
        PreviewFrame frame = new PreviewFrame(_jScrollPane, _display);
        frame.setVisible(true);
    }

    public String getTitleName() {
        if (_titleName == null) {
            return "Generate...";
        }
        return _titleName;
    }

    public void setTitleName(String title) {
        this._titleName = title;
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
        if (isLambdaCall(methodInfo)) {
            navigable.openLambdaExprInEditor(
                    methodInfo.getObjectInfo().getFullName(),
                    methodInfo.getRealName(),
                    methodInfo.getArgTypes(),
                    methodInfo.getArgTypes(),
                    methodInfo.getReturnType(),
                    navIndexMap.getOrDefault(methodInfo.getNumbering().getName(), 0)
            );
        } else {
            String className = methodInfo.getObjectInfo().getFullName();
            String methodName = methodInfo.getRealName();
            List<String> argTypes = methodInfo.getArgTypes();
            navigable.openMethodInEditor(className, methodName, argTypes);
        }
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
                    toMethodInfo.getReturnType(),
                    navIndexMap.getOrDefault(toMethodInfo.getNumbering().getName(), 0)
            );
        } else if (isLambdaCall(fromMethodInfo)) {
            LambdaExprInfo lambdaExprInfo = (LambdaExprInfo) fromMethodInfo;
            navigable.openMethodCallInsideLambdaExprInEditor(
                    lambdaExprInfo.getObjectInfo().getFullName(),
                    lambdaExprInfo.getEnclosedMethodName(),
                    lambdaExprInfo.getEnclosedMethodArgTypes(),
                    lambdaExprInfo.getArgTypes(),
                    lambdaExprInfo.getReturnType(),
                    toMethodInfo.getObjectInfo().getFullName(),
                    toMethodInfo.getRealName(),
                    toMethodInfo.getArgTypes(),
                    navIndexMap.getOrDefault(toMethodInfo.getNumbering().getName(), 0)
            );
        } else if (fromMethodInfo.getObjectInfo().hasAttribute(Info.INTERFACE_ATTRIBUTE) && fromMethodInfo.hasAttribute(Info.ABSTRACT_ATTRIBUTE)) {
            gotoMethod(toMethodInfo);
        } else {
            navigable.openMethodCallInEditor(
                    fromMethodInfo.getObjectInfo().getFullName(),
                    fromMethodInfo.getRealName(),
                    fromMethodInfo.getArgTypes(),
                    toMethodInfo.getObjectInfo().getFullName(),
                    toMethodInfo.getRealName(),
                    toMethodInfo.getArgTypes(),
                    navIndexMap.getOrDefault(toMethodInfo.getNumbering().getName(), 0)
            );
        }
    }

    private boolean isLambdaCall(MethodInfo methodInfo) {
        return Objects.equals(methodInfo.getRealName(), Constants.Lambda_Invoke);
    }

    @Override
    public void configChanged() {
        // do nothing
        //_sequenceParams = loadSequenceParams();
    }

    private class ReGenerateAction extends AnAction {
        public ReGenerateAction() {
            super("ReGenerate", "Regenerate diagram", SequencePluginIcons.PLAY_ICON);
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
            fileChooser.setSelectedFile(new File(getTitleName().replaceAll("\\.", "_")));
            fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("SVG (.svg) File", "svg"));
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JPEG (.jpg) File", "jpg"));
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG (.png) File", "png"));
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("TIF/TIFF (.tif) File", "tif"));
            fileChooser.setAcceptAllFileFilterUsed(false);

            try {
                if (fileChooser.showSaveDialog(SequencePanel.this) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    FileFilter fileFilter = fileChooser.getFileFilter();
                    String extension = ((FileNameExtensionFilter) fileFilter).getExtensions()[0];

                    File fileToSave = new File(selectedFile.getParentFile(), selectedFile.getName() + '.' + extension);

                    _display.saveImageToSvgFile(fileToSave, extension);
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
            final JFileChooser chooser = getFileChooser();
            int returnVal = chooser.showOpenDialog(SequencePanel.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
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
            super("Save As ...", "Save Diagram to SequenceDiagram text (.sdt) file", SequencePluginIcons.SAVE_ICON);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(getTitleName().replaceAll("\\.", "_") + ".sdt"));
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
            super("Export ...", "Export Diagram to PlantUML, Mermaid file", SequencePluginIcons.PUML_ICON);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(getTitleName().replaceAll("\\.", "_")));
            fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PlantUML (.puml) File", "puml"));
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Mermaid (.mmd) File", "mmd"));
            fileChooser.setAcceptAllFileFilterUsed(false);
            try {
                if (fileChooser.showSaveDialog(SequencePanel.this) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    FileFilter fileFilter = fileChooser.getFileFilter();
                    String extension = ((FileNameExtensionFilter) fileFilter).getExtensions()[0];

                    String uml = generatePumlMmd(extension);

                    File fileToSave = new File(selectedFile.getParentFile(), selectedFile.getName() + '.' + extension);
                    FileUtil.writeToFile(fileToSave, uml);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(SequencePanel.this, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(psiElement != null);
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

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(psiElement != null);
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

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(psiElement != null);
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

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(psiElement != null);
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
            String[] superClass = navigable.findSuperClass(impl);

            _sequenceParams.getImplementationWhiteList().put(
                    face,
                    new ImplementClassFilter(superClass)
            );
            generate();
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(psiElement != null);
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
                actionGroup.add(new RemoveClassAction(displayObject.getObjectInfo()));
                if ((displayObject.getObjectInfo().hasAttribute(Info.INTERFACE_ATTRIBUTE) || displayObject.getObjectInfo().hasAttribute(Info.ABSTRACT_ATTRIBUTE))
                        && !displayObject.getObjectInfo().hasAttribute(Info.EXTERNAL_ATTRIBUTE)
                        /*&& !_sequenceParams.isSmartInterface()*/) {
                    String className = displayObject.getObjectInfo().getFullName();
                    List<String> impls = navigable.findImplementations(className);
                    actionGroup.addSeparator();
                    impls.stream().sorted().forEach(impl -> actionGroup.add(new ExpendInterfaceAction(className, impl)));
                }
            } else if (screenObject instanceof DisplayMethod) {
                DisplayMethod displayMethod = (DisplayMethod) screenObject;
                actionGroup.add(new RemoveMethodAction(displayMethod.getMethodInfo()));
                if ((displayMethod.getObjectInfo().hasAttribute(Info.INTERFACE_ATTRIBUTE) || displayMethod.getObjectInfo().hasAttribute(Info.ABSTRACT_ATTRIBUTE))
                        && !displayMethod.getObjectInfo().hasAttribute(Info.EXTERNAL_ATTRIBUTE)
                        /*&& !_sequenceParams.isSmartInterface()*/) {

                    String className = displayMethod.getObjectInfo().getFullName();
                    String methodName = displayMethod.getMethodInfo().getRealName();
                    List<String> argTypes = displayMethod.getMethodInfo().getArgTypes();
                    List<String> impls = navigable.findImplementations(className, methodName, argTypes);

                    actionGroup.addSeparator();
                    impls.stream().sorted().forEach(impl -> actionGroup.add(new ExpendInterfaceAction(className, impl)));
                }
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
