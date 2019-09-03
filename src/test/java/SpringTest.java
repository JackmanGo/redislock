import com.test.wang.redislock.Application;
import com.test.wang.redislock.api.JedisClient;
import com.test.wang.redislock.testservice.ServiceTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author wangxi
 * @date 2019-09-02 20:50
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SpringTest {

    @Autowired
    private ServiceTest serviceTest;
    @Autowired
    private JedisClient jedisClient;

    @Test
    public void testService() throws InterruptedException {

        List<Thread> list = new ArrayList<Thread>();

        for (int i=0; i<100;i++) {

            Thread thread = new Thread(new Runnable() {
                public void run() {
                   serviceTest.testRedisLock();
                }
            });

            thread.start();

            list.add(thread);
        }

        for(int i=0;i <list.size();i++){

            list.get(i).join();
        }
    }

    @Test
    public void luaExec(){

        String script = "if redis.call('GET', KEYS[1]) == ARGV[1] then redis.call('DEL', KEYS[1]) else return 'fail' end";
        jedisClient.eval(script, Collections.singletonList("wang"), Collections.singletonList("xi"));

    }
}
