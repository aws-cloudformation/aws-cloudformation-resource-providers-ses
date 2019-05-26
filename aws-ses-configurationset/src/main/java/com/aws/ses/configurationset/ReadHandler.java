package com.aws.ses.configurationset;

import com.aws.cfn.proxy.AmazonWebServicesClientProxy;
import com.aws.cfn.proxy.Logger;
import com.aws.cfn.proxy.OperationStatus;
import com.aws.cfn.proxy.ProgressEvent;
import com.aws.cfn.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.ses.SesClient;
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
        this.client = SesClient.builder().build();

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

        DescribeConfigurationSetResponse response =
            this.proxy.injectCredentialsAndInvokeV2(request, this.client::describeConfigurationSet);

        return ResourceModel.builder()
            .name(response.configurationSet().name())
            .build();
    }
}
