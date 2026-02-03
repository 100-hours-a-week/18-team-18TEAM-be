package com.caro.bizkit.common.config;

import com.caro.bizkit.observability.FlowId;
import com.caro.bizkit.observability.FlowTaggingObservationConvention;
import com.caro.bizkit.observability.HttpMetricsFilterProperties;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationConvention;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(HttpMetricsFilterProperties.class)
class ObservabilityConfig {

    private final HttpMetricsFilterProperties props;

    @Bean
    ServerRequestObservationConvention serverRequestObservationConvention() {
        return new FlowTaggingObservationConvention();
    }

    @Bean
    MeterFilter denyNonBusinessHttpRequests() {
        return new MeterFilter() {
            @Override
            public MeterFilterReply accept(Meter.Id id) {
                if (!"http.server.requests".equals(id.getName())) {
                    return MeterFilterReply.NEUTRAL;
                }

                String uri = id.getTag("uri");
                if (uri == null) return MeterFilterReply.NEUTRAL;

                if (props.getDenyUriExact().contains(uri)) return MeterFilterReply.DENY;

                return MeterFilterReply.NEUTRAL;
            }
        };
    }

    @Bean
    MeterFilter normalizeHttpServerRequestsUriByFlow() {
        return new MeterFilter() {
            @Override
            public Meter.Id map(Meter.Id id) {
                if (!"http.server.requests".equals(id.getName())) {
                    return id;
                }

                String flow = id.getTag("flow");
                if (flow == null || flow.isBlank() || "unknown".equals(flow)) {
                    return id;
                }

                FlowId flowId = FlowId.fromFlowId(flow);
                if (flowId == FlowId.UNKNOWN) {
                    return id;
                }

                String normalizedUri = flowId.primaryTemplateOrUnknown();

                return id.replaceTags(
                        Tags.concat(
                                id.getTags().stream().filter(t -> !t.getKey().equals("uri")).toList(),
                                List.of(Tag.of("uri", normalizedUri))
                        )
                );
            }
        };
    }
}