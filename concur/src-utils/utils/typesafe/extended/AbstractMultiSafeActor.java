package utils.typesafe.extended;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 抽象的MultiSafeActor
 * Created by Jake on 2015/7/11.
 */
public interface AbstractMultiSafeActor {

    /**
     * 上一次执行的Actor
     */
    AtomicReference<AbstractMultiSafeActor> head = new AtomicReference<AbstractMultiSafeActor>();

}
