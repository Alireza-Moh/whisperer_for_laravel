package at.alirezamoh.whisperer_for_laravel.support.codeGeneration;

import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Field;
import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Relation;
import at.alirezamoh.whisperer_for_laravel.support.utils.LaravelPaths;
import at.alirezamoh.whisperer_for_laravel.support.utils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ModelMethodRelationGenerator {
    public static Map<String, Integer> RELATION_METHODS = new HashMap<>() {{
        put("belongsTo", 0);
        put("belongsToMany", 0);
        put("hasMany", 0);
        put("hasManyThrough", 0);
        put("hasOne", 0);
        put("hasOneOrMany", 0);
        put("hasOneOrManyThrough", 0);
        put("hasOneThrough", 0);
        put("morphMany", 0);
        put("morphOne", 0);
        put("morphTo", 0);
        put("morphToMany", 0);
    }};

    private PhpClass eloquentModelAsPhpClass;

    private List<Field> relationsAsFields;

    private final Project project;

    public ModelMethodRelationGenerator(Project project) {
        this.relationsAsFields = new ArrayList<>();
        this.project = project;
    }

    /**
     * Creates a list of relations from the model's relation methods
     *
     * @return a list of Relation objects
     */
    public @NotNull List<Relation> createMethodsFromModelRelations() {
        List<Relation> relations = new ArrayList<>();

        for (Method method : collectModelRelations()) {

            Map.Entry<String, PhpClass> methodNameAndRelatedModel = findRelationships(method);
            if (methodNameAndRelatedModel != null) {

                Relation relation = createRelation(method, methodNameAndRelatedModel);

                relations.add(relation);
                relationsAsFields.add(
                    new Field(
                        methodNameAndRelatedModel.getValue().getName(),
                        method.getName(),
                        false, relation.isArrayOrCollection()
                    )
                );
            }
        }

        return relations;
    }

    /**
     * Returns the relations as fields
     *
     * @return a list of fields representing the relations
     */
    public @NotNull List<Field> getRelationsAsFields() {
        return relationsAsFields;
    }

    public void setEloquentModelAsPhpClass(PhpClass eloquentModelAsPhpClass) {
        this.eloquentModelAsPhpClass = eloquentModelAsPhpClass;

        relationsAsFields = new ArrayList<>();
    }

    /**
     * Creates a Relation object from the given method and related model
     *
     * @param method the relationship method
     * @param methodNameAndRelatedModel the related model
     * @return a Relation object
     */
    private static @NotNull Relation createRelation(Method method, Map.Entry<String, PhpClass> methodNameAndRelatedModel) {
        return new Relation(
            method.getName(),
            methodNameAndRelatedModel.getKey(),
            "\\Illuminate\\Database\\Eloquent\\Relations\\"
                + StrUtils.ucFirst(methodNameAndRelatedModel.getKey())
                + "|"
                + methodNameAndRelatedModel.getValue().getName()
        );
    }

    /**
     * Resolves all relationships defined in the given model class
     *
     * @return a list of relationships in the given model class
     */
    private List<com.jetbrains.php.lang.psi.elements.Method> collectModelRelations() {
        List<com.jetbrains.php.lang.psi.elements.Method> relations = new ArrayList<>();

        for (com.jetbrains.php.lang.psi.elements.Method method : eloquentModelAsPhpClass.getOwnMethods()) {
            if (isRelationshipMethod(method, project)) {
                relations.add(method);
            }
        }

        return relations;
    }

    /**
     * Finds the related model relationships.
     *
     * @param foundedMethod the relationship method defined in the eloquent model
     * @return an entry containing the method name and related PhpClass, or null if no relationship is found
     */
    private @Nullable Map.Entry<String, PhpClass> findRelationships(com.jetbrains.php.lang.psi.elements.Method foundedMethod) {
        return Arrays.stream(foundedMethod.getChildren())
            .filter(element -> element instanceof GroupStatementImpl)
            .flatMap(groupStatement -> Arrays.stream(groupStatement.getChildren()))
            .filter(child -> child instanceof PhpReturnImpl)
            .flatMap(phpReturn -> Arrays.stream(phpReturn.getChildren()))
            .filter(child2 -> child2 instanceof MethodReferenceImpl)
            .map(child2 -> (MethodReferenceImpl) child2)
            .map(methodReference -> {
                PhpClass relatedModel = findRelatedModelFromMethod(foundedMethod);
                if (relatedModel != null) {
                    return new AbstractMap.SimpleEntry<>(methodReference.getName(), relatedModel);
                }
                return null;
            })
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    /**
     * Resolves the related model referenced in a relationship method
     *
     * @param foundedMethod the relationship method to analyze
     * @return the PhpClass of the related model, or null if no related model is found
     */
    private @Nullable PhpClass findRelatedModelFromMethod(com.jetbrains.php.lang.psi.elements.Method foundedMethod) {
        return Arrays.stream(foundedMethod.getChildren())
            .filter(element -> element instanceof GroupStatementImpl)
            .flatMap(groupStatement -> Arrays.stream(groupStatement.getChildren()))
            .filter(child -> child instanceof PhpReturnImpl)
            .flatMap(phpReturn -> Arrays.stream(phpReturn.getChildren()))
            .filter(child2 -> child2 instanceof MethodReferenceImpl)
            .map(child2 -> (MethodReferenceImpl) child2)
            .map(methodReference -> methodReference.getParameter(0))
            .filter(parameter -> parameter instanceof ClassConstantReferenceImpl)
            .map(parameter -> ((ClassConstantReferenceImpl) parameter).getClassReference())
            .filter(phpExpression -> phpExpression instanceof ClassReferenceImpl)
            .map(phpExpression -> ((ClassReferenceImpl) phpExpression).resolve())
            .filter(resolvedClass -> resolvedClass instanceof PhpClass)
            .map(resolvedClass -> (PhpClass) resolvedClass)
            .findFirst()
            .orElse(null);
    }

    /**
     * Determines whether the given method is a Laravel relationship method
     *
     * @param method  the method to check
     * @param project the project
     * @return true or false
     */
    private boolean isRelationshipMethod(com.jetbrains.php.lang.psi.elements.Method method, Project project) {
        return Arrays.stream(method.getChildren())
            .filter(element -> element instanceof GroupStatementImpl)
            .flatMap(element -> Arrays.stream(element.getChildren()))
            .filter(child -> child instanceof PhpReturnImpl)
            .flatMap(child -> Arrays.stream(child.getChildren()))
            .filter(child2 -> child2 instanceof MethodReferenceImpl)
            .map(child2 -> (MethodReferenceImpl) child2)
            .anyMatch(methodReference -> {
                List<PhpClassImpl> classes = MethodUtils.resolveMethodClasses(methodReference, project);
                PhpClass relationClass = PhpClassUtils.getClassByFQN(project, LaravelPaths.LaravelClasses.Model);
                String methodName = methodReference.getName();

                return methodName != null
                    && RELATION_METHODS.containsKey(methodName)
                    && relationClass != null
                    && classes.stream().anyMatch(clazz -> PhpClassUtils.isChildOf(clazz, relationClass));
            });
    }
}
