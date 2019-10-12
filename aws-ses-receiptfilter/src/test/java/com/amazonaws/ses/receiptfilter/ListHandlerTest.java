package com.amazonaws.ses.receiptfilter;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ses.model.ListReceiptFiltersResponse;
import software.amazon.awssdk.services.ses.model.ReceiptFilter;
import software.amazon.awssdk.services.ses.model.ReceiptIpFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.amazonaws.ses.receiptfilter.Matchers.assertThatModelsAreEqual;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Test
    public void handleRequest_SimpleSuccess() {
        final ListHandler handler = new ListHandler();
        final ReceiptFilter filter1 = ReceiptFilter.builder()
                .ipFilter(ReceiptIpFilter.builder().cidr("10.0.0.1/24").policy("Allow").build())
                .name("test1")
                .build();
        final ReceiptFilter filter2 = ReceiptFilter.builder()
                .ipFilter(ReceiptIpFilter.builder().cidr("192.0.2.128/26").policy("Block").build())
                .name("test2")
                .build();
        List<ReceiptFilter> receiptFilters = Stream.of(filter1, filter2).collect(Collectors.toCollection(ArrayList::new));
        final ListReceiptFiltersResponse listResponse = ListReceiptFiltersResponse.builder().filters(receiptFilters).build();
        when(proxy.injectCredentialsAndInvokeV2(any(), any())).thenReturn(listResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getResourceModels()).hasSize(2);
        assertThatModelsAreEqual(response.getResourceModels().get(0), filter1);
        assertThatModelsAreEqual(response.getResourceModels().get(1), filter2);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
