package at.alirezamoh.whisperer_for_laravel.routing.routeAction;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.whisperer_for_laravel.support.psiUtil.PsiUtil;
import at.alirezamoh.whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class resolves references controller actions and provides completion
 */
public class RouteActionReference extends PsiReferenceBase<PsiElement> {
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

    /**
     * List of method names to ignore when processing controller methods.
     */
    private final List<String> IGNORE_LIST = new ArrayList<>() {{
        add("__construct");
        add("__index");
        add("__invoke");
    }};

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
        String targetAction = StrUtil.removeQuotes(myElement.getText());

        for (Map.Entry<String, PsiElement> entry : getAllControllersWithActions().entrySet()) {
            if (entry.getKey().equals(targetAction)) {
                return entry.getValue();
            }
        }

        return null;
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
                PsiUtil.buildSimpleLookupElement(entry.getKey())
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
        String path = DEFAULT_CONTROLLER_PATH;

        if (!settingsState.isLaravelDirectoryEmpty()) {
            String laravelDir = StrUtil.addSlashes(settingsState.getLaravelDirectoryPath());
            path = laravelDir + path;
        }


        PsiDirectory controllerDir = DirectoryPsiUtil.getDirectory(project, path);

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
            String modulesPath = settingsState.getModulesDirectoryPath();
            if (!settingsState.isLaravelDirectoryEmpty()) {
                modulesPath = StrUtil.addSlashes(settingsState.getLaravelDirectoryPath())
                    + StrUtil.addSlashes(modulesPath, true, false);
            }

            PsiDirectory modulesDir = DirectoryPsiUtil.getDirectory(project, modulesPath);

            if (modulesDir != null) {
                String moduleSrc = StrUtil.addSlashes(settingsState.getModuleSrcDirectoryPath()) + "Http/Controllers/";
                for (PsiDirectory module : modulesDir.getSubdirectories()) {
                    PsiDirectory controllerDirInModule =DirectoryPsiUtil.getDirectory(
                        project,
                        StrUtil.removeDoubleSlashes(modulesPath + "/" + module.getName() + moduleSrc)
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
                    for (Method method : phpClass.getMethods()) {
                        String methodName = method.getName();
                        if (method.getModifier().isPublic() && !method.isAbstract() && !IGNORE_LIST.contains(methodName)) {
                            String methodString = fqn + "@" + methodName;
                            elements.put(methodString, method);
                        }
                    }
                }
            } else if (element instanceof PsiDirectory) {
                collectPhpClasses((PsiDirectory) element, elements);
            }
        }
    }
}
