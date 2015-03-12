package transfer.deserializer;

import org.apache.mina.util.ConcurrentHashSet;
import transfer.Inputable;
import transfer.core.ByteMeta;
import transfer.def.TransferConfig;
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

        byte typeFlag = TransferConfig.getType(flag);

        if (typeFlag != Types.COLLECTION && typeFlag != Types.ARRAY) {
            throw new IllegalTypeException(typeFlag, Types.COLLECTION, type);
        }

        Collection list = createCollection(type);

        // 读取集合的大小
        int size = BitUtils.getInt(inputable);
        if (size == 0) {
            return (T) list;
        }


        Type itemType = null;
        if (type instanceof ParameterizedType) {
            itemType = TypeUtils.getParameterizedType((ParameterizedType) type, 0);
        } else if (type instanceof Class<?> && ((Class<?>)type).isArray()) {
            itemType = ((Class<?>)type).getComponentType();
        }


        Deserializer defaultComponentDeserializer = null;
        if (itemType != null && itemType != Object.class) {
            defaultComponentDeserializer = TransferConfig.getDeserializer(itemType);// 元素解析器
        }


        // 循环解析元素
        Object component;
        if (defaultComponentDeserializer == null) {
            for (int i = 0; i < size;i++) {
                byte elementFlag = inputable.getByte();
                Deserializer componentDeserializer = TransferConfig.getDeserializer(itemType, elementFlag);// 元素解析器
                component =  componentDeserializer.deserialze(inputable, itemType, elementFlag, referenceMap);
                list.add(component);
            }
        } else {
            for (int i = 0; i < size;i++) {
                component = defaultComponentDeserializer.deserialze(inputable, itemType, inputable.getByte(), referenceMap);
                list.add(component);
            }
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


    public ByteMeta readMeta(Inputable inputable) {
        byte flag = inputable.getByte();
        byte type = TransferConfig.getType(flag);

        if (type != Types.COLLECTION && type != Types.ARRAY) {
            throw new IllegalTypeException(type, Types.COLLECTION, null);
        }
        // 读取集合的大小
        int size = BitUtils.getInt(inputable);

        ByteMeta byteDataMeta = new ByteMeta();
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
