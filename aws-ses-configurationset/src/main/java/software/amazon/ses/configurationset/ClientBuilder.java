package software.amazon.ses.configurationset;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.services.ses.SesClient;

class ClientBuilder {
    static SesClient getClient() {
        return SesClient.builder()
            .httpClient(LambdaWrapper.HTTP_CLIENT)
            .build();
    }
}
