package com.common.msg.api.storage;

import java.io.File;
import java.util.Map;

public interface StoreContext {
    String applicationId();

    File stateDir();

    Map<String, Object> appConfigs();
}


