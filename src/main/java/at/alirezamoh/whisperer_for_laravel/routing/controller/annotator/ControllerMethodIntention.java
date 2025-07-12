package at.alirezamoh.whisperer_for_laravel.routing.controller.annotator;

import at.alirezamoh.whisperer_for_laravel.support.notification.Notify;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Intention action to create a method inside a controller
 */
public class ControllerMethodIntention extends BaseIntentionAction {
    /**
     * The method name
     */
    private final String methodName;

    /**
     * The PHP class (controller) where the method will be created
     */
    private PhpClass phpClass;

    /**
     * @param methodName The method name
     */
    public ControllerMethodIntention(PhpClass phpClass, String methodName) {
        this.phpClass = phpClass;
        this.methodName = StrUtils.removeQuotes(methodName);
    }


    /**
     * Determines if this intention action is available
     * In this case, the action is always available
     *
     * @param project The current project
     * @param editor  The editor instance
     * @param psiFile The current PSI file
     * @return true or false
     */
    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        return true;
    }

    /**
     * Execute the intention action
     *
     * @param project The current project.
     * @param editor  The editor instance.
     * @param psiFile The current PSI file.
     * @throws IncorrectOperationException If the operation cannot be completed
     */
    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        Runnable runnable = () -> {
            Method method = PhpPsiElementFactory.createMethod(
                project,
                "public function " + methodName + "() {\n    // TODO\n}"
            );

            Method[] existingMethods = phpClass.getOwnMethods();
            Method insertedMethod = null;

            if (existingMethods.length > 0) {
                Method lastMethod = existingMethods[existingMethods.length - 1];
                insertedMethod = (Method) phpClass.addAfter(method, lastMethod);
            } else {
                PsiElement openingBrace = findOpeningBrace(phpClass);
                if (openingBrace != null) {
                    insertedMethod = (Method) phpClass.addAfter(method, openingBrace);
                }
                else {
                    Notify.notifyError(project, "Could not create method");
                }
            }

            // Navigate and place caret
            if (insertedMethod != null && insertedMethod.isValid()) {
                openMethodInEditor(project, insertedMethod);
            }
        };

        com.intellij.openapi.application.ApplicationManager.getApplication().runWriteAction(runnable);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Create controller method";
    }

    @Override
    public @NotNull @IntentionName String getText() {
        return "Create controller method";
    }

    private @Nullable PsiElement findOpeningBrace(PhpClass phpClass) {
        BraceFinder braceFinder = new BraceFinder();
        phpClass.acceptChildren(braceFinder);

        return braceFinder.getElementFound();
    }

    /**
     * Opens the method in the editor and places the caret at the start of the method body
     *
     * @param project The current project
     * @param method  The method to open
     */
    private void openMethodInEditor(Project project, Method method) {
        if (!method.isValid()) return;

        method.navigate(true);

        PsiElement body = method.getLastChild();
        if (body != null) {
            Editor targetEditor = com.intellij.openapi.fileEditor.FileEditorManager
                .getInstance(project)
                .getSelectedTextEditor();

            if (targetEditor != null) {
                int offset = body.getTextRange().getStartOffset() + 1;
                targetEditor.getCaretModel().moveToOffset(offset);
                com.intellij.openapi.editor.ScrollType scrollType = com.intellij.openapi.editor.ScrollType.CENTER;
                targetEditor.getScrollingModel().scrollToCaret(scrollType);
            }
        }
    }

    /**
     * Visitor to find the opening brace in a PHP class
     */
    public static class BraceFinder extends PsiElementVisitor {
        private PsiElement elementFound;

        @Override
        public void visitElement(@NotNull PsiElement element) {
            if (element.textMatches("{")) {
                elementFound = element;
                return;
            }
            super.visitElement(element);
        }

        public PsiElement getElementFound() {
            return elementFound;
        }
    }
}