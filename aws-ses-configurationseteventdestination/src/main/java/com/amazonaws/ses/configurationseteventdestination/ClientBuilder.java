package com.amazonaws.ses.configurationseteventdestination;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.services.ses.SesClient;

public class ClientBuilder {
    public static SesClient getClient() {
        ClientOverrideConfiguration clientOverrideConfiguration = ClientOverrideConfiguration.builder()
                .retryPolicy(RetryPolicy.defaultRetryPolicy())
                .build();
        return SesClient.builder()
                .overrideConfiguration(clientOverrideConfiguration)
                .build();
    }
}
