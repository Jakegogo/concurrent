package transfer.test;

import transfer.anno.Transferable;

/**
 * Created by Administrator on 2015/2/26.
 */
@Transferable(id = 2)
public enum AcountStatus {

    OPEN,

    CLOSE,

    LOCK

}
