package com.aws.ses.configurationset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.Arrays;
import java.util.List;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.ses.model.ConfigurationSet;
import software.amazon.awssdk.services.ses.model.ListConfigurationSetsResponse;

@ExtendWith(MockitoExtension.class)
class ListHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Test
    void testSuccessState() {
        // Prepare inputs
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(ResourceModel.builder().build())
                .build();
        final List<ConfigurationSet> configurationSets = Arrays.asList(
                ConfigurationSet.builder().name("configSet1").build(),
                ConfigurationSet.builder().name("configSet2").build()
        );

        // Mock
        doReturn(
                ListConfigurationSetsResponse.builder()
                        .nextToken("nextToken")
                        .configurationSets(configurationSets)
                        .build())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(), any());

        // Call
        final ProgressEvent<ResourceModel, CallbackContext> response
                = new ListHandler().handleRequest(proxy, request, null, logger);

        // Assert
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels().size()).isEqualTo(configurationSets.size());
        assertThat(response.getResourceModels().get(0).getName()).isEqualTo(configurationSets.get(0).name());
        assertThat(response.getResourceModels().get(1).getName()).isEqualTo(configurationSets.get(1).name());
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    void testThrowable() {
        // Prepare inputs
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(ResourceModel.builder().build())
                .build();

        // Mock
        doThrow(new RuntimeException())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(), any());

        // Call
        assertThrows(RuntimeException.class, () ->
                new ListHandler().handleRequest(proxy, request, null, logger));
    }
}
