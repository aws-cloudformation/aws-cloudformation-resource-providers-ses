package com.amazonaws.ses.receiptfilter;

import com.amazonaws.cloudformation.proxy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ses.model.ListReceiptFiltersRequest;
import software.amazon.awssdk.services.ses.model.ListReceiptFiltersResponse;
import software.amazon.awssdk.services.ses.model.ReceiptFilter;
import software.amazon.awssdk.services.ses.model.ReceiptIpFilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;
    private Filter filter;
    private ResourceModel model;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        filter = Filter.builder().name("test").build();
        model = ResourceModel.builder().filter(filter).build();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ReadHandler handler = new ReadHandler();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        final ReceiptFilter existingFilter = ReceiptFilter.builder()
                .ipFilter(ReceiptIpFilter.builder().cidr("10.0.0.1/24").policy("Allow").build())
                .name("test")
                .build();
        final ListReceiptFiltersResponse listResponse = ListReceiptFiltersResponse.builder().filters(existingFilter).build();
        doReturn(listResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.any(ListReceiptFiltersRequest.class),
                        ArgumentMatchers.any()
                );

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel().getFilter().getName()).isEqualTo(request.getDesiredResourceState().getFilter().getName());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_ResourceNotFound() {
        final ReadHandler handler = new ReadHandler();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        final ReceiptFilter existingFilter = ReceiptFilter.builder()
                .ipFilter(ReceiptIpFilter.builder().cidr("10.0.0.1/24").policy("Allow").build())
                .name("test1")
                .build();
        final ListReceiptFiltersResponse listResponse = ListReceiptFiltersResponse.builder().build();
        doReturn(listResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.any(ListReceiptFiltersRequest.class),
                        ArgumentMatchers.any()
                );

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
    }
}
