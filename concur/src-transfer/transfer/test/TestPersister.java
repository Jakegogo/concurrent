package transfer.test;

import transfer.ByteArray;
import transfer.Persister;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;

/**
 * Created by Administrator on 2015/2/26.
 */
public class TestPersister {

    public static void main(String[] args) throws IOException {

//        Config.registerClass(Entity.class, 1);

        Entity entity = new Entity();
        entity.setId(System.currentTimeMillis());
        entity.setUid(-101);
        entity.setFval(2.35f);
        entity.setStatus(AcountStatus.OPEN);
        entity.setDate(new Date());
        entity.setStr("jake");
        entity.setBool(true);
        entity.getFriends().add(1l);
        entity.getFriends().add(2l);
        entity.getFriends().add(3l);
        entity.setA(null);

        ByteArray byteArray = Persister.encode(entity, 187);

        byte[] bytes = byteArray.toBytes();
        System.out.println(bytes);
        System.out.println("persist length:" + bytes.length);
        writeFile("entityBytes", bytes);
        

        byte[] readBytes = readFile("entityBytes");
        Entity entity1 = Persister.decode(readBytes, Entity.class);
        System.out.println(entity1);
        System.out.println(entity1.getId());
        System.out.println(entity1.getUid());
        System.out.println(entity1.getFriends());
        System.out.println(entity1.getStatus());
        System.out.println(entity1.getDate());
        System.out.println(entity1.getStr());
        System.out.println(entity1.getBool());
        System.out.println(entity1.getFval());



        ByteArrayOutputStream bo=new ByteArrayOutputStream();
        ObjectOutputStream oo=new ObjectOutputStream(bo);
        oo.writeObject(entity);
        System.out.println("Serialize length:" + bo.size());

    }
    
    
    public static void writeFile(String name, byte[] data) {
    	FileOutputStream fout = null;
		try {
			File file = new File("C:\\" + name);
			fout = new FileOutputStream(file);

			fout.write(data);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
			if (fout != null) {
				try {
					fout.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
    
    public static byte[] readFile(String name) {
    	FileInputStream fin = null;
		try {
			File file = new File("C:\\" + name);
			fin = new FileInputStream(file);
			
			byte[] bytes = new byte[fin.available()];
			fin.read(bytes);
			fin.close();
			return bytes;
		} catch (Exception e) {
			e.printStackTrace();
			if (fin != null) {
				try {
					fin.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return null;
	}

}
;