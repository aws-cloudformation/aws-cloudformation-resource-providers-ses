package com.amazonaws.ses.receiptfilter;

import com.amazonaws.cloudformation.proxy.*;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.ListReceiptFiltersRequest;
import software.amazon.awssdk.services.ses.model.ListReceiptFiltersResponse;
import software.amazon.awssdk.services.ses.model.ReceiptFilter;

import java.util.ArrayList;
import java.util.List;

public class ListHandler extends BaseHandler<CallbackContext> {
    private AmazonWebServicesClientProxy proxy;
    private SesClient client = ClientBuilder.getClient();

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        this.proxy = proxy;
        final List<ResourceModel> models = listReceiptFilters();
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private List<ResourceModel> listReceiptFilters() {
        final ListReceiptFiltersResponse response = this.proxy.injectCredentialsAndInvokeV2(ListReceiptFiltersRequest.builder().build(), this.client::listReceiptFilters);
        List<ResourceModel> models = new ArrayList<>();
        response.filters().forEach(f -> {
            final ResourceModel model = ResourceModel.builder().filter(createFilter(f)).build();
            models.add(model);
        });
        return models;
    }

    private Filter createFilter(ReceiptFilter receiptFilter) {
        final IpFilter ipFilter = IpFilter.builder()
                .cidr(receiptFilter.ipFilter().cidr())
                .policy(receiptFilter.ipFilter().policy().name()).build();
        return Filter.builder().ipFilter(ipFilter).name(receiptFilter.name()).build();
    }
}
