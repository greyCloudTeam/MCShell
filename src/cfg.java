
import java.net.*;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.io.*;
import java.util.*;

import javax.crypto.Cipher;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
public class cfg {
	public static boolean commandStop=false;
	public static String username="MCShell";
	public static Map<String,View> allView=new HashMap<String,View>();
	public static int allViewNum=0;
	public static void println(int lv,String msg) {
		String head="[NULL]>";
		if(lv==1) {
			head="[INFO]>";
		}
		if(lv==2) {
			head="[WARNING]>";
		}
		if(lv==3) {
			head="[ERROR]>";
			System.err.println(head+msg);
			return;
		}
		System.out.println(head+msg);
	}
	public static void println(String view,int lv,String msg) {
		String head="[NULL]>";
		if(lv==1) {
			head="[INFO]>";
		}
		if(lv==2) {
			head="[WARNING]>";
		}
		if(lv==3) {
			head="[ERROR]>";
			if(allView.get(view).hide) {
				allView.get(view).leaveMsg+=head+msg+"\n";
			}else {
				System.err.println(head+msg);
			}
			return;
		}
		if(allView.get(view).hide) {
			allView.get(view).leaveMsg+=head+msg+"\n";
		}else {
			System.out.println();
		}
	}

	public static void jumpView(String id) {
		if(!allView.containsKey(id)) {
			println(3,"视图不存在");
			commandStop=false;
			return;
		}
		System.out.println("\n\n\n\n\n\n");
		View temp=allView.get(id);
		temp.hide=false;
		System.out.println(temp.leaveMsg);
		main.path=id;
		commandStop=false;
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
	public static byte[] encryptByPublicKey(byte[] plainData, PublicKey publicKey)
            throws Exception {
        Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(plainData);
    }
	public static int readVarInt(DataInputStream in) throws IOException {
		int numRead = 0 ;
	    int result = 0 ;
	    byte read;
	    do{
	    	read = in.readByte();
	        int value = (read & 0b01111111);
	        result |= (value << ( 7 * numRead));

	        numRead ++;
	        if (numRead > 5 ) {
	            throw new RuntimeException ("VarInt too big");
	        }
	     } while ((read & 0b10000000) != 0);

	     return result;
	}
	public static void pingList(String ip,String port) {
		Socket s;
		InputStream is=null;
		DataInputStream di=null;
		OutputStream os=null;
		DataOutputStream dos=null;
		try {
			int portT=Integer.parseInt(port);
			s=new Socket(ip,portT);
			cfg.println(1,"成功连接服务器！");
			
			is=s.getInputStream();
			di=new DataInputStream(is);
			os=s.getOutputStream();
			dos=new DataOutputStream(os);
			
			cfg.println(1,"准备数据包....");
			ByteArrayOutputStream b ;
			DataOutputStream handshake;
			b= new ByteArrayOutputStream();
			handshake = new DataOutputStream(b);
			handshake.write(0x00);
			cfg.writeVarInt(handshake,-1);//版本号未知
			cfg.writeVarInt(handshake,ip.length()); //ip地址长度
			handshake.writeBytes(ip); //ip
			handshake.writeShort(portT); //port
			cfg.writeVarInt(handshake, 1); //state (1 for handshake)
			byte[] hand=b.toByteArray();
			
			b = new ByteArrayOutputStream();
			handshake = new DataOutputStream(b);
			handshake.write(0x00);
			byte[] pack=b.toByteArray();
			
			/*
			
			*/
			
			cfg.writeVarInt(dos, hand.length); //prepend size
			dos.write(hand); //write handshake packet
			cfg.writeVarInt(dos, pack.length); //prepend size
			dos.write(pack); //write handshake packet
			dos.flush();
			cfg.println(1,"已发送，等待服务器的响应....");
			
			pack ri=new pack(di,false);
			println(1, "接收到数据包\n长度:"+ri.data.length+"\tid:"+ri.id);
			if(ri.id!=0x00) {
				throw new RuntimeException("错误的id");
			}
			String motdT=ri.readString();
			cfg.println(1,"-----------------------------------------------------------------------------");
			JsonParser json=new JsonParser();
            JsonElement part5 = json.parse(motdT);
            JsonElement part6=part5.getAsJsonObject().get("version");
            cfg.println(1,"服务端信息:"+part6.getAsJsonObject().get("name").getAsString());
            cfg.println(1,"协议版本号:"+part6.getAsJsonObject().get("protocol").getAsInt());
            
            JsonElement part1=part5.getAsJsonObject().get("players");
            cfg.println(1,"最大人数:"+part1.getAsJsonObject().get("max").getAsInt());
            cfg.println(1,"在线玩家数:"+part1.getAsJsonObject().get("online").getAsInt());
            String player="";
            JsonElement temp=part1.getAsJsonObject().get("sample");
            if(temp!=null) {
            	JsonArray part7=temp.getAsJsonArray();
                Iterator it=part7.iterator();
                int point=0;
                while(it.hasNext()){
                	if(point==10) {
                		break;
                	}
                    JsonElement e = (JsonElement)it.next();
                    player=player+"\t"+e.getAsJsonObject().get("name").getAsString();
                    point++;
                    //System.out.print(+",");
                }
                cfg.println(1,"玩家列表(仅显示前10名):\n"+player);
            }else {
            	cfg.println(2,"\t玩家列表不存在或没有玩家");
            }
            
            //println(1,"description:"+) <-WARNING!!!!!!!
            cfg.println(1,"-----------------------------------------------------------------------------");
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			cfg.println(1,"ping....");
			ByteArrayOutputStream b ;
			DataOutputStream handshake;
			b= new ByteArrayOutputStream();
			handshake = new DataOutputStream(b);
			handshake.write(0x01);
			handshake.writeLong(System.currentTimeMillis());
			byte[] ping=b.toByteArray();
			
			cfg.writeVarInt(dos, ping.length); //prepend size
			dos.write(ping); //write handshake packet
			dos.flush();
			
			pack ri=new pack(di,false);
			println(1, "接收到数据包\n长度:"+ri.data.length+"\tid:"+ri.id);
			if(ri.id!=0x01) {
				throw new RuntimeException("错误的id");
			}
			cfg.println(1,"服务器延迟,"+(System.currentTimeMillis()-ri.readLong())+"ms");
		}catch(RuntimeException e) {
			e.printStackTrace();
		}catch(Exception e) {
			cfg.println(2,"服务器有可能禁止了ping");
		}
		
		cfg.println(1,"完成!");
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		cfg.commandStop=false;
	}

}
