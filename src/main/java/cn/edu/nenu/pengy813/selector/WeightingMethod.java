package cn.edu.nenu.pengy813.selector;

import java.io.IOException;

/**
 * Created by py on 16-9-21.
 */
public interface WeightingMethod {
    public boolean computeAndPrint(String output_dic) throws IOException;
}
