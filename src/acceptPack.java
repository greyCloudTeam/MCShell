import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class acceptPack {
	public byte[] data=null;
	public int id=-1;
	public int point=0;
	private static ByteBuffer buffer = ByteBuffer.allocate(8); 
	public byte[] thenData=null;
	public acceptPack(byte[] data) {
		this.data=data;
		try {
			id=readVarInt();
		} catch (IOException e) {
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
	public int readIntA() {
		double a=readDouble();
		return (int)(a*32.0D);
	}
	public String readString() {
		String re="";
		try {
			int length=readVarInt();
			byte[] by=new byte[length];
			readFully(by);
			re=new String(by);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return re;
	}
	public byte[] getAllData() {
		byte[] data=new byte[this.data.length-point];
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
	public static float byte2float(byte[] b) {  
		ByteBuffer buf=ByteBuffer.allocateDirect(4);
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
		// 将byte[] 封装为 ByteBuffer 
        ByteBuffer buffer = ByteBuffer.wrap(bytes,0,8);
        return buffer.getLong();  
    }
	public static byte[] longToBytes(long x) {
        buffer.putLong(0, x);
        return buffer.array();
    }
	public static short byteToShort(byte[] b) {
        short s = 0;
        short s0 = (short) (b[0] & 0xff);
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
	public static double int2FPN(int value) {
		return (double)(value / 32.0D);
	}
	public static double byte2FPN(byte value) {
		return (double)(value / 32.0D);
	}
	public static String toChat(String data) {
		String value="";
		JsonParser json=new JsonParser();
        JsonElement part5 = json.parse(data);
        JsonElement temp=part5.getAsJsonObject().get("extra");
        if(temp!=null) {
        	JsonArray part7=temp.getAsJsonArray();
            Iterator it=part7.iterator();
            while(it.hasNext()){
                JsonElement e = (JsonElement)it.next();
                try{
                	value+=e.getAsJsonObject().get("text").getAsString();
                }catch(Exception err) {
                	value+=e.getAsString();
                }
            }
        }
        return value;
	}
}
