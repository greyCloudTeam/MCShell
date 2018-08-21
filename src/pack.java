import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class pack {
	public byte[] data=null;
	public int id=-1;
	public int point=0;//当前位置，读数据的时候也要包括这个
	private static ByteBuffer buffer = ByteBuffer.allocate(8); 
	public pack(byte[] data) {
		this.data=data;
		try {
			id=readVarInt();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	public pack(DataInputStream in,boolean compress) {
		try {
			int length=cfg.readVarInt(in);
			this.data=new byte[length];
			in.readFully(data);
			if(compress) {
				int trueLength=readVarInt();
				if(trueLength!=0) {
					byte[] temp=getAllData();
					byte[] then=ZLibUtils.decompress(data);
					this.data=then;
					point=0;
				}
			}
			id=readVarInt();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
	}
	public String readString() {//有了这个就方便多了
		String re="";
		try {
			int length=readVarInt();
			byte[] by=new byte[length];
			readFully(by);
			re=new String(by);
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		return re;
	}
	public byte[] getAllData() {
		byte[] data=new byte[this.data.length-point];//0-24 0-10 14``
		readFully(data);
		return data;
	}
	public int readInt() {
		byte[] temp=new byte[4];
		readFully(temp);
		return ByteArray2Int(temp);
	}
	public short readShort() {
		byte[] temp=new byte[2];
		readFully(temp);
		return byteToShort(temp);
	}
	public long readLong() {
		byte[] temp=new byte[8];
		readFully(temp);
		return bytesToLong(temp);
	}
	public int readUnsignedByte() {
		Byte b=readByte();
		Integer i=b.intValue();
		Integer i_trans=i&0xFF;
		return i_trans;
	}
	public boolean readBoolean() {
		byte temp=readByte();
		if(temp==0x00) {
			return false;
		}
		if(temp==0x01) {
			return true;
		}
		return false;
	}
	public static long bytesToLong(byte[] bytes) {
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip 
        return buffer.getLong();
    }
	public static byte[] longToBytes(long x) {
        buffer.putLong(0, x);
        return buffer.array();
    }
	public static short byteToShort(byte[] b) {
        short s = 0;
        short s0 = (short) (b[0] & 0xff);// 最低位
        short s1 = (short) (b[1] & 0xff);
        s1 <<= 8;
        s = (short) (s0 | s1);
        return s;
    }
	public static int ByteArray2Int(byte[] data) {
        if (data == null || data.length < 4) {
            return 0xDEADBEEF;
        }
        return ByteBuffer.wrap(data, 0, 4).getInt();
    }
	public byte readByte() {
		byte re=data[point];
		point++;
		return re;
	}
	public int readVarInt() throws IOException {
		int numRead = 0 ;
	    int result = 0 ;
	    byte read;
	    do{
	    	read = readByte();
	        int value = (read & 0b01111111);
	        result |= (value << ( 7 * numRead));

	        numRead ++;
	        if (numRead > 5 ) {
	            throw new RuntimeException ("VarInt too big");
	        }
	     } while ((read & 0b10000000) != 0);

	     return result;
	}
	public int readVarInt(int num) throws IOException {
		int numRead = 0 ;
	    int result = 0 ;
	    byte read;
	    do{
	    	read = readByte();
	    	num++;
	        int value = (read & 0b01111111);
	        result |= (value << ( 7 * numRead));

	        numRead ++;
	        if (numRead > 5 ) {
	            throw new RuntimeException ("VarInt too big");
	        }
	     } while ((read & 0b10000000) != 0);

	     return result;
	}
	public void readFully(byte[] by) {
		for(int i=0;by.length>i;i++) {
			by[i]=readByte();
		}
	}
}
