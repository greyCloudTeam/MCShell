import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;

public class sendPack {
	DataOutputStream sendStream=null;
	public ByteArrayOutputStream b;
	public DataOutputStream thisPack;
	
	public sendPack(DataOutputStream sendStream,int id) throws IOException {
		this.sendStream=sendStream;
		b = new ByteArrayOutputStream();
		thisPack = new DataOutputStream(b);
		writeVarInt(thisPack,id);
	}
	public static void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
		while (true) {
			if ((paramInt & 0xFFFFFF80) == 0) {
				out.writeByte(paramInt);
				return;
			}
			out.writeByte(paramInt & 0x7F | 0x80);
			paramInt >>>= 7;
		}
	}
	public void writeVarInt(int data) throws IOException {
		sendPack.writeVarInt(thisPack, data);
	}
	public void writeString(String data) throws IOException {
		writeVarInt(data.length());
		thisPack.writeBytes(data);
	}
	public void sendPack(boolean compress,int maxSize) throws IOException {
		byte[] data=b.toByteArray();
		if(compress) {
			ByteArrayOutputStream b ;
			DataOutputStream handshake;
			b= new ByteArrayOutputStream();
			handshake = new DataOutputStream(b);
			if(data.length>=maxSize) {
				writeVarInt(handshake,data.length);
				byte[] lengthData=b.toByteArray();
				data=ZLibUtils.compress(data);
				writeVarInt(sendStream,lengthData.length+data.length);
				sendStream.write(lengthData);
				sendStream.write(data);
			}else {
				writeVarInt(handshake,0);
				byte[] lengthData=b.toByteArray();
				writeVarInt(sendStream,lengthData.length+data.length);
				sendStream.write(lengthData);
				sendStream.write(data);
			}
		}else {
			writeVarInt(sendStream,data.length);
			sendStream.write(data);
		}
	}
}
