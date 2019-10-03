package com.amazonaws.ses.configurationseteventdestination;

import com.amazonaws.cloudformation.exceptions.ResourceNotFoundException;
import com.amazonaws.cloudformation.proxy.*;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

public class UpdateHandler extends BaseHandler<CallbackContext> {
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
            return stabilizeConfigurationSetEventDestination(callbackContext, request);
        } else {
            return updateConfigurationSetEventDestination(request);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateConfigurationSetEventDestination(ResourceHandlerRequest<ResourceModel> request) {
        final ResourceModel model = request.getDesiredResourceState();
        final String configurationSetName = model.getConfigurationSetName();
        final String eventDestinationName = model.getEventDestination().getName();

        try {
            UpdateConfigurationSetEventDestinationRequest updateConfigurationSetEventDestinationRequest =
                    UpdateConfigurationSetEventDestinationRequest.builder().configurationSetName(configurationSetName).eventDestination(Translator.translate(model.getEventDestination())).build();
            this.proxy.injectCredentialsAndInvokeV2(updateConfigurationSetEventDestinationRequest, this.client::updateConfigurationSetEventDestination);
            logger.log(String.format("%s [%s] updated successfully",
                    ResourceModel.TYPE_NAME, configurationSetName + ":" + eventDestinationName));
        } catch (ConfigurationSetDoesNotExistException e) {
            throw new ResourceNotFoundException(ResourceModel.TYPE_NAME, configurationSetName);
        } catch (EventDestinationDoesNotExistException e) {
            throw new ResourceNotFoundException(ResourceModel.TYPE_NAME, eventDestinationName);
        } catch (InvalidCloudWatchDestinationException | InvalidFirehoseDestinationException | InvalidSnsDestinationException | LimitExceededException e) {
            return ProgressEvent.defaultFailureHandler(e, HandlerErrorCode.InvalidRequest);
        }
        CallbackContext stabilizationContext = CallbackContext.builder()
                .stabilization(true)
                .build();
        return ProgressEvent.defaultInProgressHandler(
                stabilizationContext,
                5,
                model);
    }

    private ProgressEvent<ResourceModel, CallbackContext> stabilizeConfigurationSetEventDestination(CallbackContext callbackContext, ResourceHandlerRequest<ResourceModel> request) {
        final ResourceModel model = request.getDesiredResourceState();
        // read to ensure resource exists
        try {
            final ProgressEvent<ResourceModel, CallbackContext> readResult =
                    new ReadHandler().handleRequest(proxy, request, null, this.logger);
            return ProgressEvent.defaultSuccessHandler(readResult.getResourceModel());
        } catch (final ResourceNotFoundException e) {
            // resource not yet found, re-invoke
        }
        return ProgressEvent.defaultInProgressHandler(
                callbackContext,
                5,
                model);
    }
}
