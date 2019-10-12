package com.amazonaws.ses.receiptfilter;

import com.amazonaws.cloudformation.exceptions.ResourceAlreadyExistsException;
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
import software.amazon.awssdk.services.ses.model.AlreadyExistsException;
import software.amazon.awssdk.services.ses.model.CreateReceiptFilterRequest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;

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
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel()).isEqualTo(model);
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

        assertThrows(ResourceAlreadyExistsException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
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
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel()).isEqualTo(model);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        ResourceModel outModel = response.getResourceModel();
        assertThat(outModel.getFilter().getName()).startsWith("myReceiptFilter");
    }
}
