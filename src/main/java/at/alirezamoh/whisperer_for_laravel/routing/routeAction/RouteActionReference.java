package at.alirezamoh.whisperer_for_laravel.routing.routeAction;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.utils.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This class resolves references controller actions and provides completion
 */
public class RouteActionReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    /**
     * The current project
     */
    private Project project;

    /**
     * The plugin's settings state for accessing Laravel project configurations.
     */
    private final SettingsState settingsState;

    /**
     * Default path to the controllers directory in a standard Laravel application.
     */
    private final String DEFAULT_CONTROLLER_PATH = "app/Http/Controllers/";

    public RouteActionReference(@NotNull PsiElement element, TextRange rangeInElement) {
        super(element, rangeInElement);

        this.project = element.getProject();
        this.settingsState = SettingsState.getInstance(project);
    }

    /**
     * Resolves the controller actions
     * @return The resolved controller action
     */
    @Override
    public @Nullable PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean b) {
        String targetAction = StrUtils.removeQuotes(myElement.getText());

        if (!targetAction.contains("@")) {
            return new ResolveResult[0];
        }

        for (Map.Entry<String, PsiElement> entry : getAllControllersWithActions().entrySet()) {
            if (entry.getKey().equals(targetAction)) {
                return new ResolveResult[] { new PsiElementResolveResult(entry.getValue()) };
            }
        }

        String[] parts = targetAction.split("@");

        if (parts.length != 2) {
            return new ResolveResult[0];
        }

        String fullController = parts[0].trim();
        String methodName = parts[1].trim();
        int lastIndex = fullController.lastIndexOf('\\');
        String controllerName = (lastIndex != -1) ? fullController.substring(lastIndex + 1) : fullController;

        Collection<PhpClass> controllers = PhpIndexUtils.getPhpClassesByName(controllerName, project);
        List<PsiElementResolveResult> foundedVariants = new ArrayList<>();
        for (PhpClass controller : controllers) {
            Method method = controller.findMethodByName(methodName);

            if (method != null) {
                foundedVariants.add(new PsiElementResolveResult(method));
            }
        }

        return foundedVariants.toArray(new ResolveResult[0]);
    }

    /**
     * Returns an array of controller actions
     * @return middleware list
     */
    @Override
    public Object @NotNull [] getVariants() {
        List<LookupElementBuilder> variants = new ArrayList<>();

        for (Map.Entry<String, PsiElement> entry : getAllControllersWithActions().entrySet()) {
            variants.add(
                PsiElementUtils.buildSimpleLookupElement(entry.getKey())
            );
        }

        return variants.toArray();
    }

    /**
     * Collects all controller actions from the default and module-specific directories
     *
     * @return a map of action strings to their corresponding PSI elements
     */
    private Map<String, PsiElement> getAllControllersWithActions() {
        Map<String, PsiElement> elements = new HashMap<>();
        String path = PluginUtils.getProjectDirectoryBasePath(project, DEFAULT_CONTROLLER_PATH, true);

        PsiDirectory controllerDir = DirectoryUtils.getDirectory(project, path);

        if (controllerDir != null) {
            collectPhpClasses(controllerDir, elements);
        }

        collectControllersFromModules(elements);

        return elements;
    }

    /**
     * Collects controller classes and methods from module-specific directories
     *
     * @param elements the map to store discovered controller methods
     */
    private void collectControllersFromModules(Map<String, PsiElement> elements) {
        if (settingsState.isModuleApplication()) {
            String modulesPath = PluginUtils.getProjectDirectoryBasePath(project, settingsState.getModulesDirectoryPath(), true);

            PsiDirectory modulesDir = DirectoryUtils.getDirectory(project, modulesPath);

            if (modulesDir != null) {
                String moduleSrc = StrUtils.addSlashes(settingsState.getModuleSrcDirectoryPath()) + "Http/Controllers/";
                for (PsiDirectory module : modulesDir.getSubdirectories()) {
                    PsiDirectory controllerDirInModule = DirectoryUtils.getDirectory(
                        project,
                        StrUtils.removeDoubleForwardSlashes(modulesPath + "/" + module.getName() + moduleSrc)
                    );

                    if (controllerDirInModule != null) {
                        collectPhpClasses(controllerDirInModule, elements);
                    }
                }
            }
        }
    }

    /**
     * Recursively collects all Controller classes and their public methods from a directory
     *
     * @param directory the directory to scan for PHP files
     * @param elements  the map to store discovered methods with their fully qualified names
     */
    private void collectPhpClasses(PsiDirectory directory, Map<String, PsiElement> elements) {
        for (PsiElement element : directory.getChildren()) {
            if (element instanceof PhpFile controllerFile) {
                for (PhpClass phpClass : PsiTreeUtil.findChildrenOfType(controllerFile, PhpClass.class)) {
                    String fqn = phpClass.getPresentableFQN().replace("/", "\\");
                    for (Method method : PhpClassUtils.getClassPublicMethods(phpClass, false)) {
                        String methodName = method.getName();
                        String methodString = fqn + "@" + methodName;
                        elements.put(methodString, method);
                    }
                }
            } else if (element instanceof PsiDirectory) {
                collectPhpClasses((PsiDirectory) element, elements);
            }
        }
    }
}
