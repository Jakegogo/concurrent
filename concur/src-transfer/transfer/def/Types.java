package transfer.def;

public interface Types {
	// 1111 #### (240 - (byte)0xF0)
	byte OBJECT = (byte) 0xF0;
	// 1110 #### (234 - (byte)0xE0)
	byte STRING = (byte) 0xE0;
	// 1101 #### (218 - (byte)0xD0)
	byte ARRAY = (byte) 0xD0;
	// 1100 #### (192 - (byte)0xC0)
	byte MAP = (byte) 0xC0;
	// 1011 #### (176 - (byte)0xB0)
	byte BYTE_ARRAY = (byte) 0xB0;
	// 1010 #### (160 - (byte)0xA0)
	byte DATE_TIME = (byte) 0xA0;
	// 1001 #### (144 - (byte)0x90)
	byte COLLECTION = (byte) 0x90;
	// 1000 #### (128 - (byte)0x80)
	// 0111 #### (112 - (byte)0x70)
	// 0110 #### (96 - (byte)0x60)
	// 0101 #### (80 - (byte)0x50)
	byte ENUM = (byte) 0x50;
	// 0100 #### (64 - (byte)0x40)
	// 0011 #### (48 - (byte)0x30),
	// 0010 #### (32 - (byte)0x20),
	byte BOOLEAN = (byte) 0x20;
	// 0001 #### (16 - (byte)0x10),
	byte NUMBER = (byte) 0x10;
	// 0000 0001 (1 - (byte)0x01)
	byte NULL = (byte) 0x01;
	// 0000 0000 (0 - (byte)0x00)
	byte UNKOWN = (byte) 0x00;

}
