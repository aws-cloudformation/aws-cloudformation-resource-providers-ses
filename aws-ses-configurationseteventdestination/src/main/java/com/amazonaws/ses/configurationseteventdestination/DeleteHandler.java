package com.amazonaws.ses.configurationseteventdestination;

import com.amazonaws.cloudformation.exceptions.ResourceNotFoundException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import lombok.NonNull;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.ConfigurationSetDoesNotExistException;
import software.amazon.awssdk.services.ses.model.DeleteConfigurationSetEventDestinationRequest;
import software.amazon.awssdk.services.ses.model.EventDestinationDoesNotExistException;

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
            return ResourceStabilizer.deleteStabilization(proxy, request, callbackContext, logger);
        } else {
            return deleteConfigurationSetEventDestination(request);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteConfigurationSetEventDestination(@NonNull final ResourceHandlerRequest<ResourceModel> request) {
        final ResourceModel model = request.getDesiredResourceState();
        final String configurationSetName = model.getConfigurationSetName();
        final String eventDestinationName = model.getEventDestination().getName();
        try {
            final DeleteConfigurationSetEventDestinationRequest deleteConfigurationSetEventDestinationRequest = DeleteConfigurationSetEventDestinationRequest.builder()
                    .configurationSetName(model.getConfigurationSetName())
                    .eventDestinationName(model.getEventDestination().getName())
                    .build();
            proxy.injectCredentialsAndInvokeV2(deleteConfigurationSetEventDestinationRequest, this.client::deleteConfigurationSetEventDestination);
            logger.log(String.format("%s [%s] Delete initiated", ResourceModel.TYPE_NAME, configurationSetName + ":" + eventDestinationName));
        } catch (final ConfigurationSetDoesNotExistException e) {
            throw new ResourceNotFoundException(ResourceModel.TYPE_NAME, configurationSetName);
        } catch (final EventDestinationDoesNotExistException e) {
            throw new ResourceNotFoundException(ResourceModel.TYPE_NAME, eventDestinationName);
        }

        final CallbackContext stabilizationContext = CallbackContext.builder()
                .stabilization(true)
                .build();
        return ProgressEvent.defaultInProgressHandler(
                stabilizationContext,
                Constants.CALLBACK_DELAY_SECONDS,
                model);
    }
}
