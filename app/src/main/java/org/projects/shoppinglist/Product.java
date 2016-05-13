package org.projects.shoppinglist;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Balazs on 2016. 04. 04..
 */

public class Product implements Parcelable {

    String name;
    int quantity;
    String volume;

    public Product(){

    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {

        this.name = name;
    }
    public int getQuantity()
    {
        return quantity;
    }

    public void setQuantity(int quantity)
    {

        this.quantity = quantity;
    }
    public String getVolume()
    {
        return volume;
    }

    public void setVolume(String volume)
    {

        this.volume = volume;
    }

    public Product(int quantity, String volume, String name)
    {
        this.name = name;
        this.quantity = quantity;
        this.volume = volume;
    }

    /*@Override
    public String toString() {
        return quantity+" "+volume+" "+name;
    }*/

    @Override
    public String toString(){

        if (quantity == 0){
            return quantity + " " + name;
        } else {
            return quantity+" "+volume+" " + name;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(quantity);
        dest.writeString(volume);
        dest.writeString(name);
    }

    // Creator
    public static final Parcelable.Creator CREATOR
            = new Parcelable.Creator() {
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        public Object[] newArray(int size) {
            return new Object[0];
        }
    };

    // "De-parcel object
    public Product(Parcel in) {
        quantity = in.readInt();
        volume = in.readString();
        name = in.readString();
    }
}
