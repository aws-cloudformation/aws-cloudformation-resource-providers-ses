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
import software.amazon.awssdk.services.ses.model.ConfigurationSet;
import software.amazon.awssdk.services.ses.model.ConfigurationSetDoesNotExistException;
import software.amazon.awssdk.services.ses.model.CreateConfigurationSetResponse;
import software.amazon.awssdk.services.ses.model.DescribeConfigurationSetResponse;

import java.util.UUID;

import static com.aws.ses.configurationset.Matchers.assertThatModelsAreEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class CreateHandlerTest {

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
        final CreateHandler handler = new CreateHandler();

        final CreateConfigurationSetResponse createResponse = CreateConfigurationSetResponse.builder()
            .build();

        // throw for pre-describe and then return response for create
        doThrow(ConfigurationSetDoesNotExistException.class)
        .doReturn(createResponse)
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

        assertThat(response, is(not(nullValue())));
        assertThat(response.getStatus(), is(equalTo(OperationStatus.IN_PROGRESS)));
        assertThat(response.getCallbackContext(), is(not(nullValue())));
        assertThat(response.getCallbackContext().getIsStabilization(), is(true));
        assertThat(response.getCallbackDelaySeconds(), is(equalTo(5)));
        assertThat(response.getResourceModels(), is(nullValue()));
        assertThat(response.getResourceModel(), is(equalTo(model)));
        assertThat(response.getMessage(), is(nullValue()));
        assertThat(response.getErrorCode(), is(nullValue()));
    }

    @Test(expected = SdkException.class)
    public void test_HandleRequest_FailedCreate_UnknownError() {
        final CreateHandler handler = new CreateHandler();

        // throw for pre-describe and then throw arbitrary error which should propagate to be handled by wrapper
        doThrow(ConfigurationSetDoesNotExistException.class)
        .doThrow(SdkException.builder().message("test error").build())
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

        handler.handleRequest(proxy, request, null, logger);
    }

    @Test(expected = AmazonServiceException.class)
    public void test_HandleRequest_FailedCreate_AmazonServiceException() {
        final CreateHandler handler = new CreateHandler();

        // AmazonServiceExceptions should be thrown so they can be handled by wrapper
        doThrow(ConfigurationSetDoesNotExistException.class)
            .doThrow(new AmazonServiceException("test error"))
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

        handler.handleRequest(proxy, request, null, logger);
    }

    @Test
    public void test_HandleRequest_Stabilize() {
        final CreateHandler handler = new CreateHandler();

        final CallbackContext callbackContext = CallbackContext.builder()
            .isStabilization(true)
            .build();

        final ConfigurationSet set = ConfigurationSet.builder().name("test-set").build();
        final DescribeConfigurationSetResponse describeResponse = DescribeConfigurationSetResponse.builder()
            .configurationSet(set)
            .build();

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

        assertThat(response, is(not(nullValue())));
        assertThat(response.getStatus(), is(equalTo(OperationStatus.SUCCESS)));
        assertThat(response.getCallbackContext(), is(nullValue()));
        assertThat(response.getCallbackDelaySeconds(), is(equalTo(0)));
        assertThat(response.getResourceModels(), is(nullValue()));
        assertThatModelsAreEqual(response.getResourceModel(), set);
        assertThat(response.getMessage(), is(nullValue()));
        assertThat(response.getErrorCode(), is(nullValue()));
    }

    @Test
    public void test_HandleRequest_FailedPreExisting() {
        final CreateHandler handler = new CreateHandler();

        final ConfigurationSet set = ConfigurationSet.builder().name("test-set").build();
        final DescribeConfigurationSetResponse describeResponse = DescribeConfigurationSetResponse.builder()
            .configurationSet(set)
            .build();

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

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response, is(not(nullValue())));
        assertThat(response.getStatus(), is(equalTo(OperationStatus.FAILED)));
        assertThat(response.getCallbackContext(), is(nullValue()));
        assertThat(response.getCallbackDelaySeconds(), is(equalTo(0)));
        assertThat(response.getResourceModel(), is(nullValue()));
        assertThat(response.getResourceModels(), is(nullValue()));
        assertThat(response.getMessage(), is(equalTo("Resource already exits.")));
        assertThat(response.getErrorCode(), is(equalTo(HandlerErrorCode.AlreadyExists)));
    }

    @Test
    public void test_HandleRequest_WithGeneratedName() {
        final CreateHandler handler = new CreateHandler();

        final CreateConfigurationSetResponse createResponse = CreateConfigurationSetResponse.builder()
            .build();

        // throw for pre-describe and then return response for create
        doThrow(ConfigurationSetDoesNotExistException.class)
            .doReturn(createResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        // no name supplied; should be generated
        final ResourceModel model = ResourceModel.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .clientRequestToken(UUID.randomUUID().toString())
            .logicalResourceIdentifier("myConfigurationSet")
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response, is(not(nullValue())));
        assertThat(response.getStatus(), is(equalTo(OperationStatus.IN_PROGRESS)));
        assertThat(response.getCallbackContext(), is(not(nullValue())));
        assertThat(response.getCallbackContext().getIsStabilization(), is(true));
        assertThat(response.getCallbackDelaySeconds(), is(equalTo(5)));
        assertThat(response.getResourceModel(), is(not(nullValue())));
        assertThat(response.getResourceModels(), is(nullValue()));
        assertThat(response.getMessage(), is(nullValue()));
        assertThat(response.getErrorCode(), is(nullValue()));

        ResourceModel outModel = response.getResourceModel();
        assertThat(outModel.getName(), startsWith("myConfigurationSet"));
    }
}
