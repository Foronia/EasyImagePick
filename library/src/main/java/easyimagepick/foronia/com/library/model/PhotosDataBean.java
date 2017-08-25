package easyimagepick.foronia.com.library.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Foronia on 2016/6/6.
 */

public class PhotosDataBean implements Parcelable {

    private int id;

    private String photo;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public PhotosDataBean() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.photo);
    }

    protected PhotosDataBean(Parcel in) {
        this.id = in.readInt();
        this.photo = in.readString();
    }

    public static final Creator<PhotosDataBean> CREATOR = new Creator<PhotosDataBean>() {
        @Override
        public PhotosDataBean createFromParcel(Parcel source) {
            return new PhotosDataBean(source);
        }

        @Override
        public PhotosDataBean[] newArray(int size) {
            return new PhotosDataBean[size];
        }
    };
}
