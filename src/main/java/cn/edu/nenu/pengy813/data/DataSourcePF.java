package cn.edu.nenu.pengy813.data;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by py on 16-9-21.
 */
public class DataSourcePF extends DataSource {

    private DataSourceDF dsdf;

    // 每个类别下的文档
    private Multimap<String, Integer> label_docs;

    // 每个类别中词的npf
    private Table<String, String, Double> label_word_npf;

    private Map<String, Double> word_npf;

    private Map<String, Double> label_npf;

    private Map<String, Double> word_npf_df;

    private Map<String, Double> label_npf_df;

    private DataSourcePF() throws IOException {
    }

    @Override
    protected boolean resetImpl(String dataFilePath) throws IOException {

        try {
            dsdf = (DataSourceDF) SimpleDataSourcePool.create(dataFilePath, DataSourceDF.class);
        } catch (NoSuchMethodException | IllegalAccessException
                | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        label_docs = HashMultimap.create();
        label_word_npf = HashBasedTable.create();
        return false;

    }

    @Override
    public boolean load(String filePath) throws IOException {
        long stime = Clock.systemDefaultZone().millis();

        System.out.print("load datasourcePF from " + filePath + "...");


        // 每篇文档中词的pf（通过词出现的段数计算得到）
        Table<Integer, String, Double> doc_word_pf = HashBasedTable.create();

        // 每篇文档中词的npf（通过词出现的段数计算得到）
        Table<Integer, String, Double> doc_word_npf = HashBasedTable.create();

        Path path = Paths.get(filePath);
        int nextDocId = 0;
        if(!Files.exists(path))
            return false;
        // 得到各个类文件夹的path
        List<Path> classFile = Files.list(path).collect(Collectors.toList());
        for(Path cp : classFile){ //遍历每个类别文件夹，读取文档

            String label = cp.getFileName().toString(); // 类别名称

            // 得到该类所有文档文件的path
            List<Path> docFile = Files.list(cp).collect(Collectors.toList());


            // 统计该类下所有词的词频之和
            for(Path dp : docFile){// 遍历所有的文档

                int currentDocIdx = nextDocId;
                label_docs.put(label, nextDocId++ );

                try(Scanner sc = new Scanner(Files.newInputStream(dp), "GBK")){ //按行读取文档
                    while(sc.hasNextLine()){

                        // 记录在某句中已经出现的词
                        Set<String> appearedWordInSent = new HashSet<>();

                        String line = sc.nextLine();
                        String [] words = line.split(" ");

                        for(String word : words){ // 遍历每行中的所有单词

                            if(!appearedWordInSent.contains(word)){ // 发现这一行新出现的词
                                // 更新词在该文档中的pf
                                addToMap(doc_word_pf, currentDocIdx, word, 1);
                                appearedWordInSent.add(word);
                            }
                        }
                    }
                }

            }
        }
        // 计算每篇文档中词的npf
        for(Map.Entry<Integer, Map<String, Double>> d_w_e : doc_word_pf.rowMap().entrySet()){

            double pfsum = d_w_e.getValue().values().stream().reduce((sum, item) -> sum + item).get();

            for(Map.Entry<String, Double> w_e : d_w_e.getValue().entrySet()){
                doc_word_npf.put(d_w_e.getKey(), w_e.getKey(), w_e.getValue() * 1.0 / pfsum);
            }
        }

        // 计算每个类别中词的npf
        label_docs.asMap()
                .forEach((label, docIdxSet) -> {
                    getDictionary().forEach(
                            word ->{
                                double ans = docIdxSet.stream()
                                        .mapToDouble(docIdx -> {
                                            return getDocWordPF(doc_word_npf, docIdx, word);
                                        }).sum();
                                label_word_npf.put(label, word, ans);
                            });
        });
        // 计算整篇语料库下，词的npf（计算npf不使用df）
        word_npf = getDictionary().stream().collect(
                                Collectors.toMap(
                                    word -> word,
                                    word -> label_word_npf
                                            .column(word)
                                            .values()
                                            .stream()
                                            .reduce((sum, item) -> sum + item)
                                            .get()));
        // 计算某个类别下，所有词的npf之和（计算npf不使用df）
        label_npf = getLabels().stream().collect(
                                Collectors.toMap(
                                        label -> label,
                                        label -> label_word_npf
                                                .row(label)
                                                .values()
                                                .stream()
                                                .reduce((sum, item) -> sum + item)
                                                .get()));
        // 计算整篇语料库下，词的npf（计算npf使用df）
        word_npf_df = getDictionary().stream().collect(
                Collectors.toMap(
                        word -> word,
                        word -> label_word_npf
                                .column(word).entrySet()
                                .stream()
                                .mapToDouble(npf -> {
                                    String label = npf.getKey();
                                    return npf.getValue() *
                                            (dsdf.getWordDF(label, word) / dsdf.getLabelDF(label));})
                                .sum()));
        // 计算某个类别下，所有词的npf之和（计算npf使用df）
        label_npf_df = getLabels().stream().collect(
                Collectors.toMap(
                        label -> label,
                        label -> label_word_npf
                                .row(label).entrySet()
                                .stream()
                                .mapToDouble(npf -> {
                                    String word = npf.getKey();
                                    return npf.getValue() *
                                            (dsdf.getWordDF(label, word) / dsdf.getLabelDF(label));})
                                .sum()));

        System.out.println(" using " + (Clock.systemDefaultZone().millis() - stime) * 0.5 / 1000);
        return true;
    }

    @Override
    public Set<String> getDictionary() {
        return dsdf.getDictionary();
    }

    @Override
    public Set<String> getLabels() {
        return dsdf.getLabels();
    }

    @Override
    public int getLabelCn() {
        return dsdf.getLabelCn();
    }

    @Override
    public int getDicSize() {
        return dsdf.getDicSize();
    }

    @Override
    public double getDocCn(boolean useSlow) {
        return dsdf.getDocCn(useSlow);
    }

    // 得到类label下，词word的npf， withDF表示是否带df计算
    public double getWordPF(String label, String word, boolean withDF){
        if(!label_word_npf.contains(label, word))
            return 0.0d;
        else {
            double npf = label_word_npf.get(label, word);
            if(withDF)
                npf *= (dsdf.getWordDF(label, word) / dsdf.getLabelDF(label));
            return npf;
        }
    }

    // 得到整个语料下，词word的npf
    public double getWordPF(String word, boolean withDF){
        if(label_word_npf.column(word).isEmpty())
            return 0.0d;
        else{
            if(withDF){
                return word_npf_df.get(word);
            }else {
                return word_npf.get(word);
            }
        }
    }

    // 得到类label中，所有词的npf之和
    public double getLabelPF(String label, boolean withDF){
        if(label_word_npf.row(label).isEmpty())
            return 0.0d;
        else{
            if(withDF){
                return label_npf_df.get(label);
            }else {
                return label_npf.get(label);
            }
        }
    }

    // 得到一个文档中，词出现的行数（段落数）
    private double getDocWordPF(Table<Integer, String, Double> doc_word_pf, Integer docIdx, String word){
        if(doc_word_pf.contains(docIdx, word))
            return doc_word_pf.get(docIdx, word);
        else return 0.0d;
    }
}
