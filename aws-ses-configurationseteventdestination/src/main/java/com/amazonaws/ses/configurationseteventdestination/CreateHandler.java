package com.amazonaws.ses.configurationseteventdestination;

import com.amazonaws.cloudformation.exceptions.ResourceAlreadyExistsException;
import com.amazonaws.cloudformation.exceptions.ResourceNotFoundException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.HandlerErrorCode;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import com.amazonaws.cloudformation.resource.IdentifierUtils;
import com.amazonaws.util.StringUtils;
import lombok.NonNull;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.ConfigurationSetDoesNotExistException;
import software.amazon.awssdk.services.ses.model.CreateConfigurationSetEventDestinationRequest;
import software.amazon.awssdk.services.ses.model.EventDestinationAlreadyExistsException;
import software.amazon.awssdk.services.ses.model.InvalidCloudWatchDestinationException;
import software.amazon.awssdk.services.ses.model.InvalidFirehoseDestinationException;
import software.amazon.awssdk.services.ses.model.InvalidSnsDestinationException;
import software.amazon.awssdk.services.ses.model.LimitExceededException;

public class CreateHandler extends BaseHandler<CallbackContext> {
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
            return ResourceStabilizer.createStabilization(proxy, request, callbackContext, logger);
        } else {
            return createConfigurationSetEventDestination(request);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> createConfigurationSetEventDestination(@NonNull final ResourceHandlerRequest<ResourceModel> request) {
        final ResourceModel model = request.getDesiredResourceState();
        final String configurationSetName = model.getConfigurationSetName();
        final String eventDestinationName = model.getEventDestination().getName();

        // resource can auto-generate a name if not supplied by caller
        // this logic should move up into the CloudFormation engine, but
        // currently exists here for backwards-compatibility with existing models
        if (StringUtils.isNullOrEmpty(eventDestinationName)) {
            model.getEventDestination().setName(
                    IdentifierUtils.generateResourceIdentifier(
                            request.getLogicalResourceIdentifier(),
                            request.getClientRequestToken(),
                            Constants.MAX_CONFIGURATION_SET_EVENT_DESTINATION_NAME
                    )
            );
        }
        try {
            final CreateConfigurationSetEventDestinationRequest createConfigurationSetEventDestinationRequest =
                    CreateConfigurationSetEventDestinationRequest.builder().configurationSetName(configurationSetName).eventDestination(Translator.translate(model.getEventDestination())).build();
            this.proxy.injectCredentialsAndInvokeV2(createConfigurationSetEventDestinationRequest, this.client::createConfigurationSetEventDestination);
            logger.log(String.format("%s [%s] Create initiated", ResourceModel.TYPE_NAME, configurationSetName + ":" + eventDestinationName));
        } catch (final ConfigurationSetDoesNotExistException e) {
            throw new ResourceNotFoundException(ResourceModel.TYPE_NAME, configurationSetName);
        } catch (final EventDestinationAlreadyExistsException e) {
            throw new ResourceAlreadyExistsException(ResourceModel.TYPE_NAME, configurationSetName + ":" + eventDestinationName);
        } catch (final InvalidCloudWatchDestinationException | InvalidFirehoseDestinationException | InvalidSnsDestinationException | LimitExceededException e) {
            return ProgressEvent.defaultFailureHandler(e, HandlerErrorCode.InvalidRequest);
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
