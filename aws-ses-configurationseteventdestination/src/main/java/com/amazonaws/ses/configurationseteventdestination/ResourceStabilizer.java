package com.amazonaws.ses.configurationseteventdestination;

import com.amazonaws.cloudformation.exceptions.ResourceNotFoundException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import lombok.NonNull;

public class ResourceStabilizer {

    public static ProgressEvent<ResourceModel, CallbackContext> createStabilization(@NonNull final AmazonWebServicesClientProxy proxy,
                                                                                    @NonNull final ResourceHandlerRequest<ResourceModel> request,
                                                                                    @NonNull final CallbackContext callbackContext,
                                                                                    @NonNull final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();
        // read to ensure resource exists
        try {
            final ProgressEvent<ResourceModel, CallbackContext> readResult =
                    new ReadHandler().handleRequest(proxy, request, null, logger);
            return ProgressEvent.defaultSuccessHandler(readResult.getResourceModel());
        } catch (final ResourceNotFoundException e) {
            // resource not yet found, re-invoke
        }
        return ProgressEvent.defaultInProgressHandler(
                callbackContext,
                Constants.CALLBACK_DELAY_SECONDS,
                model);
    }

    public static ProgressEvent<ResourceModel, CallbackContext> deleteStabilization(@NonNull final AmazonWebServicesClientProxy proxy,
                                                                                    @NonNull final ResourceHandlerRequest<ResourceModel> request,
                                                                                    @NonNull final CallbackContext callbackContext,
                                                                                    @NonNull final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();
        // read to ensure resource no longer exists
        try {
            new ReadHandler().handleRequest(proxy, request, null, logger);
        } catch (final ResourceNotFoundException e) {
            //resource deleted
            return ProgressEvent.defaultSuccessHandler(null);
        }
        return ProgressEvent.defaultInProgressHandler(
                callbackContext,
                Constants.CALLBACK_DELAY_SECONDS,
                model);
    }

}
