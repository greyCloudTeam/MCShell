package withGui;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Iterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class User {
	public boolean yanzheng=true;
	public ChannelHandlerContext ctx;
	public void pingList(String ip,String port) {
		Socket s;
		InputStream is=null;
		DataInputStream di=null;
		OutputStream os=null;
		DataOutputStream dos=null;
		boolean flag=false;
		try {
			int portT=Integer.parseInt(port);
			s=new Socket(ip,portT);
			is=s.getInputStream();
			di=new DataInputStream(is);
			os=s.getOutputStream();
			dos=new DataOutputStream(os);
			//cfg.println(1,"成功建立连接...");
			//cfg.println(1,"发送获取motd请求....");
			ctx.channel().writeAndFlush(new TextWebSocketFrame("{\"id\":4,\"name\":\"listPing\",\"msg\":\"成功建立连接，正在获取\"}"));
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
			//cfg.println(1,"已发送，正在等待服务端的响应....");
			
			acceptPack ri=new acceptPack(di,false);
			//println(1, "接收到数据，长度:"+ri.data.length+"，id:"+ri.id);
			ctx.channel().writeAndFlush(new TextWebSocketFrame("{\"id\":4,\"name\":\"listPing\",\"msg\":\"接收到来自服务器的数据，正在处理\"}"));
			if(ri.id!=0x00) {
				//throw new RuntimeException("收到的数据包不正确！");
				ctx.channel().writeAndFlush(new TextWebSocketFrame("{\"id\":5,\"name\":\"listPing\",\"msg\":\"接收到的数据包不正确\"}"));
			}
			String motdT=ri.readString();
			JsonParser json=new JsonParser();
            JsonElement part5 = json.parse(motdT);
            System.out.println("listPing"+ctx.channel().writeAndFlush(new TextWebSocketFrame(motdT)));
			/*
			//cfg.println(1,"-----------------------------------------------------------------------------");
			JsonParser json=new JsonParser();
            JsonElement part5 = json.parse(motdT);
            JsonElement part6=part5.getAsJsonObject().get("version");
            //cfg.println(1,"服务端信息:"+part6.getAsJsonObject().get("name").getAsString());
            //cfg.println(1,"协议版本号:"+part6.getAsJsonObject().get("protocol").getAsInt());
            
            JsonElement part1=part5.getAsJsonObject().get("players");
            //cfg.println(1,"最大人数:"+part1.getAsJsonObject().get("max").getAsInt());
            //cfg.println(1,"在线人数:"+part1.getAsJsonObject().get("online").getAsInt());
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
                //cfg.println(1,"玩家列表(仅显示前10个玩家):\n"+player);
            }else {
            	//cfg.println(2,"\t玩家列表不存在");
            }
            */
            //println(1,"description:"+) <-WARNING!!!!!!!
            //cfg.println(1,"-----------------------------------------------------------------------------");
		}catch(Exception e) {
			ctx.channel().writeAndFlush(new TextWebSocketFrame("{\"id\":5,\"name\":\"listPing\",\"msg\":\"获取motd时发生错误："+e.getMessage()+"\"}"));
			flag=true;
		}
		if(flag) {
			return;
		}
		try {
			//cfg.println(1,"ping....");
			
			sendPack ping=new sendPack(dos,0x01);
			ping.thisPack.writeLong(System.currentTimeMillis());
			
			ping.sendPack(false,-1);
			dos.flush();
			
			acceptPack ri=new acceptPack(di,false);
			//println(1, "接收到数据包，长度:"+ri.data.length+"，id:"+ri.id);
			if(ri.id!=0x01) {
				throw new RuntimeException("接收到的数据包不正确！");
			}
			//cfg.println(1,"服务器延迟,"+(System.currentTimeMillis()-ri.readLong())+"ms");
		}catch(RuntimeException e) {
			ctx.channel().writeAndFlush(new TextWebSocketFrame("{\"id\":5,\"name\":\"listPing\",\"msg\":\"ping时发生错误"+e.getMessage()+"\"}"));
		}catch(Exception e) {
			//cfg.println(2,"服务端可能禁止了ping");
		}
		/*
		//cfg.println(1,"完成！");
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO 閼奉亜濮╅悽鐔稿灇閻拷 catch 閸э拷
			e.printStackTrace();
		}
		*/
		//cfg.commandStop=false;
	}
}
