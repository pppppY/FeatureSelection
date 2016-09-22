package cn.edu.nenu.pengy813.selector;

import cn.edu.nenu.pengy813.data.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by py on 16-9-21.
 */
public class PFMethodDF extends WeightingMethodtc{

    private PFMethodDF(DataSource ds) {
        super(ds);
    }
    public static PFMethodDF build(String dataPath){
        DataSource dspf = null;
        try {
            dspf = SimpleDataSourcePool.create(dataPath, DataSourcePF.class);
        } catch (NoSuchMethodException | IllegalAccessException
                | InstantiationException | InvocationTargetException | IOException e) {
            System.err.println("Build CMFS instance fail: " + e.getMessage());
        }
        return new PFMethodDF(dspf);
    }

    @Override
    protected double weighting(String label, String word) {
        DataSourcePF dspf = (DataSourcePF)ds;

        return Math.pow(dspf.getWordPF(label, word, true), 2)
                / dspf.getLabelPF(label, true)
                / dspf.getWordPF(word, true);
    }
}
