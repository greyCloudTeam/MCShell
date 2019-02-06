package withGui;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
 
import java.time.LocalDateTime;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
 
/**
 * 针对websocket的自定义处理器
 *
 * @author Driss
 * @time 2018/9/9 下午1:39
 * @email tt.ckuiry@foxmail.com
 */
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
 
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
    	System.out.println(msg.text());
    	JsonParser json=new JsonParser();
        JsonElement data = json.parse(msg.text());
        if(data.getAsJsonObject().get("id").getAsInt()==1) {
        	if(data.getAsJsonObject().get("key").getAsString().equals(cfg.key)) {
        		ctx.channel().writeAndFlush(new TextWebSocketFrame("{\"id\":2}"));
        		cfg.user.put(ctx.channel().id().asLongText(), new User());
        		System.out.println(ctx.channel().id().asLongText()+"通过验证");
        	}else {
        		ctx.channel().writeAndFlush(new TextWebSocketFrame("{\"id\":3}"));
        		System.out.println(ctx.channel().id().asLongText()+"验证失败");
        	}
        }
        if(data.getAsJsonObject().get("id").getAsInt()==0){
        	ctx.channel().writeAndFlush(new TextWebSocketFrame("{\"id\":1,\"var\":\""+cfg.var+"\"}"));
        }
 
 
        //读取收到的信息写回到客户端
        //ctx.channel().writeAndFlush(new TextWebSocketFrame("服务器时间: " + LocalDateTime.now()));
 
    }
 
    /**
     * 连接建立时
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("handlerAddred " + ctx.channel().id().asLongText());
        
    }
 
    /**
     * 连接关闭时
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    	try{
    		cfg.user.remove(ctx.channel().id().asLongText());
    	}catch(Exception e) {}
        System.out.println("handlerRemoved " + ctx.channel().id().asLongText());
        
    }
 
    /**
     * 异常发生时
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("异常发生");
        ctx.close();
    }
}
