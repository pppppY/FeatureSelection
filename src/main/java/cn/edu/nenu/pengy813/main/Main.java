package cn.edu.nenu.pengy813.main;

import cn.edu.nenu.pengy813.data.DataSource;

import java.io.IOException;
import java.net.URL;

/**
 * Created by py on 16-9-20.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        URL dataPath = ClassLoader.getSystemClassLoader().getResource("data");
        DataSource ds = new DataSource(dataPath.getPath());

        System.out.println("docCn: " + ds.getDocCn(false));
        System.out.println("labelCn: " + ds.getLabelCn());
        System.out.println("C1 " + ds.getLabelDF("C1"));
        System.out.println("C2 " + ds.getLabelDF("C2"));

        System.out.println("C1 A " + ds.getWordDF("C1", "A"));
        System.out.println("C1 B " + ds.getWordDF("C1", "B"));
        System.out.println("C2 A " + ds.getWordDF("C2", "A"));
        System.out.println("C2 B " + ds.getWordDF("C2", "B"));


        System.out.println("A TRUE " + ds.getWordDF("A", true));
        System.out.println("A FALSE " + ds.getWordDF("A", false));
        System.out.println("B TRUE " + ds.getWordDF("B", true));
        System.out.println("B FALSE " + ds.getWordDF("B", false));

    }
}
