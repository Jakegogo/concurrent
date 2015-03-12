package transfer.test;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.objectweb.asm.MethodVisitor;
import transfer.Outputable;
import transfer.compile.AsmSerializerContext;
import transfer.def.TransferConfig;
import transfer.serializer.Serializer;
import transfer.utils.BitUtils;
import transfer.utils.IdentityHashMap;

public class Entity_1
  implements Serializer
{
  public void serialze(Outputable paramOutputable, Object paramObject, IdentityHashMap paramIdentityHashMap)
  {
    if (paramObject == null)
    {
      paramOutputable.putByte((byte)1);
      return;
    }
    paramOutputable.putByte((byte)-16);
    Entity localEntity = (Entity)paramObject;
    BitUtils.putInt2(paramOutputable, 1);
    serialze_id_1(paramOutputable, localEntity.getId(), paramIdentityHashMap);
    serialze_uid_2(paramOutputable, Integer.valueOf(localEntity.getUid()), paramIdentityHashMap);
    serialze_num_3(paramOutputable, Integer.valueOf(localEntity.getNum()), paramIdentityHashMap);
    serialze_name_4(paramOutputable, localEntity.getName(), paramIdentityHashMap);
    serialze_a_5(paramOutputable, localEntity.getA(), paramIdentityHashMap);
    serialze_fval_6(paramOutputable, Float.valueOf(localEntity.getFval()), paramIdentityHashMap);
    serialze_status_7(paramOutputable, localEntity.getStatus(), paramIdentityHashMap);
    serialze_date_8(paramOutputable, localEntity.getDate(), paramIdentityHashMap);
    serialze_str_9(paramOutputable, localEntity.getStr(), paramIdentityHashMap);
    serialze_bool_10(paramOutputable, localEntity.getBool(), paramIdentityHashMap);
    serialze_map_11(paramOutputable, localEntity.getMap(), paramIdentityHashMap);
    serialze_friends_13(paramOutputable, localEntity.getFriends(), paramIdentityHashMap);
  }

  public void serialze_id_1(Outputable paramOutputable, Object paramObject, IdentityHashMap paramIdentityHashMap)
  {
    if (paramObject == null)
    {
      paramOutputable.putByte((byte)1);
      return;
    }
    Number localNumber = (Number)paramObject;
    putLongVal(paramOutputable, localNumber);
  }

  private void putIntVal(Outputable paramOutputable, Number paramNumber)
  {
    int i = paramNumber.intValue();
    int j = 0;
    if (i < 0)
    {
      i = -i;
      j = 8;
    }
    if (i < 128)
    {
      paramOutputable.putByte(new byte[] { (byte)(0x10 | j | 0x0), (byte)i });
    }
    else
    {
      byte[] arrayOfByte = new byte[4];
      int k = 4;
      while (i > 0)
      {
        arrayOfByte[(--k)] = ((byte)i);
        i >>= 8;
      }
      paramOutputable.putByte((byte)(0x10 | j | 3 - k));
      paramOutputable.putBytes(arrayOfByte, k, 4 - k);
    }
  }

  private void putLongVal(Outputable paramOutputable, Number paramNumber)
  {
    long l = paramNumber.longValue();
    int i = 0;
    if (l < 0L)
    {
      l = -l;
      i = 8;
    }
    if (l < 128L)
    {
      paramOutputable.putByte(new byte[] { (byte)(0x10 | i | 0x0), (byte)(int)l });
    }
    else
    {
      byte[] arrayOfByte = new byte[8];
      int j = 8;
      while (l > 0L)
      {
        arrayOfByte[(--j)] = ((byte)(int)l);
        l >>= 8;
      }
      paramOutputable.putByte((byte)(0x10 | i | 7 - j));
      paramOutputable.putBytes(arrayOfByte, j, 8 - j);
    }
  }

  public void serialze_uid_2(Outputable paramOutputable, Object paramObject, IdentityHashMap paramIdentityHashMap)
  {
    if (paramObject == null)
    {
      paramOutputable.putByte((byte)1);
      return;
    }
    Number localNumber = (Number)paramObject;
    putIntVal(paramOutputable, localNumber);
  }

  public void serialze_num_3(Outputable paramOutputable, Object paramObject, IdentityHashMap paramIdentityHashMap)
  {
    if (paramObject == null)
    {
      paramOutputable.putByte((byte)1);
      return;
    }
    Number localNumber = (Number)paramObject;
    putIntVal(paramOutputable, localNumber);
  }

  public void serialze_name_4(Outputable paramOutputable, Object paramObject, IdentityHashMap paramIdentityHashMap)
  {
    if (paramObject == null)
    {
      paramOutputable.putByte((byte)1);
      return;
    }
    paramOutputable.putByte((byte)-32);
    CharSequence localCharSequence = (CharSequence)paramObject;
    String str = localCharSequence.toString();
    byte[] arrayOfByte = str.getBytes();
    BitUtils.putInt(paramOutputable, arrayOfByte.length);
    paramOutputable.putBytes(arrayOfByte);
  }

  public void serialze_a_5(Outputable paramOutputable, Object paramObject, IdentityHashMap paramIdentityHashMap)
  {
    if (paramObject == null)
    {
      paramOutputable.putByte((byte)1);
      return;
    }
    paramOutputable.putByte((byte)-80);
    byte[] arrayOfByte = (byte[])paramObject;
    BitUtils.putInt(paramOutputable, arrayOfByte.length);
    paramOutputable.putBytes(arrayOfByte);
  }

  public void serialze_fval_6(Outputable paramOutputable, Object paramObject, IdentityHashMap paramIdentityHashMap)
  {
    if (paramObject == null)
    {
      paramOutputable.putByte((byte)1);
      return;
    }
    Number localNumber = (Number)paramObject;
    paramOutputable.putByte((byte)32);
    BitUtils.putInt(paramOutputable, Float.floatToRawIntBits(localNumber.floatValue()));
  }

  public void serialze_status_7(Outputable paramOutputable, Object paramObject, IdentityHashMap paramIdentityHashMap)
  {
    if (paramObject == null)
    {
      paramOutputable.putByte((byte)1);
      return;
    }
    paramOutputable.putByte((byte)80);
    Enum localEnum = (Enum)paramObject;
    BitUtils.putInt2(paramOutputable, 3);
    int i = localEnum.ordinal();
    BitUtils.putInt2(paramOutputable, i);
  }

  public void serialze_date_8(Outputable paramOutputable, Object paramObject, IdentityHashMap paramIdentityHashMap)
  {
    if (paramObject == null)
    {
      paramOutputable.putByte((byte)1);
      return;
    }
    paramOutputable.putByte((byte)-96);
    Date localDate = (Date)paramObject;
    BitUtils.putLong(paramOutputable, localDate.getTime());
  }

  public void serialze_str_9(Outputable paramOutputable, Object paramObject, IdentityHashMap paramIdentityHashMap)
  {
    if (paramObject == null)
    {
      paramOutputable.putByte((byte)1);
      return;
    }
    paramOutputable.putByte((byte)-32);
    CharSequence localCharSequence = (CharSequence)paramObject;
    String str = localCharSequence.toString();
    byte[] arrayOfByte = str.getBytes();
    BitUtils.putInt(paramOutputable, arrayOfByte.length);
    paramOutputable.putBytes(arrayOfByte);
  }

  public void serialze_bool_10(Outputable paramOutputable, Object paramObject, IdentityHashMap paramIdentityHashMap)
  {
    if (paramObject == null)
    {
      paramOutputable.putByte((byte)1);
      return;
    }
    paramOutputable.putByte((byte)48);
    Boolean localBoolean = (Boolean)paramObject;
    int i;
    if (localBoolean.booleanValue())
      i = 1;
    else
      i = 0;
    paramOutputable.putByte((byte)(0x30 | i));
  }

  public void serialze_map_11(Outputable paramOutputable, Object paramObject, IdentityHashMap paramIdentityHashMap)
  {
    if (paramObject == null)
    {
      paramOutputable.putByte((byte)1);
      return;
    }
    paramOutputable.putByte((byte)-64);
    Map localMap = (Map)paramObject;
    BitUtils.putInt(paramOutputable, localMap.size());
    Iterator localIterator = localMap.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      Object localObject1 = localEntry.getKey();
      serialze_default_12(paramOutputable, localObject1, paramIdentityHashMap);
      Object localObject2 = localEntry.getValue();
      Serializer localSerializer = TransferConfig.getSerializer(localObject2.getClass());
      localSerializer.serialze(paramOutputable, localObject2, paramIdentityHashMap);
    }
  }

  public void serialze_default_12(Outputable paramOutputable, Object paramObject, IdentityHashMap paramIdentityHashMap)
  {
    if (paramObject == null)
    {
      paramOutputable.putByte((byte)1);
      return;
    }
    paramOutputable.putByte((byte)-32);
    CharSequence localCharSequence = (CharSequence)paramObject;
    String str = localCharSequence.toString();
    byte[] arrayOfByte = str.getBytes();
    BitUtils.putInt(paramOutputable, arrayOfByte.length);
    paramOutputable.putBytes(arrayOfByte);
  }

  public void serialze_friends_13(Outputable paramOutputable, Object paramObject, IdentityHashMap paramIdentityHashMap)
  {
    if (paramObject == null)
    {
      paramOutputable.putByte((byte)1);
      return;
    }
    paramOutputable.putByte((byte)-112);
    Collection localCollection = (Collection)paramObject;
    BitUtils.putInt(paramOutputable, localCollection.size());
    Iterator localIterator = localCollection.iterator();
    while (localIterator.hasNext())
    {
      Object localObject = localIterator.next();
      serialze_default_14(paramOutputable, localObject, paramIdentityHashMap);
    }
  }

  public void serialze_default_14(Outputable paramOutputable, Object paramObject, IdentityHashMap paramIdentityHashMap)
  {
    if (paramObject == null)
    {
      paramOutputable.putByte((byte)1);
      return;
    }
    Number localNumber = (Number)paramObject;
    putLongVal(paramOutputable, localNumber);
  }

  public void compile(Type paramType, MethodVisitor paramMethodVisitor, AsmSerializerContext paramAsmContext)
  {
  }
}