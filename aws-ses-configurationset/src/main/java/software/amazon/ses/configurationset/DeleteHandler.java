package software.amazon.ses.configurationset;

import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.ConfigurationSetDoesNotExistException;
import software.amazon.awssdk.services.ses.model.DeleteConfigurationSetRequest;

import static software.amazon.ses.configurationset.ResourceModelExtensions.getPrimaryIdentifier;

public class DeleteHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {
        final SesClient client = ClientBuilder.getClient();
        final ResourceModel model = request.getDesiredResourceState();

        try {
            final DeleteConfigurationSetRequest deleteConfigurationSetRequest = DeleteConfigurationSetRequest.builder()
                .configurationSetName(model.getName())
                .build();
            proxy.injectCredentialsAndInvokeV2(deleteConfigurationSetRequest, client::deleteConfigurationSet);
            logger.log(String.format("%s [%s] deleted successfully",
                ResourceModel.TYPE_NAME, getPrimaryIdentifier(model).toString()));
        } catch (final ConfigurationSetDoesNotExistException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getName());
        }

        return ProgressEvent.defaultSuccessHandler(null);
    }
}
