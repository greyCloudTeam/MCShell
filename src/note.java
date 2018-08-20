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
           */