package com.common.msg.kafka.network;

import com.common.msg.kafka.common.MessageTypeEnum;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InflightRequestManager {
    private static final Logger log = LoggerFactory.getLogger(InflightRequestManager.class);


    private final int maxInFlightRequestsPerConnection;

    private final Map<InetSocketAddress, Map<Long, ClientNetworkService.InFlightRequest>> requests = new ConcurrentHashMap<>();


    private final AtomicInteger inFlightRequestCount = new AtomicInteger(0);

    public InflightRequestManager(int maxInFlightRequestsPerConnection) {
        this.maxInFlightRequestsPerConnection = maxInFlightRequestsPerConnection;
    }


    public void add(InetSocketAddress node, Long requestId, ClientNetworkService.InFlightRequest request) {
        Map<Long, ClientNetworkService.InFlightRequest> reqs = this.requests.get(node);
        if (reqs == null) {
            synchronized (this.requests) {
                reqs = this.requests.get(node);
                if (reqs == null) {
                    reqs = new ConcurrentHashMap<>();
                }
            }
            this.requests.put(node, reqs);
        }
        reqs.put(requestId, request);
        this.inFlightRequestCount.incrementAndGet();
    }


    public ClientNetworkService.InFlightRequest complete(InetSocketAddress node, Long requestId) {
        log.debug("complete: node[{}] requestId[{}]", node, requestId);

        ClientNetworkService.InFlightRequest inFlightRequest = (ClientNetworkService.InFlightRequest) ((Map) this.requests.get(node)).remove(requestId);
        this.inFlightRequestCount.decrementAndGet();
        return inFlightRequest;
    }


    public ClientNetworkService.InFlightRequest get(InetSocketAddress node, Long requestId) {
        return (ClientNetworkService.InFlightRequest) ((Map) this.requests.get(node)).get(requestId);
    }


    public List<ClientNetworkService.InFlightRequest> getTimedOutRequests(long now, long requestTimeoutMs) {
        List<ClientNetworkService.InFlightRequest> timedOutRequests = new ArrayList<>();
        for (Map.Entry<InetSocketAddress, Map<Long, ClientNetworkService.InFlightRequest>> entry : this.requests.entrySet()) {
            for (Map.Entry<Long, ClientNetworkService.InFlightRequest> innerEntry : (Iterable<Map.Entry<Long, ClientNetworkService.InFlightRequest>>) ((Map) entry.getValue()).entrySet()) {
                long timeSinceSend = now - ((ClientNetworkService.InFlightRequest) innerEntry.getValue()).createdTimeMs;
                if (timeSinceSend > requestTimeoutMs) {
                    timedOutRequests.add(innerEntry.getValue());
                    ((Map) entry.getValue()).remove(innerEntry.getKey());
                    this.inFlightRequestCount.decrementAndGet();
                    log.debug("timedOutRequests: node[{}] requestId[{}] type[{}]", new Object[]{entry
                            .getKey(), innerEntry
                            .getKey(),
                            MessageTypeEnum.getEnum(((ClientNetworkService.InFlightRequest) innerEntry.getValue()).request.getType()).getClz().getSimpleName()});
                }
            }
        }


        return timedOutRequests;
    }


    public int count() {
        return this.inFlightRequestCount.get();
    }

    public List<ClientNetworkService.InFlightRequest> clear(InetSocketAddress node) {
        Map<Long, ClientNetworkService.InFlightRequest> reqs = this.requests.get(node);
        List<ClientNetworkService.InFlightRequest> clearRequests = null;
        if (reqs != null) {
            clearRequests = new ArrayList<>(reqs.size());
            clearRequests.addAll(reqs.values());
            this.inFlightRequestCount.getAndAdd(-reqs.size());
            this.requests.remove(node);
        }


        return clearRequests;
    }
}


