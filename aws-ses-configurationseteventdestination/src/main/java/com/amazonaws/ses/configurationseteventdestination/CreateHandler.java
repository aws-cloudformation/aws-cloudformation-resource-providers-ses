package com.amazonaws.ses.configurationseteventdestination;

import com.amazonaws.cloudformation.exceptions.ResourceAlreadyExistsException;
import com.amazonaws.cloudformation.exceptions.ResourceNotFoundException;
import com.amazonaws.cloudformation.proxy.*;
import com.amazonaws.cloudformation.resource.IdentifierUtils;
import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

public class CreateHandler extends BaseHandler<CallbackContext> {
    private AmazonWebServicesClientProxy proxy;
    private SesClient client;
    private Logger logger;
    public static final int MAX_CONFIGURATION_SET_EVENT_DESTINATION_NAME = 64;

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
            return createConfigurationSetEventDestination(request);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> createConfigurationSetEventDestination(ResourceHandlerRequest<ResourceModel> request) {
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
                            MAX_CONFIGURATION_SET_EVENT_DESTINATION_NAME
                    )
            );
        }
        try {
            CreateConfigurationSetEventDestinationRequest createConfigurationSetEventDestinationRequest =
                    CreateConfigurationSetEventDestinationRequest.builder().configurationSetName(configurationSetName).eventDestination(Translator.translate(model.getEventDestination())).build();
            this.proxy.injectCredentialsAndInvokeV2(createConfigurationSetEventDestinationRequest, this.client::createConfigurationSetEventDestination);
            logger.log(String.format("%s [%s] created successfully",
                    ResourceModel.TYPE_NAME, configurationSetName + ":" + eventDestinationName));
        } catch (ConfigurationSetDoesNotExistException e) {
            throw new ResourceNotFoundException(ResourceModel.TYPE_NAME, configurationSetName);
        } catch (EventDestinationAlreadyExistsException e) {
            throw new ResourceAlreadyExistsException(ResourceModel.TYPE_NAME, configurationSetName + ":" + eventDestinationName);
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
