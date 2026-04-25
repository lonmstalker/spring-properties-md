package io.github.springpropertiesmd.core.generator;

import io.github.springpropertiesmd.api.model.DocumentationBundle;
import io.github.springpropertiesmd.core.config.GeneratorConfig;

public sealed interface MarkdownGenerator permits TableMarkdownGenerator {

    RenderedDocumentation render(DocumentationBundle bundle, GeneratorConfig config);

    default String generate(DocumentationBundle bundle, GeneratorConfig config) {
        return render(bundle, config).singleContent();
    }
}
