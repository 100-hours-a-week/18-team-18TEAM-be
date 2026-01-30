package com.caro.bizkit.observability;

import java.util.List;

public class FlowResolver {

    private static final List<FlowId> FLOWS = FlowId.definedRoutes();

    private FlowResolver() {}

    public static String resolve(String method, String path) {
        if (method == null || path == null) {
            return FlowId.UNKNOWN.flowId();
        }

        String normalizedPath = stripQuery(path);

        return FLOWS.stream()
                .filter(f -> f.method().name().equalsIgnoreCase(method))
                .filter(f -> f.pathTemplates().stream().anyMatch(tpl -> matchesTemplate(normalizedPath, tpl)))
                .findFirst()
                .map(FlowId::flowId)
                .orElse(FlowId.UNKNOWN.flowId());
    }

    static boolean matchesTemplate(String path, String template) {
        if (template == null || template.isBlank()) return false;

        return path.equals(template)
                || path.startsWith(template + "/");
    }

    static String stripQuery(String path) {
        int idx = path.indexOf('?');
        return (idx >= 0) ? path.substring(0, idx) : path;
    }
}