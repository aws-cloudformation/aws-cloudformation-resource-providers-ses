package com.aws.ses.configurationset;

import com.aws.cfn.proxy.AmazonWebServicesClientProxy;
import com.aws.cfn.proxy.HandlerErrorCode;
import com.aws.cfn.proxy.Logger;
import com.aws.cfn.proxy.ProgressEvent;
import com.aws.cfn.proxy.OperationStatus;
import com.aws.cfn.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .errorCode(HandlerErrorCode.NotUpdatable)
            .status(OperationStatus.FAILED)
            .build();
    }
}
