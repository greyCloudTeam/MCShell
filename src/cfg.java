
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
	public static ArrayList<String> NP=new ArrayList<String>();
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
			System.out.println(head+msg);
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
		View now=allView.get(main.path);
		if(now!=null)
			now.hide=true;
		temp.hide=false;
		System.out.println(temp.leaveMsg);
		main.path=id;
		commandStop=false;
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
	public static void ping(String ip,String port) {
		Socket s;
		InputStream is=null;
		DataInputStream di=null;
		OutputStream os=null;
		DataOutputStream dos=null;
		try {
			int portT=Integer.parseInt(port);
			s=new Socket(ip,portT);
			is=s.getInputStream();
			di=new DataInputStream(is);
			os=s.getOutputStream();
			dos=new DataOutputStream(os);
			cfg.println(1,"ping....");
			
			sendPack ping=new sendPack(dos,0x01);
			ping.thisPack.writeLong(System.currentTimeMillis());
			
			ping.sendPack(false,-1);
			dos.flush();
			
			acceptPack ri=new acceptPack(di,false);
			println(1, "接收到数据包，长度:"+ri.data.length+"，id:"+ri.id);
			if(ri.id!=0x01) {
				throw new RuntimeException("接收到的数据包不正确！");
			}
			cfg.println(1,"服务器延迟,"+(System.currentTimeMillis()-ri.readLong())+"ms");
		}catch(RuntimeException e) {
			e.printStackTrace();
		}catch(Exception e) {
			cfg.println(2,"服务端可能禁止了ping");
		}
		
		cfg.println(1,"完成！");
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO 閼奉亜濮╅悽鐔稿灇閻拷 catch 閸э拷
			e.printStackTrace();
		}
		cfg.commandStop=false;
	}
	public static void pingList(String ip,String port) {
		Socket s;
		InputStream is=null;
		DataInputStream di=null;
		OutputStream os=null;
		DataOutputStream dos=null;
		try {
			//System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
			int portT=Integer.parseInt(port);
			s=new Socket(ip,portT);
			is=s.getInputStream();
			di=new DataInputStream(is);
			os=s.getOutputStream();
			dos=new DataOutputStream(os);
			cfg.println(1,"成功建立连接...");
			cfg.println(1,"发送获取motd请求....");
			
			sendPack hand=new sendPack(dos,0x00);
			hand.writeVarInt(-1);
			hand.writeString(ip);
			hand.thisPack.writeShort(portT);
			hand.writeVarInt(1);
			
			sendPack pack=new sendPack(dos,0x00);
			
			sendPack.writeVarInt(dos,hand.b.toByteArray().length);
			dos.write(hand.b.toByteArray());
			
			sendPack.writeVarInt(dos,pack.b.toByteArray().length);
			dos.write(pack.b.toByteArray());

			dos.flush();
			cfg.println(1,"已发送，正在等待服务端的响应....");
			
			acceptPack ri=new acceptPack(di,false);
			println(1, "接收到数据，长度:"+ri.data.length+"，id:"+ri.id);
			if(ri.id!=0x00) {
				throw new RuntimeException("收到的数据包不正确！");
			}
			String motdT=ri.readString();
			println(1,motdT);
			//System.out.println(motdT);
			//System.out.println(motdT);
			//System.out.println(motdT);
			cfg.println(1,"-----------------------------------------------------------------------------");
			JsonParser json=new JsonParser();
            JsonElement part5 = json.parse(motdT);
            JsonElement part6=part5.getAsJsonObject().get("version");
            cfg.println(1,"服务端信息:"+part6.getAsJsonObject().get("name").getAsString());
            cfg.println(1,"协议版本号:"+part6.getAsJsonObject().get("protocol").getAsInt());
            
            JsonElement part1=part5.getAsJsonObject().get("players");
            cfg.println(1,"最大人数:"+part1.getAsJsonObject().get("max").getAsInt());
            cfg.println(1,"在线人数:"+part1.getAsJsonObject().get("online").getAsInt());
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
                cfg.println(1,"玩家列表(仅显示前10个玩家):\n"+player);
            }else {
            	cfg.println(2,"\t玩家列表不存在");
            }
            
            //println(1,"description:"+) <-WARNING!!!!!!!
            cfg.println(1,"-----------------------------------------------------------------------------");
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			cfg.println(1,"ping....");
			while(true) {
			sendPack ping=new sendPack(dos,0x01);
			ping.thisPack.writeLong(System.currentTimeMillis());
			
			ping.sendPack(false,-1);
			dos.flush();
			
			acceptPack ri=new acceptPack(di,false);
			println(1, "接收到数据包，长度:"+ri.data.length+"，id:"+ri.id);
			if(ri.id!=0x01) {
				throw new RuntimeException("接收到的数据包不正确！");
			}
			cfg.println(1,"服务器延迟,"+(System.currentTimeMillis()-ri.readLong())+"ms");
			}
		}catch(RuntimeException e) {
			e.printStackTrace();
		}catch(Exception e) {
			cfg.println(2,"服务端可能禁止了ping");
		}
		
		cfg.println(1,"完成！");
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO 閼奉亜濮╅悽鐔稿灇閻拷 catch 閸э拷
			e.printStackTrace();
		}
		cfg.commandStop=false;
	}
}
