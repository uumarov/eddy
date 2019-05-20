package it.umarov.cloud.client;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileEntry {
    private String name;
    private long size;
    private String strSize;
    private String absolutePath;
    private Path file;

    public FileEntry(String name, long size){
        this.name = name;
        this.size = size;
        this.strSize = strSize(size, true);
    }

    public FileEntry(String name, long size, String absolutePath){
        this.name = name;
        this.size = size;
        this.strSize = strSize(size, true);
        this.absolutePath = absolutePath;
        this.file = Paths.get(this.absolutePath);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
        this.strSize = strSize(size, true);
    }

    public String getStrSize() {
        return strSize;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    @Override
    public String toString() {
        return this.name;
    }

    private static String strSize(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public boolean renameFile(String name){

        if(file.toFile().renameTo(new java.io.File(name))) {
            setAbsolutePath(file.getParent().toAbsolutePath().normalize() + "\\" + name);
            this.name = name;
            return true;
        }
        return false;
    }


    public boolean delete(){
        return file.toFile().delete();
    }
}
