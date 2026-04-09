package com.mall.common.api;

public class PageData<T> {
    private int page;
    private int size;
    private long total;
    private java.util.List<T> data;

    public PageData(int page, int size, long total, java.util.List<T> data) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.data = data;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public java.util.List<T> getData() {
        return data;
    }

    public void setData(java.util.List<T> data) {
        this.data = data;
    }
}
