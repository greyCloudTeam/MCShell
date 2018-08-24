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
	
	@Override
	public void run() {
		cfg.commandStop=false;
		try {
			client=new Socket(ip,port);
			cfg.println(name,1,"成功连接服务器！");
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
					cfg.println(name,1, "接收到数据包\n长度:"+ri.data.length+"\tid:"+ri.id);
					if(mode==MODE_LOGIN) {
						if(ri.id==0x00) {
							throw new RuntimeException("不能连接这个服务器:\n"+ri.readString());
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
							cfg.println(name, 1,"服务端要求保持连接,随机id:"+id);
							sendPack p=new sendPack(dos,0x00);
							p.thisPack.writeInt(id);
							p.sendPack(false, -1);
						}else if(ri.id==0x01) {//join game
							cfg.println(name, 2,"连接到游戏");
							int eid=ri.readInt();//实体id
							int gamemode=ri.readUnsignedByte();//游戏模式，0生存，1，创造，2冒险，3旁观
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
							byte world=ri.readByte();//不清楚，-1虚空，0主世界，1结束
							String worldT="未知:"+world;
							if(world==-1)
								worldT="虚空";
							if(world==0)
								worldT="主世界";
							if(world==1)
								worldT="下界";
							int dif=ri.readUnsignedByte();//难度，0和平，1简单，2普通，3困难
							String difT="未知:"+dif;
							if(dif==0)
								difT="和平";
							if(dif==1)
								difT="简单";
							if(dif==2)
								difT="普通";
							if(dif==3)
								difT="困难";
							int max=ri.readUnsignedByte();//最大玩家，曾经被客户端用来绘制玩家列表，但现在被忽略了
							String type=ri.readString();//可以有default, flat, largeBiomes, amplified, default_1_1，对应：默认，平坦，大型生物群系，已放大，默认_1_1
							if(type.equals("default"))
								type="普通,默认";
							if(type.equals("flat"))
								type="平坦";
							if(type.equals("largeBiomes"))
								type="大型生物群系";
							if(type.equals("default_1_1"))
								type="默认_1_1";
							//boolean RDI=ri.readBoolean();//Reduced Debug Info的简写，减少调试信息，对MCShell来说是没有用的，0x01表示为true，0x00表示为false
							cfg.println(name,1,"服务器地图信息\n"+
											"实体id:"+eid+"\n"+
											"游戏模式(gamemode):"+modeT+"\n"+
											"当前位置:"+worldT+"\n"+
											"难度:"+difT+"\n"+
											"最大玩家:"+max+"\n"+
											"地图类型:"+type);
											//"减少调试信息?:"+RDI);
						}else if(ri.id==0x02) {
							String msg=ri.readString();
							cfg.println(name, 1,"聊天信息:\n"+msg);
						}else if(ri.id==0x03) {
							cfg.println(name,1,"服务器时间更新:\n"+
									"地图时代:"+ri.readLong()+"\t当天的时间:"+ri.readLong());
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
								cfg.println(name, 1,"实体装备更新:\n"+
											"实体id:"+eid+"\t插槽:"+slot+"\t块id:"+id+"\t物品数量:"+num+"\t损坏程度:"+s);
								continue;
							}
							cfg.println(name, 1,"实体装备更新:\n"+
									"实体id:"+eid+"\t插槽:"+slot+"\t块id:空");
						}else if(ri.id==0x05) {
							cfg.println(name, 2,"位置更新!你当前的位置:\n"+
										"x:"+ri.readInt()+"\ty:"+ri.readInt()+"\tz:"+ri.readInt());
						}else if(ri.id==0x06){
							float heal=ri.readFloat();
							short food=ri.readShort();
							float bh=ri.readFloat();
							if(heal<=0){
								cfg.println(name, 2,"你已经死亡！");
								continue;
							}
							cfg.println(name, 2,"你的血量/饥饿值更新!\n"+
										"血量:"+heal+"\t饥饿值:"+food+"\t饥饿值饱和度:"+bh);
						}else if(ri.id==0x07) {
							int world=ri.readInt();//不清楚，-1虚空，0主世界，1结束
							String worldT="未知:"+world;
							if(world==-1)
								worldT="虚空";
							if(world==0)
								worldT="主世界";
							if(world==1)
								worldT="下界";
							int dif=ri.readUnsignedByte();//难度，0和平，1简单，2普通，3困难
							String difT="未知:"+dif;
							if(dif==0)
								difT="和平";
							if(dif==1)
								difT="简单";
							if(dif==2)
								difT="普通";
							if(dif==3)
								difT="困难";
							
							int gamemode=ri.readUnsignedByte();//游戏模式，0生存，1，创造，2冒险，3旁观
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
							
							String type=ri.readString();//可以有default, flat, largeBiomes, amplified, default_1_1，对应：默认，平坦，大型生物群系，已放大，默认_1_1
							if(type.equals("default"))
								type="普通,默认";
							if(type.equals("flat"))
								type="平坦";
							if(type.equals("largeBiomes"))
								type="大型生物群系";
							if(type.equals("default_1_1"))
								type="默认_1_1";
							
							cfg.println(name,2,"服务端要求更改维度:\n"+
										"维度:"+worldT+"\t难度:"+difT+"\t游戏模式:"+modeT+"\t地图类型:"+type);
						}else if(ri.id==0x08) { 
							double x=ri.readDouble();
							double y=ri.readDouble();
							double z=ri.readDouble();
							float ph=ri.readFloat();
							float p=ri.readFloat();
							boolean d=ri.readBoolean();
							cfg.println(name, 2,"你的位置被更改:\n"+
												"X:"+x+"\tY:"+y+"\tZ:"+z+"\t偏航:"+ph+"\t俯视:"+p+"\t在地上:"+d);
						}else if(ri.id==0x09) {
							byte i=ri.readByte();
							cfg.println(name,2,"你选择的在物品栏中的物品索引被更改:"+i);
						}else if(ri.id==0x0A) {
							int eid=ri.readInt();
							int x=ri.readInt();
							int y=ri.readUnsignedByte();
							int z=ri.readInt();
							cfg.println(name,1,"有玩家或你自己到床上睡觉了:\n"+
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
							cfg.println(name, 1,"实体动画需要更改:\n"+
												"实体id:"+eid+"\t动画:"+idT);
						}else if(ri.id==0x0C) {
							int eid=ri.readVarInt();
							String uuid=ri.readString();
							String name=ri.readString();
							int x=ri.readInt();
							int y=ri.readInt();
							int z=ri.readInt();
							byte ph=ri.readByte();
							byte p=ri.readByte();
							short item=ri.readShort();
							cfg.println(name, 1,"玩家进入当前可见范围:\n"+
												"实体id:"+eid+"\t玩家uuid:"+uuid+"\t玩家名字:"+name+"\n"+
												"X:"+x+"\tY:"+y+"\tZ:"+z+"\t偏航:"+ph+"\t俯视:"+p+"\t手上的物品:"+item);
						}else if(ri.id==0x0D) {
							int eid=ri.readInt();
							int item=ri.readInt();
							cfg.println(name, 1,"有人拾取物品，实体id:"+eid+",物品实体id:"+item);
						}else if(ri.id==0x11) {
							cfg.println(name,1,"服务器产出经验\n"+
												"实体id:"+ri.readVarInt()+"\t坐标:\n"+
												"x:"+ri.readInt()+"\ty:"+ri.readInt()+"\tz:"+ri.readInt()+"\n"+
												"数量:"+ri.readShort());
						}else if(ri.id==0x12) {
							cfg.println(name,1,"服务端设置实体速度:\n"+
												"实体id:"+ri.readInt()+"\tX速度:"+ri.readShort()+"\tY速度:"+ri.readShort()+"\tZ速度:"+ri.readShort());
						}else if(ri.id==0x13) {
							cfg.println(name, 1,"实体被摧毁，数量"+ri.readByte());
						}else if(ri.id==0x15) {
							cfg.println(name, 1,"实体相对移动，实体id:"+ri.readInt()+"，坐标:\n"+
												"X:"+ri.readByte()+"\tY:"+ri.readByte()+"\tZ"+ri.readByte());
						}else if(ri.id==0x16) {
							cfg.println(name,1,"实体旋转，实体id:"+ri.readInt()+",X旋转:"+ri.readByte()+",Y旋转:"+ri.readByte());
						}else if(ri.id==0x17) {
							cfg.println(name, 1,"实体旋转+移动,实体id："+ri.readInt()+",坐标:\n" + 
												"X:"+ri.readByte()+"\tY:"+ri.readByte()+"\tZ"+ri.readByte()+"\n"+
												"X旋转:"+ri.readByte()+"\tY旋转:"+ri.readByte());
						}else if(ri.id==0x18) {
							cfg.println(name,1,"实体移动超过4个块，实体id:"+ri.readInt()+",坐标:\n"+
												"X:"+ri.readInt()+"\tY:"+ri.readInt()+"\tZ:"+ri.readInt()+"\n"+
												"X旋转:"+ri.readByte()+"\tY旋转:"+ri.readByte());
						}else if(ri.id==0x19) {
							cfg.println(name,1,"实体头部旋转，实体id"+ri.readInt()+"，X旋转:"+ri.readByte());
						}else if(ri.id==0x1A) {
							int eid=ri.readInt();
							byte type=ri.readByte();
							String t="未知";
							if(type==0)
								t="与生物有关的东西？";
							if(type==1)
								t="受伤";
							if(type==2)
								t="与玩家实体有关的东西？";
							if(type==3)
								t="死亡";
							if(type==6)
								t="驯服狼中..";
							if(type==7)
								t="狼被驯服了";
							if(type==8)
								t="狼自己抖水";
							if(type==9)
								t="（自我）服务器接受的饮食";
							if(type==10)
								t="绵羊正在吃草";
							if(type==11)
								t="铁傀儡交出一支玫瑰";
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
								t="爱上人类？";
							cfg.println(name, 1,"实体状态被改变，实体id:"+eid+",改变后状态:"+t);
						}else if(ri.id==0x1F) {
							cfg.println(name,1,"你的等级或经验改变，经验条:"+ri.readFloat()+"/1,等级:"+ri.readShort()+",总经验:"+ri.readShort());
						}
					}
				}catch(EOFException e1) {
					//cfg.println(name,3,"读取消息时出错:"+e1);
					//break;
				}catch(RuntimeException e2) {
					cfg.println(name,3,"登陆时出错:"+e2);
					e2.printStackTrace();
					break;
				}catch(Exception e3) {
					cfg.println(name,3,"读取消息时出错:"+e3);
				}
			}
		}catch(Exception e){
			cfg.println(name,3,"登陆时出错:"+e);
		}
	}
}