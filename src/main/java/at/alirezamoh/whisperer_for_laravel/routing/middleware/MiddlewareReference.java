package at.alirezamoh.whisperer_for_laravel.routing.middleware;

import at.alirezamoh.whisperer_for_laravel.gate.visitors.GateProcessor;
import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.utils.DirectoryUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class resolves references to middleware groups and aliases and provides completion
 */
public class MiddlewareReference extends PsiReferenceBase<PsiElement> {
    /**
     * The current project
     */
    private Project project;

    private final String BOOTSTRAP_APP_FILE = "/bootstrap/app.php";

    private final String BASE_MIDDLEWARE_FILE_PATH = "/vendor/laravel/framework/src/Illuminate/Foundation/Configuration/Middleware.php";

    private final SettingsState settingsState;

    private GateProcessor gateProcessor;

    public MiddlewareReference(@NotNull PsiElement element, TextRange rangeInElement) {
        super(element, rangeInElement);

        this.project = element.getProject();
        this.settingsState = SettingsState.getInstance(project);
        gateProcessor = new GateProcessor(project);
    }

    /**
     * Resolves the middleware name or alias reference to the corresponding PSI element
     * @return The resolved middleware or null
     */
    @Override
    public @Nullable PsiElement resolve() {
        String targetMiddleware = StrUtils.removeQuotes(myElement.getText());
        PsiElement foundedElement = null;

        for (PsiElement element : collectAllMiddlewares()) {
            if (StrUtils.removeQuotes(element.getText()).equals(targetMiddleware)) {
                foundedElement = element;
            }
        }

        if (foundedElement == null) {
            if (targetMiddleware.startsWith("can:")) {
                targetMiddleware = targetMiddleware.substring("can:".length());
            }
            foundedElement = gateProcessor.findGateAbility(
                PhpPsiElementFactory.createStringLiteralExpression(project, targetMiddleware, false)
            );
        }

        return foundedElement;
    }

    /**
     * Returns an array of middleware names and aliases
     * @return middleware list
     */
    @Override
    public Object @NotNull [] getVariants() {
        List<LookupElementBuilder> variants = new ArrayList<>();

        for (PsiElement element : collectAllMiddlewares()) {
            variants.add(
                PsiElementUtils.buildSimpleLookupElement(StrUtils.removeQuotes(element.getText()))
            );
        }

        GateProcessor gateProcessor = new GateProcessor(project);
        for (String gate : gateProcessor.collectGates()) {
            variants.add(
                PsiElementUtils.buildSimpleLookupElement("can:" + gate)
            );
        }

        return variants.toArray();
    }

    private List<PsiElement> collectAllMiddlewares() {
        String filename = BOOTSTRAP_APP_FILE;
        String baseMiddlewareFilePath = BASE_MIDDLEWARE_FILE_PATH;

        if (!settingsState.isProjectDirectoryEmpty()) {
            String laravelDir = StrUtils.addSlashes(settingsState.getProjectDirectoryPath(), false, true);
            filename = laravelDir + filename;
            baseMiddlewareFilePath = laravelDir + baseMiddlewareFilePath;
        }
        PsiFile appFile = DirectoryUtils.getFileByName(project, filename);
        PsiFile baseMiddlewareFile = DirectoryUtils.getFileByName(project, baseMiddlewareFilePath);

        AppFileVisitor visitor = new AppFileVisitor();

        if (appFile != null) {
            appFile.acceptChildren(visitor);
        }

        if (baseMiddlewareFile != null) {
            baseMiddlewareFile.acceptChildren(visitor);
        }


        return visitor.getElements();
    }
}
