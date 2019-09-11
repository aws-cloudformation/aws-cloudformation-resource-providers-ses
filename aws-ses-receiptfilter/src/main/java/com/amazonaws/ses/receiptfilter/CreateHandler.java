package com.amazonaws.ses.receiptfilter;

import com.amazonaws.cloudformation.exceptions.ResourceAlreadyExistsException;
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

import java.util.Optional;

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
        try {
            final CreateReceiptFilterRequest createReceiptFilterRequest = CreateReceiptFilterRequest.builder()
                    .filter(Translator.translate(model.getFilter()))
                    .build();
            this.proxy.injectCredentialsAndInvokeV2(createReceiptFilterRequest, this.client::createReceiptFilter);
            logger.log(String.format("%s [%s] created successfully", ResourceModel.TYPE_NAME, model.getFilter().getName()));
        } catch (AlreadyExistsException e) {
            throw new ResourceAlreadyExistsException(ResourceModel.TYPE_NAME, model.getFilter().getName());
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
        final Optional<ResourceModel> readReceiptFilterResult = readReceiptFilter(proxy, request);
        if (readReceiptFilterResult.isPresent()) {
            return ProgressEvent.defaultSuccessHandler(readReceiptFilterResult.get());
        }
        return ProgressEvent.defaultInProgressHandler(
                callbackContext,
                5,
                model);
    }

    private Optional<ResourceModel> readReceiptFilter(final @NonNull AmazonWebServicesClientProxy proxy,
                                                      final @NonNull ResourceHandlerRequest<ResourceModel> request) {
        final ResourceModel model = request.getDesiredResourceState();
        final ProgressEvent<ResourceModel, CallbackContext> listResult = new ListHandler().handleRequest(proxy, request, null, this.logger);
        final Optional<ResourceModel> receiptFilterModel = listResult.getResourceModels().stream()
                .filter(listModel -> listModel.getFilter().getName().equals(model.getFilter().getName()))
                .findFirst();
        return receiptFilterModel;
    }
}
