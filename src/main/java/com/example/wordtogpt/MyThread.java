package com.example.wordtogpt;

import javafx.scene.control.ProgressIndicator;
import javafx.scene.text.Text;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;

import static com.example.wordtogpt.HelloController.isFileEmpty;

public class MyThread extends Thread {
    private Text outText;
    private ProgressIndicator progressIndicator;
    private final String fontName;
    private final float fontSize;
    private final String filePath;
    private final int wordLimit;
    private Text finaloutText;
    private String promt;
    @Override
    public void run() {
        System.out.println("Файл не пустой. Выполняем какие-то действия...");
        System.out.println("Этот код выполняется в потоке");
        outText.setText("Копируем файл...\n");
        outText.setText("Копируем файл...\n" + "Читаем файл...\n");
        boolean flag = true;
        while (flag == true) {
            int wordCount = 0;
            String finalText = "";
            String content = "";
            try {
                content = new String(Files.readAllBytes(Paths.get(filePath)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
                if (content.trim().isEmpty()) {
                    flag = false;
                    JOptionPane.showMessageDialog(null, "Ваше предупреждение здесь!");
                    return;
                } else {
                    flag = true;
                }
            int wordCounti = 0;
            try {
                // Открытие файла Word
                FileInputStream fis = new FileInputStream(new File(filePath));
                XWPFDocument document = new XWPFDocument(fis);

                // Получение списка абзацев из документа
                List<XWPFParagraph> paragraphs = document.getParagraphs();
                Iterator<XWPFParagraph> iterator = paragraphs.iterator();
                int i = 0;
                // Проверка каждого абзаца
                while (iterator.hasNext()) {
                    XWPFParagraph para = iterator.next();
                    boolean skipParagraph = false;

                    // Получение списка текстовых элементов (run) абзаца
                    List<XWPFRun> runs = para.getRuns();

                    // Проверка каждого текстового элемента (run)
                    for (XWPFRun run : runs) {
                        String fontNames = run.getFontFamily();
                        int fontSizes = run.getFontSize();
                        // Проверка соответствия шрифта и размера
                        if (fontNames == null) {
                            skipParagraph = true;
                            break;
                        }
                        if (!fontNames.equalsIgnoreCase(fontName) || fontSizes != fontSize) {
                            skipParagraph = true;
                            break; // Прерываем цикл, если хотя бы один элемент не соответствует
                        }
                    }


                    // Если абзац не соответствует шрифту, пропускаем его
                    if (skipParagraph) {
                        continue;
                    }

                    // Подсчет количества слов в абзаце
                    String[] words = para.getText().split("\\s+");
                    wordCount = wordCount + words.length;

                    // Если количество слов в абзаце превышает лимит, пропускаем его
                    if (wordCount > wordLimit) {
                        continue;
                    }
                    wordCounti = wordCount;
                    // Печать абзаца
                    finalText = finalText + para.getText() + "\n";
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            finalText = finalText.trim();
            outText.setText(finalText);
            saveFile(promt, finalText);
            try {
                FileInputStream fis = new FileInputStream(filePath);
                XWPFDocument document = new XWPFDocument(fis);

                List<XWPFParagraph> paragraphs = document.getParagraphs();
                Iterator<XWPFParagraph> iterator = paragraphs.iterator();
                int wordConiy = 0;
                while (iterator.hasNext()) {
                    XWPFParagraph para = iterator.next();

                    // Получение списка текстовых элементов (run) абзаца
                    List<XWPFRun> runs = para.getRuns();
                    // Проверка каждого текстового элемента (run)
                    for (int i = 0; i < runs.size(); i++) {
                        XWPFRun run = runs.get(i);
                        String text = run.getText(0);

                        if (text != null) {
                            String[] words = text.split("\\s+");
                            if (wordConiy < wordCounti) {
                                int wordsToRemove = Math.min(wordCounti - wordConiy, words.length);
                                StringBuilder newText = new StringBuilder();
                                for (int j = wordsToRemove; j < words.length; j++) {
                                    newText.append(words[j]).append(" ");
                                }
                                run.setText(newText.toString(), 0);
                                wordConiy += wordsToRemove;
                            } else {
                                break;
                            }
                        }
                    }
                    // Если достигнуто требуемое количество удаленных слов, прерываем обход документа
                    if (wordConiy >= wordCounti) {
                        break;
                    }
                }
                try (FileOutputStream out = new FileOutputStream(filePath)) {
                    document.write(out);
                }

                System.out.println("Удалено " + wordConiy + " слов.");
            } catch (IOException e) {
                e.printStackTrace();
            }


            try {
                // Укажите путь к вашему .exe файлу
                String finline = "";
                try {
                    FileReader fr = new FileReader("response.txt");
                    BufferedReader br = new BufferedReader(fr);

                    // Чтение строк из файла
                    runpython();
                    String line;

                    while ((line = br.readLine()) != null) {
                        // Вывод строки на экран
                        System.out.println(line);
                        finline = finline + line + "\n";
                    }
                    System.out.println(finline);
                    boolean flags = false;
                    while (flags == false) {
                        // Если условие не выполнено, выводим сообщение об ошибке
                        if (finline.equals("当前地区当日额度已消耗完, 请尝试更换网络环境") || finline.equals("")) {
                            JOptionPane.showMessageDialog(null, "Ошибка: Смените VPN!");
                        } else {
                            flags = true;
                            break;
                        }
                        finline = "";
                        runpython();
                        while ((line = br.readLine()) != null) {
                            // Вывод строки на экран
                            System.out.println(line);
                            finline = finline + line + "\n";
                        }
                    }
                    ////////////////////////
                    // Создаем новый документ
                    XWPFDocument document = new XWPFDocument();

                    // Создаем параграф
                    XWPFParagraph paragraph = document.createParagraph();

                    // Создаем объект для работы с текстом в параграфе
                    XWPFRun run = paragraph.createRun();

                    // Задаем текст, который хотим записать в документ

                    // Устанавливаем текст в объекте run
                    run.setText(finline);

                    // Указываем путь к файлу, в который будем записывать документ
                    String filePath = "parsed_text.docx";

                    try {
                        // Создаем поток для записи в файл
                        FileOutputStream out = new FileOutputStream(filePath);
                        // Записываем содержимое документа в файл
                        document.write(out);
                        // Закрываем поток
                        out.close();
                        System.out.println("Файл успешно создан: " + filePath);
                    } catch (IOException e) {
                        System.out.println("Ошибка при записи файла: " + e.getMessage());
                        e.printStackTrace();
                    }
                    br.close();
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finaloutText.setText(finline);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
    }



    public static void main(String[] args) {

    }
    public MyThread(Text text, Text finaloutText, String promt, String filePath, int wordCount, String fontName, float fontSize,
                    ProgressIndicator progressIndicator){
        this.outText = text;
        this.filePath = filePath;
        this.wordLimit = wordCount;
        this.fontSize = fontSize;
        this.fontName = fontName;
        this.finaloutText = finaloutText;
        this.promt = promt;
        this.progressIndicator = progressIndicator;
    }
    public void saveFile(String promt, String str){
        try {
            // Создание объекта File с указанием пути к файлу
            File file = new File("cache.txt");

            // Создание объектов FileWriter и BufferedWriter для записи в файл
            FileWriter fw = new FileWriter(file, false);
            BufferedWriter bw = new BufferedWriter(fw);

            // Запись строки в файл
            bw.write(promt + str);

            // Закрытие BufferedWriter и FileWriter
            bw.close();
            fw.close();

            System.out.println("Строка успешно записана в файл.");

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader("cache.txt"));
            StringBuilder sb = new StringBuilder();
            String line;

            // Чтение файла и удаление пустых строк
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    sb.append(line).append("\n");
                }
            }
            reader.close();

            // Запись обновленного текста обратно в файл
            BufferedWriter writer = new BufferedWriter(new FileWriter("cache.txt"));
            writer.write(sb.toString());
            writer.close();

            System.out.println("Пустые строки удалены успешно.");
        } catch (IOException e) {
            System.out.println("Произошла ошибка при обработке файла: " + e.getMessage());
        }

    }
    public void runpython() throws IOException, InterruptedException {
        String command = "prog.exe";

        // Запустить процесс
        Process process = Runtime.getRuntime().exec(command);
        InputStream inputStream = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line); // Выводим каждую строку на экран
        }
        int exitCode = process.waitFor();
        String finline = "";
        // Вывести код завершения (может быть полезно для отладки)
        System.out.println("Код завершения: " + exitCode);
    }
    public static int countWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        String[] words = text.split("\\s+"); // Разделение строки по пробелам
        return words.length;
    }

}