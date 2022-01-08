package com.common.msg.api.util;

import com.common.msg.api.exception.MqClientException;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;


public class LocalHostUtil {
    private static volatile String cachedIpAddress;

    public static void setIp(String ip) {

        cachedIpAddress = ip;
    }


    public static String getIp() {
        Enumeration<NetworkInterface> netInterfaces;
        if (null != cachedIpAddress) {
            return cachedIpAddress;
        }

        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ex) {
            throw new MqClientException(ex);
        }
        String localIpAddress = null;
        while (netInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = netInterfaces.nextElement();
            Enumeration<InetAddress> ipAddresses = netInterface.getInetAddresses();
            while (ipAddresses.hasMoreElements()) {
                InetAddress ipAddress = ipAddresses.nextElement();
                if (isPublicIpAddress(ipAddress)) {
                    String publicIpAddress = ipAddress.getHostAddress();
                    cachedIpAddress = publicIpAddress;
                    return publicIpAddress;
                }
                if (isLocalIpAddress(ipAddress)) {
                    localIpAddress = ipAddress.getHostAddress();
                }
            }
        }
        cachedIpAddress = localIpAddress;
        return localIpAddress;
    }

    private static boolean isPublicIpAddress(InetAddress ipAddress) {
        return (!ipAddress.isSiteLocalAddress() && !ipAddress.isLoopbackAddress() && !isV6IpAddress(ipAddress));
    }

    private static boolean isLocalIpAddress(InetAddress ipAddress) {
        return (ipAddress.isSiteLocalAddress() && !ipAddress.isLoopbackAddress() && !isV6IpAddress(ipAddress));
    }

    private static boolean isV6IpAddress(InetAddress ipAddress) {

        return ipAddress.getHostAddress().contains(":");
    }


    public static String getHostName() {

        return getLocalHost().getHostName();
    }

    private static InetAddress getLocalHost() {
        InetAddress result;
        try {
            result = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            throw new MqClientException(ex);
        }
        return result;
    }
}

