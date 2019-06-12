package com.aws.ses.receiptfilter;

import java.io.InputStream;

class Configuration extends BaseConfiguration {

    public Configuration() {
        super("aws-ses-receiptfilter.json");
    }

    public InputStream resourceSchema() {
        return this.getClass().getClassLoader().getResourceAsStream(schemaFilename);
    }

}
