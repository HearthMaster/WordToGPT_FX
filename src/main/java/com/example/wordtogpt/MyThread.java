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
                FileInputStream fis = new FileInputStream(new File(filePath));
                XWPFDocument document = new XWPFDocument(fis);

            
                List<XWPFParagraph> paragraphs = document.getParagraphs();
                Iterator<XWPFParagraph> iterator = paragraphs.iterator();
                int i = 0;
                
                while (iterator.hasNext()) {
                    XWPFParagraph para = iterator.next();
                    boolean skipParagraph = false;

            
                    List<XWPFRun> runs = para.getRuns();

                
                    for (XWPFRun run : runs) {
                        String fontNames = run.getFontFamily();
                        int fontSizes = run.getFontSize();
                    
                        if (fontNames == null) {
                            skipParagraph = true;
                            break;
                        }
                        if (!fontNames.equalsIgnoreCase(fontName) || fontSizes != fontSize) {
                            skipParagraph = true;
                            break; 
                        }
                    }



                    if (skipParagraph) {
                        continue;
                    }

                
                    String[] words = para.getText().split("\\s+");
                    wordCount = wordCount + words.length;

                
                    if (wordCount > wordLimit) {
                        continue;
                    }
                    wordCounti = wordCount;
        
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

        
                    List<XWPFRun> runs = para.getRuns();
    
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
                
                String finline = "";
                try {
                    FileReader fr = new FileReader("response.txt");
                    BufferedReader br = new BufferedReader(fr);

                
                    runpython();
                    String line;

                    while ((line = br.readLine()) != null) {
                    
                        System.out.println(line);
                        finline = finline + line + "\n";
                    }
                    System.out.println(finline);
                    boolean flags = false;
                    while (flags == false) {
                    
                        if (finline.equals("当前地区当日额度已消耗完, 请尝试更换网络环境") || finline.equals("")) {
                            JOptionPane.showMessageDialog(null, "Ошибка: Смените VPN!");
                        } else {
                            flags = true;
                            break;
                        }
                        finline = "";
                        runpython();
                        while ((line = br.readLine()) != null) {
                        
                            System.out.println(line);
                            finline = finline + line + "\n";
                        }
                    }
                    ////////////////////////
                
                    XWPFDocument document = new XWPFDocument();

                    
                    XWPFParagraph paragraph = document.createParagraph();

            
                    XWPFRun run = paragraph.createRun();

                

            
                    run.setText(finline);

            
                    String filePath = "parsed_text.docx";

                    try {
                 
                        FileOutputStream out = new FileOutputStream(filePath);
                    
                        document.write(out);
                        
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
        
             File file= new File("cache.txt");

        
            FileWriter fw = new FileWriter(file, false);
            BufferedWriter bw = new BufferedWriter(fw);

            
            bw.write(promt + str);

    
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

        
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    sb.append(line).append("\n");
                }
            }
            reader.close();

        
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

        
        Process process = Runtime.getRuntime().exec(command);
        InputStream inputStream = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line); 
        }
        int exitCode = process.waitFor();
        String finline = "";
        //
        System.out.println("Код завершения: " + exitCode);
    }
    public static int countWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        String[] words = text.split("\\s+"); 
        return words.length;
    }

}
