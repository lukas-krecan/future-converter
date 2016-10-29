package net.javacrumbs.futureconverter.common.internal;

import java.util.Optional;

public interface OriginSource {
    Object getOrigin();

    static <T> Object extractOriginalValue(Object potentialSource, Class<?> expectedClass) {
        if (potentialSource instanceof OriginSource) {
            Object origin = ((OriginSource) potentialSource).getOrigin();
            if (expectedClass.isAssignableFrom(origin.getClass())) {
                return Optional.of(origin);
            }
        }
        return Optional.empty();
    }
}
