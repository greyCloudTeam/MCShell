import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class acceptPack {
	public byte[] data=null;
	public int id=-1;
	public int point=0;//当前位置，读数据的时候也要包括这个
	private static ByteBuffer buffer = ByteBuffer.allocate(8); 
	public byte[] thenData=null;
	public acceptPack(byte[] data) {
		this.data=data;
		try {
			id=readVarInt();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	public acceptPack(DataInputStream in,boolean compress) throws IOException {
			int length=cfg.readVarInt(in);
			this.data=new byte[length];
			in.readFully(data);
			this.thenData=data;
			if(compress) {
				int trueLength=readVarInt();
				if(trueLength!=0) {
					byte[] temp=getAllData();
					byte[] then=ZLibUtils.decompress(temp);
					this.data=then;
					point=0;
				}
			}
			id=readVarInt();
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
	public double readDouble() {
		byte[] temp=new byte[8];
		readFully(temp);
		return bytes2Double(temp);
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
	public float readFloat() {
		byte[] temp=new byte[4];
		readFully(temp);
		return byte2float(temp);
	}
	/**
	 * 字节转换为浮点
	 * 
	 * @param b 字节（至少4个字节）
	 * @param index 开始位置
	 * @return
	 */
	public static float byte2float(byte[] b) {  
		ByteBuffer buf=ByteBuffer.allocateDirect(4); //无额外内存的直接缓存
		//buf=buf.order(ByteOrder.LITTLE_ENDIAN);//默认大端，小端用这行
		buf.put(b);
		buf.rewind();
		float f2=buf.getFloat();       
		return f2;
	}
	public static double bytes2Double(byte[] arr) {
		long value = 0;
		for (int i = 0; i < 8; i++) {
			value |= ((long) (arr[i] & 0xff)) << (8 * i);
		}
		return Double.longBitsToDouble(value);
	}
	public static long bytesToLong(byte[] bytes) {
		buffer.clear();
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
