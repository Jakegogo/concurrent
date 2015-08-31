package transfer.anno;

import java.lang.annotation.*;

/**
 * 传输类注解
 * Created by Jake on 2015/2/26.
 */
@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Transferable {

    /**
     * 传输类唯一ID
     * @return int 唯一Id
     */
    int id();

}
