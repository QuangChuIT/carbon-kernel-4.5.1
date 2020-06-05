package org.wso2.carbon.user.core.model;

import java.util.Properties;

public class UserProfile {
    Properties userProperties = new Properties();

    public Properties getUserProperties() {
        return userProperties;
    }

    public void setUserProperties(Properties userProperties) {
        this.userProperties = userProperties;
    }
}

