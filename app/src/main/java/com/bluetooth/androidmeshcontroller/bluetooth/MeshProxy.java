package com.bluetooth.androidmeshcontroller.bluetooth;

import java.util.Arrays;
import java.util.Objects;

public class MeshProxy {

    private String bdaddr;
    private byte id_type; // 0x00=network, 0x01=node
    private byte [] network_id; // if id_type==0x00 : 8 bytes
    private byte [] proxy_hash; // if id_type==0x01 : 8 bytes
    private byte [] random; // if id_type==0x01 : 8 bytes

    public MeshProxy(String bdaddr,byte[] network_id) {
        this.bdaddr = bdaddr;
        this.id_type = 0x00;
        this.network_id = network_id;
    }

    public MeshProxy(String bdaddr,byte[] hash, byte[] random) {
        this.bdaddr = bdaddr;
        this.id_type = 0x01;
        this.proxy_hash = hash;
        this.random = random;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeshProxy proxy = (MeshProxy) o;
        return bdaddr.equals(proxy.bdaddr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bdaddr);
    }

    public String getBdaddr() {
        return bdaddr;
    }

    public byte getId_type() {
        return id_type;
    }

    public byte[] getNetwork_id() {
        return network_id;
    }

    public byte[] getProxyHash() {
        return proxy_hash;
    }

    public byte[] getRandom() {
        return random;
    }

    @Override
    public String toString() {
        String proxy = "MeshProxy{" +
                "bdaddr=" + bdaddr +
                ", id_type=" + id_type;
        if (this.id_type == 0x00) {
            proxy = proxy + ", network_id=" + Arrays.toString(network_id) + "}";
            return proxy;
        }
        if (this.id_type == 0x01) {
            proxy = proxy + ", hash=" + Arrays.toString(proxy_hash) +
                    ", random=" + Arrays.toString(random) +
                    "}";
            return proxy;
        }
        proxy = proxy + " INVALID ID TYPE!";
        return proxy;
    }
}
