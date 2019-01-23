package com.bugtracking.server.dto;

import java.util.List;

public class PageableItemsDescription<I> {

    private long total;
    private List<I> items;

    public PageableItemsDescription() {
    }

    public PageableItemsDescription(long total, List<I> items) {
        this.total = total;
        this.items = items;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<I> getItems() {
        return items;
    }

    public void setItems(List<I> items) {
        this.items = items;
    }
}
