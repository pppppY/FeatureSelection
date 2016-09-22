package cn.edu.nenu.pengy813.selector;

import cn.edu.nenu.pengy813.data.DataSource;
import cn.edu.nenu.pengy813.data.DataSourceDF;
import cn.edu.nenu.pengy813.data.DataSourceTF;
import cn.edu.nenu.pengy813.data.SimpleDataSourcePool;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by py on 16-9-21.
 */
public class CMFS extends WeightingMethodtc{

    private CMFS(DataSource ds) {
        super(ds);
    }

    public static CMFS build(String dataPath){
        DataSource dstf = null;
        try {
            dstf = SimpleDataSourcePool.create(dataPath, DataSourceTF.class);
        } catch (NoSuchMethodException | IllegalAccessException
                | InstantiationException | InvocationTargetException | IOException e) {
            System.err.println("Build CMFS instance fail: " + e.getMessage());
        }
        return new CMFS(dstf);
    }

    @Override
    protected double weighting(String label, String word) {
        DataSourceTF dstf = (DataSourceTF)ds;
        return Math.pow(dstf.getWordTF(label, word) + 1, 2)
                / (dstf.getWordTF(word)+ ds.getLabelCn())
                / (dstf.getLabelTF(label) + ds.getDicSize());
    }

}
