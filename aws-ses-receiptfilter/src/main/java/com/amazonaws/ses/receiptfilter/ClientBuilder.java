package com.amazonaws.ses.receiptfilter;

import software.amazon.awssdk.services.ses.SesClient;

public class ClientBuilder {
    public static SesClient getClient() {
        return SesClient.builder().build();
    }
}
