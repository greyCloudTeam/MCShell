/*
			//<----------WARNING---����Ĵ����Ǽ��ܵģ�����û�������˺������ԣ�����Ҳ��û����
			int serverIDL=readVarInt(di);
			byte[] serverIDB=new byte[serverIDL];
			di.readFully(serverIDB);
			String serverID=new String(serverIDB);
			
			//int PKL=readVarInt(di); <--WARNING:�ٷ��ĵ���д����varint����ʵӦ����short
			int PKL=di.readShort();
			byte[] PK=new byte[PKL];
			di.readFully(PK);
			
			int VTL=di.readShort();//<--WARNING:�ٷ��ĵ���д����varint����ʵӦ����short,�ٷ��ĵ���Ĳ�����
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
			
			//������1.12.2Э������ݰ�
			else if(ri.id==0x03) {
							cfg.println(name,2,"������Ҫ������ѹ��...");
							int value=ri.readVarInt();
							cfg.println(name,2,"ѹ��ǰ������ݰ���С:"+value+"byte");
							maxPackSize=value;
							compression=true;
						}
			else if(ri.id==0x01) {//
							cfg.println(name, 1,"��������������\n");
							cfg.println(name,1,"ʵ��id:"+ri.readVarInt()+"\t����:\n");
						
										"x:"+ri.readDouble()+"\ty:"+ri.readDouble()+"\tz:"+ri.readDouble()+"\n"+
										"����:"+ri.readShort());
							
						}
						else if(ri.id==0x18) {
							String name=ri.readString();
							byte[] data=ri.getAllData();
							cfg.println(this.name, 2,"���յ�����˵Ĳ����Ϣ\n"+
										"�������:"+name+"\t����:"+Arrays.toString(data)+"\n"+
										"ת��Ϊ�ı�:"+new String(data));
							
						}else if(ri.id==0x0d) {
							int dif=ri.readUnsignedByte();
							String text="δ֪:"+dif;
							if(dif==0)
								text="��ƽ";
							if(dif==1)
								text="��";
							if(dif==2)
								text="��ͨ";
							if(dif==3)
								text="����";
							cfg.println(this.name,2,"�����Ҫ������Ѷ�:"+text);
						}else if(ri.id==0x2c) {
							byte nl=ri.readByte();
							cfg.println(name, 2, "��ӵ����Щ����:"+nl+"\n"+
										"�����ٶ�:"+ri.readFloat()+"\t��Ұ:"+ri.readFloat());
						}else if(ri.id==0x3a) {
							byte num=ri.readByte();
							cfg.println(name, 1,"��Ʒ��ѡ�������Ѹ���:"+num);
						}else if(ri.id==0x1b) {
							cfg.println(name,1, "ʵ��״̬���ı�\n"+
										"ʵ��id:"+ri.readInt()+"\t���ĺ�״̬:"+ri.readByte());
						}else if(ri.id==0x31) {//�����ϳ�
							int action=ri.readVarInt();
							String actionText="δ֪";
							if(action==0)
								actionText="����";
							if(action==1)
								actionText="���";
							if(action==2)
								actionText="ɾ��";
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
								cfg.println(name, 1,"�����ϳ�(�������Ϣ���ڲ�֪����η��룬���Զ���Ӣ��)\n"+
											"action:"+actionText+"\tCrafting Book Open"+bool1+"\tFiltering Craftable:"+bool2+"\n"+
											"Recipe IDs1:\n"+Arrays.toString(array1)+"\n"+
											"Recipe IDs2:\n"+Arrays.toString(array2));
								continue;
							}
							cfg.println(name, 1,"�����ϳ�(�������Ϣ���ڲ�֪����η��룬���Զ���Ӣ��)\t"+
									"action:"+actionText+"\tCrafting Book Open"+bool1+"\tFiltering Craftable:"+bool2+"\n"+
									"Recipe IDs1:\n"+Arrays.toString(array1)+"\n"+
									"Recipe IDs2:\n������");
						}else if(ri.id==0x2e) {
							int action=ri.readVarInt();
							int playNum=ri.readVarInt();
							String status="δ֪";
							if(action==0)
								status="�������";
							if(action==1)
								status="������Ϸģʽ";
							if(action==2)
								status="�����ӳ�";
							if(action==3)
								status="��������";
							if(action==4)
								status="ɾ�����";
							String msg="����б����:"+status;
							for(int i=0;i<playNum;i++) {
								byte[] uuid=new byte[16];
								ri.readFully(uuid);
								msg+="\nuuid:"+Arrays.toString(uuid);
								if(action==0) {
									msg+="\n�����:"+ri.readString();
									int num=ri.readVarInt();
									for(int a=0;a<num;a++) {
										msg+="\n������:"+ri.readString();
										msg+="\t����ֵ:"+ri.readString();
										boolean sign=ri.readBoolean();
										msg+="\t�Ƿ�ǩ��:"+sign;
										if(sign) {
											msg+="\tǩ��:"+ri.readString();
										}
									}
									msg+="\n��Ϸģʽ:"+ri.readVarInt();
									msg+="\t�ӳ�:"+ri.readVarInt()+"ms";
									if(ri.readBoolean())
										msg+="\t��ʾ����:\n"+ri.readString();
								}
								if(action==1) {
									msg+="\n��Ϸģʽ:"+ri.readVarInt();
								}
								if(action==2) {
									msg+="\n�ӳ�:"+ri.readVarInt()+"ms";
								}
								if(action==3) {
									boolean bool=ri.readBoolean();
									msg+="\n�Ƿ������ʾ����:"+bool;
									if(bool)
										msg+="\t��ʾ����:\n"+ri.readString();
								}
								msg+="\n"+"--------------------------------------------------------";
							}
           */