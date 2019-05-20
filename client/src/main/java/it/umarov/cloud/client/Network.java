package it.umarov.cloud.client;

import javafx.scene.control.ProgressBar;

import java.io.File;
import java.io.*;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;

public class Network {

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public Network() throws IOException {
        connect();
    }

    public void connect() throws IOException {
        socket = new Socket("localhost", 8189);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());

    }

    public void disconnect() throws IOException {
        in.close();
        out.close();
        socket.close();
    }

    public boolean login(String user, String pass) throws IOException {
        int result;
        String authMessage = "/auth";
        byte[] authMessageByte = authMessage.getBytes();
        int userLength = user.length();
        int passLength = pass.length();
        byte[] userBytes = user.getBytes();
        byte[] passBytes = pass.getBytes();
        out.write(authMessageByte);
        out.writeInt(userLength);
        out.write(userBytes);
        out.writeInt(passLength);
        out.write(passBytes);
        out.flush();
        result = in.readInt();
        return result == 1161;
    }

    public void getFile(String currentDir, String filename) throws IOException {
        out.write(16);
        byte[] filenameBytes = filename.getBytes();
        out.writeInt(filenameBytes.length);
        out.write(filenameBytes);
        long fileSize = in.readLong();
        System.out.println("get file size: " + fileSize);
        FileOutputStream fos = new FileOutputStream(currentDir+"/"+filename);
        FileChannel channel = fos.getChannel();
        long count = 0;
        while(count != fileSize) {
            count = channel.transferFrom(Channels.newChannel(in), channel.position(), fileSize);
        }
        channel.close();
        fos.close();
        out.flush();
        int res = in.readInt();
        System.out.println(res);
    }

    public void sendFile(File file) throws IOException {
        out.write(15);
        InputStream fis = new BufferedInputStream(new FileInputStream(file));
        byte[] filenameBytes = file.getName().getBytes();
        System.out.println(filenameBytes.length);
        out.writeInt(filenameBytes.length);
        out.write(filenameBytes);
        long fileSize = file.length();
        out.writeLong(fileSize);
        byte[] buf = new byte[8192];
        int count;
        long countL = 0;
        while ((count = fis.read(buf)) > 0) {
            out.write(buf, 0, count);
            countL += count;
            System.out.println(countL+":"+fileSize);
        }
        fis.close();
        out.flush();
        int res = in.readInt();
        System.out.println(res);
    }

    public void renameFile(String targetFilename, String newFilename) throws IOException {
        out.write(26);
        System.out.println("send rename 26 byte");
        byte[] targetFilenameBytes = targetFilename.getBytes();
        byte[] newFilenameBytes = newFilename.getBytes();
        out.writeInt(targetFilenameBytes.length);
        out.write(targetFilenameBytes);
        out.writeInt(newFilenameBytes.length);
        out.write(newFilenameBytes);
        out.flush();
        int res = in.readInt();
        System.out.println(res);
    }

    public void deleteFile(String filename) throws IOException {
        out.write(27);
        byte[] filenameBytes = filename.getBytes();
        out.writeInt(filenameBytes.length);
        out.write(filenameBytes);
        out.flush();
        int res = in.readInt();
        System.out.println(res);
    }

    public ArrayList<String> getFilelist() throws IOException, InterruptedException {
        ArrayList<String> fileList = new ArrayList<>();
        out.write(33);
        int arrLength = in.readInt();
        System.out.println("length " + arrLength);
        if(arrLength!=-1) {
            String strFilelist = in.readUTF();
            System.out.println(strFilelist);
            System.out.println(strFilelist.length());
            fileList.addAll(Arrays.asList(strFilelist.split("\n")));
        }

        return fileList;
    }


}
