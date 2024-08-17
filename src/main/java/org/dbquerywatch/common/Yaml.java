package org.dbquerywatch.common;

import com.google.errorprone.annotations.Var;

import java.util.Collection;
import java.util.Map;

import static java.lang.String.format;

public final class Yaml {
    private static final char LF = '\n';
    private static final String FILLER = "                                ";

    private final StringBuilder sb;

    public Yaml(StringBuilder sb) {
        this.sb = sb;
    }

    public void toYaml(Object object) {
        toYaml(0, object);
    }

    @SuppressWarnings("unchecked")
    private void toYaml(int margin, Object object) {
        if (object instanceof Pojo) {
            toYaml(margin, ((Pojo) object).toPojo());
        } else if (object instanceof Collection) {
            collectionToYaml(margin, (Collection<Object>) object);
        } else if (object instanceof Map) {
            mapToYaml(margin, (Map<String, Object>) object);
        } else {
            if (lastChar() == LF) {
                sb.replace(sb.length() - 1, sb.length(), " ");
            }
            sb.append(object).append(LF);
        }
    }

    private void collectionToYaml(int margin, Collection<?> col) {
        if (col.isEmpty()) {
            toYaml(margin, "[]");
            return;
        }
        @Var int index = 0;
        for (Object item : col) {
            index++;
            if (item instanceof HasTitle) {
                tab(margin).append(format("## %d/%d %s", index, col.size(), ((HasTitle) item).title())).append(LF);
            }
            tab(margin).append("- ");
            toYaml(margin + 2, item);
        }
    }

    private void mapToYaml(int margin, Map<String, ?> map) {
        if (map.isEmpty()) {
            toYaml(margin, "{}");
            return;
        }
        map.forEach((key, value) -> {
            tab(margin).append(key)
                .append(':').append(LF);
            toYaml(margin + 4, value);
        });
    }

    private char lastChar() {
        return sb.length() > 0 ? sb.charAt(sb.length() - 1) : '\0';
    }

    private StringBuilder tab(int margin) {
        if (lastChar() == LF) {
            sb.append(FILLER, 0, margin);
        }
        return sb;
    }
}
