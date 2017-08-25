package easyimagepick.foronia.com.library.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Foronia on 2016/8/6.
 */
public  class ImageBeanUtil {

//    static List<String> AllImg=new ArrayList<>();
    static Set<String> SelectImg=new HashSet<>();

    static String absolutePath;

    public static String getAbsolutePath() {
        return absolutePath;
    }

    public static void setAbsolutePath(String Path) {
        absolutePath=Path;
    }

  /*  public static void addAllImageToBean(List<String> mlist){
        AllImg.addAll(mlist);
    }

    public static void removeAllImg(){
        AllImg.clear();
    }*/

    public static void addImg(String path){
        SelectImg.add(path);
    }

    public static void removeImg(String path){
        SelectImg.remove(path);
    }

    public static void removeSelectAllImg(){
        SelectImg.clear();
    }

    public static Boolean checkIsIn(String path){
        if(SelectImg.contains(path)){
            return true;
        }else {
            return false;
        }
    }

    public static int getImgCount(){
        return SelectImg.size();
    }

   /* public static List<String> getAllImg(){
        return AllImg;
    }
*/
    public static Set<String> getSelectImg(){
        return SelectImg;
    }


}
