package com.ef.mediaroutingengine.config;

import java.util.Set;
import org.springframework.beans.factory.annotation.Value;

/**
 * Sentinel Redis Sentinel Config.
 */
public class Sentinel {

    private String master;
    private Set<String> nodes;

    private String password;
    @Value("${spring.redis.sentinel.enable}")
    private boolean enable;

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public Set<String> getNodes() {
        return nodes;
    }

    public void setNodes(Set<String> nodes) {
        this.nodes = nodes;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
