package com.tl.backend.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubscriptionRequest {
    private String username;
    private String paymentMethodId;
    private String priceId;
}
