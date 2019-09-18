package com.amazonaws.ses.receiptfilter;

import com.amazonaws.cloudformation.proxy.*;
import lombok.NonNull;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.DeleteReceiptFilterRequest;
import software.amazon.awssdk.services.ses.model.DeleteReceiptFilterResponse;

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
            return stabilizeReceiptFilter(proxy, callbackContext, request);
        } else {
            return deleteReceiptFilter(proxy, request);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteReceiptFilter(final @NonNull AmazonWebServicesClientProxy proxy,
                                                                              final @NonNull ResourceHandlerRequest<ResourceModel> request) {
        final ResourceModel model = request.getDesiredResourceState();
        final String receiptFilterName = model.getFilter().getName();

        //check if resource exists
        final ProgressEvent<ResourceModel, CallbackContext> readResult =
                new ReadHandler().handleRequest(proxy, request, null, this.logger);
        if (readResult.isFailed() && readResult.getErrorCode().equals(HandlerErrorCode.NotFound)) {
            final String errorMessage = "A requested resource was not found";
            logger.log(String.format(errorMessage + " of type  '%s' with identifier '%s'", ResourceModel.TYPE_NAME, receiptFilterName));
            return ProgressEvent.failed(null, null, HandlerErrorCode.NotFound, errorMessage);
        }

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

    private ProgressEvent<ResourceModel, CallbackContext> stabilizeReceiptFilter(final @NonNull AmazonWebServicesClientProxy proxy,
                                                                                 final @NonNull CallbackContext callbackContext,
                                                                                 final @NonNull ResourceHandlerRequest<ResourceModel> request) {
        ResourceModel model = request.getDesiredResourceState();
        final ProgressEvent<ResourceModel, CallbackContext> readResult =
                new ReadHandler().handleRequest(proxy, request, null, this.logger);

        if (readResult.isFailed() && readResult.getErrorCode().equals(HandlerErrorCode.NotFound)) {
            return ProgressEvent.defaultSuccessHandler(null);
        }
        if (readResult.isSuccess()) {
            //resource still exists, re-invoke delete
        }
        return ProgressEvent.defaultInProgressHandler(
                callbackContext,
                5,
                model);
    }
}
