package cn.tanlw.etcd;

import com.google.protobuf.ByteString;
import com.ibm.etcd.api.KeyValue;
import com.ibm.etcd.client.EtcdClient;
import com.ibm.etcd.client.KvStoreClient;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author longyang.lin
 * @description
 * @create 2018年05月18日10:35
 */
@Slf4j
public class ETCDUtil {
    private static Environment environment;
    private static KvStoreClient instance;


    private ETCDUtil() {
    }

    public static KvStoreClient getInstance() {
        if (instance == null) {
            synchronized (ETCDUtil.class) {
                Assert.notNull(environment, "environment为空");
                if (instance == null) {
                    String host = environment.getProperty("etcd.host");
                    Assert.notNull(host, "请配置etcd.host");
                    String port = environment.getProperty("etcd.port");
                    Assert.notNull(port, "请配置etcd.port");
                    instance = EtcdClient.forEndpoint(host, Integer.parseInt(port)).withPlainText().build();
                }
            }
        }
        return instance;
    }

    public static void watch(String key, StreamObserver observer) {
        try {
            ByteString bytes = ByteString.copyFrom(key, "utf-8");
            if (isPrevExist(bytes)) {
                getInstance().getKvClient().watch(bytes).start(observer);
                log.debug("添加{}监听", key);
            } else {
                log.debug("ETCD不存在" + key + "未添加监听");
            }
        } catch (Exception e) {
            log.error("添加监听失败", e);
        }
    }

    public static List<KeyValue> getByPrefix(String prefix) {
        List<KeyValue> list = new ArrayList<>();
        try {
            ByteString key = ByteString.copyFrom(prefix, "utf-8");
            list = getInstance().getKvClient().get(key).asPrefix().sync().getKvsList();
        } catch (UnsupportedEncodingException e) {
            log.error("获取key前缀节点失败", e);
        }
        return list;
    }

    /**
     * 添加
     *
     * @param key
     * @param value
     * @param ttl   过期时间(秒)
     */
    public static boolean put(String key, String value, long ttl) {
        try {
            ByteString k = ByteString.copyFrom(key, "utf-8");
            ByteString v = ByteString.copyFrom(value, "utf-8");
            long id = getInstance().getLeaseClient().create(ttl).get().getID();
            return getInstance().getKvClient().put(k, v, id).sync().isInitialized();
        } catch (Exception e) {
            log.error("添加key，Value失败", e);
        }
        return false;
    }

    public static String get(String key){
        ByteString k = null;
        try {
            k = ByteString.copyFrom(key, "utf-8");
            return String.valueOf(getInstance().getKvClient().get(k).sync().getKvs(0).getValue().toStringUtf8());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isPrevExist(ByteString key) {
        List<KeyValue> kvsList = getInstance().getKvClient().get(key).sync().getKvsList();
        for (KeyValue keyValue : kvsList) {
            if (StringUtils.hasText(keyValue.getKey().toStringUtf8())) {
                return true;
            }
        }

        return false;
    }

    public static void setEnvironment(Environment environment) {
        ETCDUtil.environment = environment;
    }
}