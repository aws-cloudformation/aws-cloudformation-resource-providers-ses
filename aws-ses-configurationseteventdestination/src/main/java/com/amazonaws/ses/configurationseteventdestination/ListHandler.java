package com.amazonaws.ses.configurationseteventdestination;

import com.amazonaws.cloudformation.exceptions.ResourceNotFoundException;
import com.amazonaws.cloudformation.proxy.*;
import lombok.NonNull;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.ConfigurationSetDoesNotExistException;
import software.amazon.awssdk.services.ses.model.DescribeConfigurationSetRequest;
import software.amazon.awssdk.services.ses.model.DescribeConfigurationSetResponse;

import java.util.List;
import java.util.stream.Collectors;

public class ListHandler extends BaseHandler<CallbackContext> {
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

        final List<ResourceModel> models = listEventDestinationsGivenConfigurationSet(request.getDesiredResourceState().getConfigurationSetName());

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private List<ResourceModel> listEventDestinationsGivenConfigurationSet(@NonNull final String configurationSetName) {
        final DescribeConfigurationSetRequest request = DescribeConfigurationSetRequest
                .builder()
                .configurationSetName(configurationSetName)
                .build();
        final DescribeConfigurationSetResponse response;
        try {
            response = this.proxy.injectCredentialsAndInvokeV2(request, this.client::describeConfigurationSet);
        } catch (final ConfigurationSetDoesNotExistException e) {
            throw new ResourceNotFoundException(ResourceModel.TYPE_NAME, configurationSetName);
        }

        return response.eventDestinations()
                .stream()
                .map(e -> ResourceModel.builder()
                        .configurationSetName(configurationSetName)
                        .eventDestination(Translator.translate(e))
                        .build())
                .collect(Collectors.toList());
    }
}
