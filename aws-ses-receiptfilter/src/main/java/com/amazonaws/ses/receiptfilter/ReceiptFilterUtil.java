package com.amazonaws.ses.receiptfilter;

import com.amazonaws.cloudformation.exceptions.ResourceNotFoundException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import lombok.Builder;
import lombok.NonNull;

import java.util.Optional;

@Builder
public class ReceiptFilterUtil {
    private Logger logger;

    /**
     * ReceiptFilter resource type does not support a read API.
     * This util method is a wrapper on the list API to fetch a ReceiptFilter instance given its name
     *
     * @param proxy
     * @param request
     * @return ResourceModel
     */
    public ResourceModel readReceiptFilter(final @NonNull AmazonWebServicesClientProxy proxy,
                                           final @NonNull ResourceHandlerRequest<ResourceModel> request) {
        final ResourceModel model = request.getDesiredResourceState();
        final String receiptFilterName = getPrimaryIdentifier(model);
        final ProgressEvent<ResourceModel, CallbackContext> listResult = new ListHandler().handleRequest(proxy, request, null, logger);
        final Optional<ResourceModel> receiptFilterModel = listResult.getResourceModels().stream()
                .filter(listModel -> listModel.getFilter().getName().equals(receiptFilterName))
                .findFirst();
        if (!receiptFilterModel.isPresent()) {
            throw new ResourceNotFoundException(ResourceModel.TYPE_NAME, receiptFilterName);
        }
        return receiptFilterModel.get();
    }

    public String getPrimaryIdentifier(ResourceModel model) {
        return model.getFilter().getName();
    }
}
