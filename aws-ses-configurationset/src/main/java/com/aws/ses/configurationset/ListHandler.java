package com.aws.ses.configurationset;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.ListConfigurationSetsRequest;
import software.amazon.awssdk.services.ses.model.ListConfigurationSetsResponse;

import java.util.ArrayList;
import java.util.List;

public class ListHandler extends BaseHandler<CallbackContext> {

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

        final List<ResourceModel> models = new ArrayList<>();
        final ListConfigurationSetsResponse listConfigurationSetsResponse =
                listNamedQueries(request.getNextToken());
        listConfigurationSetsResponse.configurationSets().forEach(c ->
                models.add(ResourceModel.builder()
                        .name(c.name())
                        .build())
        );

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModels(models)
            .status(OperationStatus.SUCCESS)
            .build();
    }

    private ListConfigurationSetsResponse listNamedQueries(final String nextToken) {
        final ListConfigurationSetsRequest listConfigurationSetsRequest =
                ListConfigurationSetsRequest.builder()
                        .nextToken(nextToken)
                        .maxItems(50)
                        .build();
        try {
            return proxy.injectCredentialsAndInvokeV2(
                    listConfigurationSetsRequest,
                    client::listConfigurationSets);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
