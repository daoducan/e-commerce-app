package com.andd.customer.handler;

import java.util.Map;

public record ErrorResponse(
        Map<String, String> errors
) {
}
