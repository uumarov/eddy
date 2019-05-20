package it.umarov.eddy.cloud.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ProtocolHandler extends ChannelInboundHandlerAdapter {

    private Proto proto = new Proto();
    private Proto.DataType type;
    private ByteBuf buf = new PooledByteBufAllocator().buffer(0);

    public Proto getProto() {
        return proto;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.name() + " hadler added");

    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.name() + "handler removed");

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {


        ByteBuf msgbuf = ((ByteBuf) msg);
        buf.writeBytes(msgbuf, msgbuf.readableBytes());


            if (proto.getState() == -1) {
                byte firstByte = buf.readByte();
                type = Proto.DataType.getDataTypeFromByte(firstByte);
                proto.setState(0);
                proto.setReqLen(4);
                System.out.println(firstByte);
                System.out.println(type);
            }

            switch (type) {
                case GETFILE:
                    proto.getFile(buf, ctx);
                    break;
                case SENDFILE:
                    proto.sendFile(buf, ctx);
                    break;
                case RENAME:
                    proto.renameFile(buf, ctx);
                    break;
                case DELETE:
                    proto.deleteFile(buf, ctx);
                    break;
                case FILELIST:
                    proto.filelist(buf, ctx);
                    break;
            }

        msgbuf.release();
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}