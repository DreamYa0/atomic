package com.atomic.tools.zookeeper;

import com.atomic.tools.dubbo.DubboRegisterService;
import com.google.common.collect.Maps;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by luoqinqin on 16/5/14.
 */
@ThreadSafe
public class ZkConsole implements Watcher {

    private final CountDownLatch connectedSemaphore = new CountDownLatch(1);
    private String host = "192.168.143.34:2181";
    private volatile ZooKeeper zookeeper = null;
    private ConcurrentMap<String, DubboRegisterService> serviceMap;

    public ZkConsole(String host) {
        if (host != null) {
            this.host = host.trim();
        }
        init();
    }

    private void init() {
        try {
            serviceMap = Maps.newConcurrentMap();
            int sessionTimeout = 1000;
            zookeeper = new ZooKeeper(host, sessionTimeout, this);
            update();
        } catch (Exception e) {
            e.printStackTrace();
            serviceMap = null;
        }
    }


    /**
     * 更新当前zk链接下的所有服务地址
     * 可用于冒烟等待工具
     * @throws Exception
     */
    public synchronized void update() throws Exception {
        List<String> strs = zookeeper.getChildren("/META_INF/dubbo", false); //获取所有dubbo服务
        serviceMap.clear();
        strs.forEach(serviceName -> {
            String nodeString = "/META_INF/dubbo/" + serviceName + "/providers";
            try {
                List<String> providers = zookeeper.getChildren(nodeString, false);
                if (providers.size() > 0) {
                    providers.forEach(registerUrl -> {
                        DubboRegisterService dubboRegisterService = new DubboRegisterService(registerUrl);
                        String key = dubboRegisterService.getServiceName();
                        if (dubboRegisterService.getParams().containsKey("default.version")) {
                            key = key + ":" + dubboRegisterService.getParams().get("default.version");
                        }
                        serviceMap.put(key, dubboRegisterService);
                    });
                }
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public Map<String, DubboRegisterService> getServiceMap() {
        return serviceMap;
    }

    public DubboRegisterService getDubboService(String name) {
        if (serviceMap == null) {
            init();
            if (serviceMap == null) { //初始化失败
                return null;
            }
        }
        DubboRegisterService dubboRegisterService = serviceMap.get(name);
        if (dubboRegisterService != null) {
            return dubboRegisterService;
        } else {
            Iterator<String> it = serviceMap.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                if (key.contains(".")) { //先精确查询
                    String ss[] = key.split("\\.");
                    if (ss[ss.length - 1].equals(name)) {
                        return serviceMap.get(key);
                    }
                } else if (key.endsWith(name)) { //再模糊查询
                    return serviceMap.get(key);
                }
            }
        }
        return null;
    }

    public DubboRegisterService getDubboServiceByUrl(String url) {
        if (serviceMap == null) {
            init();
            if (serviceMap == null) { //初始化失败
                return null;
            }
        }
        Iterator<DubboRegisterService> it = serviceMap.values().iterator();
        while (it.hasNext()) {
            DubboRegisterService dubboRegisterService = it.next();
            if (dubboRegisterService.getPostDomain().equals(url)) {
                return dubboRegisterService;
            }
        }
        return null;
    }

    @Override
    public void process(WatchedEvent event) {
        System.out.println("收到事件通知：" + event.getState() + "\n");
        if (Event.KeeperState.SyncConnected == event.getState()) {
            connectedSemaphore.countDown();
        }
    }
}
