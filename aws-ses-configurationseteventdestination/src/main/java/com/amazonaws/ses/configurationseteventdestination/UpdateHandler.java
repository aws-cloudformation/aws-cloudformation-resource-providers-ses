package com.amazonaws.ses.configurationseteventdestination;

import com.amazonaws.cloudformation.exceptions.ResourceNotFoundException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.HandlerErrorCode;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import lombok.NonNull;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.ConfigurationSetDoesNotExistException;
import software.amazon.awssdk.services.ses.model.EventDestinationDoesNotExistException;
import software.amazon.awssdk.services.ses.model.InvalidCloudWatchDestinationException;
import software.amazon.awssdk.services.ses.model.InvalidFirehoseDestinationException;
import software.amazon.awssdk.services.ses.model.InvalidSnsDestinationException;
import software.amazon.awssdk.services.ses.model.LimitExceededException;
import software.amazon.awssdk.services.ses.model.UpdateConfigurationSetEventDestinationRequest;

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

        return updateConfigurationSetEventDestination(request);

    }

    private ProgressEvent<ResourceModel, CallbackContext> updateConfigurationSetEventDestination(@NonNull final ResourceHandlerRequest<ResourceModel> request) {
        final ResourceModel model = request.getDesiredResourceState();
        final String configurationSetName = model.getConfigurationSetName();
        final String eventDestinationName = model.getEventDestination().getName();

        try {
            final UpdateConfigurationSetEventDestinationRequest updateConfigurationSetEventDestinationRequest =
                    UpdateConfigurationSetEventDestinationRequest.builder().configurationSetName(configurationSetName).eventDestination(Translator.translate(model.getEventDestination())).build();
            this.proxy.injectCredentialsAndInvokeV2(updateConfigurationSetEventDestinationRequest, this.client::updateConfigurationSetEventDestination);
            logger.log(String.format("%s [%s] Updated Successfully", ResourceModel.TYPE_NAME, configurationSetName + ":" + eventDestinationName));
        } catch (final ConfigurationSetDoesNotExistException e) {
            throw new ResourceNotFoundException(ResourceModel.TYPE_NAME, configurationSetName);
        } catch (final EventDestinationDoesNotExistException e) {
            throw new ResourceNotFoundException(ResourceModel.TYPE_NAME, eventDestinationName);
        } catch (final InvalidCloudWatchDestinationException | InvalidFirehoseDestinationException | InvalidSnsDestinationException | LimitExceededException e) {
            return ProgressEvent.defaultFailureHandler(e, HandlerErrorCode.InvalidRequest);
        }
        return ProgressEvent.defaultSuccessHandler(model);
    }
}
