package io.github.springpropertiesmd.core.generator;

import io.github.springpropertiesmd.api.model.DocumentationBundle;
import io.github.springpropertiesmd.core.config.GeneratorConfig;

public sealed interface MarkdownGenerator permits TableMarkdownGenerator {

    String generate(DocumentationBundle bundle, GeneratorConfig config);
}
