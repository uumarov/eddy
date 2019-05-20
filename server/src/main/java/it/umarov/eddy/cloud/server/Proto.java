package it.umarov.eddy.cloud.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Proto {


    public enum DataType {
        EMPTY((byte)-1), GETFILE((byte)15), SENDFILE((byte)16), RENAME((byte)26), DELETE((byte)27), FILELIST((byte)33);

        byte firstMessageByte;

        DataType(byte firstMessageByte) {
            this.firstMessageByte = firstMessageByte;
        }

        static DataType getDataTypeFromByte(byte b) {
            if (b == GETFILE.firstMessageByte) {
                return GETFILE;
            }
            if (b == SENDFILE.firstMessageByte) {
                return SENDFILE;
            }
            if (b == RENAME.firstMessageByte) {
                return RENAME;
            }
            if (b == DELETE.firstMessageByte) {
                return DELETE;
            }
            if (b == FILELIST.firstMessageByte) {
                return FILELIST;
            }

            return EMPTY;
        }
    }

    private String userName;
    private int state = -1;
    private int reqLen = -1;
    private long fileLen = 0;
    private long countL = 0;
    private FileOutputStream fos;
    private FileChannel channelIn;
    private FileInputStream fis;
    private File targetFile;

    public Proto() {

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        if(!new File(userName).exists()) {
            new File(userName).mkdir();
        }
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getReqLen() {
        return reqLen;
    }

    public void setReqLen(int reqLen) {
        this.reqLen = reqLen;
    }


    public void sendFile(ByteBuf buf, ChannelHandlerContext ctx) throws IOException {
        if (state == 0) {
            if (buf.readableBytes() < reqLen) {
                return;
            }

            reqLen = buf.readInt();
            state = 1;
            System.out.println("filename length: " + reqLen);
        }

        if (state == 1) {
            if (buf.readableBytes() < reqLen) {
                return;
            }
            byte[] data = new byte[reqLen];
            buf.readBytes(data);
            String str = new String(data);
            System.out.println(str);
            RandomAccessFile raf = null;
            File file = new File(userName + File.separator + str);
            try {
                raf = new RandomAccessFile(file, "r");
                fileLen = raf.length();
            } catch (Exception e) {
                ctx.writeAndFlush(Unpooled.copyInt(1600))
                        .addListener((ChannelFutureListener) future2 -> System.out.println("send error"));
                state = -1;
                reqLen = -1;
                e.printStackTrace();
                return;
            } finally {
                if (fileLen < 0 && raf != null) {
                    raf.close();
                }
            }

            System.out.println(fileLen);
            final DefaultFileRegion dfr = new DefaultFileRegion(raf.getChannel(),0,fileLen);
            ctx.writeAndFlush(Unpooled.wrappedBuffer(longToBytes(fileLen)))
                    .addListener((ChannelFutureListener) future -> ctx.writeAndFlush(dfr)
                            .addListener((ChannelFutureListener) future1 -> {
                                ctx.writeAndFlush(Unpooled.copyInt(1601))
                                        .addListener((ChannelFutureListener) future2 -> {
                                            System.out.println("send done!");
                                        });

            }));


            state = -1;
            reqLen = -1;
            System.out.println("done");


        }


    }

    public void getFile(ByteBuf buf, ChannelHandlerContext ctx) throws IOException {
        if (state == 0) {
            if (buf.readableBytes() < reqLen) {
                return;
            }

            reqLen = buf.readInt();
            state = 1;
            System.out.println("filename length: " + reqLen);
        }

        if (state == 1) {
            if (buf.readableBytes() < reqLen) {
                return;
            }
            byte[] data = new byte[reqLen];
            buf.readBytes(data);
            String str = new String(data);
            state = 2;
            reqLen = 8;
            System.out.println(str);
            fos = new FileOutputStream(userName + File.separator + str);
        }

        if (state == 2) {
            if (buf.readableBytes() < reqLen) {
                return;
            }
            fileLen = buf.readLong();
            state = 3;
            System.out.println("file size: " + fileLen);
            channelIn = fos.getChannel();
        }

        if (state == 3) {

            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            ByteBuffer nioBuf = ByteBuffer.wrap(bytes);
            countL += nioBuf.remaining();
            while(nioBuf.hasRemaining()) {
                channelIn.write(nioBuf);
            }


        }

        if (state == 3 && countL == fileLen) {
            ctx.writeAndFlush(Unpooled.copyInt(1501))
                    .addListener((ChannelFutureListener) future -> System.out.println("getfile done!"));
            channelIn.close();
            fos.close();
            countL = 0;
            state = -1;
            reqLen = -1;
        }
    }

    public void renameFile(ByteBuf buf, ChannelHandlerContext ctx) throws FileNotFoundException {
        if (state == 0) {
            if (buf.readableBytes() < reqLen) {
                return;
            }

            reqLen = buf.readInt();
            state = 1;
            System.out.println("target filename length: " + reqLen);
        }

        if (state == 1) {
            if (buf.readableBytes() < reqLen) {
                return;
            }
            byte[] data = new byte[reqLen];
            buf.readBytes(data);
            String targetFilename = new String(data);
            state = 2;
            reqLen = 4;
            System.out.println(targetFilename);
            targetFile = new File(userName + File.separator + targetFilename);
        }

        if (state == 2) {
            if (buf.readableBytes() < reqLen) {
                return;
            }

            reqLen = buf.readInt();
            state = 3;
            System.out.println("new filename length: " + reqLen);
        }

        if (state == 3) {
            if (buf.readableBytes() < reqLen) {
                return;
            }
            byte[] data = new byte[reqLen];
            buf.readBytes(data);
            String newFilename = new String(data);
            System.out.println(newFilename);
            File newFile = new File(targetFile.getParent()+"/"+newFilename);
            if (targetFile.renameTo(newFile)) {
                ctx.writeAndFlush(Unpooled.copyInt(2601))
                        .addListener((ChannelFutureListener) future -> System.out.println("rename done!"));
            } else {
                ctx.writeAndFlush(Unpooled.copyInt(2600))
                        .addListener((ChannelFutureListener) future -> System.out.println("rename error!"));
            }
            state = -1;
            reqLen = -1;
            targetFile = null;
            System.out.println("also " + buf.readableBytes());
        }

    }

    public void deleteFile(ByteBuf buf, ChannelHandlerContext ctx) {
        if (state == 0) {
            if (buf.readableBytes() < reqLen) {
                return;
            }

            reqLen = buf.readInt();
            state = 1;
            System.out.println("filename to delete length: " + reqLen);
        }

        if (state == 1) {
            if (buf.readableBytes() < reqLen) {
                return;
            }
            byte[] data = new byte[reqLen];
            buf.readBytes(data);
            String targetFilename = new String(data);
            System.out.println(targetFilename);
            targetFile = new File(userName + File.separator + targetFilename);
            if(targetFile.delete()){
                ctx.writeAndFlush(Unpooled.copyInt(2701))
                        .addListener((ChannelFutureListener) future -> System.out.println("delete done!"));
            } else {
                ctx.writeAndFlush(Unpooled.copyInt(2700))
                        .addListener((ChannelFutureListener) future -> System.out.println("delete error!"));
            }
            state = -1;
            reqLen = -1;
        }

    }

    public void filelist(ByteBuf buf, ChannelHandlerContext ctx) throws IOException {
        if (state == 0) {
            System.out.println("send filelist");

            ByteBuf filelistByteBuf = Unpooled.buffer();
            ByteBufOutputStream byteBufOutputStream = new ByteBufOutputStream(filelistByteBuf);

            File dir = new File(userName + File.separator);
            System.out.println(dir.getAbsolutePath());
            File[] filelist = dir.listFiles();
            StringBuilder sb = new StringBuilder();
            if(filelist.length != 0) {
                for (File f :
                        filelist) {
                    System.out.println(f.getName() + " " + f.length());
                    sb.append(f.getName() +"\t" + f.length() + "\n");
                }

                System.out.println(sb + "\t" + sb.length());


                    byteBufOutputStream.writeInt(sb.length());
                    byteBufOutputStream.writeUTF(sb.toString());

                    byteBufOutputStream.close();
                    ctx.writeAndFlush(filelistByteBuf)
                            .addListener((ChannelFutureListener) future -> System.out.println("send filelist done!"));
                    state = -1;
                    reqLen = -1;

            } else {
                ctx.writeAndFlush(Unpooled.copyInt(-1))
                        .addListener((ChannelFutureListener) future -> System.out.println("filelist is null!"));
                state = -1;
                reqLen = -1;
            }
        }
    }

    private byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= 8;
        }
        return result;
    }


}
