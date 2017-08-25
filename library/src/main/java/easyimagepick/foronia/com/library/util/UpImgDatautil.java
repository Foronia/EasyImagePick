package easyimagepick.foronia.com.library.util;

import java.io.File;
import java.util.HashMap;

/**
 * Created by Foronia on 2016/9/13.
 */
public class UpImgDatautil {

    static HashMap<String,File> map=new HashMap<>();

    public static HashMap<String, File> getMap() {
        return map;
    }

    public static int getMapSize(){
        return map.size();
    }


    public static void addMapitem(String str,File file){
        map.put(str,file);
    }
    public static void removeMapitem(String str){
        if (map.containsKey(str)) {
            if(map.get(str).exists()){
                map.get(str).delete();
            }
        }
        map.remove(str);
    }
    public static void MapClear(){
        map.clear();
    }




 }


