package com.example.wordtogpt;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class HelloController {

    @FXML
    private TextField fontText;
    @FXML
    private TextField fontSizeText;

    @FXML
    private Label leftStatus;
    @FXML
    public Text finaloutText;

    @FXML
    public Text outText;
    @FXML
    private ProgressIndicator progressBar;

    @FXML
    private TextArea promtTextArea;

    @FXML
    private Label rightStatus;

    @FXML
    private Button runButton;

    @FXML
    private Button selectFileButton;

    @FXML
    private Text selectedFileText;

    @FXML
    private TextField wordsText;

    @FXML
    protected void selectFile(ActionEvent event) {
        Stage stage = (Stage) selectFileButton.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл .docx");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Документы Word (*.docx)", "*.docx");
        fileChooser.getExtensionFilters().add(extFilter);
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            selectedFileText.setText(selectedFile.getAbsolutePath());
        } else {
            selectedFileText.setText("Файл не выбран.");
        }
    }
    @FXML
    protected void runProgram(ActionEvent event) throws InterruptedException {
        Path targetPath = null;
        try {
            Path sourcePath = new File(selectedFileText.getText()).toPath();
            String copyFileName = "копия_" + sourcePath.getFileName().toString();
            targetPath = sourcePath.resolveSibling(copyFileName);
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Копия файла создана: " + targetPath.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        progressBar.setVisible(true);
            MyThread myThread = new MyThread(outText,finaloutText, promtTextArea.getText(), targetPath.toString(), Integer.parseInt(wordsText.getText())
                    , fontText.getText(), Float.parseFloat(fontSizeText.getText()), progressBar);
            // Ваш код, который нужно выполнить, пока файл не пустой

            myThread.start();
        progressBar.setVisible(false);
    }
    public static boolean isFileEmpty(String filePath) {
        File file = new File(filePath);
        return file.length() == 0;
    }
    private static final Lock lock = new ReentrantLock();
    private static void waitForThreadToFinish(Thread thread) {
        lock.lock();
        try {
            thread.join(); // Ожидание завершения потока
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}