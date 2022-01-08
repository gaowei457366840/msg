package com.common.msg.kafka.network.netty;

import com.common.msg.api.util.StringUtil;
import com.common.msg.api.exception.MqConfigException;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClientUtils {
    private static final Logger log = LoggerFactory.getLogger(ClientUtils.class);


    private static final Pattern HOST_PORT_PATTERN = Pattern.compile(".*?\\[?([0-9a-zA-Z\\-%._:]*)\\]?:([0-9]+)");

    public static LinkedList<InetSocketAddress> parseAndValidateAddresses(String addr) {
        String[] urls = addr.split(",");
        LinkedList<InetSocketAddress> addresses = new LinkedList<>();
        for (String url : urls) {
            parseAddresses(addresses, url);
        }
        if (addresses.isEmpty())
            throw new MqConfigException("No resolvable bootstrap urls given in mq.router.url");
        return addresses;
    }

    public static List<InetSocketAddress> parseAndValidateAddresses(List<String> addrs) {
        List<InetSocketAddress> addresses = new ArrayList<>();
        for (String url : addrs) {
            parseAddresses(addresses, url);
        }
        if (addresses.isEmpty())
            throw new MqConfigException("No resolvable bootstrap urls given in mq.router.url");
        return addresses;
    }

    public static void closeQuietly(Closeable c, String name, AtomicReference<Throwable> firstException) {
        if (c != null) {
            try {
                c.close();
            } catch (Throwable t) {
                firstException.compareAndSet(null, t);
                log.error("Failed to close " + name, t);
            }
        }
    }


    public static String getHost(String address) {
        Matcher matcher = HOST_PORT_PATTERN.matcher(address);
        return matcher.matches() ? matcher.group(1) : null;
    }


    public static Integer getPort(String address) {
        Matcher matcher = HOST_PORT_PATTERN.matcher(address);
        return matcher.matches() ? Integer.valueOf(Integer.parseInt(matcher.group(2))) : null;
    }

    private static void parseAddresses(List<InetSocketAddress> addresses, String url) {
        if (url != null && !url.isEmpty())
            try {
                String host = getHost(url);
                Integer port = getPort(url);
                if (StringUtil.isNullOrEmpty(host)) {
                    host = url;
                }
                if (!StringUtil.isNullOrEmpty(host) && port == null && (host.contains(".com") || host.contains(".net"))) {
                    port = Integer.valueOf(80);
                }
                if (StringUtil.isNullOrEmpty(host) || port == null) {
                    throw new MqConfigException("Invalid url in mq.router.url: " + url);
                }
                InetSocketAddress address = new InetSocketAddress(host, port.intValue());

                if (address.isUnresolved()) {
                    log.warn("Removing server {} from {} as DNS resolution failed for {}", new Object[]{url, "mq.router.url", host});
                } else {
                    addresses.add(address);
                }
            } catch (IllegalArgumentException e) {
                throw new MqConfigException("Invalid port in mq.router.url: " + url);
            }
    }
}


