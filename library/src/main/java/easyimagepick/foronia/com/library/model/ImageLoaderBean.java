package easyimagepick.foronia.com.library.model;

/**
 * Created by Foronia on 2016/8/5.
 */
public class ImageLoaderBean {



    private String mdirpath;//文件夹路径
    private String mname;//文件名称
    private String firstImgPath;//第一张图片的路径
    private int mcount;//文件夹中文件的数量

    public String getMdirpath() {
        return mdirpath;
    }

    public void setMdirpath(String mdirpath) {
        this.mdirpath = mdirpath;
    }

    public String getMname() {
        return mname;
    }

    public void setMname(String mname) {
        this.mname = mname;
    }

    public String getFirstImgPath() {
        return firstImgPath;
    }

    public void setFirstImgPath(String firstImgPath) {
        this.firstImgPath = firstImgPath;
    }

    public int getMcount() {
        return mcount;
    }

    public void setMcount(int mcount) {
        this.mcount = mcount;
    }
}
