package at.alirezamoh.whisperer_for_laravel.indexes;

import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.io.VoidDataExternalizer;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RouteIndex extends FileBasedIndexExtension<String, Void> {
    public static final ID<String, Void> INDEX_ID = ID.create("whisperer_for_laravel.routing");

    /**
     * The name of the method used to define route names
     */
    private final String ROUTE_METHOD_NAME = "name";

    /**
     * The names of the route helper functions
     */
    public static Map<String, Integer> ROUTE_METHODS = new HashMap<>() {{
        put("get", 1);
        put("post", 1);
        put("put", 1);
        put("delete", 1);
        put("patch", 1);
        put("options", 1);
        put("any", 1);
        put("fallback", 0);
        put("match", 0);
    }};

    /**
     * The namespaces of the `Route` facade and class
     */
    private final List<String> ROUTE_NAMESPACES = new ArrayList<>() {{
        add("\\Illuminate\\Routing\\Route");
        add("\\Illuminate\\Support\\Facades\\Route");
        add("\\Route");
    }};

    @Override
    public @NotNull ID<String, Void> getName() {
        return INDEX_ID;
    }

    @Override
    public @NotNull  DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            Project project = inputData.getProject();

            if (PluginUtils.shouldNotCompleteOrNavigate(project)) {
                return Collections.emptyMap();
            }

            PsiFile file = inputData.getPsiFile();

            if (!(file instanceof PhpFile)) {
                return Collections.emptyMap();
            }

            Map<String, Void> routes = new HashMap<>();

            for (MethodReference methodReference : PsiTreeUtil.findChildrenOfType(file, MethodReference.class)) {
                if (isLaravelRouteMethod(methodReference)) {
                    String routeData = extractRouteData(methodReference);
                    if (routeData != null) {
                        routes.put(routeData, null);
                    }
                }
            }

            return routes;
        };
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public @NotNull DataExternalizer<Void> getValueExternalizer() {
        return VoidDataExternalizer.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public FileBasedIndex.@NotNull InputFilter getInputFilter() {
        return file -> file.getFileType() == PhpFileType.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public boolean traceKeyHashToVirtualFileMapping() {
        return true;
    }

    private boolean isLaravelRouteMethod(MethodReference methodReference) {
        PhpExpression routeClassReference = methodReference.getClassReference();

        return routeClassReference instanceof ClassReferenceImpl classReferences
            && ROUTE_METHODS.containsKey(methodReference.getName())
            && classReferences.getFQN() != null
            && ROUTE_NAMESPACES.contains(classReferences.getFQN());
    }

    private @Nullable String extractRouteData(MethodReference methodReference) {
        if (!isValidRouteMethod(methodReference)) {
            return null;
        }

        PsiElement routeNameParameter = getParentParameter(methodReference);
        if (routeNameParameter == null) {
            return null;
        }

        String routeName = StrUtils.removeQuotes(routeNameParameter.getText());
        PsiElement uriParameter = methodReference.getParameter(0);

        if (uriParameter == null) {
            return null;
        }

        return createRouteData(routeName, uriParameter, methodReference);
    }

    private boolean isValidRouteMethod(MethodReference methodReference) {
        PsiElement parent = methodReference.getParent();
        if (parent instanceof MethodReferenceImpl routeNameMethod) {
            return Objects.equals(routeNameMethod.getName(), ROUTE_METHOD_NAME);
        }
        return false;
    }

    private @Nullable PsiElement getParentParameter(MethodReference methodReference) {
        PsiElement parent = methodReference.getParent();
        if (parent instanceof MethodReferenceImpl parentMethodReference) {
            return parentMethodReference.getParameter(0);
        }
        return null;
    }

    private String createRouteData(String routeName, PsiElement uriParameter, MethodReference methodReference) {
        String uri = StrUtils.removeQuotes(uriParameter.getText());
        int offset = methodReference.getTextOffset();

        return uri + " | " + routeName + " | " + offset;
    }
}
