/*
			//<----------WARNING---下面的代码是加密的，但是没有正版账号做测试，所以也就没用了
			int serverIDL=readVarInt(di);
			byte[] serverIDB=new byte[serverIDL];
			di.readFully(serverIDB);
			String serverID=new String(serverIDB);
			
			//int PKL=readVarInt(di); <--WARNING:官方文档上写的是varint，其实应该是short
			int PKL=di.readShort();
			byte[] PK=new byte[PKL];
			di.readFully(PK);
			
			int VTL=di.readShort();//<--WARNING:官方文档上写的是varint，其实应该是short,官方文档真的不靠谱
			byte[] VT=new byte[VTL];
			di.readFully(VT);
			
			println(1,"login info:\n"+
					"server ID length:"+serverIDL+"\tserver ID:"+serverID+"\n"+
					"public key length:"+PKL+"\tpublic key:"+Arrays.toString(PK)+"\n"+
					"Verify Token Length:"+VTL+"\tVerify Token:"+Arrays.toString(VT));
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(PK);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            byte[] SST=encryptByPublicKey(SS,publicKey);
            byte[] VTT=encryptByPublicKey(VT,publicKey);
            println(1,"built key....done!");
            b= new ByteArrayOutputStream();
			handshake = new DataOutputStream(b);
			handshake.write(0x01);
			handshake.writeShort(SST.length);
			handshake.write(SST);
			handshake.writeShort(VTT.length);
			handshake.write(VTT);
			byte[] zkey=b.toByteArray();
			println(1,"built pack....done!");
			
			writeVarInt(dos, zkey.length); //prepend size
			dos.write(zkey); //write handshake packet
			dos.flush();
			println(1,"had sent!");
			
			length=readVarInt(di);
			println(1,"Accept data "+length+" byte");
			id=readVarInt(di);
			println(1,"Accept packet,id:"+id);
			
			if(id==0x03) {
				println(2,"Enables compression...");
			}
			if(id==0x02) {
				println(1,"Login Success!");
			}
			
			//下面是1.12.2协议的数据包
			else if(ri.id==0x03) {
							cfg.println(name,2,"服务器要求启用压缩...");
							int value=ri.readVarInt();
							cfg.println(name,2,"压缩前最大数据包大小:"+value+"byte");
							maxPackSize=value;
							compression=true;
						}
			else if(ri.id==0x01) {//
							cfg.println(name, 1,"服务器产出经验\n");
							cfg.println(name,1,"实体id:"+ri.readVarInt()+"\t坐标:\n");
						
										"x:"+ri.readDouble()+"\ty:"+ri.readDouble()+"\tz:"+ri.readDouble()+"\n"+
										"数量:"+ri.readShort());
							
						}
						else if(ri.id==0x18) {
							String name=ri.readString();
							byte[] data=ri.getAllData();
							cfg.println(this.name, 2,"接收到服务端的插件信息\n"+
										"插件名字:"+name+"\t数据:"+Arrays.toString(data)+"\n"+
										"转换为文本:"+new String(data));
							
						}else if(ri.id==0x0d) {
							int dif=ri.readUnsignedByte();
							String text="未知:"+dif;
							if(dif==0)
								text="和平";
							if(dif==1)
								text="简单";
							if(dif==2)
								text="普通";
							if(dif==3)
								text="困难";
							cfg.println(this.name,2,"服务端要求更改难度:"+text);
						}else if(ri.id==0x2c) {
							byte nl=ri.readByte();
							cfg.println(name, 2, "你拥有这些能力:"+nl+"\n"+
										"飞行速度:"+ri.readFloat()+"\t视野:"+ri.readFloat());
						}else if(ri.id==0x3a) {
							byte num=ri.readByte();
							cfg.println(name, 1,"物品栏选项索引已更新:"+num);
						}else if(ri.id==0x1b) {
							cfg.println(name,1, "实体状态被改变\n"+
										"实体id:"+ri.readInt()+"\t更改后状态:"+ri.readByte());
						}else if(ri.id==0x31) {//解锁合成
							int action=ri.readVarInt();
							String actionText="未知";
							if(action==0)
								actionText="设置";
							if(action==1)
								actionText="添加";
							if(action==2)
								actionText="删除";
							boolean bool1=ri.readBoolean();
							boolean bool2=ri.readBoolean();
							int[] array1=new int[ri.readVarInt()];
							for(int i=0;i<array1.length;i++) {
								array1[i]=ri.readVarInt();
							}
							if(gamemode==0) {
								int[] array2=new int[ri.readVarInt()];
								for(int i=0;i<array2.length;i++) {
									array2[i]=ri.readVarInt();
								}
								cfg.println(name, 1,"解锁合成(下面的信息由于不知道如何翻译，所以都是英文)\n"+
											"action:"+actionText+"\tCrafting Book Open"+bool1+"\tFiltering Craftable:"+bool2+"\n"+
											"Recipe IDs1:\n"+Arrays.toString(array1)+"\n"+
											"Recipe IDs2:\n"+Arrays.toString(array2));
								continue;
							}
							cfg.println(name, 1,"解锁合成(下面的信息由于不知道如何翻译，所以都是英文)\t"+
									"action:"+actionText+"\tCrafting Book Open"+bool1+"\tFiltering Craftable:"+bool2+"\n"+
									"Recipe IDs1:\n"+Arrays.toString(array1)+"\n"+
									"Recipe IDs2:\n不存在");
						}else if(ri.id==0x2e) {
							int action=ri.readVarInt();
							int playNum=ri.readVarInt();
							String status="未知";
							if(action==0)
								status="新增玩家";
							if(action==1)
								status="更新游戏模式";
							if(action==2)
								status="更新延迟";
							if(action==3)
								status="更新名称";
							if(action==4)
								status="删除玩家";
							String msg="玩家列表更新:"+status;
							for(int i=0;i<playNum;i++) {
								byte[] uuid=new byte[16];
								ri.readFully(uuid);
								msg+="\nuuid:"+Arrays.toString(uuid);
								if(action==0) {
									msg+="\n玩家名:"+ri.readString();
									int num=ri.readVarInt();
									for(int a=0;a<num;a++) {
										msg+="\n属性名:"+ri.readString();
										msg+="\t属性值:"+ri.readString();
										boolean sign=ri.readBoolean();
										msg+="\t是否签名:"+sign;
										if(sign) {
											msg+="\t签名:"+ri.readString();
										}
									}
									msg+="\n游戏模式:"+ri.readVarInt();
									msg+="\t延迟:"+ri.readVarInt()+"ms";
									if(ri.readBoolean())
										msg+="\t显示名称:\n"+ri.readString();
								}
								if(action==1) {
									msg+="\n游戏模式:"+ri.readVarInt();
								}
								if(action==2) {
									msg+="\n延迟:"+ri.readVarInt()+"ms";
								}
								if(action==3) {
									boolean bool=ri.readBoolean();
									msg+="\n是否存在显示名称:"+bool;
									if(bool)
										msg+="\t显示名称:\n"+ri.readString();
								}
								msg+="\n"+"--------------------------------------------------------";
							}
           */