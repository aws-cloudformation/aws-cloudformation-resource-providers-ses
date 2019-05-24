package com.aws.ses.configurationset;

import java.io.InputStream;

class Configuration extends BaseConfiguration {

    public Configuration() {
        super("aws-ses-configurationset.json");
    }

    public InputStream resourceSchema() {
        return this.getClass().getClassLoader().getResourceAsStream(schemaFilename);
    }

}
