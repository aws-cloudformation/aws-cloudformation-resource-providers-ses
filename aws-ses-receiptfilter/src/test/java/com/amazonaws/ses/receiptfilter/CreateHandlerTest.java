package com.amazonaws.ses.receiptfilter;

import com.amazonaws.cloudformation.proxy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ses.model.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private ResourceModel model;
    private ResourceHandlerRequest<ResourceModel> request;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        model = ResourceModel.builder()
                .filter(Filter.builder()
                        .ipFilter(IpFilter.builder().cidr("10.0.0.1/24").policy("Allow").build())
                        .name("test1")
                        .build())
                .build();
        request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final CreateHandler handler = new CreateHandler();
        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackContext()).isNotNull();
        assertThat(response.getCallbackContext().getStabilization()).isTrue();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(5);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel()).isEqualTo(model);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_Stabilize() {
        final CreateHandler handler = new CreateHandler();
        final CallbackContext callbackContext = CallbackContext.builder()
                .stabilization(true)
                .build();
        final ReceiptFilter existingFilter = ReceiptFilter.builder()
                .ipFilter(ReceiptIpFilter.builder().cidr("10.0.0.1/24").policy("Allow").build())
                .name("test1")
                .build();
        final ListReceiptFiltersResponse listResponse = ListReceiptFiltersResponse.builder().filters(existingFilter).build();
        doReturn(listResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.any(ListReceiptFiltersRequest.class),
                        ArgumentMatchers.any()
                );

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, callbackContext, logger);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        Matchers.assertThatModelsAreEqual(response.getResourceModel(), existingFilter);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_FailedCreate_AlreadyExistingException() {
        final CreateHandler handler = new CreateHandler();
        doThrow(AlreadyExistsException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.any(CreateReceiptFilterRequest.class),
                        ArgumentMatchers.any()
                );

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.AlreadyExists);
    }

    @Test
    public void handleRequest_WithGeneratedName() {
        final CreateHandler handler = new CreateHandler();
        final ResourceModel model = ResourceModel.builder()
                .filter(Filter.builder()
                        .ipFilter(IpFilter.builder().cidr("10.0.0.1/24").policy("Allow").build())
                        .build())
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .clientRequestToken(UUID.randomUUID().toString())
                .logicalResourceIdentifier("myReceiptFilter")
                .desiredResourceState(model)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackContext()).isNotNull();
        assertThat(response.getCallbackContext().getStabilization()).isTrue();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(5);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel()).isEqualTo(model);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        ResourceModel outModel = response.getResourceModel();
        assertThat(outModel.getFilter().getName()).startsWith("myReceiptFilter");
    }

    @Test
    public void handleRequest_StabilizeAgain() {
        final CreateHandler handler = new CreateHandler();
        final CallbackContext callbackContext = CallbackContext.builder()
                .stabilization(true)
                .build();
        final ListReceiptFiltersResponse listResponse = ListReceiptFiltersResponse.builder().build();
        doReturn(listResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.any(ListReceiptFiltersRequest.class),
                        ArgumentMatchers.any()
                );

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, callbackContext, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackContext()).isNotNull();
        assertThat(response.getCallbackContext().getStabilization()).isTrue();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(5);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel()).isEqualTo(model);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
