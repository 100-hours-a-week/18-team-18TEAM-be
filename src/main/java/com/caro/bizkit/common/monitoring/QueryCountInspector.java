package com.caro.bizkit.common.monitoring;

import java.util.concurrent.atomic.AtomicInteger;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.stereotype.Component;

@Component
public class QueryCountInspector implements StatementInspector {

    private static final ThreadLocal<AtomicInteger> queryCount = ThreadLocal.withInitial(AtomicInteger::new);

    @Override
    public String inspect(String sql) {
        queryCount.get().incrementAndGet();
        return sql;
    }

    public static int getCount() {
        return queryCount.get().get();
    }

    public static void reset() {
        queryCount.get().set(0);
    }
}
