

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class File {
	public static String readFile_string(String filePath){  
		String content="";
	    try {  
	        String encoding = "UTF-8";  
	        java.io.File file = new java.io.File(filePath);  
	        if (file.isFile() && file.exists()) {  
	            InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);  
	            BufferedReader bufferedReader = new BufferedReader(read);  
	            String lineTxt = null;  
	            while ((lineTxt = bufferedReader.readLine()) != null) {  
	            	content=content+lineTxt+"\n";
	            }  
	            read.close();  
	        } else {  
	            //System.err.println("ERROR:"+filePath+" not found");
	        }  
	    } catch (Exception e) {  
	    	System.err.println("ERROR:"+filePath+" have exception");
	    	e.printStackTrace();
	    }  
	    //System.out.println(content);
	    return content;  
	}  
	public static byte[] readFile_byte_small(String filename) throws IOException {  
		  
		java.io.File f = new java.io.File(filename);  
        if (!f.exists()) {  
            throw new FileNotFoundException(filename);  
        }  
  
        FileChannel channel = null;  
        FileInputStream fs = null;  
        try {  
            fs = new FileInputStream(f);  
            channel = fs.getChannel();  
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());  
            while ((channel.read(byteBuffer)) > 0) {  
                // do nothing  
                // System.out.println("reading");  
            }  
            return byteBuffer.array();  
        } catch (IOException e) {  
            e.printStackTrace();  
            throw e;  
        } finally {  
            try {  
                channel.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
            try {  
                fs.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
    }  
	public static String getPath() {
		return System.getProperty("user.dir");
	}
	public static void writeFile_text(String path,String context) {
	    java.io.File f = new java.io.File(path);
	    OutputStreamWriter writer = null;
	    BufferedWriter bw = null;
	    try {
	        OutputStream os = new FileOutputStream(f);
	        writer = new OutputStreamWriter(os);
	        bw = new BufferedWriter(writer);
	        bw.write(context);
	        bw.flush();
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            bw.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}
}
