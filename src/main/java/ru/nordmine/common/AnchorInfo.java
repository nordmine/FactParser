package ru.nordmine.common;

import com.google.common.base.Objects;

public class AnchorInfo {

    private String address;
    private String title;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("address", address)
                .add("title", title)
                .toString();
    }
}
