package it.umarov.cloud.client;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;


import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    final private DirectoryChooser directoryChooser = new DirectoryChooser();

    private Network net;
    private Stage primaryStage;
    private ObservableList<FileEntry> filelist;
    private ObservableList<FileEntry> srvFilelist;


    public void setStage(Stage stage){
        this.primaryStage = stage;
    }

    public void setNet(Network net) {
        this.net = net;
    }


    @FXML
    TableView<FileEntry> fileListTable;

    @FXML
    TableView<FileEntry> srvFileListTable;

    @FXML
    TableColumn<FileEntry, String> srvColName;

    @FXML
    TableColumn<FileEntry, String> colName;

    @FXML
    Label currentDir;

    @Override
    public void initialize(URL location, ResourceBundle resources)  {
        Platform.runLater(() -> {
            configureDirectoryChooser();
            currentDir.setText(new File("./client_storage/").getAbsolutePath());

            try {
                initializeFilelistTable();
                initializeSrvFilelistTable();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

    }

    private void configureDirectoryChooser() {
        directoryChooser.setTitle("Выберите папку");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
    }

    private void initializeFilelistTable() throws IOException {
        fileListTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        refreshDir(currentDir.getText());
        colName.setCellFactory(TextFieldTableCell.forTableColumn());
        colName.setOnEditCommit((CellEditEvent<FileEntry, String> event) -> {
            TablePosition<FileEntry, String> pos = event.getTablePosition();
            String newFilename = event.getNewValue();
            int row = pos.getRow();
            FileEntry fileEntry = event.getTableView().getItems().get(row);
            if(!fileEntry.renameFile(newFilename)) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось переименовать файл", ButtonType.OK);
                alert.showAndWait();
            }
            try {
                refreshDir(currentDir.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void initializeSrvFilelistTable() throws IOException, InterruptedException {
        srvFileListTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        refreshSrvDir();
        srvColName.setCellFactory(TextFieldTableCell.forTableColumn());
        srvColName.setOnEditCommit((CellEditEvent<FileEntry, String> event) -> {
            TablePosition<FileEntry, String> pos = event.getTablePosition();
            String oldFileName = event.getOldValue();
            String newFilename = event.getNewValue();
            int row = pos.getRow();
            FileEntry fileEntry = event.getTableView().getItems().get(row);
            fileEntry.setName(newFilename);

            try {
                net.renameFile(oldFileName, newFilename);
                refreshSrvDir();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void btnChooseDirectory() throws IOException {
        File dir = directoryChooser.showDialog(primaryStage);
        if(dir != null){
            currentDir.setText(dir.getAbsolutePath());
            refreshDir(dir.getAbsolutePath());
        }
    }

    public void btnRenameFile(){
        int row = fileListTable.getFocusModel().getFocusedIndex();
        fileListTable.getSelectionModel().getTableView().edit(row,colName);
    }

    public void btnRemoveFile() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Удалить выбранные файлы?", ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get().getText().equals("OK")) {
            ObservableList<FileEntry> items = fileListTable.getSelectionModel().getSelectedItems();
            int i = 0;
            for (FileEntry item : items) {
                if(!item.delete()) i++;
            }
            if (i > 0){
                Alert error = new Alert(Alert.AlertType.ERROR, "Не удалось удалить некоторые файлы", ButtonType.OK);
                error.showAndWait();
            }
            refreshDir(currentDir.getText());
        }
    }

    public void btnRefresh() throws IOException {
        refreshDir(currentDir.getText());
    }

    private void refreshDir(String text) throws IOException {
        filelist = new FileList(text).getFileList();
        fileListTable.setItems(filelist);
    }

    private void refreshSrvDir() throws IOException, InterruptedException {
        srvFilelist = new FileList().getFileList(net.getFilelist());
        srvFileListTable.setItems(srvFilelist);
    }

    public void btnSrvRenameFile(ActionEvent actionEvent) {
        int row = srvFileListTable.getFocusModel().getFocusedIndex();
        srvFileListTable.getSelectionModel().getTableView().edit(row,srvColName);
    }

    public void btnSrvRemoveFile(ActionEvent actionEvent) throws IOException, InterruptedException {
        String filename = srvFileListTable.getSelectionModel().getSelectedItem().getName();
        net.deleteFile(filename);
        refreshSrvDir();
    }

    public void btnSrvRefresh(ActionEvent actionEvent) throws IOException, InterruptedException {
        refreshSrvDir();
    }

    public void btnUpload(ActionEvent actionEvent) throws IOException, InterruptedException {
        File file = new File(fileListTable.getSelectionModel().getSelectedItem().getAbsolutePath());
        net.sendFile(file);
        refreshSrvDir();
    }

    public void btnDownload(ActionEvent actionEvent) throws IOException {
        String filename = srvFileListTable.getSelectionModel().getSelectedItem().getName();
        net.getFile(currentDir.getText(),filename);
        refreshDir(currentDir.getText());
    }

}



