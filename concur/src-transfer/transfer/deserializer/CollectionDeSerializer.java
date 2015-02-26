package transfer.deserializer;

import org.apache.mina.util.ConcurrentHashSet;
import transfer.Inputable;
import transfer.core.ByteDataMeta;
import transfer.def.Config;
import transfer.def.Types;
import transfer.exception.IllegalTypeException;
import transfer.utils.BitUtils;
import transfer.utils.IntegerMap;
import transfer.utils.TypeUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 集合解析器
 * <br/>尽量指定泛型类型,可提升解析性能
 * Created by Jake on 2015/2/23.
 */
public class CollectionDeSerializer implements Deserializer {


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        byte typeFlag = Config.getType(flag);

        if (typeFlag != Types.COLLECTION && typeFlag != Types.ARRAY) {
            throw new IllegalTypeException(typeFlag, Types.COLLECTION, type);
        }

        Collection list = createCollection(type);

        Object[] array = (Object[]) ArrayDeSerializer.getInstance().deserialze(inputable, type, (byte) (Types.ARRAY | Config.getExtra(flag)), referenceMap);

        for(Object element : array) {
            list.add(element);
        }

        return (T) list;
    }


    protected Collection createCollection(Type type) {

        if (type == null || type == Collection.class || type == Object.class) {
            return new ArrayList();
        }

        Class<?> rawClass = TypeUtils.getRawClass(type);

        Collection list;

        if (rawClass == AbstractCollection.class) {
            list = new ArrayList();
        } else if (rawClass == ConcurrentHashSet.class) {
            list = new ConcurrentHashSet();
        } else if (rawClass.isAssignableFrom(HashSet.class)) {
            list = new HashSet();
        } else if (rawClass.isAssignableFrom(LinkedHashSet.class)) {
            list = new LinkedHashSet();
        } else if (rawClass.isAssignableFrom(TreeSet.class)) {
            list = new TreeSet();
        } else if (rawClass.isAssignableFrom(ArrayList.class)) {
            list = new ArrayList();
        } else if (rawClass.isAssignableFrom(EnumSet.class)) {

            Type itemType;
            if (type instanceof ParameterizedType) {
                itemType = ((ParameterizedType) type).getActualTypeArguments()[0];
            } else {
                itemType = Object.class;
            }
            list = EnumSet.noneOf((Class<Enum>)itemType);

        } else {

            try {
                list = (Collection) rawClass.newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("create instane error, class " + rawClass.getName());
            }

        }
        return list;
    }


    public ByteDataMeta readMeta(Inputable inputable) {
        byte flag = inputable.getByte();
        byte type = Config.getType(flag);

        if (type != Types.COLLECTION && type != Types.ARRAY) {
            throw new IllegalTypeException(type, Types.COLLECTION, null);
        }
        // 读取集合的大小
        int size = BitUtils.getInt(inputable);

        ByteDataMeta byteDataMeta = new ByteDataMeta();
        byteDataMeta.setComponentSize(size);
        byteDataMeta.setFlag(flag);
        byteDataMeta.setIteratorAble(true);

        return byteDataMeta;
    }

    private static CollectionDeSerializer instance = new CollectionDeSerializer();

    public static CollectionDeSerializer getInstance() {
        return instance;
    }

}
