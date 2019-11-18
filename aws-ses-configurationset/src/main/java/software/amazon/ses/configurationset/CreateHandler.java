package software.amazon.ses.configurationset;

import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;
import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.ConfigurationSet;
import software.amazon.awssdk.services.ses.model.ConfigurationSetAlreadyExistsException;
import software.amazon.awssdk.services.ses.model.CreateConfigurationSetRequest;
import software.amazon.awssdk.services.ses.model.InvalidConfigurationSetException;
import software.amazon.awssdk.services.ses.model.LimitExceededException;

import static software.amazon.ses.configurationset.ResourceModelExtensions.getPrimaryIdentifier;

public class CreateHandler extends BaseHandler<CallbackContext> {

    public static final int MAX_LENGTH_CONFIGURATION_SET_NAME = 64;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {
        final SesClient client = ClientBuilder.getClient();
        final ResourceModel model = request.getDesiredResourceState();

        // resource can auto-generate a name if not supplied by caller
        // this logic should move up into the CloudFormation engine, but
        // currently exists here for backwards-compatibility with existing models
        if (StringUtils.isNullOrEmpty(model.getName())) {
            model.setName(
                IdentifierUtils.generateResourceIdentifier(
                    request.getLogicalResourceIdentifier(),
                    request.getClientRequestToken(),
                    MAX_LENGTH_CONFIGURATION_SET_NAME
                )
            );
        }

        // pre-creation read to ensure no existing resource exists
        try {
            new ReadHandler().handleRequest(proxy, request, null, logger);
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, model.getName());
        } catch (final CfnNotFoundException e) {
            // no existing resource, creation can proceed
        }

        final CreateConfigurationSetRequest createConfigurationSetRequest =
                CreateConfigurationSetRequest.builder()
                    .configurationSet(ConfigurationSet.builder()
                        .name(model.getName())
                        .build())
                    .build();
        try {
            proxy.injectCredentialsAndInvokeV2(createConfigurationSetRequest, client::createConfigurationSet);
            logger.log(String.format("%s [%s] created successfully",
                ResourceModel.TYPE_NAME, getPrimaryIdentifier(model).toString()));
        } catch (ConfigurationSetAlreadyExistsException e) {
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, getPrimaryIdentifier(model).toString());
        } catch (InvalidConfigurationSetException e) {
            throw new CfnInvalidRequestException(createConfigurationSetRequest.toString(), e);
        } catch (LimitExceededException e) {
            throw new CfnServiceLimitExceededException(ResourceModel.TYPE_NAME, e.toString());
        }

        return ProgressEvent.defaultSuccessHandler(model);
    }
}
