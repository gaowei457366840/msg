package com.common.msg.api.event;

/**
 * 事件监听
 * @param <T>
 */
public interface EventListener<T> extends EventIdentity {
    void listen(T paramT);
}


