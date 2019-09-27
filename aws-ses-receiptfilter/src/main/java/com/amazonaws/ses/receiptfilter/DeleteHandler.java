package com.amazonaws.ses.receiptfilter;

import com.amazonaws.cloudformation.exceptions.ResourceNotFoundException;
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

        if (callbackContext != null && callbackContext.getStabilization()) {
            return stabilizeReceiptFilter(callbackContext, request);
        } else {
            return deleteReceiptFilter(request);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteReceiptFilter(final @NonNull ResourceHandlerRequest<ResourceModel> request) {
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

        CallbackContext stabilizationContext = CallbackContext.builder()
                .stabilization(true)
                .build();
        return ProgressEvent.defaultInProgressHandler(
                stabilizationContext,
                5,
                model);
    }

    private ProgressEvent<ResourceModel, CallbackContext> stabilizeReceiptFilter(final @NonNull CallbackContext callbackContext,
                                                                                 final @NonNull ResourceHandlerRequest<ResourceModel> request) {
        ResourceModel model = request.getDesiredResourceState();
        try {
            new ReadHandler().handleRequest(proxy, request, null, this.logger);
        } catch (final ResourceNotFoundException e) {
            return ProgressEvent.defaultSuccessHandler(null);
        }
        return ProgressEvent.defaultInProgressHandler(
                callbackContext,
                5,
                model);
    }
}
