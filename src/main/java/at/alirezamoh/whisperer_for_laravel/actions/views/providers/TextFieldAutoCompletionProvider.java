package at.alirezamoh.whisperer_for_laravel.actions.views.providers;

import com.intellij.ui.TextFieldWithAutoCompletion;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

public class TextFieldAutoCompletionProvider extends TextFieldWithAutoCompletion.StringsCompletionProvider  {
    public TextFieldAutoCompletionProvider(@Nullable Collection<String> variants, @Nullable Icon icon) {
        super(variants, icon);
    }
}