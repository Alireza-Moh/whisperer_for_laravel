package at.alirezamoh.whisperer_for_laravel.support.utils;

import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.ArrayList;
import java.util.List;

public class PhpClassUtils {
    /**
     * Retrieves all public methods of a given PHP class excluding constructor
     *
     * @param phpClass The PhpClass instance to extract public methods from.
     * @return A list of public methods (non-magic) defined in the given class
     */
    public static List<Method> getClassPublicMethod(PhpClass phpClass) {
        ArrayList<Method> methods = new ArrayList<>();

        for(Method method: phpClass.getMethods()) {
            if(method.getAccess().isPublic() && !method.getName().startsWith("__")) {
                methods.add(method);
            }
        }

        return methods;
    }
}
