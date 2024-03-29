package com.mware.termsOfUse;

import com.mware.core.config.ConfigOption;
import com.mware.core.config.OptionHolder;

import static com.mware.core.config.OptionChecker.disallowEmpty;

public class TermsOfUseOptions extends OptionHolder {
    public static final ConfigOption<String> TITLE_PROPERTY = new ConfigOption<>(
            "termsOfUse.title",
            "",
            disallowEmpty(),
            String.class,
            "Terms of Use"
    );

    public static final ConfigOption<String> HTML_PROPERTY = new ConfigOption<>(
            "termsOfUse.html",
            "",
            disallowEmpty(),
            String.class,
            "Please agree to these Terms of Use"
    );

    public static final ConfigOption<String> DATE_PROPERTY = new ConfigOption<>(
            "termsOfUse.date",
            "",
            String.class,
            null
    );

    private TermsOfUseOptions() {
        super();
    }

    private static volatile TermsOfUseOptions instance;

    public static synchronized TermsOfUseOptions instance() {
        if (instance == null) {
            instance = new TermsOfUseOptions();
            // Should initialize all static members first, then register.
            instance.registerOptions();
        }
        return instance;
    }
}
