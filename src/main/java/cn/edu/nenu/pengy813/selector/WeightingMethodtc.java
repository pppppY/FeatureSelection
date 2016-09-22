package cn.edu.nenu.pengy813.selector;

import cn.edu.nenu.pengy813.data.DataSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by py on 16-9-21.
 * 需要先计算P(ti|ck)的加权方法的模板类，不同的加权方法需要实现自己的weighting()方法
 */
public abstract class WeightingMethodtc implements WeightingMethod {
    protected DataSource ds;

    public WeightingMethodtc(DataSource ds) {
        this.ds = ds;
    }

    // 计算label类下词word的权重
    abstract protected double weighting(String label, String word);

    public boolean computeAndPrint(String output_dic) throws IOException{
        Set<String> labels = ds.getLabels();
        Set<String> words = ds.getDictionary();

        labels.forEach(label -> {

            List wordDF =
                    words.stream()
                            .collect(Collectors.toMap(word -> word, word -> weighting(label, word)))
                            .entrySet()
                            .stream()
                            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                            .collect(Collectors.toList());
            try {
                printData2File(output_dic, label, wordDF);
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        return false;
    }

    public Map<String, List<Map.Entry<String, Double>>> compute(){
        Set<String> labels = ds.getLabels();
        Set<String> words = ds.getDictionary();

        // 将每个类别下，将词按文档频率由高到低排序,然后输出
        return labels.stream().collect(Collectors.toMap(label -> label, label ->{
            return words.stream()
                    .collect(Collectors.toMap(word -> word, word -> weighting(label, word)))
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .collect(Collectors.toList());
        }));
    }

    // 将各类下，词的权重输出到文件
    protected boolean printData2File(String output_dic,
                                     String fileName,
                                     List<Map.Entry<String, Object>> data) throws IOException {
        Path odic = Paths.get(output_dic);

        if(!Files.exists(odic))

        Files.createDirectory(odic);

        try(PrintWriter out = new PrintWriter(
                Files.newOutputStream(odic.resolve(fileName),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE))){
            data.forEach(entry ->{
                out.println(entry.getKey() + " " + entry.getValue());
            });
        }
        return true;
    }
}
