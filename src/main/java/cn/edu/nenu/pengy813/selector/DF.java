package cn.edu.nenu.pengy813.selector;

import cn.edu.nenu.pengy813.data.DataSourceDF;
import cn.edu.nenu.pengy813.data.DataSource;
import cn.edu.nenu.pengy813.data.DataSourceTF;
import cn.edu.nenu.pengy813.data.SimpleDataSourcePool;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by py on 16-9-21.
 */
public class DF extends WeightingMethodtc {

    private DF(DataSource ds) {
        super(ds);
    }
    public static DF build(String dataPath){
        DataSource dsdf = null;
        try {
            dsdf = SimpleDataSourcePool.create(dataPath, DataSourceDF.class);
        } catch (NoSuchMethodException | IllegalAccessException
                | InstantiationException | InvocationTargetException | IOException e) {
            System.err.println("Build DF instance fail: " + e.getMessage());
        }
        return new DF(dsdf);
    }

    @Override
    protected double weighting(String label, String word) {
        return ((DataSourceDF)ds).getWordDF(label,word);
    }

}
