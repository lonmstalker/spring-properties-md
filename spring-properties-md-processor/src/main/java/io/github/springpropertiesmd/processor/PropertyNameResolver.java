package io.github.springpropertiesmd.processor;

public class PropertyNameResolver {

    public String resolve(String prefix, String fieldName) {
        if (prefix == null || prefix.isEmpty()) {
            return toKebabCase(fieldName);
        }
        return prefix + "." + toKebabCase(fieldName);
    }

    static String toKebabCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('-');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
