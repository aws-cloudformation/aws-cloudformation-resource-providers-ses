package com.aws.ses.configurationset;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.HandlerErrorCode;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.ses.model.ConfigurationSetDoesNotExistException;
import software.amazon.awssdk.services.ses.model.DeleteConfigurationSetResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class DeleteHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Before
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
    }

    @Test
    public void test_HandleRequest_SimpleSuccess() {
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

        final ProgressEvent response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response, is(not(nullValue())));
        assertThat(response.getStatus(), is(equalTo(OperationStatus.IN_PROGRESS)));
        assertThat(response.getCallbackContext(), is(not(nullValue())));
        assertThat(((CallbackContext)response.getCallbackContext()).getIsStabilization(), is(true));
        assertThat(response.getCallbackDelaySeconds(), is(equalTo(5)));
        assertThat(response.getResourceModel(), is(equalTo(model)));
        assertThat(response.getResourceModels(), is(nullValue()));
        assertThat(response.getMessage(), is(nullValue()));
        assertThat(response.getErrorCode(), is(nullValue()));
    }

    @Test(expected = SdkException.class)
    public void test_HandleRequest_FailedDelete_UnknownError() {
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

        final ProgressEvent response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response, is(not(nullValue())));
        assertThat(response.getStatus(), is(equalTo(OperationStatus.FAILED)));
        assertThat(response.getCallbackContext(), is(nullValue()));
        assertThat(response.getCallbackDelaySeconds(), is(equalTo(0)));
        assertThat(response.getResourceModel(), is(nullValue()));
        assertThat(response.getResourceModels(), is(nullValue()));
        assertThat(response.getMessage(), is(equalTo("test error")));
        assertThat(response.getErrorCode(), is(equalTo(HandlerErrorCode.InternalFailure)));
    }

    @Test(expected = AmazonServiceException.class)
    public void test_HandleRequest_FailedDelete_AmazonServiceException() {
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

        final ProgressEvent response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response, is(not(nullValue())));
        assertThat(response.getStatus(), is(equalTo(OperationStatus.FAILED)));
        assertThat(response.getCallbackContext(), is(nullValue()));
        assertThat(response.getCallbackDelaySeconds(), is(equalTo(0)));
        assertThat(response.getResourceModel(), is(nullValue()));
        assertThat(response.getResourceModels(), is(nullValue()));
        assertThat(response.getMessage(), is(equalTo("test error (Service: null; Status Code: 0; Error Code: null; Request ID: null)")));
        assertThat(response.getErrorCode(), is(equalTo(HandlerErrorCode.ServiceException)));
    }

    @Test
    public void test_HandleRequest_Stabilize() {
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

        final ProgressEvent response = handler.handleRequest(proxy, request, callbackContext, logger);

        assertThat(response, is(not(nullValue())));
        assertThat(response.getStatus(), is(equalTo(OperationStatus.SUCCESS)));
        assertThat(response.getCallbackContext(), is(nullValue()));
        assertThat(response.getCallbackDelaySeconds(), is(equalTo(0)));
        assertThat(response.getResourceModel(), is(nullValue()));
        assertThat(response.getResourceModels(), is(nullValue()));
        assertThat(response.getMessage(), is(nullValue()));
        assertThat(response.getErrorCode(), is(nullValue()));
    }

    @Test
    public void test_HandleRequest_NonExistingSuccess() {
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

        final ProgressEvent response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response, is(not(nullValue())));
        assertThat(response.getStatus(), is(equalTo(OperationStatus.SUCCESS)));
        assertThat(response.getCallbackContext(), is(nullValue()));
        assertThat(response.getCallbackDelaySeconds(), is(equalTo(0)));
        assertThat(response.getResourceModel(), is(nullValue()));
        assertThat(response.getResourceModels(), is(nullValue()));
        assertThat(response.getMessage(), is(nullValue()));
        assertThat(response.getErrorCode(), is(nullValue()));
    }
}
