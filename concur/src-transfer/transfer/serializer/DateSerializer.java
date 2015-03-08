package transfer.serializer;

import org.objectweb.asm.MethodVisitor;
import transfer.Outputable;
import transfer.def.Types;
import transfer.utils.BitUtils;
import transfer.utils.IdentityHashMap;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * 日期编码器
 * Created by Jake on 2015/2/26.
 */
public class DateSerializer implements Serializer {


    @Override
    public void serialze(Outputable outputable, Object object, IdentityHashMap referenceMap) {

        if (object == null) {
            NULL_SERIALIZER.serialze(outputable, object, referenceMap);
            return;
        }

        outputable.putByte(Types.DATE_TIME);

        Date date = (Date) object;

        BitUtils.putLong(outputable, date.getTime());
    }

    @Override
    public void compile(Type type, MethodVisitor mw) {

    }


    private static DateSerializer instance = new DateSerializer();

    public static DateSerializer getInstance() {
        return instance;
    }

}
