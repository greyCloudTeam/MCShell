/*
			//<----------WARNING---娑撳娼伴惃鍕敩閻焦妲搁崝鐘茬槕閻ㄥ嫸绱濇担鍡樻Ц濞屸剝婀佸锝囧鐠愶箑褰块崑姘ゴ鐠囨洩绱濋幍锟芥禒銉ょ瘍鐏忚鲸鐥呴悽銊ょ啊
			int serverIDL=readVarInt(di);
			byte[] serverIDB=new byte[serverIDL];
			di.readFully(serverIDB);
			String serverID=new String(serverIDB);
			
			//int PKL=readVarInt(di); <--WARNING:鐎规ɑ鏌熼弬鍥ㄣ�傛稉濠傚晸閻ㄥ嫭妲竩arint閿涘苯鍙剧�圭偛绨茬拠銉︽Цshort
			int PKL=di.readShort();
			byte[] PK=new byte[PKL];
			di.readFully(PK);
			
			int VTL=di.readShort();//<--WARNING:鐎规ɑ鏌熼弬鍥ㄣ�傛稉濠傚晸閻ㄥ嫭妲竩arint閿涘苯鍙剧�圭偛绨茬拠銉︽Цshort,鐎规ɑ鏌熼弬鍥ㄣ�傞惇鐔烘畱娑撳秹娼拫锟�
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
			
			//娑撳娼伴弰锟�1.12.2閸楀繗顔呴惃鍕殶閹诡喖瀵�
			else if(ri.id==0x03) {
							cfg.println(name,2,"閺堝秴濮熼崳銊洣濮瑰倸鎯庨悽銊ュ竾缂傦拷...");
							int value=ri.readVarInt();
							cfg.println(name,2,"閸樺缂夐崜宥嗘付婢堆勬殶閹诡喖瀵樻径褍鐨�:"+value+"byte");
							maxPackSize=value;
							compression=true;
						}
			else if(ri.id==0x01) {//
							cfg.println(name, 1,"閺堝秴濮熼崳銊ら獓閸戣櫣绮℃瀛縩");
							cfg.println(name,1,"鐎圭偘缍媔d:"+ri.readVarInt()+"\t閸ф劖鐖�:\n");
						
										"x:"+ri.readDouble()+"\ty:"+ri.readDouble()+"\tz:"+ri.readDouble()+"\n"+
										"閺佷即鍣�:"+ri.readShort());
							
						}
						else if(ri.id==0x18) {
							String name=ri.readString();
							byte[] data=ri.getAllData();
							cfg.println(this.name, 2,"閹恒儲鏁归崚鐗堟箛閸旓紕顏惃鍕絻娴犳湹淇婇幁鐥媙"+
										"閹绘帊娆㈤崥宥呯摟:"+name+"\t閺佺増宓�:"+Arrays.toString(data)+"\n"+
										"鏉烆剚宕叉稉鐑樻瀮閺堬拷:"+new String(data));
							
						}else if(ri.id==0x0d) {
							int dif=ri.readUnsignedByte();
							String text="閺堫亞鐓�:"+dif;
							if(dif==0)
								text="閸滃苯閽�";
							if(dif==1)
								text="缁狅拷閸楋拷";
							if(dif==2)
								text="閺咁噣锟斤拷";
							if(dif==3)
								text="閸ヤ即姣�";
							cfg.println(this.name,2,"閺堝秴濮熺粩顖濐洣濮瑰倹娲块弨褰掓鎼达拷:"+text);
						}else if(ri.id==0x2c) {
							byte nl=ri.readByte();
							cfg.println(name, 2, "娴ｇ姵瀚㈤張澶庣箹娴滄稖鍏橀崝锟�:"+nl+"\n"+
										"妞嬬偠顢戦柅鐔峰:"+ri.readFloat()+"\t鐟欏棝鍣�:"+ri.readFloat());
						}else if(ri.id==0x3a) {
							byte num=ri.readByte();
							cfg.println(name, 1,"閻椻晛鎼ч弽蹇涳拷澶愩�嶇槐銏犵穿瀹稿弶娲块弬锟�:"+num);
						}else if(ri.id==0x1b) {
							cfg.println(name,1, "鐎圭偘缍嬮悩鑸碉拷浣筋潶閺�鐟板綁\n"+
										"鐎圭偘缍媔d:"+ri.readInt()+"\t閺囧瓨鏁奸崥搴ｅЦ閹拷:"+ri.readByte());
						}else if(ri.id==0x31) {//鐟欙綁鏀ｉ崥鍫熷灇
							int action=ri.readVarInt();
							String actionText="閺堫亞鐓�";
							if(action==0)
								actionText="鐠佸墽鐤�";
							if(action==1)
								actionText="濞ｈ濮�";
							if(action==2)
								actionText="閸掔娀娅�";
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
								cfg.println(name, 1,"鐟欙綁鏀ｉ崥鍫熷灇(娑撳娼伴惃鍕繆閹垳鏁辨禍搴濈瑝閻儵浜炬俊鍌欑秿缂堟槒鐦ч敍灞惧娴犮儵鍏橀弰顖濆閺傦拷)\n"+
											"action:"+actionText+"\tCrafting Book Open"+bool1+"\tFiltering Craftable:"+bool2+"\n"+
											"Recipe IDs1:\n"+Arrays.toString(array1)+"\n"+
											"Recipe IDs2:\n"+Arrays.toString(array2));
								continue;
							}
							cfg.println(name, 1,"鐟欙綁鏀ｉ崥鍫熷灇(娑撳娼伴惃鍕繆閹垳鏁辨禍搴濈瑝閻儵浜炬俊鍌欑秿缂堟槒鐦ч敍灞惧娴犮儵鍏橀弰顖濆閺傦拷)\t"+
									"action:"+actionText+"\tCrafting Book Open"+bool1+"\tFiltering Craftable:"+bool2+"\n"+
									"Recipe IDs1:\n"+Arrays.toString(array1)+"\n"+
									"Recipe IDs2:\n娑撳秴鐡ㄩ崷锟�");
						}else if(ri.id==0x2e) {
							int action=ri.readVarInt();
							int playNum=ri.readVarInt();
							String status="閺堫亞鐓�";
							if(action==0)
								status="閺傛澘顤冮悳鈺侇啀";
							if(action==1)
								status="閺囧瓨鏌婂〒鍛婂灆濡�崇础";
							if(action==2)
								status="閺囧瓨鏌婂鎯扮箿";
							if(action==3)
								status="閺囧瓨鏌婇崥宥囆�";
							if(action==4)
								status="閸掔娀娅庨悳鈺侇啀";
							String msg="閻溾晛顔嶉崚妤勩�冮弴瀛樻煀:"+status;
							for(int i=0;i<playNum;i++) {
								byte[] uuid=new byte[16];
								ri.readFully(uuid);
								msg+="\nuuid:"+Arrays.toString(uuid);
								if(action==0) {
									msg+="\n閻溾晛顔嶉崥锟�:"+ri.readString();
									int num=ri.readVarInt();
									for(int a=0;a<num;a++) {
										msg+="\n鐏炵偞锟窖冩倳:"+ri.readString();
										msg+="\t鐏炵偞锟窖冿拷锟�:"+ri.readString();
										boolean sign=ri.readBoolean();
										msg+="\t閺勵垰鎯佺粵鎯ф倳:"+sign;
										if(sign) {
											msg+="\t缁涙儳鎮�:"+ri.readString();
										}
									}
									msg+="\n濞撳憡鍨欏Ο鈥崇础:"+ri.readVarInt();
									msg+="\t瀵ゆ儼绻�:"+ri.readVarInt()+"ms";
									if(ri.readBoolean())
										msg+="\t閺勫墽銇氶崥宥囆�:\n"+ri.readString();
								}
								if(action==1) {
									msg+="\n濞撳憡鍨欏Ο鈥崇础:"+ri.readVarInt();
								}
								if(action==2) {
									msg+="\n瀵ゆ儼绻�:"+ri.readVarInt()+"ms";
								}
								if(action==3) {
									boolean bool=ri.readBoolean();
									msg+="\n閺勵垰鎯佺�涙ê婀弰鍓с仛閸氬秶袨:"+bool;
									if(bool)
										msg+="\t閺勫墽銇氶崥宥囆�:\n"+ri.readString();
								}
								msg+="\n"+"--------------------------------------------------------";
							}
           */
