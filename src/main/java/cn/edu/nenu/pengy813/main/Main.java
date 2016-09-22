package cn.edu.nenu.pengy813.main;

import cn.edu.nenu.pengy813.data.*;
import cn.edu.nenu.pengy813.selector.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.time.Clock;
import java.time.LocalTime;

/**
 * Created by py on 16-9-20.
 */
public class Main {

    public static void main(String[] args) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String dataPath = "/workspace/tmp/train"; // 语料所在路径
        String resultPath = "/workspace/tmp/resultso"; // 输出结果的路径

        long stime = Clock.systemDefaultZone().millis();
        System.out.println("Start experiment ...");
        System.out.println("Data path is " + dataPath);
        WeightingMethod df = DF.build(dataPath);
        df.computeAndPrint(resultPath + "/DF");

        WeightingMethod chiSquare = ChiSquare.build(dataPath);
        chiSquare.computeAndPrint(resultPath + "/ChiSqure");

        WeightingMethod ig = IG.build(dataPath);
        ig.computeAndPrint(resultPath + "/IG");

        WeightingMethod cmfs = CMFS.build(dataPath);
        cmfs.computeAndPrint(resultPath + "/CMFS");

        WeightingMethod pfdf = PFMethodDF.build(dataPath);
        pfdf.computeAndPrint(resultPath + "/PFDF");

        WeightingMethod pfndf = PFMethodNDF.build(dataPath);
        pfndf.computeAndPrint(resultPath + "/PFNDF");

        System.out.println("Finish all using "
                + (Clock.systemDefaultZone().millis() - stime) * 0.5 / 1000
                + " s");
        System.out.println("saving result in " + resultPath + "/");
    }
}
