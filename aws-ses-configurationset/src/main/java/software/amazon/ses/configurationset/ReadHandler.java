package software.amazon.ses.configurationset;

import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.ConfigurationSetDoesNotExistException;
import software.amazon.awssdk.services.ses.model.DescribeConfigurationSetRequest;
import software.amazon.awssdk.services.ses.model.DescribeConfigurationSetResponse;

public class ReadHandler extends BaseHandler<CallbackContext> {

    private AmazonWebServicesClientProxy proxy;
    private SesClient client;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {
        this.proxy = proxy;
        this.client = ClientBuilder.getClient();

        final ResourceModel model = describeConfigurationSet(request.getDesiredResourceState().getName());

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }

    private ResourceModel describeConfigurationSet(final String configurationSetName) {
        final DescribeConfigurationSetRequest request = DescribeConfigurationSetRequest
            .builder()
            .configurationSetName(configurationSetName)
            .build();

        final DescribeConfigurationSetResponse response;
        try {
            response = this.proxy.injectCredentialsAndInvokeV2(request, this.client::describeConfigurationSet);
        } catch (final ConfigurationSetDoesNotExistException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, configurationSetName);
        }

        return ResourceModel.builder()
            .name(response.configurationSet().name())
            .build();
    }
}
