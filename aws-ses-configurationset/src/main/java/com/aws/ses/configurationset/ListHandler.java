package com.aws.ses.configurationset;

import com.aws.cfn.proxy.AmazonWebServicesClientProxy;
import com.aws.cfn.proxy.Logger;
import com.aws.cfn.proxy.OperationStatus;
import com.aws.cfn.proxy.ProgressEvent;
import com.aws.cfn.proxy.ResourceHandlerRequest;
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

        final List<ResourceModel> models = listConfigurationSets();

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModels(models)
            .status(OperationStatus.SUCCESS)
            .build();
    }

    private List<ResourceModel> listConfigurationSets() {
        final ListConfigurationSetsRequest request = ListConfigurationSetsRequest.builder()
            .maxItems(50)
            .build();

        ListConfigurationSetsResponse response =
            this.proxy.injectCredentialsAndInvokeV2(request, this.client::listConfigurationSets);

        List<ResourceModel> models = new ArrayList<>();
        response.configurationSets().forEach(c ->{
            ResourceModel model = ResourceModel.builder()
                .name(c.name())
                .build();
            models.add(model);
        });

        return models;
    }
}
