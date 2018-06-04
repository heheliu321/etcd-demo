package cn.tanlw.etcd;

import com.ibm.etcd.api.Event;
import com.ibm.etcd.client.kv.WatchUpdate;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

/**
 * @author longyang.lin
 * @description
 * @create 2018年05月18日10:37
 */
@Slf4j
public class WatchListener implements StreamObserver<WatchUpdate> {

    @Override
    public void onNext(WatchUpdate value) {
        try {
            if (value != null && value.getEvents() != null && value.getEvents().size() > 0) {
                Event event = value.getEvents().get(0);
                if (event != null && event.getType().equals(Event.EventType.DELETE)) {
                    String key = event.getKv().getKey().toStringUtf8();
                    String v = event.getKv().getValue().toStringUtf8();
                    System.out.println("Event");
                    log.debug("监听到EventType.PUT,修改switchCache的key->{}，value->{}", key, v);
                }
            }
        } catch (Exception e) {
            log.warn("watch exception", e);
        }
        System.out.println("onNext");
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("ERROR  ERROR   ERROR");
    }

    @Override
    public void onCompleted() {
        System.out.println("onCompleted  onCompleted   onCompleted");
    }
}
