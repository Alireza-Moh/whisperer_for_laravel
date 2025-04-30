package at.alirezamoh.whisperer_for_laravel.actions.models.codeGenerationHelperModels;

import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Field;
import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Method;
import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.ReadProperty;
import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Relation;

import java.util.ArrayList;
import java.util.List;

public class LaravelModel {
    private String namespaceName;

    private List<Field> fields;

    private List<ReadProperty> readProperties;

    private List<Method> methods;

    private List<Relation> relations;

    private String tableName;

    private String modelName;

    public LaravelModel() {
        this.fields = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.relations = new ArrayList<>();
        this.readProperties = new ArrayList<>();
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public List<ReadProperty> getReadProperties() {
        return readProperties;
    }

    public void setReadProperties(List<ReadProperty> readProperties) {
        this.readProperties = readProperties;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }

    public void addMethod(Method method) {
        this.methods.add(method);
    }

    public void addMethods(List<Method> methods) {
        this.methods.addAll(methods);
    }

    public void addField(Field field) {
        this.fields.add(field);
    }

    public void addReadProperty(ReadProperty readProperty) {
        this.readProperties.add(readProperty);
    }
}
