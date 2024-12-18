package at.alirezamoh.whisperer_for_laravel.inertia;

import com.intellij.psi.PsiFile;

public class InertiaPage {
    private final String path;

    private PsiFile file;

    public InertiaPage(String path, PsiFile file) {
        this.path = path;
        this.file = file;
    }

    public InertiaPage(String path) {
        this.path = path;
    }

    public PsiFile getFile() {
        return file;
    }

    public String getPath() {
        return path;
    }
}
