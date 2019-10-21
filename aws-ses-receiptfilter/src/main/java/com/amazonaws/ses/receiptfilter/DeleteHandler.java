package com.amazonaws.ses.receiptfilter;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import lombok.NonNull;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.DeleteReceiptFilterRequest;

public class DeleteHandler extends BaseHandler<CallbackContext> {
    private AmazonWebServicesClientProxy proxy;
    private SesClient client;
    private Logger logger;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        this.proxy = proxy;
        this.client = ClientBuilder.getClient();
        this.logger = logger;
        return deleteReceiptFilter(request);
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteReceiptFilter(final ResourceHandlerRequest<ResourceModel> request) {
        final ResourceModel model = request.getDesiredResourceState();
        final String receiptFilterName = model.getFilter().getName();

        //check if resource exists
        new ReadHandler().handleRequest(proxy, request, null, this.logger);

        DeleteReceiptFilterRequest deleteReceiptFilterRequest = DeleteReceiptFilterRequest.builder()
                .filterName(receiptFilterName)
                .build();

        // API Documentation - https://docs.aws.amazon.com/ses/latest/APIReference/API_DeleteReceiptFilter.html
        this.proxy.injectCredentialsAndInvokeV2(deleteReceiptFilterRequest, this.client::deleteReceiptFilter);
        logger.log(String.format("%s [%s] deleted successfully", ResourceModel.TYPE_NAME, receiptFilterName));

        return ProgressEvent.defaultSuccessHandler(null);
    }
}
