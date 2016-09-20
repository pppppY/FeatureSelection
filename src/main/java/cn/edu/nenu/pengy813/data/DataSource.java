package cn.edu.nenu.pengy813.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by py on 16-9-20.
 */
public class DataSource {
    // TODO: 16-9-20 改成读配置文件的形式
    private static String dataFilePath = "data";

    private Set<String> dictionary = new HashSet<>();
    // 整个语料库下，词的文档频率
    private Map<String, Integer> wordDF = new HashMap<>();

    // 每种类别的文档频率
    private Map<String, Integer> labelDF = new HashMap<>();

    // 每种类别下，词的文档频率
    private Table<String, String, Integer> label_word_df = HashBasedTable.create();

    // 每种类别下，每个词的词频
    private Table<String, String, Integer> label_word_tf = HashBasedTable.create();

    // 每个类别下的文档
    private Multimap<String, Integer> label_docs = HashMultimap.create();

    // 每篇文档中词的pf（通过词出现的段数计算得到）
    private Table<Integer, String, Integer> doc_word_pf = HashBasedTable.create();

    // 每篇文档中词的npf（通过词出现的段数计算得到）
    private Table<Integer, String, Double> doc_word_npf = HashBasedTable.create();

    // 每个类别中词的npf
    private Table<String, String, Double> label_word_npf = HashBasedTable.create();

    public DataSource(String dataFilePath) throws IOException {
        reset(dataFilePath);
    }
    public boolean reset(String dataFilePath) throws IOException {

        wordDF = new HashMap<>();
        labelDF = new HashMap<>();
        label_word_df = HashBasedTable.create();
        label_word_tf = HashBasedTable.create();
        doc_word_pf = HashBasedTable.create();
        doc_word_npf = HashBasedTable.create();
        return load(dataFilePath);

    }
    private boolean load(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        int nextDocId = 0;
        if(!Files.exists(path))
            return false;
//        Map<>
        // 得到各个类文件夹的path
        List<Path> classFile = Files.list(path).collect(Collectors.toList());
        for(Path cp : classFile){ //遍历每个类别文件夹，读取文档

            String label = cp.getFileName().toString(); // 类别名称

            // 得到该类所有文档文件的path
            List<Path> docFile = Files.list(cp).collect(Collectors.toList());

            // 修改该类别的文档频率
            Integer ldf = labelDF.putIfAbsent(label, 0);
            ldf = ldf == null ? 0 : ldf;
            labelDF.put(label, ldf + docFile.size());

            for(Path dp : docFile){// 遍历所有的文档
                int currentDocIdx = nextDocId;
                label_docs.put(label, nextDocId ++ );
                // 记录在文档中已经出现的词
                Set<String> appearedWordIndoc = new HashSet<>();
                try(Scanner sc = new Scanner(Files.newInputStream(dp))){ //按行读取文档
                    while(sc.hasNextLine()){
                        // 记录在某句中已经出现的词
                        Set<String> appearedWordInSent = new HashSet<>();
                        String line = sc.nextLine();
                        String [] words = line.split(" ");

                        for(String word : words){ // 遍历每行中的所有单词

                            // 将词加入词典
                            dictionary.add(word);

                            if(!appearedWordInSent.contains(word)){ // 发现这一行新出现的词
                                // 更新词在该文档中的pf
                                int v = 0;
                                if(doc_word_pf.contains(currentDocIdx, word)){
                                    v = doc_word_pf.get(currentDocIdx, word);
                                }
                                doc_word_pf.put(currentDocIdx, word, v + 1);

                                appearedWordInSent.add(word);
                            }

                            if(!appearedWordIndoc.contains(word)){ // 发现文档中新出现的词

                                // 更新词在整个语料中的文档频率
                                Integer v = wordDF.putIfAbsent(word, 0);
                                v = v == null? 0 : v;
                                wordDF.put(word, v + 1);

                                // 更新词在某个类别中的文档频率
                                v = 0;
                                if(label_word_df.contains(label, word)){
                                    v = label_word_df.get(label, word);
                                }
                                label_word_df.put(label, word, v + 1);

                                appearedWordIndoc.add(word);
                            }

                            // 更新词在某类下的词频
                            int v = 0;
                            if(label_word_tf.contains(label, word)){
                                v = label_word_tf.get(label, word);
                            }
                            label_word_tf.put(label, word, v + 1);

                        }
                    }
                }

            }
        }
        // 计算每篇文档中词的npf
        for(Map.Entry<Integer, Map<String, Integer>> d_w_e : doc_word_pf.rowMap().entrySet()){

            int pfsum = d_w_e.getValue().values().stream().reduce((sum, item) -> sum + item).get();

            for(Map.Entry<String, Integer> w_e : d_w_e.getValue().entrySet()){
                doc_word_npf.put(d_w_e.getKey(), w_e.getKey(), w_e.getValue() * 1.0 / pfsum);
            }
        }
        // 计算每个类别中词的npf
        dictionary.stream().forEach(word -> { //对于词典中每一个词
            label_docs.asMap().forEach((label, docIdxSet) -> { // 一类中的所有文档
                // 计算类label 中所有文档关于词 word 的 npf 之和
                double classNPF = docIdxSet.stream()
                        .mapToDouble(docIdx -> {
                            Double npf = doc_word_npf.get(docIdx, word);
                            if(npf == null) npf = 0.0;
                            return npf;
                        }).sum();
                label_word_npf.put(label, word, classNPF);
            });
        });
//        label_docs.asMap().forEach((key, docIdxSet) -> {
//            docIdxSet.stream().forEach( docIdx -> {
//                doc_word_npf.row(docIdx).forEach((word, npf) -> {
//
//                });
//            });
//        });

        System.out.println("word_df " + wordDF);
        System.out.println("label_df " + labelDF);
        System.out.println("label_word_df " + label_word_df);
        System.out.println("label_word_tf " + label_word_tf);
        System.out.println("label_docs " + label_docs);
        System.out.println("doc_word_pf " + doc_word_pf);
        System.out.println("doc_word_npf " + doc_word_npf);
        System.out.println("label_word_npf " + label_word_npf);
//        ObjectMapper mapper = new ObjectMapper()
//                .registerModule(new GuavaModule());
//        String lwnjson = mapper.writeValueAsString(label_word_npf);
//        System.out.println(label_word_npf.getClass());
//        mapper.readValue(lwnjson, label_word_npf.getClass());
//        System.out.println(lwnjson);
        return true;
    }
    //类别数
    public int getLabelCn(){
        return labelDF.size();
    }
    //文档数
    public int getDocCn(boolean useSlow){
        return labelDF.values().stream()
                .reduce((sum , item) -> sum + item).get();
    }

    // 得到单词在某个类中的文档频率
    public int getWordDF(String label, String word){
        if(label_word_df.contains(label, word))
            return label_word_df.get(label, word);
        else return 0;
    }
    // 得到每个类别的文档频率
    public int getLabelDF(String label){
        if(labelDF.containsKey(label))
            return labelDF.get(label);
        else return 0;
    }
    // 得到在整个语料库中每个词的文档频率, useSlow=true表示不使用缓存的wordDF映射
    // TODO: 16-9-20 测试不缓存和缓存之间的性能差异
    public int getWordDF(String word, boolean useSlow){
        if(useSlow && label_word_df.containsColumn(word))
            return label_word_df.column(word).values()
                    .stream().reduce((sum, item) -> sum + item)
                    .get();
        else if(!useSlow && wordDF.containsKey(word))
            return wordDF.get(word);
        else return 0;
    }
}
