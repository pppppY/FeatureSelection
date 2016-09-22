package cn.edu.nenu.pengy813.selector;

import cn.edu.nenu.pengy813.data.DataSourceDF;
import cn.edu.nenu.pengy813.data.DataSource;
import cn.edu.nenu.pengy813.data.SimpleDataSourcePool;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by py on 16-9-21.
 */
public class ChiSquare extends WeightingMethodtc {
    private ChiSquare(DataSource ds) {
        super(ds);
    }

    public static ChiSquare build(String dataPath){
        DataSource dsdf = null;
        try {
            dsdf = SimpleDataSourcePool.create(dataPath, DataSourceDF.class);
        } catch (NoSuchMethodException | IllegalAccessException
                | InstantiationException | InvocationTargetException | IOException e) {
            System.err.println("Build ChiSquare instance fail: " + e.getMessage());
        }
        return new ChiSquare(dsdf);
    }
    @Override
    protected double weighting(String label, String word) {
        DataSourceDF dsdf = (DataSourceDF)ds;

        double docCN = ds.getDocCn(false); // 文档总数
        double a = dsdf.getWordDF(label, word); // 在类label中包含word的文档数
        double b = dsdf.getLabelDF(label) - a; // 在类label中不包含word的文档数
        double c = dsdf.getWordDF(word, false) - a; // 其他类中包含word的文档数
        double d = docCN - a - b - c ; // 其他类中不包含word的文档数
//        System.out.println(label+"  "+word);
//        System.out.println(a + " " + b + " " + c + " " + d +" " + docCN);
//        System.out.println(docCN * Math.pow(a * d - b * c, 2) / ((a + b) * (a + c) * (c + d) * (b + d)));
        // 计算卡方值
        return docCN * Math.pow(a * d - b * c, 2) / ((a + b) * (a + c) * (c + d) * (b + d));
    }

}
