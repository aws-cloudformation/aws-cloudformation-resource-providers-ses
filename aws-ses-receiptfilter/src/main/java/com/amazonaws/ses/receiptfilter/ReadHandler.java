package com.amazonaws.ses.receiptfilter;

import com.amazonaws.cloudformation.proxy.*;

import java.util.Objects;
import java.util.Optional;

public class ReadHandler extends BaseHandler<CallbackContext> {
    private AmazonWebServicesClientProxy proxy;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        this.proxy = proxy;
        final ResourceModel model = request.getDesiredResourceState();

        final String receiptFilterName = model.getFilter().getName();
        final ProgressEvent<ResourceModel, CallbackContext> listResult = new ListHandler().handleRequest(proxy, request, null, logger);
        final Optional<ResourceModel> receiptFilterModel = listResult.getResourceModels().stream()
                .filter(listModel -> listModel.getFilter().getName().equals(receiptFilterName))
                .findFirst();
        if (!receiptFilterModel.isPresent()) {
            final String errorMessage = "A requested resource was not found";
            logger.log(String.format(errorMessage + " of type  '%s' with identifier '%s'", ResourceModel.TYPE_NAME, receiptFilterName));
            return ProgressEvent.failed(null, null, HandlerErrorCode.NotFound, errorMessage);
        }
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(receiptFilterModel.get())
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
