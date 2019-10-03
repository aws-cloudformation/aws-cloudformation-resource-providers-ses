package com.amazonaws.ses.configurationseteventdestination;

import com.amazonaws.cloudformation.exceptions.ResourceNotFoundException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandler<CallbackContext> {

    private Logger logger;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();
        final String configurationSetName = model.getConfigurationSetName();
        final String eventDestinationName = model.getEventDestination().getName();

        final ProgressEvent<ResourceModel, CallbackContext> listResult = new ListHandler().handleRequest(proxy, request, null, logger);


        return listResult.getResourceModels()
                .stream()
                .filter(c -> eventDestinationName.equals(c.getEventDestination().getName()))
                .findFirst()
                .map(ProgressEvent::<ResourceModel, CallbackContext>defaultSuccessHandler)
                .orElseGet(() -> {
                    throw new ResourceNotFoundException(ResourceModel.TYPE_NAME, configurationSetName + ":" + eventDestinationName);
                });
    }
}
