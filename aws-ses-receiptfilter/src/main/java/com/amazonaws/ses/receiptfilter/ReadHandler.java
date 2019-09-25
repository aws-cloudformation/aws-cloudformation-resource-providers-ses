package com.amazonaws.ses.receiptfilter;

import com.amazonaws.cloudformation.exceptions.ResourceNotFoundException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;

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
            throw new ResourceNotFoundException(ResourceModel.TYPE_NAME, receiptFilterName);
        }
        return ProgressEvent.defaultSuccessHandler(receiptFilterModel.get());
    }
}
