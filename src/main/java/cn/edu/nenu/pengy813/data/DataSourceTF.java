package cn.edu.nenu.pengy813.data;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by py on 16-9-21.
 */
public class DataSourceTF extends DataSource {

    private DataSourceTF() throws IOException {

    }

    // 整个语料库中，每个词出现的频率
    private Map<String, Double> wordTF;

    // 每个类中所有词的频率之和
    private Map<String, Double> labelTF;

    // 每种类别下，每个词的词频
    private Table<String, String, Double> label_word_tf;

    @Override
    protected boolean resetImpl(String dataFilePath) {
        wordTF = new HashMap<>();
        labelTF = new HashMap<>();
        label_word_tf = HashBasedTable.create();

        return false;
    }

    @Override
    public boolean load(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if(!Files.exists(path))

            return false;
        // 得到各个类文件夹的path
        List<Path> classFile = Files.list(path).collect(Collectors.toList());
        for(Path cp : classFile){ //遍历每个类别文件夹，读取文档

            String label = cp.getFileName().toString(); // 类别名称

            // 得到该类所有文档文件的path
            List<Path> docFile = Files.list(cp).collect(Collectors.toList());


            // 统计该类下所有词的词频之和
            int wordCount = 0;
            for(Path dp : docFile){// 遍历所有的文档

                try(Scanner sc = new Scanner(Files.newInputStream(dp), "GBK")){ //按行读取文档
                    while(sc.hasNextLine()){

                        String line = sc.nextLine();
                        String [] words = line.split(" ");

                        wordCount += words.length;

                        for(String word : words){ // 遍历每行中的所有单词

                            // 更新word的词频
                            Double tf = wordTF.get(word);
                            if(tf == null)
                                wordTF.put(word, 1.0);
                            else wordTF.put(word, tf + 1);

                            // 更新词在某类下的词频
                            double v = 0.0;
                            if(label_word_tf.contains(label, word)){
                                v = label_word_tf.get(label, word);
                            }
                            label_word_tf.put(label, word, v + 1);

                        }
                    }
                }

            }
            // 更新类label中，所有词的词频之和
            Double ltf = labelTF.get(label);
            if(ltf == null)
                labelTF.put(label, wordCount * 1.0d);
            else labelTF.put(label, ltf + wordCount);
        }

        System.out.println("word_tf " + wordTF);
        System.out.println("label_tf " + labelTF);
        System.out.println("label_word_tf " + label_word_tf);
        return true;
    }
    // 得到词典
    @Override
    public Set<String> getDictionary() {
        return wordTF.keySet();
    }

    //得到所有类别标识
    @Override
    public Set<String> getLabels(){
        return labelTF.keySet();
    }

    //类别数
    @Override
    public int getLabelCn(){
        return labelTF.size();
    }

    // 得到词典的大小
    @Override
    public int getDicSize(){
        return getDictionary().size();
    }
    //文档数
    @Override
    public double getDocCn(boolean useSlow){
        return labelTF.values().stream()
                .reduce((sum , item) -> sum + item).get();
    }

    // 得到一个类下，一个词的词频
    public double getWordTF(String label, String word){
        if(label_word_tf.contains(label, word))
            return label_word_tf.get(label, word);
        else return 0;
    }

    // 得到整个语料库中，一个词的词频
    public double getWordTF(String word){
        if(wordTF.containsKey(word)){
            return wordTF.get(word);
        }else return  0;
    }

    //得到一个类下所有词出现的频率之和
    public double getLabelTF(String label){
        if(labelTF.containsKey(label))
            return labelTF.get(label);
        else return 0;
    }
}
