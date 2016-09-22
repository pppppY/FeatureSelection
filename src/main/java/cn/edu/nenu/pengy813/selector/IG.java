package cn.edu.nenu.pengy813.selector;

import cn.edu.nenu.pengy813.data.DataSourceDF;
import cn.edu.nenu.pengy813.data.DataSource;
import cn.edu.nenu.pengy813.data.DataSourceTF;
import cn.edu.nenu.pengy813.data.SimpleDataSourcePool;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
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
 */
public class IG implements WeightingMethod{

    protected DataSource ds;

    private IG(DataSource ds) {
        this.ds = ds;
    }

    public static IG build(String dataPath){
        DataSource dsdf = null;
        try {
            dsdf = SimpleDataSourcePool.create(dataPath, DataSourceDF.class);
        } catch (NoSuchMethodException | IllegalAccessException
                | InstantiationException | InvocationTargetException | IOException e) {
            System.err.println("Build CMFS instance fail: " + e.getMessage());
        }
        return new IG(dsdf);
    }

    public boolean computeAndPrint(String output_dic) throws IOException {
        DataSourceDF dsdf = (DataSourceDF)ds;

        Set<String> labels = ds.getLabels();
        Set<String> words = ds.getDictionary();

        // 计算HC
        double HC = labels.stream()
                            .mapToDouble(label -> {
                            double pc = dsdf.getLabelDF(label) / ds.getDocCn(false);
                            return pc * Math.log(pc);})
                            .sum() * -1.0d;
        // 计算H(C|t), 并排序
        List data = words.stream()
                .collect(Collectors.toMap(word->word, word->{
                        // P(t)
                        double pt = dsdf.getWordDF(word, false) / ds.getDocCn(false);
                        // P(t_)
                        double pt_ = 1 - pt;
                         // sum(P(ci|t)logP(ci|t))
                        double tmp1 = labels.stream()
                                .mapToDouble(label -> {
                                    double pct =dsdf.getWordDF(label, word) / dsdf.getWordDF(word, false);
                                    if(pct == 0) return 0.0d;
                                    else return pct * Math.log(pct);})
                                .sum() * pt * -1.0d;
                        // sum(P(ci|t_)logP(ci|t_))
                        double tmp2 = labels.stream()
                                .mapToDouble(label -> {
                                    double pct_ = (dsdf.getWordDF(word, false) - dsdf.getWordDF(label, word))
                                                    / (dsdf.getDocCn(false) - dsdf.getWordDF(word, false));
                                    if (pct_ == 0) return 0.0d;
                                    else return pct_ * Math.log(pct_);})
                                .sum() * pt_ * -1.0d;
                        return  tmp1 + tmp2;}))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toList());
        printData2File(output_dic, "ALL", data);

        return true;
    }
    private boolean printData2File(String output_dic,
                                   String fileName,
                                   List<Map.Entry<String, Object>> wordWeight) throws IOException {
        Path odic = Paths.get(output_dic);

        if(!Files.exists(odic))
            Files.createDirectory(odic);
        try(PrintWriter out = new PrintWriter(
                Files.newOutputStream(odic.resolve(fileName),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE))){
            wordWeight.forEach(entry ->{
                out.println(entry.getKey() + " " + entry.getValue());
            });
        }
        return true;
    }
}
