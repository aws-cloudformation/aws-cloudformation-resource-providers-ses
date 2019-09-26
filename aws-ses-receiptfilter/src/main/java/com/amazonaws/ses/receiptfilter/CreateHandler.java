package com.amazonaws.ses.receiptfilter;

import com.amazonaws.cloudformation.exceptions.ResourceAlreadyExistsException;
import com.amazonaws.cloudformation.exceptions.ResourceNotFoundException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import com.amazonaws.cloudformation.resource.IdentifierUtils;
import com.amazonaws.util.StringUtils;
import lombok.NonNull;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.AlreadyExistsException;
import software.amazon.awssdk.services.ses.model.CreateReceiptFilterRequest;

public class CreateHandler extends BaseHandler<CallbackContext> {
    private AmazonWebServicesClientProxy proxy;
    private SesClient client;
    private Logger logger;
    public static final int MAX_LENGTH_RECEIPTFILTER_NAME = 64;

    @Override
    public ProgressEvent<ResourceModel,
            CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        this.proxy = proxy;
        this.client = ClientBuilder.getClient();
        this.logger = logger;

        if (callbackContext != null && callbackContext.getStabilization()) {
            return stabilizeReceiptFilter(proxy, callbackContext, request);
        } else {
            return createReceiptFilter(proxy, request);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> createReceiptFilter(final @NonNull AmazonWebServicesClientProxy proxy,
                                                                              final @NonNull ResourceHandlerRequest<ResourceModel> request) {
        final ResourceModel model = request.getDesiredResourceState();
        // resource can auto-generate a name if not supplied by caller
        // this logic should move up into the CloudFormation engine, but
        // currently exists here for backwards-compatibility with existing models
        if (StringUtils.isNullOrEmpty(model.getFilter().getName())) {
            model.getFilter().setName(
                    IdentifierUtils.generateResourceIdentifier(
                            request.getLogicalResourceIdentifier(),
                            request.getClientRequestToken(),
                            MAX_LENGTH_RECEIPTFILTER_NAME
                    )
            );
        }
        final String receiptFilterName = model.getFilter().getName();
        try {
            final CreateReceiptFilterRequest createReceiptFilterRequest = CreateReceiptFilterRequest.builder()
                    .filter(Translator.translate(model.getFilter()))
                    .build();

            //API Documentation : https://docs.aws.amazon.com/ses/latest/APIReference/API_CreateReceiptFilter.html
            this.proxy.injectCredentialsAndInvokeV2(createReceiptFilterRequest, this.client::createReceiptFilter);
            logger.log(String.format("%s [%s] created successfully", ResourceModel.TYPE_NAME, receiptFilterName));
        } catch (AlreadyExistsException e) {
            throw new ResourceAlreadyExistsException(ResourceModel.TYPE_NAME, receiptFilterName);
        }
        CallbackContext stabilizationContext = CallbackContext.builder()
                .stabilization(true)
                .build();
        return ProgressEvent.defaultInProgressHandler(
                stabilizationContext,
                5,
                model);
    }

    private ProgressEvent<ResourceModel, CallbackContext> stabilizeReceiptFilter(final @NonNull AmazonWebServicesClientProxy proxy,
                                                                                 final @NonNull CallbackContext callbackContext,
                                                                                 final @NonNull ResourceHandlerRequest<ResourceModel> request) {
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
