package com.amazonaws.ses.receiptfilter;

import com.amazonaws.cloudformation.exceptions.ResourceNotFoundException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();

        final String receiptFilterName = model.getFilter().getName();
        final ProgressEvent<ResourceModel, CallbackContext> listResult = new ListHandler().handleRequest(proxy, request, null, logger);

        return listResult.getResourceModels()
                .stream()
                .filter(listModel -> receiptFilterName.equals(listModel.getFilter().getName()))
                .findFirst()
                .map(ProgressEvent::<ResourceModel, CallbackContext>defaultSuccessHandler)
                .orElseGet(() -> {
                    throw new ResourceNotFoundException(ResourceModel.TYPE_NAME, receiptFilterName);
                });
    }
}
