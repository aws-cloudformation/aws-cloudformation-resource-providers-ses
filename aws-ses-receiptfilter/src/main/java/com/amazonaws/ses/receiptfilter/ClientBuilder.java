package com.amazonaws.ses.receiptfilter;

import com.amazonaws.retry.PredefinedRetryPolicies;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.services.ses.SesClient;

public class ClientBuilder {
    public static SesClient getClient() {
        return SesClient.builder()
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .retryPolicy(RetryPolicy.builder().numRetries(PredefinedRetryPolicies.DEFAULT_MAX_ERROR_RETRY).build())
                        .build())
                .build();
    }
}
