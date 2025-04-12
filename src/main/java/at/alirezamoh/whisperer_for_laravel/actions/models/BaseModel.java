package at.alirezamoh.whisperer_for_laravel.actions.models;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;

import java.util.Arrays;

/**
 * Base class for model objects
 */
abstract public class BaseModel {

    protected SettingsState settingsState;

    /**
     * The name of the file or entity
     */
    protected String name;

    /**
     * The unformatted full path to the module in this format "/app/Cms/all"
     */
    protected String unformattedModuleFullPath;

    /**
     * The formatted full path to the module in this format "\App\Cms\All"
     */
    protected String formattedModuleFullPath;

    /**
     * The destination path for the file, where the file is saved
     */
    protected String destination;

    /**
     * The namespace for the PHP class inside the file in this format "App\Cms\All\View\Components"
     */
    protected String namespace;

    /**
     * The slug for the file like "LoginController" the "Controller" is the slug
     */
    protected String slug;

    /**
     * The file extension
     */
    protected String extension;

    /**
     * The actual file name without the extension
     */
    protected String filePath;

    protected String defaultDestination;

    protected boolean withoutModuleSrcPath = false;

    /**
     * @param name                      The name of the file or entity
     * @param unformattedModuleFullPath The unformatted full path to the module
     * @param formattedModuleFullPath   The formatted full path to the module
     * @param defaultDestination        The destination path for the file
     * @param slug                      The slug for the file
     * @param extension                 The file extension
     * @param namespace                 The namespace for the PHP class
     */
    public BaseModel(
        SettingsState settingsState,
        String name,
        String unformattedModuleFullPath,
        String formattedModuleFullPath,
        String defaultDestination,
        String slug,
        String extension,
        String namespace
    )
    {
        this.settingsState = settingsState;
        this.name = removeFileExtension(name, extension);
        this.unformattedModuleFullPath = unformattedModuleFullPath;
        this.formattedModuleFullPath = formattedModuleFullPath;
        this.slug = slug;
        this.extension = extension;
        this.defaultDestination = defaultDestination;

        initDestination();
        initNamespace(namespace);
        initFilePath();
    }

    public BaseModel() {}

    public abstract void setWithoutModuleSrc();

    /**
     * Removes the file extension from a filename
     *
     * @param filename The filename
     * @param extension The extension to remove
     * @return The filename without the extension
     */
    public String removeFileExtension(String filename, String extension) {
        return filename.endsWith(extension) ? filename.replace(extension, "") : filename;
    }

    /**
     * Returns the actual file name by removing the possible folder path
     * @return File name
     */
    public String getName() {
        String[] names = getNameAsArray();
        String substractedString = names[names.length - 1];

        if (substractedString.endsWith(slug)) {
            return substractedString;
        }

        String modelName = names[names.length - 1];

        return modelName.endsWith(slug) ? modelName : modelName + slug;
    }

    /**
     * Returns the slug for entity like "Controller" or "Job"
     * @return The namespace.
     */
    public String getSlug() {
        return slug;
    }

    /**
     * Returns the namespace for the file or entity
     * @return The namespace.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Returns the file extension
     * @return The file extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Returns the folder path by removing the actual file name
     * @return Folder path in this format "folder/anotherFolder"
     */
    public String getFolderPath() {
        StringBuilder folderPath = new StringBuilder();

        for (String element : getFolderNames()) {
            folderPath.append(element).append("/");
        }

        if (!folderPath.isEmpty()) {
            folderPath.setLength(folderPath.length() - 1);
        }

        return folderPath.toString();
    }

    /**
     * Returns the destination path for the file or entity
     * @return The destination path
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Returns the file path
     */
    public String getFilePath() {
        return StrUtils.addSlashes(filePath);
    }

    public String getFormattedModuleFullPath() {
        return formattedModuleFullPath;
    }

    /**
     * Sets the destination path
     * @param destination The new name
     */
    public void setDestination(String destination) {
        this.destination = destination;

        if (settingsState.isProjectDirectoryEmpty()) {
            filePath = StrUtils.removeDoubleForwardSlashes(destination + "/" + getName() + extension);
        }
        else {
            filePath = StrUtils.removeDoubleForwardSlashes(
                StrUtils.addSlashes(settingsState.getProjectDirectoryPath())
                    + destination
                    + "/"
                    + getName()
                    + extension
            );
        }
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Sets the name of the file or entity
     * @param name The new name
     */
    public void setName(String name) {
        this.name = name;
    }

    protected String getModelVariableName(String eloquentModelName) {
        char firstChar = eloquentModelName.charAt(0);

        if (Character.isUpperCase(firstChar)) {
            return Character.toLowerCase(firstChar) + eloquentModelName.substring(1);

        } else {
            return eloquentModelName;
        }
    }

    /**
     * Sets the namespace for the PHP class
     * @param namespace The new namespace
     */
    public void initNamespace(String namespace) {
        if (formattedModuleFullPath.isEmpty()) {
            if (getFolderPath().isEmpty()) {
                this.namespace = namespace;
            }
            else {
                this.namespace = namespace
                    + "\\"
                    + getFolderPath().replace("/", "\\");
            }
        }
        else {
            if (getFolderPath().isEmpty()) {
                this.namespace = formattedModuleFullPath + "\\" + namespace;
            }
            else {
                this.namespace = formattedModuleFullPath
                    + "\\"
                    + namespace
                    + "\\"
                    + getFolderPath().replace("/", "\\");
            }
        }
    }

    public String getUnformattedModuleFullPath() {
        return unformattedModuleFullPath;
    }

    protected void initFilePath() {
        filePath = StrUtils.removeDoubleForwardSlashes(destination + "/" + getName() + extension);
    }

    protected void initDestination() {
        this.setWithoutModuleSrc();

        if (withoutModuleSrcPath) {
            this.destination = generateDestinationBasedWithoutModuleSrcPath();
        }
        else {
            this.destination = generateDestinationBasedWithModuleSrcPath();
        }
    }

    protected String normalizeFolderPath() {
        String normalizedPath = getFolderPath().replace('\\', '/');

        if (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }

        if (normalizedPath.endsWith("/")) {
            normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
        }

        return normalizedPath;
    }

    /**
     * Returns an array of folder names from the file path
     * @return An array of folder names
     */
    private String[] getFolderNames() {
        String[] names = getNameAsArray();
        return Arrays.copyOf(names, names.length-1);
    }

    /**
     * Splits the file name into an array of strings
     * @return An array of strings representing the file name
     */
    public String[] getNameAsArray() {
        return name.split("[\\\\/]");
    }

    private String generateDestinationBasedWithModuleSrcPath() {
        String basePath = "";
        String normalizedFolderPath = normalizeFolderPath();

        if (settingsState.isModuleApplication()) {
            if (settingsState.isProjectDirectoryEmpty()) {
                if (!settingsState.isModuleSrcDirectoryEmpty() && !withoutModuleSrcPath) {
                    if (unformattedModuleFullPath.equals("/app")) {
                        basePath = unformattedModuleFullPath + defaultDestination;
                    }
                    else {
                        basePath = unformattedModuleFullPath
                            + StrUtils.addSlashes(settingsState.getModuleSrcDirectoryPath())
                            + defaultDestination;
                    }
                }

                if (!normalizedFolderPath.isEmpty()) {
                    basePath = basePath + StrUtils.addSlashes(normalizedFolderPath);
                }
            }
            else {
                basePath = StrUtils.addSlashes(settingsState.getProjectDirectoryPath())
                    + unformattedModuleFullPath;

                if (!settingsState.isModuleSrcDirectoryEmpty() && !withoutModuleSrcPath) {
                    if (!unformattedModuleFullPath.equals("/app")) {
                        basePath = basePath + StrUtils.addSlashes(settingsState.getModuleSrcDirectoryPath());
                    }
                }

                basePath = basePath + StrUtils.addSlashes(defaultDestination);

                if (!normalizedFolderPath.isEmpty()) {
                    basePath = basePath + StrUtils.addSlashes(normalizedFolderPath);
                }
            }
        }
        else {
            if (settingsState.isProjectDirectoryEmpty()) {
                basePath = unformattedModuleFullPath + StrUtils.addSlashes(defaultDestination);

                if (!normalizedFolderPath.isEmpty()) {
                    basePath = basePath + normalizedFolderPath;
                }
            }
            else {
                basePath = StrUtils.removeDoubleForwardSlashes(
                    StrUtils.addSlashes(settingsState.getProjectDirectoryPath())
                        + unformattedModuleFullPath
                        + StrUtils.addSlashes(defaultDestination)
                );

                if (!normalizedFolderPath.isEmpty()) {
                    basePath = basePath + StrUtils.addSlashes(normalizedFolderPath);
                }
            }
        }

        return StrUtils.removeDoubleForwardSlashes(basePath);
    }

    public String generateDestinationBasedWithoutModuleSrcPath() {
        String basePath = "";
        String normalizedFolderPath = normalizeFolderPath();

        if (settingsState.isModuleApplication()) {
            if (settingsState.isProjectDirectoryEmpty()) {
                if (!settingsState.isModuleSrcDirectoryEmpty() && withoutModuleSrcPath) {
                    if (unformattedModuleFullPath.equals("/app")) {
                        basePath = defaultDestination;
                    }
                    else {
                        basePath = unformattedModuleFullPath + defaultDestination;
                    }
                }

                if (!normalizedFolderPath.isEmpty()) {
                    basePath = basePath + StrUtils.addSlashes(normalizedFolderPath);
                }
            }
            else {
                if (unformattedModuleFullPath.equals("/app")) {
                    basePath = StrUtils.addSlashes(settingsState.getProjectDirectoryPath())
                        + StrUtils.addSlashes(defaultDestination);
                }
                else {
                    basePath = StrUtils.addSlashes(settingsState.getProjectDirectoryPath())
                        + unformattedModuleFullPath
                        + StrUtils.addSlashes(defaultDestination);
                }

                if (!normalizedFolderPath.isEmpty()) {
                    basePath = basePath + StrUtils.addSlashes(normalizedFolderPath);
                }
            }
        }
        else {
            if (settingsState.isProjectDirectoryEmpty()) {
                basePath = unformattedModuleFullPath + StrUtils.addSlashes(defaultDestination);

                if (!normalizedFolderPath.isEmpty()) {
                    basePath = basePath + normalizedFolderPath;
                }
            }
            else {
                basePath = StrUtils.removeDoubleForwardSlashes(
                    StrUtils.addSlashes(settingsState.getProjectDirectoryPath())
                        + unformattedModuleFullPath
                        + StrUtils.addSlashes(defaultDestination)
                );

                if (!normalizedFolderPath.isEmpty()) {
                    basePath = basePath + StrUtils.addSlashes(normalizedFolderPath);
                }
            }
        }

        return StrUtils.removeDoubleForwardSlashes(basePath);
    }
}