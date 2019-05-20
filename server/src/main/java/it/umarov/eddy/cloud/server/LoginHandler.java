package it.umarov.eddy.cloud.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.davidmoten.rx.jdbc.Database;

public class LoginHandler extends ChannelInboundHandlerAdapter {
    final private LoginHandler currentHandler = this;
    private Database db;

    public LoginHandler(Database db) {
        this.db = db;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        if (buf.readableBytes() < 5) {
            return;
        }
        byte[] authBytes = new byte[5];
        buf.readBytes(authBytes);
        String message = new String(authBytes);
        if (message.equals("/auth")) {
            int userLength = buf.readInt();
            byte[] userBytes = new byte[userLength];
            buf.readBytes(userBytes);
            int passLength = buf.readInt();
            byte[] passBytes = new byte[passLength];
            buf.readBytes(passBytes);
            String user = new String(userBytes);
            String pass = new String(passBytes);

            db.select("select name from user where name=? and password=?")
                    .parameters(user, pass)
                    .count()
                    .subscribe(c -> {
                        if(c > 0) {
                            ctx.writeAndFlush(Unpooled.copyInt(1161)).addListener(future -> {
                                System.out.println("auth OK");
                                ctx.pipeline().get(ProtocolHandler.class).getProto().setUserName(user);
                                ctx.pipeline().remove(currentHandler);

                            });
                        } else {
                            ctx.writeAndFlush(Unpooled.copyInt(-10)).addListener(future -> {
                                System.out.println("auth ERROR");
                                buf.release();
                            });
                        }
                    });

        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
