package com.caro.bizkit.observability;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class FlowResolver {

    private static final List<Route> ROUTES = FlowId.definedRoutes().stream()
            .flatMap(f -> f.pathTemplates().stream().map(tpl -> Route.of(f, tpl)))
            .sorted(Comparator.comparingInt(Route::specificityScore).reversed())
            .toList();

    private FlowResolver() {}

    public static String resolve(String method, String pathOrTemplate) {
        if (method == null || pathOrTemplate == null) return FlowId.UNKNOWN.flowId();

        FlowId.HttpMethod incoming = toHttpMethod(method);
        String path = normalizePath(stripQuery(pathOrTemplate));

        return ROUTES.stream()
                .filter(r -> r.flow.method() == FlowId.HttpMethod.ANY || r.flow.method() == incoming)
                .filter(r -> r.pattern.matcher(path).matches())
                .findFirst()
                .map(r -> r.flow.flowId())
                .orElse(FlowId.UNKNOWN.flowId());
    }

//    query string 삭제
    static String stripQuery(String path) {
        int idx = path.indexOf('?');
        return (idx >= 0) ? path.substring(0, idx) : path;
    }

//    일반화
    static String normalizePath(String path) {
        if (path == null || path.isBlank()) return "UNKNOWN";
        String p = path.trim();
        if (p.length() > 1 && p.endsWith("/")) p = p.substring(0, p.length() - 1);
        return p;
    }

    private static FlowId.HttpMethod toHttpMethod(String method) {
        return switch (method.toUpperCase()) {
            case "GET" -> FlowId.HttpMethod.GET;
            case "POST" -> FlowId.HttpMethod.POST;
            case "PUT" -> FlowId.HttpMethod.PUT;
            case "PATCH" -> FlowId.HttpMethod.PATCH;
            case "DELETE" -> FlowId.HttpMethod.DELETE;
            default -> FlowId.HttpMethod.ANY;
        };
    }

    private static final class Route {
        private final FlowId flow;
        private final Pattern pattern;
        private final int specificityScore;

        private Route(FlowId flow, Pattern pattern, int specificityScore) {
            this.flow = flow;
            this.pattern = pattern;
            this.specificityScore = specificityScore;
        }

        static Route of(FlowId flow, String template) {
            String normalized = normalizePath(template);

            String regex = normalized
                    .replaceAll("\\{[^/]+}", "[^/]+");

            Pattern p = Pattern.compile("^" + regex + "$");
            return new Route(flow, p, computeSpecificityScore(normalized));
        }

//        더 구체적인 template으로 매칭
        static int computeSpecificityScore(String template) {
            String[] segs = template.split("/");
            int fixed = 0, vars = 0;
            for (String s : segs) {
                if (s.isBlank()) continue;
                if (s.startsWith("{") && s.endsWith("}")) vars++;
                else fixed++;
            }
            return fixed * 10 - vars;
        }

        int specificityScore() {
            return specificityScore;
        }
    }


}