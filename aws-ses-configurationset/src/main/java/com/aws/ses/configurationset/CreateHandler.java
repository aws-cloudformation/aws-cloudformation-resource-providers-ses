package com.aws.ses.configurationset;

import com.amazonaws.cloudformation.exceptions.CfnAlreadyExistsException;
import com.amazonaws.cloudformation.exceptions.CfnInvalidRequestException;
import com.amazonaws.cloudformation.exceptions.CfnServiceLimitExceededException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import com.amazonaws.cloudformation.resource.IdentifierUtils;
import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.ConfigurationSet;
import software.amazon.awssdk.services.ses.model.ConfigurationSetAlreadyExistsException;
import software.amazon.awssdk.services.ses.model.CreateConfigurationSetRequest;
import software.amazon.awssdk.services.ses.model.InvalidConfigurationSetException;
import software.amazon.awssdk.services.ses.model.LimitExceededException;

import static com.aws.ses.configurationset.ResourceModelExtensions.getPrimaryIdentifier;

public class CreateHandler extends BaseHandler<CallbackContext> {

    public static final int MAX_LENGTH_CONFIGURATION_SET_NAME = 64;

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
        ResourceModel model = request.getDesiredResourceState();

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

        return createResource(model);
    }

    private ProgressEvent<ResourceModel, CallbackContext> createResource(ResourceModel model) {
        createConfigurationSet(model);
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private void createConfigurationSet(final ResourceModel model) {
        final CreateConfigurationSetRequest createConfigurationSetRequest =
                CreateConfigurationSetRequest.builder()
                        .configurationSet(ConfigurationSet.builder()
                                .name(model.getName())
                                .build())
                        .build();
        try {
            proxy.injectCredentialsAndInvokeV2(
                    createConfigurationSetRequest,
                    client::createConfigurationSet);
            logger.log(String.format("%s [%s] created successfully",
                    ResourceModel.TYPE_NAME, getPrimaryIdentifier(model).toString()));
        } catch (ConfigurationSetAlreadyExistsException e) {
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, getPrimaryIdentifier(model).toString());
        } catch (InvalidConfigurationSetException e) {
            throw new CfnInvalidRequestException(createConfigurationSetRequest.toString(), e);
        } catch (LimitExceededException e) {
            throw new CfnServiceLimitExceededException(ResourceModel.TYPE_NAME, e.toString());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
