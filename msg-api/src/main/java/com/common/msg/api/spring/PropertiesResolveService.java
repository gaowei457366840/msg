package com.common.msg.api.spring;

import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Service;
import org.springframework.util.StringValueResolver;


@Service
public class PropertiesResolveService
        implements EmbeddedValueResolverAware, PriorityOrdered {
    private StringValueResolver stringValueResolver;

    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.stringValueResolver = resolver;
    }

    public String getPropertiesValue(String name) {
        if (!name.contains("${")) {
            name = "${" + name + "}";
        }
        return this.stringValueResolver.resolveStringValue(name);
    }


    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}

