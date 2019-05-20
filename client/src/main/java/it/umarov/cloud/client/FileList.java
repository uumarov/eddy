package it.umarov.cloud.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;

public class FileList {
    private String strPath;

    public FileList(){

    }

    public FileList(String strPath){
        this.strPath = strPath;
    }



    public ObservableList<FileEntry> getFileList() throws IOException {
        ObservableList<FileEntry> list = FXCollections.observableArrayList();
        Path path = Paths.get(strPath);
        Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), 1, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = file.getFileName().toString();
                long fileSize = file.toFile().length();
                String absolutePath = file.toAbsolutePath().toString();
                if (!file.toFile().isDirectory()){
                    list.add(new FileEntry(fileName, fileSize, absolutePath));
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });

        return list;
    }

    public ObservableList<FileEntry> getFileList(ArrayList<String> arrayList) throws IOException {
        ObservableList<FileEntry> list = FXCollections.observableArrayList();
        for (String s :
                arrayList) {
            String name = s.split("\t")[0];
            long size = Long.parseLong(s.split("\t")[1]);
            list.add(new FileEntry(name, size));
        }

        return list;
    }


}
