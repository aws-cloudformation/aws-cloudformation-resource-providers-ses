package com.aws.ses.configurationset;

import com.amazonaws.cloudformation.exceptions.CfnAlreadyExistsException;
import com.amazonaws.cloudformation.exceptions.CfnInvalidRequestException;
import com.amazonaws.cloudformation.exceptions.CfnServiceLimitExceededException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.ses.model.ConfigurationSetAlreadyExistsException;
import software.amazon.awssdk.services.ses.model.CreateConfigurationSetResponse;
import software.amazon.awssdk.services.ses.model.InvalidConfigurationSetException;
import software.amazon.awssdk.services.ses.model.LimitExceededException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class CreateHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Test
    void testSuccessState() {
        // Prepare inputs
        final String configSetName = "myConfigSet";
        final ResourceModel resourceModel = ResourceModel.builder().name(configSetName).build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        // Mock
        doReturn(CreateConfigurationSetResponse.builder().build())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(), any());

        // Call
        final ProgressEvent<ResourceModel, CallbackContext> response
                = new CreateHandler().handleRequest(proxy, request, null, logger);

        // Assert
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel().getName()).isEqualTo(configSetName);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    void testSuccessStateWithGeneratedName() {
        // Prepare inputs
        final String logicalResourceId = "myConfigSet";
        final ResourceModel resourceModel = ResourceModel.builder().build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken(UUID.randomUUID().toString())
                .logicalResourceIdentifier(logicalResourceId)
                .desiredResourceState(resourceModel)
                .build();

        // Mock
        doReturn(CreateConfigurationSetResponse.builder().build())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(), any());

        // Call
        final ProgressEvent<ResourceModel, CallbackContext> response
                = new CreateHandler().handleRequest(proxy, request, null, logger);

        // Assert
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel().getName()).startsWith(logicalResourceId);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    void testConfigurationSetAlreadyExistsException() {
        // Prepare inputs
        final ResourceModel resourceModel = ResourceModel.builder().name("name").build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        // Mock
        doThrow(ConfigurationSetAlreadyExistsException.builder().build())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(), any());

        // Call
        assertThrows(CfnAlreadyExistsException.class, () ->
                new CreateHandler().handleRequest(proxy, request, null, logger));
    }

    @Test
    void testInvalidConfigurationSetException() {
        // Prepare inputs
        final ResourceModel resourceModel = ResourceModel.builder().name("name").build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        // Mock
        doThrow(InvalidConfigurationSetException.builder().build())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(), any());

        // Call
        assertThrows(CfnInvalidRequestException.class, () ->
                new CreateHandler().handleRequest(proxy, request, null, logger));
    }

    @Test
    void testLimitExceededException() {
        // Prepare inputs
        final ResourceModel resourceModel = ResourceModel.builder().name("name").build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        // Mock
        doThrow(LimitExceededException.builder().build())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(), any());

        // Call
        assertThrows(CfnServiceLimitExceededException.class, () ->
                new CreateHandler().handleRequest(proxy, request, null, logger));
    }

    @Test
    void testThrowable() {
        // Prepare inputs
        final ResourceModel resourceModel = ResourceModel.builder().name("name").build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        // Mock
        doThrow(new RuntimeException())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(), any());

        // Call
        assertThrows(RuntimeException.class, () ->
                new CreateHandler().handleRequest(proxy, request, null, logger));
    }

}
