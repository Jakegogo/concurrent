package dbcache.test;

import dbcache.DbCacheService;
import utils.JsonUtils;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import transfer.ByteArray;
import transfer.Transfer;
import transfer.def.TransferConfig;

/**
 * Created by Administrator on 2015/2/26.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
@Component
public class TestTransfer {

    @Autowired
    private DbCacheService<Entity, Long> cacheService;

    @org.junit.Test
    public void t1() {

    	TransferConfig.registerClass(Entity.class, 1);

        Entity entity = this.cacheService.get(1l);

        ByteArray byteArray = Transfer.encode(entity);

        byte[] bytes = byteArray.toBytes();
        System.out.println(bytes.length);



        Entity entity1 = Transfer.decode(bytes, Entity.class);
        System.out.println(JsonUtils.object2JsonString(entity1).getBytes().length);
    }

}
