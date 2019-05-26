package com.aws.ses.configurationset;

import com.aws.cfn.proxy.AmazonWebServicesClientProxy;
import com.aws.cfn.proxy.HandlerErrorCode;
import com.aws.cfn.proxy.Logger;
import com.aws.cfn.proxy.ProgressEvent;
import com.aws.cfn.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.ConfigurationSetDoesNotExistException;
import software.amazon.awssdk.services.ses.model.DeleteConfigurationSetRequest;

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

        if (callbackContext != null && callbackContext.getIsStabilization()) {
            return stabilizeConfigurationSet(proxy, callbackContext, request);
        } else {
            return deleteConfigurationSet(proxy, request);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteConfigurationSet(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request) {

        final ResourceModel model = request.getDesiredResourceState();

        try {
            final DeleteConfigurationSetRequest deleteConfigurationSetRequest = DeleteConfigurationSetRequest.builder()
                .configurationSetName(model.getName())
                .build();
            proxy.injectCredentialsAndInvokeV2(deleteConfigurationSetRequest, this.client::deleteConfigurationSet);
            logger.log(String.format("SES Configuration Set with Name [%s] deleted successfully", model.getName()));
        } catch (ConfigurationSetDoesNotExistException e) {
            logger.log(String.format("SES Configuration Set with Name [%s] is already deleted", model.getName()));
            return new ProgressEvent<ResourceModel, CallbackContext>().defaultSuccessHandler(null);
        } catch (Exception e) {
            this.logger.log("An error occurred deleting an SES Configuration Set: " + e.toString());
            return new ProgressEvent<ResourceModel, CallbackContext>().defaultFailureHandler(
                e,
                HandlerErrorCode.ServiceException);
        }

        CallbackContext stabilizationContext = CallbackContext.builder()
            .isStabilization(true)
            .build();
        return new ProgressEvent<ResourceModel, CallbackContext>().defaultInProgressHandler(
            stabilizationContext,
            1,
            model);
    }

    private ProgressEvent<ResourceModel, CallbackContext> stabilizeConfigurationSet(
        final AmazonWebServicesClientProxy proxy,
        final CallbackContext callbackContext,
        final ResourceHandlerRequest<ResourceModel> request) {
        ResourceModel model = request.getDesiredResourceState();

        // read to ensure resource no longer exists
        try {
            new ReadHandler().handleRequest(proxy, request, null, this.logger);
        } catch (final ConfigurationSetDoesNotExistException e) {
            return new ProgressEvent<ResourceModel, CallbackContext>().defaultSuccessHandler(null);
        } catch (final Exception e) {
            this.logger.log("An error occurred stabilizing an SES Configuration Set: " + e.toString());
            return new ProgressEvent<ResourceModel, CallbackContext>().defaultFailureHandler(
                e,
                HandlerErrorCode.ServiceException);
        }

        return new ProgressEvent<ResourceModel, CallbackContext>().defaultInProgressHandler(
            callbackContext,
            1,
            model);
    }
}
