package vanstudio.sequence.ext.uast

import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.FileIndexUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import com.intellij.psi.util.ClassUtil
import com.intellij.util.concurrency.NonUrgentExecutor
import vanstudio.sequence.openapi.SequenceNavigable
import vanstudio.sequence.util.MyPsiUtil

class UastSequenceNavigable(val project : Project) : SequenceNavigable {
    override fun openClassInEditor(className: String?) {
        ReadAction.nonBlocking<Pair<VirtualFile, Int>> {
            val psiClass = ClassUtil.findPsiClass(PsiManager.getInstance(project), className!!)
            val virtualFile = MyPsiUtil.findVirtualFile(psiClass)
            val offset = MyPsiUtil.findNaviOffset(psiClass)
            Pair(virtualFile, offset)
        }.finishOnUiThread(ModalityState.defaultModalityState()) {
            if (it != null) openInEditor(it.first, it.second, project)
        }.inSmartMode(project).submit(NonUrgentExecutor.getInstance())
    }

    override fun openMethodInEditor(className: String?, methodName: String?, argTypes: MutableList<String>?) {
        TODO("Not yet implemented")
    }

    override fun isInsideAMethod(): Boolean {
        TODO("Not yet implemented")
    }

    override fun openMethodCallInEditor(
        fromClass: String?,
        fromMethod: String?,
        fromArgTypes: MutableList<String>?,
        toClass: String?,
        toMethod: String?,
        toArgType: MutableList<String>?,
        offset: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun findImplementations(className: String?): MutableList<String> {
        TODO("Not yet implemented")
    }

    override fun findImplementations(
        className: String?,
        methodName: String?,
        argTypes: MutableList<String>?
    ): MutableList<String> {
        TODO("Not yet implemented")
    }

    override fun openLambdaExprInEditor(
        fromClass: String?,
        fromMethod: String?,
        fromArgTypes: MutableList<String>?,
        argTypes: MutableList<String>?,
        returnType: String?,
        integer: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun openMethodCallInsideLambdaExprInEditor(
        fromClass: String?,
        enclosedMethodName: String?,
        enclosedMethodArgTypes: MutableList<String>?,
        argTypes: MutableList<String>?,
        returnType: String?,
        toClass: String?,
        toMethod: String?,
        toArgTypes: MutableList<String>?,
        offset: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun findSuperClass(className: String?): Array<String> {
        TODO("Not yet implemented")
    }

    private fun getFileEditorManager(project: Project): FileEditorManager? {
        return FileEditorManager.getInstance(project)
    }

    private fun openInEditor(virtualFile: VirtualFile?, offset: Int, project: Project) {
        if (virtualFile == null) return

        // temporary check offset MUST less than File length
        val length = virtualFile.length
        getFileEditorManager(project)?.openTextEditor(
            OpenFileDescriptor(
                project,
                virtualFile, if (offset > length) length.toInt() else offset
            ), true
        )
    }
}