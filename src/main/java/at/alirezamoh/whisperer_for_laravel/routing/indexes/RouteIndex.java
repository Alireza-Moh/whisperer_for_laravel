package at.alirezamoh.whisperer_for_laravel.routing.indexes;

import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.FrameworkUtils;
import at.alirezamoh.whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.lang.javascript.index.gist.ListExternalizer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RouteIndex extends FileBasedIndexExtension<String, List<RouteData>> {
    public static final ID<String, List<RouteData>> INDEX_ID = ID.create("idea_whisperer_for_laravel.routing");

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
    public @NotNull ID<String, List<RouteData>> getName() {
        return INDEX_ID;
    }

    @Override
    public @NotNull  DataIndexer<String, List<RouteData>, FileContent> getIndexer() {
        return inputData -> {
            if (!FrameworkUtils.isLaravelProject(inputData.getProject())) {
                return Collections.emptyMap();
            }

            PsiFile file = inputData.getPsiFile();

            if (!(file instanceof PhpFile)) {
                return Collections.emptyMap();
            }

            Map<String, List<RouteData>> routes = new HashMap<>();
            List<RouteData> routeList = new ArrayList<>();

            for (MethodReference methodReference : PsiTreeUtil.findChildrenOfType(file, MethodReference.class)) {
                if (isLaravelRouteMethod(methodReference)) {
                    RouteData routeData = extractRouteData(methodReference);
                    if (routeData != null) {
                        routeList.add(routeData);
                    }
                }
            }

            routes.put(inputData.getPsiFile().getVirtualFile().getPath(), routeList);

            return routes;
        };
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public @NotNull DataExternalizer<List<RouteData>> getValueExternalizer() {
        return new ListExternalizer<>(new RouteDataExternalizer());
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public FileBasedIndex.@NotNull InputFilter getInputFilter() {
        return file -> file.getName().endsWith(".php") && file.getPath().contains("routes/");
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    private boolean isLaravelRouteMethod(MethodReference methodReference) {
        PhpExpression routeClassReference = methodReference.getClassReference();

        return routeClassReference instanceof ClassReferenceImpl classReferences
            && ROUTE_METHODS.containsKey(methodReference.getName())
            && ROUTE_NAMESPACES.contains(classReferences.getFQN());
    }

    private @Nullable RouteData extractRouteData(MethodReference methodReference) {
        if (!isValidRouteMethod(methodReference)) {
            return null;
        }

        PsiElement routeNameParameter = getParentParameter(methodReference);
        if (routeNameParameter == null) {
            return null;
        }

        String routeName = StrUtil.removeQuotes(routeNameParameter.getText());
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

    private RouteData createRouteData(String routeName, PsiElement uriParameter, MethodReference methodReference) {
        String uri = StrUtil.removeQuotes(uriParameter.getText());
        String filePath = methodReference.getContainingFile().getVirtualFile().getPath();
        int offset = methodReference.getTextOffset();

        return new RouteData(routeName, uri, filePath, offset);
    }
}
