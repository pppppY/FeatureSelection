package cn.edu.nenu.pengy813.data;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by py on 16-9-21.
 */
public class DataSourceDF extends DataSource {

    // 整个语料库下，词的文档频率
    private Map<String, Double> wordDF;

    // 每种类别的文档频率
    private Map<String, Double> labelDF;

    // 每种类别下，词的文档频率
    private Table<String, String, Double> label_word_df;

    private  DataSourceDF() throws IOException {

    }


    @Override
    protected boolean resetImpl(String dataFilePath) {

        wordDF = new HashMap<>();
        labelDF = new HashMap<>();
        label_word_df = HashBasedTable.create();

        return false;
    }

    @Override
    public boolean load(String filePath) throws IOException {
        long stime = Clock.systemDefaultZone().millis();
        System.out.print("load datasourceDF from " + filePath + "...");

        Path path = Paths.get(filePath);
        if(!Files.exists(path))
            return false;

        // 得到各个类文件夹的path
        List<Path> classFile = Files.list(path).collect(Collectors.toList());
        for(Path cp : classFile){ //遍历每个类别文件夹，读取文档

            String label = cp.getFileName().toString(); // 类别名称

            // 得到该类所有文档文件的path
            List<Path> docFile = Files.list(cp).collect(Collectors.toList());

            // 修改该类别的文档频率
            addToMap(labelDF, label, docFile.size());

            for(Path dp : docFile){// 遍历所有的文档
                // 记录在文档中已经出现的词
                Set<String> appearedWordIndoc = new HashSet<>();
                try(Scanner sc = new Scanner(Files.newInputStream(dp), "GBK")){ //按行读取文档
                    while(sc.hasNextLine()){

                        String line = sc.nextLine();
                        String [] words = line.split(" ");

                        for(String word : words){ // 遍历每行中的所有单词
                            if(!appearedWordIndoc.contains(word)){ // 发现文档中新出现的词

                                // 更新词在整个语料中的文档频率
                                addToMap(wordDF, word, 1);
                                // 更新词在某个类别中的文档频率
                                addToMap(label_word_df, label, word, 1);
                                appearedWordIndoc.add(word);
                            }
                        }
                    }
                }

            }
        }

        System.out.println(" using " + (Clock.systemDefaultZone().millis() - stime) * 0.5 / 1000);
        return true;
    }

    // 得到词典
    @Override
    public Set<String> getDictionary() {
        return wordDF.keySet();
    }

    //得到所有类别标识
    @Override
    public Set<String> getLabels(){
        return labelDF.keySet();
    }

    //类别数
    @Override
    public int getLabelCn(){
        return labelDF.size();
    }

    // 得到词典的大小
    @Override
    public int getDicSize(){
        return getDictionary().size();
    }
    //文档数
    @Override
    public double getDocCn(boolean useSlow){
        return labelDF.values().stream()
                .reduce((sum , item) -> sum + item).get();
    }


    // 得到单词在某个类中的文档频率
    public double getWordDF(String label, String word){
        if(label_word_df.contains(label, word))
            return label_word_df.get(label, word);
        else return 0;
    }
    // 得到每个类别的文档频率
    public double getLabelDF(String label){
        if(labelDF.containsKey(label))
            return labelDF.get(label);
        else return 0;
    }
    // 得到在整个语料库中每个词的文档频率, useSlow=true表示不使用缓存的wordDF映射
    // TODO: 16-9-20 测试不缓存和缓存之间的性能差异
    public double getWordDF(String word, boolean useSlow){
        if(useSlow && label_word_df.containsColumn(word))
            return label_word_df.column(word).values()
                    .stream().reduce((sum, item) -> sum + item)
                    .get();
        else if(!useSlow && wordDF.containsKey(word))
            return wordDF.get(word);
        else return 0;
    }
}
