package tech.manggocli.infrastructure.codegen.shared;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;

public final class TemplateRenderer {
    private TemplateRenderer() {
    }

    private static final MustacheFactory mustacheFactory = new DefaultMustacheFactory(
            "templates"
    ) {

        @Override
        public void encode(String value, Writer writer) {
            try {
                writer.write(value);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    };

    public static String render(final String template, final Object context) {
        final var mustache = mustacheFactory.compile(template);
        final var writer = new StringWriter();
        mustache.execute(writer, context);
        return writer.toString();
    }
}
