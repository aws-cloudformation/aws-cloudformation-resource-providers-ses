package com.aws.ses.configurationset;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.HandlerErrorCode;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.ConfigurationSet;
import software.amazon.awssdk.services.ses.model.ConfigurationSetAlreadyExistsException;
import software.amazon.awssdk.services.ses.model.ConfigurationSetDoesNotExistException;
import software.amazon.awssdk.services.ses.model.CreateConfigurationSetRequest;

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

        if (callbackContext != null && callbackContext.getIsStabilization()) {
            return stabilizeConfigurationSet(proxy, callbackContext, request);
        } else {
            return createConfigurationSet(proxy, request);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> createConfigurationSet(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request) {
        ResourceModel model = request.getDesiredResourceState();

        // pre-creation read to ensure no existing resource exists
        try {
            final ProgressEvent<ResourceModel, CallbackContext> readResult =
                new ReadHandler().handleRequest(proxy, request, null, this.logger);
            if (readResult.getResourceModel() != null &&
                readResult.getResourceModel().getName().equals(model.getName())) {

                this.logger.log(String.format("SES Configuration Set with Name [%s] already exists", model.getName()));
                return ProgressEvent.defaultFailureHandler(
                    ConfigurationSetAlreadyExistsException.builder()
                        .message("Resource already exits.")
                        .build(),
                    HandlerErrorCode.AlreadyExists);
            }
        } catch (final ConfigurationSetDoesNotExistException e) {
            // no existing resource, creation can proceed
        }

        try {
            final CreateConfigurationSetRequest createConfigurationSetRequest =
                CreateConfigurationSetRequest.builder()
                    .configurationSet(ConfigurationSet.builder()
                        .name(model.getName())
                        .build())
                    .build();
            proxy.injectCredentialsAndInvokeV2(createConfigurationSetRequest, this.client::createConfigurationSet);
            this.logger.log(String.format("SES Configuration Set with Name [%s] created successfully", model.getName
                                                                                                                 ()));
        } catch (final ConfigurationSetAlreadyExistsException e) {
            this.logger.log(String.format("SES Configuration Set with Name [%s] was already created", model.getName()));
            return ProgressEvent.defaultFailureHandler(
                // failing here would suggest a conflicting operation was performed out of band
                ConfigurationSetAlreadyExistsException.builder()
                    .message("Resource already exits.")
                    .build(),
                HandlerErrorCode.AlreadyExists);
        }

        CallbackContext stabilizationContext = CallbackContext.builder()
            .isStabilization(true)
            .build();
        return ProgressEvent.defaultInProgressHandler(
            stabilizationContext,
            5,
            model);
    }

    private ProgressEvent<ResourceModel, CallbackContext> stabilizeConfigurationSet(
        final AmazonWebServicesClientProxy proxy,
        final CallbackContext callbackContext,
        final ResourceHandlerRequest<ResourceModel> request) {
        ResourceModel model = request.getDesiredResourceState();

        // read to ensure resource exists
        try {
            final ProgressEvent<ResourceModel, CallbackContext> readResult =
                new ReadHandler().handleRequest(proxy, request, null, this.logger);
            return ProgressEvent.defaultSuccessHandler(readResult.getResourceModel());
        } catch (final ConfigurationSetDoesNotExistException e) {
            // resource not yet found, re-invoke
        }

        return ProgressEvent.defaultInProgressHandler(
            callbackContext,
            5,
            model);
    }
}
