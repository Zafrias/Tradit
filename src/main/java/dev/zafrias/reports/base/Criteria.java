package dev.zafrias.reports.base;

import lombok.Data;

@Data
public final class Criteria {
    private final String field;
    private final Object value;

}
