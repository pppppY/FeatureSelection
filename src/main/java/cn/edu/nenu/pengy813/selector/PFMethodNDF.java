package cn.edu.nenu.pengy813.selector;

import cn.edu.nenu.pengy813.data.DataSource;
import cn.edu.nenu.pengy813.data.DataSourceDF;
import cn.edu.nenu.pengy813.data.DataSourcePF;
import cn.edu.nenu.pengy813.data.SimpleDataSourcePool;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by py on 16-9-21.
 */
public class PFMethodNDF extends WeightingMethodtc{

    private PFMethodNDF(DataSource ds) {
        super(ds);
    }

    public static PFMethodNDF build(String dataPath){
        DataSource dspf = null;
        try {
            dspf = SimpleDataSourcePool.create(dataPath, DataSourcePF.class);
        } catch (NoSuchMethodException | IllegalAccessException
                | InstantiationException | InvocationTargetException | IOException e) {
            System.err.println("Build CMFS instance fail: " + e.getMessage());
        }
        return new PFMethodNDF(dspf);
    }

    @Override
    protected double weighting(String label, String word) {
        DataSourcePF dspf = (DataSourcePF)ds;

        return Math.pow(dspf.getWordPF(label, word, false), 2)
                / dspf.getLabelPF(label, false)
                / dspf.getWordPF(word, false);
    }
}
