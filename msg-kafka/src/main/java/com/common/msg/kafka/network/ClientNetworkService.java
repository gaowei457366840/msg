package com.common.msg.kafka.network;

import com.common.msg.api.config.ConfigManager;
import com.common.msg.api.util.MillisecondClock;
import com.common.msg.api.util.ProtobufUtil;
import com.common.msg.api.util.StringUtil;
import com.common.msg.api.util.ThreadUtil;
import com.common.msg.kafka.network.netty.MessageClient;
import com.google.protobuf.ByteString;
import com.common.msg.api.bootstrap.MqConfig;
import com.common.msg.api.exception.MqClientException;
import com.common.msg.api.exception.MqConfigException;
import com.common.msg.kafka.common.MessageTypeEnum;
import com.common.msg.kafka.common.ResultCodeEnum;
import com.common.msg.kafka.core.CheckTransactionStatusHandler;
import com.common.msg.kafka.core.dispatcher.ClientCommonDispatcher;
import com.common.msg.kafka.core.model.BrokerCommandRequest;
import com.common.msg.kafka.core.model.BrokerMetadataRequest;
import com.common.msg.kafka.core.model.BrokerMetadataResponse;
import com.common.msg.kafka.core.model.CheckTransactionStatusRequest;
import com.common.msg.kafka.core.model.CommonRequest;
import com.common.msg.kafka.core.model.CommonResponse;
import com.common.msg.kafka.core.model.HandshakeRequest;
import com.common.msg.kafka.core.model.HandshakeResponse;
import com.common.msg.kafka.core.model.JoinGroupRequest;
import com.common.msg.kafka.core.model.JoinGroupResponse;
import com.common.msg.kafka.core.model.SendMessageResponse;
import com.common.msg.kafka.core.model.TopicMetadata;
import com.common.msg.kafka.network.netty.ClientUtils;
import io.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClientNetworkService
        implements NetworkService {
    private static final Logger log = LoggerFactory.getLogger(ClientNetworkService.class);

    private final CheckTransactionStatusHandler checkTransactionStatusHandler;

    private final ClientCommonDispatcher dispatcher;

    private final InflightRequestManager inflightManager;

    private final BlockingQueue<InFlightRequest> inflightQueue;

    private final MessageClient client;

    private final ExecutorService sender;

    private final AtomicLong opaque;

    private final ExecutorService executor;

    private final Random random;

    private final long timeout;

    private final long waitTime;

    private final int retries;

    private volatile boolean isInitiated;

    private volatile boolean isConcurrent;

    private volatile boolean isClose;


    public ClientNetworkService(ClientCommonDispatcher dispatcher) {

        this.dispatcher = dispatcher;

        this.inflightManager = new InflightRequestManager(1024);

        this.checkTransactionStatusHandler = new CheckTransactionStatusHandler();

        this.sender = ThreadUtil.newSinglePool("sender");

        this.inflightQueue = new ArrayBlockingQueue<>(ConfigManager.getInt("mq.special.message.queue.size", 4096));

        this.timeout = ConfigManager.getLong("mq.special.message.timeout.ms", 20000L);

        this.executor = ThreadUtil.newCachedPool("connection");

        this.opaque = new AtomicLong(0L);

        this.random = new Random();

        if (this.timeout < 5000L || this.timeout > 30000L) {

            throw new MqConfigException("[mq.special.message.timeout.ms] must be 5000 ~ 30000.");
        }

        this.waitTime = this.timeout - 1000L;

        this.retries = ConfigManager.getInt("mq.special.message.retry.times", 3);

        this.client = new MessageClient(this);

        start();
    }


    public void join(List<MqConfig> list) {

        if (!this.isInitiated) {

            initConnect();
        }

        if (this.isInitiated) {

            this.dispatcher.updateTopicMetadata(retryJoin(list));
        }
    }


    public void connect() {

        if (!this.isInitiated) {

            initConnect();
        }

        if (!this.isConcurrent) {

            for (Map.Entry<InetSocketAddress, BrokerMetadataManager.BrokerMetadata> map : BrokerMetadataManager.getBrokerMetadataMap().entrySet()) {

                if (!((BrokerMetadataManager.BrokerMetadata) map.getValue()).isActive) {

                    connect(map.getKey(), false);
                }
            }

            this.isConcurrent = true;
        }
    }


    public void connect(final InetSocketAddress address, boolean isRetry) {

        if (this.isClose)
            return;

        if (isRetry && !BrokerMetadataManager.isContains(address))
            return;
        try {

            BrokerMetadataManager.setOffline(address);

            handleDisconnectionRequests(address);


            if (!BrokerMetadataManager.needReconnection(address)) {
                return;
            }

            BrokerMetadataManager.onReconnection(address);

        } catch (Throwable e) {

            log.warn("Clear disconnection info failed. address:" + address, e);
        }


        if (isRetry) {

            log.warn("Connection dead, will be reconnection to the server. address:" + address);
        }

        this.executor.submit(new Runnable() {
            public void run() {

                int delayTime = 0;

                int retryTimes = 0;

                InetSocketAddress unActiveAddress = address;

                while (!Thread.currentThread().isInterrupted() && !ClientNetworkService.this.isClose) {

                    if (delayTime < 50) {

                        delayTime += 10;
                    } else {

                        delayTime = 2;
                    }

                    if (retryTimes == 5 || retryTimes == 60) {

                        retryTimes = 0;

                        if (!ClientNetworkService.this.checkMetadata(unActiveAddress)) {

                            if (ClientNetworkService.this.isConcurrent) {
                                break;
                            }


                            for (Map.Entry<InetSocketAddress, BrokerMetadataManager.BrokerMetadata> map : BrokerMetadataManager.getBrokerMetadataMap().entrySet()) {

                                if (!((InetSocketAddress) map.getKey()).equals(unActiveAddress)) {

                                    unActiveAddress = map.getKey();
                                }
                            }
                        }
                    }


                    if (!ClientNetworkService.this.tryConnect(unActiveAddress) && !ClientNetworkService.this.isClose) {

                        ThreadUtil.sleep((delayTime * 1000));
                    } else {

                        ClientNetworkService.log.info("Connection to the server: " + unActiveAddress);
                        break;
                    }

                    retryTimes++;
                }
            }
        });
    }


    public void send(CommonRequest request, String destination, InetSocketAddress node, long createdTimeMs, Callback<CommonResponse, Boolean> callback) {

        long requestId = this.opaque.incrementAndGet();

        MessageProtobuf.Message.Builder builder = MessageProtobuf.Message.newBuilder();

        builder.setBody(ByteString.copyFrom(ProtobufUtil.serializer(request)));

        builder.setType(request.getType());

        builder.setVersion("v2");

        builder.setRequestId(requestId);


        InFlightRequest inFlightRequest = new InFlightRequest(builder.build(), destination, node, !request.isOneway(), callback, createdTimeMs);


        try {

            this.inflightQueue.put(inFlightRequest);

        } catch (InterruptedException e) {

            log.warn("Send request failed.", e);
        }
    }


    public void receive(InetSocketAddress node, MessageProtobuf.Message data) {

        if (data.getType().equals(MessageTypeEnum.SEND_MESSAGE_RESP.getCode())) {

            SendMessageResponse response = (SendMessageResponse) ProtobufUtil.deserializer(data.getBody().toByteArray(),
                    MessageTypeEnum.getEnum(data.getType()).getClz());

            response.setRequestId(data.getRequestId());

            response.setNode(node);

            completeResponses(node, (CommonResponse) response);

        } else if (data.getType().equals(MessageTypeEnum.CHECK_TRANSACTION_STATUS_REQ.getCode())) {

            CheckTransactionStatusRequest request = (CheckTransactionStatusRequest) ProtobufUtil.deserializer(data.getBody().toByteArray(), CheckTransactionStatusRequest.class);


            this.checkTransactionStatusHandler.handle(request, node);

        } else if (data.getType().equals(MessageTypeEnum.HANDSHAKE_RESP.getCode())) {

            HandshakeResponse response = (HandshakeResponse) ProtobufUtil.deserializer(data.getBody().toByteArray(),
                    MessageTypeEnum.getEnum(data.getType()).getClz());

            response.setRequestId(data.getRequestId());

            completeResponses(node, (CommonResponse) response);

        } else if (data.getType().equals(MessageTypeEnum.JOIN_GROUP_RESP.getCode())) {

            JoinGroupResponse response = (JoinGroupResponse) ProtobufUtil.deserializer(data.getBody().toByteArray(),
                    MessageTypeEnum.getEnum(data.getType()).getClz());

            response.setRequestId(data.getRequestId());

            completeResponses(node, (CommonResponse) response);

        } else if (data.getType().equals(MessageTypeEnum.BROKER_METADATA_RESP.getCode())) {

            BrokerMetadataResponse response = (BrokerMetadataResponse) ProtobufUtil.deserializer(data.getBody().toByteArray(),
                    MessageTypeEnum.getEnum(data.getType()).getClz());

            response.setRequestId(data.getRequestId());

            completeResponses(node, (CommonResponse) response);

        } else if (data.getType().equals(MessageTypeEnum.COMMON_RESP.getCode())) {

            CommonResponse response = (CommonResponse) ProtobufUtil.deserializer(data.getBody().toByteArray(),
                    MessageTypeEnum.getEnum(data.getType()).getClz());

            response.setRequestId(data.getRequestId());

            completeResponses(node, response);

        } else if (data.getType().equals(MessageTypeEnum.BROKER_COMMAND_REQUEST.getCode())) {

            brokerCommand(data);
        } else {

            log.warn("Not support this data from server {}.", data);
        }
    }


    public void close() {

        this.isClose = true;
        try {

            this.checkTransactionStatusHandler.close();

            BrokerMetadataManager.clear();

            this.executor.shutdownNow();

            this.sender.shutdownNow();

            this.client.close();

        } catch (Throwable e) {

            log.warn("Shutdown message client network failed.", e);
        }
    }


    private List<TopicMetadata> retryJoin(List<MqConfig> list) {

        JoinGroupResponse response = joinGroupRequest(list);

        if (response == null || response.getLocalException() != null || response.getResultCode() != ResultCodeEnum.SUCCESS.getCode()) {

            throw new MqClientException("[Join request] failed. " + response);
        }

        return response.getTopicMetadata();
    }

    private synchronized void initConnect() {

        if (!this.isInitiated) {

            LinkedList<InetSocketAddress> urls = ClientUtils.parseAndValidateAddresses(
                    ConfigManager.getString("mq.router.url", ""));

            InetSocketAddress address = urls.get(this.random.nextInt(100) % urls.size());

            int i = 0;

            while (i < 5 && !this.isClose) {

                if (tryConnect(address)) {


                    if (address.getHostName().contains(".com") || address.getHostName().contains(".net")) {

                        BrokerMetadataManager.setHostAddress(address.getAddress().getHostAddress());
                    }

                    this.isInitiated = true;
                    return;
                }

                address = urls.getLast();

                i++;

                ThreadUtil.sleep((100 + i * 1000));
            }

            throw new MqClientException("Connect to mq.router.url" + urls + "failed.");
        }
    }

    private void start() {

        this.sender.submit(new Runnable() {
            public void run() {

                InFlightRequest inFlightRequest = null;
                try {

                    while (!Thread.currentThread().isInterrupted() && !ClientNetworkService.this.isClose) {
                        try {

                            inFlightRequest = ClientNetworkService.this.inflightQueue.poll(100L, TimeUnit.MILLISECONDS);

                            if (inFlightRequest != null) {

                                if (!ClientNetworkService.this.isInitiated) {

                                    ClientNetworkService.this.initConnect();
                                }

                                ClientNetworkService.this.send(inFlightRequest);
                            }

                            ClientNetworkService.this.handleTimedOutRequests(MillisecondClock.now());
                        } catch (InterruptedException e) {

                            Thread.currentThread().interrupt();

                        } catch (MqClientException e1) {

                            ClientNetworkService.log.warn("Send request to server failed.", (Throwable) e1);

                            if (inFlightRequest != null && inFlightRequest.callback != null) {

                                CommonResponse response = new CommonResponse();

                                response.setLocalException((RuntimeException) e1);

                                inFlightRequest.callback.call(response);
                            }
                        }
                    }

                } catch (Throwable t) {

                    ClientNetworkService.log.warn("Uncaught error in send request to server.", t);
                }
            }
        });
    }

    private InetSocketAddress send(InFlightRequest inFlightRequest) {

        BrokerMetadataManager.BrokerMetadata node = getActiveNode(inFlightRequest);

        try {

            if (node == null) {

                if (inFlightRequest.expectResponse) {

                    CommonResponse response = new CommonResponse();

                    response.setResultCode(ResultCodeEnum.NO_AVAILABLE_CONN.getCode());

                    response.setResultMsg(ResultCodeEnum.NO_AVAILABLE_CONN.getMemo());

                    response.setLocalException((RuntimeException) new MqClientException(response.getResultMsg()));

                    inFlightRequest.callback.call(response);
                }
            } else {

                this.inflightManager.add(node.address,
                        Long.valueOf(inFlightRequest.request.getRequestId()), inFlightRequest);


                inFlightRequest.setSendTimeMs(MillisecondClock.now());

                this.client.send(node.channel, inFlightRequest.request);

                log.debug("Send data to server node[{}] isOneway[{}] type[{}]", new Object[]{node, inFlightRequest.request,


                        Boolean.valueOf(!inFlightRequest.expectResponse),
                        MessageTypeEnum.getEnum(inFlightRequest.request.getType()).getClz().getSimpleName()});

                return node.address;
            }

        } catch (Throwable t) {

            throw new MqClientException(t);
        }

        return null;
    }

    private BrokerMetadataManager.BrokerMetadata getActiveNode(InFlightRequest inFlightRequest) {

        BrokerMetadataManager.BrokerMetadata node = null;
        try {

            if (inFlightRequest.node != null) {

                node = BrokerMetadataManager.getActiveNode(inFlightRequest.node);
            }


            if (node == null) {

                long time = MillisecondClock.now() - inFlightRequest.createdTimeMs;
                do {

                    node = BrokerMetadataManager.getActiveNode();

                    if (node != null) {
                        break;
                    }

                    time += 50L;

                    ThreadUtil.sleep(50L);
                }
                while (time < this.waitTime);
            }


        } catch (Throwable t) {

            log.warn("Get active server node failed, " + inFlightRequest, t);
        }

        return node;
    }

    private void brokerCommand(MessageProtobuf.Message message) {
        BrokerCommandRequest request = (BrokerCommandRequest) ProtobufUtil.deserializer(message.getBody().toByteArray(), BrokerCommandRequest.class);

        log.info("Server brokerCommandRequest " + request);
        if (request.getCommand() == 0) {


            for (String url : request.getIpList()) {
                String host = ClientUtils.getHost(url);
                Integer port = ClientUtils.getPort(url);
                if (StringUtil.isNullOrEmpty(host) || port == null) {
                    log.warn("Parse server address failed. url" + url);
                    break;
                }
                InetSocketAddress address = new InetSocketAddress(host, port.intValue());
                if (this.isConcurrent && BrokerMetadataManager.putIfAbsent(address) == null) {
                    connect(address, false);
                }
            }
        }
        if (request.getCommand() == 1) {
            this.dispatcher.rewind(request.getContent());
        }
        if (request.getCommand() == 2) {

            this.dispatcher.transfer(request.getContent());
        }
    }

    @Deprecated
    private void handleCompleteSends(InetSocketAddress node, long requestId) {

        InFlightRequest request = this.inflightManager.get(node, Long.valueOf(requestId));

        if (!request.expectResponse) {

            this.inflightManager.complete(node, Long.valueOf(requestId));
        }
    }

    private void handleTimedOutRequests(long now) {

        List<InFlightRequest> inFlightRequests = new ArrayList<>();

        inFlightRequests.addAll(this.inflightManager.getTimedOutRequests(now, this.waitTime));

        if (inFlightRequests.isEmpty())
            return;
        for (InFlightRequest request : inFlightRequests) {

            if (request.expectResponse) {

                CommonResponse response = new CommonResponse();

                response.setResultCode(ResultCodeEnum.TIMEOUT.getCode());

                response.setResultMsg(String.format(ResultCodeEnum.TIMEOUT.getMemo(), new Object[]{Long.valueOf(this.timeout)}));

                response.setLocalException((RuntimeException) new MqClientException(response.getResultMsg()));

                request.callback.call(response);
            }
        }
    }

    private void handleDisconnectionRequests(InetSocketAddress node) {

        List<InFlightRequest> inFlightRequests = this.inflightManager.clear(node);

        if (null == inFlightRequests)
            return;
        for (InFlightRequest request : inFlightRequests) {

            if (request.retries >= this.retries || MillisecondClock.now() - request.createdTimeMs > this.waitTime) {

                if (request.expectResponse) {

                    CommonResponse response = new CommonResponse();

                    response.setResultCode(ResultCodeEnum.DISCONNECTION.getCode());

                    response.setResultMsg(String.format(ResultCodeEnum.DISCONNECTION.getMemo(), new Object[]{node.toString()}));

                    response.setLocalException((RuntimeException) new MqClientException(response.getResultMsg()));

                    request.callback.call(response);
                }
                continue;
            }
            try {

                log.warn("Send data to server failed when disconnection and will be retry in later. type[{}]",
                        MessageTypeEnum.getEnum(request.request.getType()).getClz().getSimpleName());

                request.setRetries(request.retries + 1);

                this.inflightQueue.put(request);

            } catch (InterruptedException e) {

                log.warn("Send request failed.", e);
            }
        }
    }


    private void completeResponses(InetSocketAddress node, CommonResponse response) {

        log.debug("Receive {} node[{}]", response, node);

        InFlightRequest inFlightRequest = this.inflightManager.complete(node, Long.valueOf(response.getRequestId()));

        if (null == inFlightRequest)
            return;
        try {

            inFlightRequest.callback.call(response);

        } catch (MqClientException e) {

            log.error("Uncaught error in request completion:" + response, (Throwable) e);
        }
    }

    private boolean tryConnect(InetSocketAddress address) {

        ChannelFuture channelFuture = null;
        try {

            channelFuture = this.client.connect(address);

            HandshakeResponse response = handshakeRequest(channelFuture, address);

            if (response != null && response.getLocalException() == null && response.getResultCode() == ResultCodeEnum.SUCCESS.getCode()) {

                BrokerMetadataManager.setOnline(address, channelFuture.channel());

                updateRouterMetadata((BrokerMetadataResponse) response);

                return true;
            }

            log.warn("[Handshake] failed. [{}] [{}]", address, response);

            channelFuture.channel().close();

            return false;
        } catch (Throwable e) {

            if (channelFuture != null) {

                channelFuture.channel().close();
            }

            log.error("Connect to server failed. address:" + address, e);


            return false;
        }
    }

    private boolean checkMetadata(InetSocketAddress address) {

        BrokerMetadataResponse response = brokerMetadataRequest();

        if (response == null || response.getLocalException() != null || response.getResultCode() != ResultCodeEnum.SUCCESS.getCode()) {

            log.warn("brokerMetadataRequest failed. node[{}] response[{}]", address, response);

            return true;
        }

        updateRouterMetadata(response);


        return BrokerMetadataManager.isContains(address);
    }

    private void updateRouterMetadata(BrokerMetadataResponse response) {

        List<InetSocketAddress> addresses = new ArrayList<>();


        for (String url : response.getRouterAddresses()) {

            String host = ClientUtils.getHost(url);

            Integer port = ClientUtils.getPort(url);

            if (StringUtil.isNullOrEmpty(host) || port == null) {

                log.warn("Parse server address failed. url" + url);
                break;
            }

            InetSocketAddress address = new InetSocketAddress(host, port.intValue());

            addresses.add(address);
        }

        List<List<InetSocketAddress>> changedAddresses = BrokerMetadataManager.checkAndPut(addresses);

        if (changedAddresses != null) {


            if (((List) changedAddresses.get(0)).size() > 0) {

                for (InetSocketAddress inetSocketAddress : changedAddresses.get(0)) ;
            }


            if (((List) changedAddresses.get(1)).size() > 0 &&
                    this.isConcurrent) {

                for (InetSocketAddress addedUrl : changedAddresses.get(1)) {

                    connect(addedUrl, false);
                }
            }
        }
    }


    private HandshakeResponse handshakeRequest(ChannelFuture channelFuture, InetSocketAddress address) {

        HandshakeRequest handshakeRequest = new HandshakeRequest();

        handshakeRequest.setType(MessageTypeEnum.HANDSHAKE_REQ.getCode());

        handshakeRequest.setSarname(ConfigManager.getSarName(""));

        handshakeRequest.setVersion("2.0.0");

        MessageProtobuf.Message request = buildSendMessage(MessageTypeEnum.HANDSHAKE_REQ,
                ProtobufUtil.serializer(handshakeRequest));

        final SyncFuture<HandshakeResponse> future = new SyncFuture<>();


        InFlightRequest inFlightRequest = new InFlightRequest(request, null, address, true, new Callback<CommonResponse, Boolean>() {
            public Boolean call(CommonResponse param) {
                if (param.getLocalException() != null) {
                    future.setError(param.getLocalException());
                } else if (param instanceof HandshakeResponse) {
                    future.setResponse((HandshakeResponse) param);
                } else {
                    HandshakeResponse response = new HandshakeResponse();
                    response.setResultCode(param.getResultCode());
                    response.setResultMsg(param.getResultMsg());
                    response.setRequestId(param.getRequestId());
                    response.setNode(param.getNode());
                    future.setResponse(response);
                }
                return Boolean.valueOf(true);
            }
        }, MillisecondClock.now());


        inFlightRequest.setRetries(this.retries);

        HandshakeResponse response = null;
        try {

            this.inflightManager.add(address,
                    Long.valueOf(inFlightRequest.request.getRequestId()), inFlightRequest);


            inFlightRequest.setSendTimeMs(MillisecondClock.now());


            this.client.send(channelFuture.channel(), inFlightRequest.request);

            log.info("Send handshakeRequest requestId[{}] addr[{}].", Long.valueOf(request.getRequestId()), address);

            response = future.get(this.waitTime, TimeUnit.MILLISECONDS);

            if (null != response) {

                log.info("Receive " + response);
            }

        } catch (Throwable e) {

            log.warn("Processed handshakeResponse failed." + handshakeRequest, e);
        }

        return response;
    }

    private BrokerMetadataResponse brokerMetadataRequest() {

        BrokerMetadataRequest brokerMetadataRequest = new BrokerMetadataRequest();

        brokerMetadataRequest.setType(MessageTypeEnum.BROKER_METADATA_REQ.getCode());

        brokerMetadataRequest.setSarname(ConfigManager.getSarName(""));

        MessageProtobuf.Message request = buildSendMessage(MessageTypeEnum.BROKER_METADATA_REQ,
                ProtobufUtil.serializer(brokerMetadataRequest));

        final SyncFuture<BrokerMetadataResponse> future = new SyncFuture<>();


        InFlightRequest inFlightRequest = new InFlightRequest(request, null, null, true, new Callback<CommonResponse, Boolean>() {
            public Boolean call(CommonResponse param) {
                if (param.getLocalException() != null) {
                    future.setError(param.getLocalException());
                } else if (param instanceof BrokerMetadataResponse) {
                    future.setResponse((BrokerMetadataResponse) param);
                } else {
                    BrokerMetadataResponse response = new BrokerMetadataResponse();
                    response.setResultCode(param.getResultCode());
                    response.setResultMsg(param.getResultMsg());
                    response.setRequestId(param.getRequestId());
                    response.setNode(param.getNode());
                    future.setResponse(response);
                }
                return Boolean.valueOf(true);
            }
        }, MillisecondClock.now());


        BrokerMetadataResponse response = null;
        try {

            this.inflightQueue.put(inFlightRequest);

            log.info("Send brokerMetadataRequest requestId[{}].", Long.valueOf(request.getRequestId()));

            response = future.get(this.waitTime, TimeUnit.MILLISECONDS);

            if (null != response) {

                log.info("Receive " + response);
            }

        } catch (Throwable e) {

            log.warn("Processed brokerMetadataResponse failed." + brokerMetadataRequest, e);
        }

        return response;
    }


    private JoinGroupResponse joinGroupRequest(List<MqConfig> list) {

        JoinGroupResponse response = null;

        JoinGroupRequest joinGroupRequest = new JoinGroupRequest(ConfigManager.getSarName(""), "2.0.0");

        if (joinGroupRequest.setConfigList(list)) {

            this.isConcurrent = true;
        }


        final SyncFuture<JoinGroupResponse> future = new SyncFuture<>();

        MessageProtobuf.Message request = buildSendMessage(MessageTypeEnum.JOIN_GROUP_REQ, ProtobufUtil.serializer(joinGroupRequest));


        InFlightRequest inFlightRequest = new InFlightRequest(request, null, null, true, new Callback<CommonResponse, Boolean>() {
            public Boolean call(CommonResponse param) {
                if (param.getLocalException() != null) {
                    future.setError(param.getLocalException());
                } else if (param instanceof JoinGroupResponse) {
                    future.setResponse((JoinGroupResponse) param);
                } else {
                    JoinGroupResponse response = new JoinGroupResponse();
                    response.setResultCode(param.getResultCode());
                    response.setResultMsg(param.getResultMsg());
                    response.setRequestId(param.getRequestId());
                    response.setNode(param.getNode());
                    future.setResponse(response);
                }
                return Boolean.valueOf(true);
            }
        }, MillisecondClock.now());

        try {

            this.inflightQueue.put(inFlightRequest);

            log.info("Send joinGroupRequest requestId[{}], config list[{}]", Long.valueOf(request.getRequestId()), list);

            response = future.get(this.waitTime, TimeUnit.MILLISECONDS);

            if (null != response) {

                log.info("Receive " + response);
            }

        } catch (Throwable e) {

            log.warn("Process joinGroupRequest failed." + joinGroupRequest, e);
        }

        return response;
    }

    private MessageProtobuf.Message buildSendMessage(MessageTypeEnum typeEnum, byte[] message) {

        MessageProtobuf.Message.Builder builder = MessageProtobuf.Message.newBuilder();

        builder.setType(typeEnum.getCode());

        builder.setVersion("v2");

        builder.setBody(ByteString.copyFrom(message));

        builder.setRequestId(this.opaque.incrementAndGet());

        return builder.build();
    }


    static class InFlightRequest {
        final MessageProtobuf.Message request;


        final String destination;


        final InetSocketAddress node;

        final boolean expectResponse;

        final Callback<CommonResponse, Boolean> callback;

        final long createdTimeMs;

        long sendTimeMs;

        int retries;


        InFlightRequest(MessageProtobuf.Message request, String destination, InetSocketAddress node, boolean expectResponse, Callback<CommonResponse, Boolean> callback, long createdTimeMs) {

            this.request = request;

            this.destination = destination;

            this.node = node;

            this.expectResponse = expectResponse;

            this.callback = callback;

            this.createdTimeMs = createdTimeMs;
        }

        void setSendTimeMs(long sendTimeMs) {

            this.sendTimeMs = sendTimeMs;
        }

        void setRetries(int retries) {

            this.retries = retries;
        }


        public String toString() {

            return "InFlightRequest{requestType=" +
                    MessageTypeEnum.getEnum(this.request.getType()).getClz().getSimpleName() + ", requestId=" + (
                    (this.request.getRequestId() > 0L) ? request.getRequestId() : "") + ", destination='" + (
                    StringUtil.isNullOrEmpty(this.destination) ? "" : this.destination) + '\'' + ", node=" + this.node + ", expectResponse=" + this.expectResponse + ", createdTimeMs=" + this.createdTimeMs + ", sendTimeMs=" + this.sendTimeMs + ", retries=" + this.retries + '}';

        }
    }
}


