package com.aws.ses.configurationset;

import static com.aws.ses.configurationset.ResourceModelExtensions.getPrimaryIdentifier;

import com.amazonaws.cloudformation.exceptions.CfnNotUpdatableException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        throw new CfnNotUpdatableException(
                ResourceModel.TYPE_NAME,
                getPrimaryIdentifier(request.getDesiredResourceState()).toString()
        );
    }
}
