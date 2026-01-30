package com.caro.bizkit.common.config;

import com.caro.bizkit.observability.FlowId;
import com.caro.bizkit.observability.FlowTaggingObservationConvention;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.config.MeterFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationConvention;

import java.util.List;

@Slf4j
@Configuration
class ObservabilityConfig {

    @Bean
    ServerRequestObservationConvention serverRequestObservationConvention() {
        return new FlowTaggingObservationConvention();
    }

    @Bean
    public MeterFilter normalizeHttpServerRequestsUriByFlow() {
        return new MeterFilter() {
            @Override
            public Meter.Id map(Meter.Id id) {
                if (!id.getName().startsWith("http.server.requests")) {
                    return id;
                }

                String flow = id.getTag("flow");
                if (flow == null || flow.isBlank() || flow.equals("unknown")) {
                    return id;
                }

                String uri = id.getTag("uri");
                if (uri == null) {
                    return id;
                }

                String normalized = FlowId.fromFlowId(flow).primaryTemplateOrUnknown();

                return id.replaceTags(
                        Tags.concat(
                                id.getTags().stream().filter(t -> !t.getKey().equals("uri")).toList(),
                                List.of(Tag.of("uri", normalized))
                        )
                );
            }
        };
    }

}
