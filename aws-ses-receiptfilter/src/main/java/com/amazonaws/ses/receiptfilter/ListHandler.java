package com.amazonaws.ses.receiptfilter;

import com.amazonaws.cloudformation.proxy.*;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.ListReceiptFiltersRequest;
import software.amazon.awssdk.services.ses.model.ListReceiptFiltersResponse;

import java.util.List;
import java.util.stream.Collectors;

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
        /*  API documentation : https://docs.aws.amazon.com/ses/latest/APIReference/API_ListReceiptFilters.html
        This API does not support pagination */
        final ListReceiptFiltersResponse response = this.proxy.injectCredentialsAndInvokeV2(ListReceiptFiltersRequest.builder().build(), this.client::listReceiptFilters);
        return response.filters()
                .stream()
                .map(f -> ResourceModel.builder().filter(Translator.translate(f)).build())
                .collect(Collectors.toList());
    }
}
