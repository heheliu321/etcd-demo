package cn.tanlw.etcd;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ETCDUtilTest {

    @Autowired
    Environment environment;

    public void watch() {
    }

    @Test
    public void put() {
        ETCDUtil.setEnvironment(environment);
        System.out.println(ETCDUtil.put("name","zhangsan",5));
        System.out.println(ETCDUtil.get("name"));
        ETCDUtil.watch("name", new WatchListener());
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}