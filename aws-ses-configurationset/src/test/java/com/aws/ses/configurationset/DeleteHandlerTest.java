package com.aws.ses.configurationset;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.cloudformation.exceptions.CfnNotFoundException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.ses.model.ConfigurationSet;
import software.amazon.awssdk.services.ses.model.ConfigurationSetDoesNotExistException;
import software.amazon.awssdk.services.ses.model.DeleteConfigurationSetResponse;
import software.amazon.awssdk.services.ses.model.DescribeConfigurationSetResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final DeleteHandler handler = new DeleteHandler();

        doReturn(DeleteConfigurationSetResponse.builder().build())
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .name("test-set")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackContext()).isNotNull();
        assertThat(response.getCallbackContext().getIsStabilization()).isTrue();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(5);
        assertThat(response.getResourceModel()).isEqualTo(model);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_FailedDelete_UnknownError() {
        final DeleteHandler handler = new DeleteHandler();

        // all Exceptions should be thrown so they can be handled by wrapper
        doThrow(SdkException.builder().message("test error").build())
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .name("test-set")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThrows(SdkException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }

    @Test
    public void handleRequest_FailedDelete_AmazonServiceException() {
        final DeleteHandler handler = new DeleteHandler();

        // AmazonServiceExceptions should be thrown so they can be handled by wrapper
        doThrow(new AmazonServiceException("test error"))
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .name("test-set")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThrows(AmazonServiceException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }

    @Test
    public void handleRequest_Stabilize() {
        final DeleteHandler handler = new DeleteHandler();

        final CallbackContext callbackContext = CallbackContext.builder()
            .isStabilization(true)
            .build();

        doThrow(ConfigurationSetDoesNotExistException.class)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .name("test-set")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, callbackContext, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_StabilizeAgain() {
        final DeleteHandler handler = new DeleteHandler();

        final CallbackContext callbackContext = CallbackContext.builder()
            .isStabilization(true)
            .build();

        final ConfigurationSet set = ConfigurationSet.builder().name("test-set").build();
        final DescribeConfigurationSetResponse describeResponse = DescribeConfigurationSetResponse.builder()
            .configurationSet(set)
            .build();

        // stabilize check finds set still not deleted, so retries
        doReturn(describeResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .name("test-set")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, callbackContext, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackContext()).isEqualTo(callbackContext);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(5);
        assertThat(response.getResourceModel()).isEqualTo(model);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_NonExistingSuccess() {
        final DeleteHandler handler = new DeleteHandler();

        doThrow(ConfigurationSetDoesNotExistException.class)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .name("test-set")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThrows(CfnNotFoundException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }
}
