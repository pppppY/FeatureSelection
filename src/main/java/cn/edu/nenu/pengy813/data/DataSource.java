package cn.edu.nenu.pengy813.data;

import com.google.common.collect.Table;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Created by py on 16-9-21.
 */
abstract public class DataSource {


    public DataSource() throws IOException {

    }

    public boolean reset(String dataFilePath) throws IOException {
        resetImpl(dataFilePath);
        return load(dataFilePath);
    }
    abstract protected boolean resetImpl(String dataFilePath) throws IOException;
    abstract public boolean load(String dataFilePath) throws IOException;
    // 得到词典
    abstract public Set<String> getDictionary();
    //得到所有类别标识
    abstract public Set<String> getLabels();
    //类别数
    abstract public int getLabelCn();
    // 得到词典的大小
    abstract public int getDicSize();
    //文档数
    abstract public double getDocCn(boolean useSlow);

    // 工具方法，将map中对应键的值加x
    public static void addToMap(Map<String, Double> map, String key, double num){
        Double v = map.get(key);
        if(v == null)
            map.put(key, num);
        else map.put(key, v + num);
    }
    public static void addToMap(Table<String, String, Double> map, String key1, String key2, double num){
        if(map.contains(key1, key2))
            map.put(key1, key2, map.get(key1, key2) + num);
        else map.put(key1, key2, num);
    }
    public static void addToMap(Table<Integer, String, Double> map, Integer key1, String key2, double num){
        if(map.contains(key1, key2))
            map.put(key1, key2, map.get(key1, key2) + num);
        else map.put(key1, key2, num);
    }
}

