import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Iterator;

import javax.crypto.Cipher;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class View extends Thread{
	public String name="Unkown-View";
	public boolean compression=false;
	public int maxPackSize=-1;
	public final int MODE_PLAY=2;
	public final int MODE_LOGIN=1;
	public final int MODE_LEAVE=3;
	public int mode=MODE_LOGIN;
	public String username="MCShell";
	public String uuid="";
	public String ip="";
	public int port=0;
	public Socket client;
	public InputStream is=null;
	public DataInputStream di=null;
	public OutputStream os=null;
	public DataOutputStream dos=null;
	public int version=-1;
	public boolean stop=false;
	public boolean hide=true;
	public String leaveMsg="";
	public boolean showC=false;
	public int gamemode=0;
	public long[] time= {0,0};
	public boolean[] lv= {true,false,false,false};//msg pos obj detail
	
	@Override
	public void run() {
		cfg.commandStop=false;
		try {
			client=new Socket(ip,port);
			cfg.println(name,1,"成功连接服务器");
			is=client.getInputStream();
			di=new DataInputStream(is);
			os=client.getOutputStream();
			dos=new DataOutputStream(os);
			
			cfg.println(name,1,"准备数据包....");
			
			sendPack hand=new sendPack(dos,0x00);
			hand.writeVarInt(version);
			hand.writeString(ip);
			hand.thisPack.writeShort(port);
			hand.writeVarInt(2);
			
			sendPack username=new sendPack(dos,0x00);
			username.writeString(cfg.username);
			
			hand.sendPack(false, -1);
			dos.flush();
			cfg.println(name,1,"已发送登陆包....");
			
			username.sendPack(false, -1);
			dos.flush();
			cfg.println(name,1,"等待响应....");
			while(!stop) {
				try {
					acceptPack ri=new acceptPack(di,compression);
					if(lv[3])
						cfg.println(name,1, "接收到数据包，长度:"+ri.data.length+",id:"+ri.id);
					if(mode==MODE_LOGIN) {
						if(ri.id==0x00) {
							cfg.println(name,3,"不能连接到这个服务器:\n"+ri.readString());
							break;
						}else if(ri.id==0x02) {
							uuid=ri.readString();
							this.username=ri.readString();
							mode=MODE_PLAY;
							
							cfg.println(name,1,"登陆成功!\n"+
									"uuid:"+uuid+"\n"+
									"username:"+this.username);
						}else if(ri.id==0x01) {
							cfg.println(name,3,"服务器开启了正版验证");
							break;
						}
					}else if(mode==MODE_PLAY) {
						if(ri.id==0x00) {
							int id=ri.readInt();
							sendPack p=new sendPack(dos,0x00);
							p.thisPack.writeInt(id);
							p.sendPack(false, -1);
						}else if(ri.id==0x01) {//join game
							cfg.println(name, 2,"连接到游戏");
							int eid=ri.readInt();
							int gamemode=ri.readUnsignedByte();
							this.gamemode=gamemode;
							String modeT="未知:"+gamemode;
							if(gamemode==0)
								modeT="生存";
							if(gamemode==1)
								modeT="创造";
							if(gamemode==2)
								modeT="冒险";
							if(gamemode==3)
								modeT="旁观";
							byte world=ri.readByte();
							String worldT="未知:"+world;
							if(world==-1)
								worldT="虚空";
							if(world==0)
								worldT="主世界";
							if(world==1)
								worldT="下界";
							int dif=ri.readUnsignedByte();
							String difT="未知:"+dif;
							if(dif==0)
								difT="和平";
							if(dif==1)
								difT="简单";
							if(dif==2)
								difT="普通";
							if(dif==3)
								difT="困难";
							int max=ri.readUnsignedByte();
							String type=ri.readString();
							if(type.equals("default"))
								type="普通，默认";
							if(type.equals("flat"))
								type="平坦";
							if(type.equals("largeBiomes"))
								type="大型生物群系";
							if(type.equals("default_1_1"))
								type="默认1_1";
							cfg.println(this.name,1,"服务器地图信息"+
											"实体id:"+eid+"\n"+
											"游戏模式(gamemode):"+modeT+"\n"+
											"当前维度:"+worldT+"\n"+
											"难度:"+difT+"\n"+
											"最大玩家:"+max+"\n"+
											"地图类型:"+type);
							//command.run(main.login);
						}else if(ri.id==0x02) {
							String msg=ri.readString();
							if(lv[0])
								cfg.println(this.name, 1,"聊天信息:"+acceptPack.toChat(msg));
						}else if(ri.id==0x03) {
							time[0]=ri.readLong();
							time[1]=ri.readLong();
						}else if(ri.id==0x04) {
							int eid=ri.readInt();
							short slot=ri.readShort();
							String slotS="未知";
							if(slot==0)
								slotS="持有";
							if(slot==1)
								slotS="靴子";
							if(slot==2)
								slotS="绑腿";
							if(slot==3)
								slotS="胸铠";
							if(slot==4)
								slotS="头盔";
							short id=ri.readShort();
							if(id!=-1) {
								byte num=ri.readByte();
								short s=ri.readShort();
								if(lv[2])
									cfg.println(this.name, 1,"实体装备更新:\n"+
												"实体id:"+eid+"\t插槽:"+slot+"\t块id:"+id+"\t物品数量:"+num+"\t块id:"+s);
								continue;
							}
							if(lv[2])
								cfg.println(this.name, 1,"实体装备更新:\n"+
										"实体id:"+eid+"\t插槽:"+slot+"\t块id:空");
						}else if(ri.id==0x05) {
							if(lv[1])
								cfg.println(this.name, 2,"位置更新！你当前的位置:\n"+
											"x:"+ri.readInt()+"\ty:"+ri.readInt()+"\tz:"+ri.readInt());
						}else if(ri.id==0x06){
							float heal=ri.readFloat();
							short food=ri.readShort();
							float bh=ri.readFloat();
							if(heal<=0){
								cfg.println(this.name, 2,"你已经死亡");
								continue;
							}
							cfg.println(this.name, 2,"你的血量/饥饿值更新!\n"+
										"血量:"+heal+"\t饥饿值:"+food+"\t饥饿值饱和度:"+bh);
						}else if(ri.id==0x07) {
							int world=ri.readInt();
							String worldT="未知:"+world;
							if(world==-1)
								worldT="虚空";
							if(world==0)
								worldT="主世界";
							if(world==1)
								worldT="下界";
							int dif=ri.readUnsignedByte();
							String difT="未知:"+dif;
							if(dif==0)
								difT="和平";
							if(dif==1)
								difT="简单";
							if(dif==2)
								difT="普通";
							if(dif==3)
								difT="困难";
							
							int gamemode=ri.readUnsignedByte();
							this.gamemode=gamemode;
							String modeT="未知:"+gamemode;
							if(gamemode==0)
								modeT="生存";
							if(gamemode==1)
								modeT="创造";
							if(gamemode==2)
								modeT="冒险";
							if(gamemode==3)
								modeT="旁观";
							
							String type=ri.readString();//閸欘垯浜掗張濉猠fault, flat, largeBiomes, amplified, default_1_1閿涘苯顕惔鏃撶窗姒涙顓婚敍灞介挬閸э讣绱濇径褍鐎烽悽鐔哄⒖缂囥倗閮撮敍灞藉嚒閺�鎯с亣閿涘矂绮拋顦�1_1
							if(type.equals("default"))
								type="普通，默认";
							if(type.equals("flat"))
								type="平坦";
							if(type.equals("largeBiomes"))
								type="大型生物群系";
							if(type.equals("default_1_1"))
								type="默认1_1";
							
							cfg.println(this.name,2,"服务端要求更改维度:\n"+
										"维度:"+worldT+"\t难度:"+difT+"\t游戏模式:"+modeT+"\t地图类型:"+type);
						}else if(ri.id==0x08) { 
							double x=ri.readDouble();
							double y=ri.readDouble();
							double z=ri.readDouble();
							float ph=ri.readFloat();
							float p=ri.readFloat();
							boolean d=ri.readBoolean();
							if(lv[1])
								cfg.println(this.name, 2,"你的位置被更改:\n"+
														"X:"+x+"\tY:"+y+"\tZ:"+z+"\tX旋转:"+ph+"\tY旋转:"+p+"\t在地上:"+d);
						}else if(ri.id==0x09) {
							byte i=ri.readByte();
							if(lv[2])
								cfg.println(this.name,2,"你选择的在物品栏中的物品索引被更改:"+i);
						}else if(ri.id==0x0A) {
							int eid=ri.readInt();
							int x=ri.readInt();
							int y=ri.readUnsignedByte();
							int z=ri.readInt();
							cfg.println(this.name,1,"有玩家或你自己到床上睡觉了:\n"+
												"eid:"+eid+"\tX:"+x+"\tY:"+y+"\tZ:"+z);
						}else if(ri.id==0x0B) {
							int eid=ri.readVarInt();
							int id=ri.readUnsignedByte();
							String idT="未知";
							if(id==0)
								idT="摆臂";
							if(id==1)
								idT="受伤";
							if(id==2)
								idT="离开床";
							if(id==3)
								idT="吃东西";
							if(id==4)
								idT="不断掉血";
							if(id==5)
								idT="中了药水";
							if(id==104)
								idT="蹲伏";
							if(id==105)
								idT="起来";
							if(lv[2])
								cfg.println(this.name, 1,"实体动画需要更改:\n"+
												"实体id:"+eid+"\t动画:"+idT);
						}else if(ri.id==0x0C) {
							int eid=ri.readVarInt();
							String uuid=ri.readString();
							String name=ri.readString();
							double x=acceptPack.int2FPN(ri.readInt());
							double y=acceptPack.int2FPN(ri.readInt());
							double z=acceptPack.int2FPN(ri.readInt());
							byte ph=ri.readByte();
							byte p=ri.readByte();
							short item=ri.readShort();
							if(lv[1])
								cfg.println(this.name, 1,"玩家进入当前可见范围:\n"+
												"实体id:"+eid+"\t玩家uuid:"+uuid+"\t玩家名字:"+name+"\n"+
												"X:"+x+"\tY:"+y+"\tZ:"+z+"\tX旋转:"+ph+"\tY旋转:"+p+"\t手上的物品:"+item);
						}else if(ri.id==0x0D) {
							int eid=ri.readInt();
							int item=ri.readInt();
							if(lv[2])
								cfg.println(this.name, 1,"有人拾取物品，实体id:"+eid+",物品实体id:"+item);
						}else if(ri.id==0x11) {
							if(lv[2])
								cfg.println(this.name,1,"服务器产出经验"+
												"实体id:"+ri.readVarInt()+"\t坐标:\n"+
												"x:"+acceptPack.int2FPN(ri.readInt())+"\ty:"+acceptPack.int2FPN(ri.readInt())+"\tz:"+acceptPack.int2FPN(ri.readInt())+"\n"+
												"数量:"+ri.readShort());
						}else if(ri.id==0x12) {
							if(lv[2])
								cfg.println(this.name,1,"服务端设置实体速度:\n"+
												"实体id:"+ri.readInt()+"\tX速度:"+ri.readShort()+"\tY速度:"+ri.readShort()+"\tZ速度:"+ri.readShort());
						}else if(ri.id==0x13) {
							if(lv[2])
								cfg.println(this.name, 1,"实体被摧毁，数量："+ri.readByte());
						}else if(ri.id==0x15) {
							if(lv[1])
								cfg.println(this.name, 1,"实体相对移动，实体id:"+ri.readInt()+"，坐标:\n"+
												"X:"+acceptPack.byte2FPN(ri.readByte())+"\tY:"+acceptPack.byte2FPN(ri.readByte())+"\tZ"+acceptPack.byte2FPN(ri.readByte()));
						}else if(ri.id==0x16) {
							if(lv[1])
								cfg.println(this.name,1,"实体旋转，实体id："+ri.readInt()+",X旋转:"+ri.readByte()+",Y旋转:"+ri.readByte());
						}else if(ri.id==0x17) {
							if(lv[1])
								cfg.println(this.name, 1,"实体旋转+移动"+ri.readInt()+",坐标:\n" + 
												"X:"+acceptPack.byte2FPN(ri.readByte())+"\tY:"+acceptPack.byte2FPN(ri.readByte())+"\tZ"+acceptPack.byte2FPN(ri.readByte())+"\n"+
												"X旋转:"+ri.readByte()+"\tY旋转:"+ri.readByte());
						}else if(ri.id==0x18) {
							if(lv[1])
								cfg.println(this.name,1,"实体移动超过4个块:"+ri.readInt()+",坐标:\n"+
												"X:"+acceptPack.int2FPN(ri.readInt())+"\tY:"+acceptPack.int2FPN(ri.readInt())+"\tZ:"+acceptPack.int2FPN(ri.readInt())+"\n"+
												"X旋转:"+ri.readByte()+"\tY旋转:"+ri.readByte());
							
						}else if(ri.id==0x19) {
							if(lv[1])
								cfg.println(this.name,1,"实体头部旋转，实体id:"+ri.readInt()+"，X旋转:"+ri.readByte());
						}else if(ri.id==0x1A) {
							int eid=ri.readInt();
							byte type=ri.readByte();
							String t="未知";
							if(type==0)
								t="与生物有关的东西";
							if(type==1)
								t="受伤";
							if(type==2)
								t="与玩家实体有关的东西";
							if(type==3)
								t="死亡";
							if(type==6)
								t="驯服狼中..";
							if(type==7)
								t="狼被驯服了";
							if(type==8)
								t="狼自己抖水";
							if(type==9)
								t="(自我)服务器接受的饮食";
							if(type==10)
								t="绵羊正在吃草";
							if(type==11)
								t="傀儡交出玫瑰";
							if(type==12)
								t="村民产生爱心";
							if(type==13)
								t="一个村民生气并寻求报复";
							if(type==14)
								t="村民很快乐";
							if(type==15)
								t="女巫施法";
							if(type==16)
								t="僵尸剧烈摇晃变成村民";
							if(type==17)
								t="烟花爆炸";
							if(type==18)
								t="爱上人类";
							if(lv[2])
								cfg.println(this.name, 1,"实体状态被改变，实体id:"+eid+",改变后状态:"+t);
						}else if(ri.id==0x1F) {
							cfg.println(this.name,1,"你的等级或经验改变，经验条:"+ri.readFloat()+"/1,等级:"+ri.readShort()+"，总经验:"+ri.readShort());
						}else if(ri.id==0x27) {
							cfg.println(this.name, 2,"发生了爆炸:\n"+
												"X:"+ri.readFloat()+"\tY:"+ri.readFloat()+"\tZ:"+ri.readFloat()+"\t半径:"+ri.readFloat());
						}else if(ri.id==0x28) {
							int m=ri.readInt();
							String text="未知";
							if(m==1000||m==1001)
								text="点击";
							if(m==1002)
								text="bow";
							if(m==1003)
								text="开门或关门";
							if(m==1004)
								text="嘶嘶声";
							if(m==1005)
								text="播放音乐光盘";
							if(m==1007)
								text="恶魂冲锋";
							if(m==1008)
								text="恶魂吐出火球";
							if(m==1009)
								text="恶魂吐出较小的火球";
							if(m==1010)
								text="僵尸木头？";
							if(m==1011)
								text="僵尸金属？";
							if(m==1012)
								text="僵尸木头摧毁？";
							if(m==1013)
								text="凋零产出";
							if(m==1014)
								text="凋零攻击";
							if(m==1015)
								text="蝙蝠落下？";
							if(m==1016)
								text="僵尸变异？";
							if(m==1017)
								text="僵尸恢复？";
							if(m==1018)
								text="enderdragon end";
							if(m==1020)
								text="铁砧损坏";
							if(m==1021)
								text="铁砧使用";
							if(m==1022)
								text="铁砧落下";
							if(m==2000)
								text="产生10个烟雾颗粒";
							if(m==2002)
								text="飞溅药水";
							if(lv[2])
								cfg.println(this.name, 1,"实体影响:\n"+
												"X:"+ri.readInt()+"\tY:"+ri.readByte()+"\tZ:"+ri.readInt()+"\t效果:"+text);
						}else if(ri.id==0x2B) {
							int be=ri.readUnsignedByte();
							String text="未知";
							if(be==0)
								text="床无效";
							if(be==1)
								text="结束下雨";
							if(be==2)
								text="开始下雨";
							if(be==3)
								text="改变游戏模式";
								float value=ri.readFloat();
								if(value==0)
									text+="，生存";
								if(value==1)
									text+="，创造";
								if(value==2)
									text+="，冒险";
							if(be==4)
								text="输入学分";
							if(be==6)
								text="击中玩家";
							cfg.println(this.name, 1,"改变游戏状态:"+text);
						}else if(ri.id==0x2D) {
							cfg.println(this.name,1,"打开窗口，窗口id:"+ri.readUnsignedByte()+",窗口类型:"+ri.readUnsignedByte()+",窗口标题:"+ri.readString()+",插槽数量:"+ri.readUnsignedByte());
						}else if(ri.id==0x2E) {
							cfg.println(this.name, 1,"关闭窗口，窗口id:"+ri.readUnsignedByte());
						}else if(ri.id==0x2F) {
							byte id=ri.readByte();
							short slot=ri.readShort();
							short bid=ri.readShort();
							if(bid==-1) {
								if(lv[2])
									cfg.println(this.name,1,"窗口插槽更新，窗口id:"+id+",插槽id:"+slot+",这个插槽变为空");
							}else {
								byte num=ri.readByte();
								short s=ri.readShort();
								if(lv[2])
									cfg.println(this.name,1,"窗口插槽更新，窗口id:"+id+",插槽id:"+slot+",物品id:"+bid+",数量:"+num+",损坏程度:"+s);
							}
						}else if(ri.id==0x30) {
							if(lv[2])
								cfg.println(this.name, 1,"窗口插槽更新，窗口id:"+ri.readUnsignedByte());
						}else if(ri.id==0x31) {
							if(lv[2])
								cfg.println(this.name, 1,"窗口属性值更新，窗口id:"+ri.readUnsignedByte()+",属性:"+ri.readShort()+",值:"+ri.readShort());
						}else if(ri.id==0x32) {
							int id=ri.readUnsignedByte();
							short code=ri.readShort();
							boolean y=ri.readBoolean();
							String text="";
							if(y)
								text="服务器接受客户端请求，窗口id:";
							else
								text="服务器拒绝客户但请求，窗口id:";
							if(lv[2])
								cfg.println(this.name, 1,text+id+",行动编号:"+code);
						}else if(ri.id==0x33) {
							if(lv[2])
								cfg.println(this.name, 1,"发现公告牌,X:"+ri.readInt()+",Y:"+ri.readShort()+",Z:"+ri.readInt()+",下面是公告牌的消息:\n"+
												ri.readString()+"\n"+
												ri.readString()+"\n"+
												ri.readString()+"\n"+
												ri.readString());
						}else if(ri.id==0x36) {
							if(lv[2])
								cfg.println(this.name, 1,"公告牌编辑器打开,X:"+ri.readInt()+",Y:"+ri.readInt()+",Z:"+ri.readInt());
						}else if(ri.id==0x37) {
							int cout=ri.readVarInt();
							String text="下面混统计信息:";
							for(int i=0;i<cout;i++) {
								text+="\n统计的名称:"+ri.readString()+",值:"+ri.readVarInt()+"\n-----------------------------------------------------";
							}
							if(lv[2])
								cfg.println(this.name, 1,text);
						}else if(ri.id==0x38) {
							cfg.println(this.name, 1,"玩家名:"+ri.readString()+",在线:"+ri.readBoolean()+",延迟:"+ri.readShort()+"ms");
						}else if(ri.id==0x39) {
							if(lv[2])
								cfg.println(this.name, 1,"玩家能力:"+ri.readByte()+",飞行速度:"+ri.readFloat()+",行走速度:"+ri.readFloat());
						}else if(ri.id==0x3A) {
							cfg.println(this.name, 1,"玩家列表完毕:"+ri.readString());
						}else if(ri.id==0x3B) {
							String name=ri.readString();
							String value=ri.readString();
							byte flag=ri.readByte();
							String text="";
							if(flag==0)
								text="创建记分板";
							if(flag==1)
								text="删除记分板";
							if(flag==2)
								text="更新显示文本";
							if(lv[2])
								cfg.println(this.name, 1,"记分板更新:"+text+",玩家名称:"+name+",值:"+value);
						}else if(ri.id==0x3C) {
							String name=ri.readString();
							byte o=ri.readByte();
							if(o==0)
								if(lv[2])
									cfg.println(this.name, 1,"分数更新，玩家名:"+name+"，分数名称:"+ri.readString()+",值:"+ri.readInt());
							else
								if(lv[2])
									cfg.println(this.name, 1,"分数删除:"+name);
						}else if(ri.id==0x3D) {
							byte o=ri.readByte();
							String text="未知";
							if(o==0)
								text="列表";
							if(o==1)
								text="边栏";
							if(o==2)
								text="名字下面";
							if(lv[2])
								cfg.println(this.name,1,"记分板显示，位置:"+text+",分数名称:"+ri.readString());
						}else if(ri.id==0x3E) {
							String name=ri.readString();
							byte mode=ri.readByte();
							if(mode==0) {
								String sname=ri.readString();
								String q=ri.readString();
								String h=ri.readString();
								byte ys=ri.readByte();
								String ysText="查看友好隐形?";
								if(ys==0)
									ysText="关闭";
								if(ys==1)
									ysText="打开";
								short num=ri.readShort();
								String list="";
								for(int i=0;i<num;i++) {
									list+=ri.readString()+"\t";
								}
								if(lv[2])
									cfg.println(this.name,1,"团队创建，名称:"+name+",显示名称:"+sname+",团队前缀:"+q+",团队后缀:"+h+",友善之火:"+ysText+",玩家数量:"+num+"\n"+list);
								continue;
							}
							if(mode==2) {
								String sname=ri.readString();
								String q=ri.readString();
								String h=ri.readString();
								byte ys=ri.readByte();
								String ysText="查看友好隐形?";
								if(ys==0)
									ysText="关闭";
								if(ys==1)
									ysText="打开";
								if(lv[2])
									cfg.println(this.name,1,"团队信息更改:团队名称"+name+"，显示名称:"+sname+",团队前缀:"+q+",团队后缀:"+h+",友善之火:"+ysText);
								continue;
							}
							if(mode==3||mode==4) {
								short num=ri.readShort();
								String list="";
								for(int i=0;i<num;i++) {
									list+=ri.readString()+"\t";
								}
								cfg.println(this.name,1,"团队成员进入或删除，团队名称:"+name+",玩家数量:"+num+"\n"+list);
								continue;
							}
						}else if(ri.id==0x3F) {
							String qd=ri.readString();
							ri.readVarInt();
							byte[] data=ri.getAllData();
							cfg.println(this.name, 1,"收到插件信息，插件:"+qd+",数据:"+Arrays.toString(data)+"\n转换成文本:"+new String(data));
						}else if(ri.id==0x40) {
							cfg.println(this.name, 2,"服务端主动要求断开:\n"+ri.readString()+"\n因为服务器已经断开，所以你不能继续使用本视图，请输入\"fuck\"命令销毁本视图");
							mode=MODE_LEAVE;
							break;
						}
					}
				}catch(EOFException e1) {
					//cfg.println(name,3,"鐠囪褰囧☉鍫熶紖閺冭泛鍤柨锟�:"+e1);
					//break;
				}catch(Exception e3) {
					cfg.println(name,3,"执行命令时发生错误，你与服务器的连接已断开:"+e3);
					e3.printStackTrace();
					break;
				}
			}
		}catch(Exception e){
			cfg.println(name,3,"连接时发生错误:"+e);
		}
	}
}
