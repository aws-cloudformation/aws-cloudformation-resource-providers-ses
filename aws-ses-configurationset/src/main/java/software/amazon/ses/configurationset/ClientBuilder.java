package software.amazon.ses.configurationset;

import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.cloudformation.LambdaWrapper;

class ClientBuilder {
    static SesClient getClient() {
        return SesClient.builder()
            .httpClient(LambdaWrapper.HTTP_CLIENT)
            .build();
    }
}
