package com.caro.bizkit.observability;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum FlowId {

    UNKNOWN("unknown", HttpMethod.ANY, List.of()),
    WALLET_LIST_GET("wallet_list_get", HttpMethod.GET, List.of("/api/wallets")),
    CARD_LIST_GET("card_list_get", HttpMethod.GET, List.of("/api/cards", "/api/cards/me")),
    CARD_GET("card_Get", HttpMethod.GET, List.of("/api/cards/{card_id}", "/api/cards/me/latest")),
    CARD_REGISTER("card_register", HttpMethod.POST, List.of("/api/wallets"));

    private final String flowId;
    private final HttpMethod method;
    private final List<String> pathTemplates;

    FlowId(String flowId, HttpMethod method, List<String> pathTemplates) {
        this.flowId = flowId;
        this.method = method;
        this.pathTemplates = pathTemplates;
    }

    public String flowId() {
        return flowId;
    }

    public HttpMethod method() {
        return method;
    }

    public List<String> pathTemplates() {
        return pathTemplates;
    }

    public String primaryTemplateOrUnknown() {
        return (pathTemplates == null || pathTemplates.isEmpty()) ? "UNKNOWN" : pathTemplates.get(0);
    }

    private static final Map<String, FlowId> BY_FLOW_ID =
            List.of(values()).stream()
                    .collect(Collectors.toUnmodifiableMap(FlowId::flowId, Function.identity()));

    public static FlowId fromFlowId(String flowId) {
        if (flowId == null) return UNKNOWN;
        return BY_FLOW_ID.getOrDefault(flowId, UNKNOWN);
    }

    public static List<FlowId> definedRoutes() {
        return List.of(values()).stream()
                .filter(f -> f.pathTemplates != null && !f.pathTemplates.isEmpty())
                .toList();
    }

    public enum HttpMethod {
        GET, POST, PUT, PATCH, DELETE, ANY
    }
}