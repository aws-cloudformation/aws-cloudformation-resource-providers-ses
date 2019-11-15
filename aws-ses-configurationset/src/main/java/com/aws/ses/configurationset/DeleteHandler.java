package com.aws.ses.configurationset;

import com.amazonaws.cloudformation.exceptions.CfnNotFoundException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.ConfigurationSetDoesNotExistException;
import software.amazon.awssdk.services.ses.model.DeleteConfigurationSetRequest;

import static com.aws.ses.configurationset.ResourceModelExtensions.getPrimaryIdentifier;

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

        return deleteResource(request.getDesiredResourceState());
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteResource(ResourceModel model) {
        deleteConfigurationSet(model);
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private void deleteConfigurationSet(final ResourceModel model) {
        final DeleteConfigurationSetRequest deleteConfigurationSetRequest =
                DeleteConfigurationSetRequest.builder()
                        .configurationSetName(model.getName())
                .build();
        try {
            proxy.injectCredentialsAndInvokeV2(deleteConfigurationSetRequest,
                    client::deleteConfigurationSet);
            logger.log(String.format("%s [%s] deleted successfully",
                    ResourceModel.TYPE_NAME, getPrimaryIdentifier(model).toString()));
        } catch (ConfigurationSetDoesNotExistException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, getPrimaryIdentifier(model).toString());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
