
package com.example.porvenirsteaks.data.model.responses;

import java.util.List;

public class PaginatedResponse<T> {
    private int current_page;
    private List<T> data;
    private String first_page_url;
    private Integer from;
    private int last_page;
    private String last_page_url;
    private String next_page_url;
    private String path;
    private int per_page;
    private String prev_page_url;
    private Integer to;
    private int total;

    public int getCurrentPage() {
        return current_page;
    }

    public List<T> getData() {
        return data;
    }

    public String getFirstPageUrl() {
        return first_page_url;
    }

    public Integer getFrom() {
        return from;
    }

    public int getLastPage() {
        return last_page;
    }

    public String getLastPageUrl() {
        return last_page_url;
    }

    public String getNextPageUrl() {
        return next_page_url;
    }

    public String getPath() {
        return path;
    }

    public int getPerPage() {
        return per_page;
    }

    public String getPrevPageUrl() {
        return prev_page_url;
    }

    public Integer getTo() {
        return to;
    }

    public int getTotal() {
        return total;
    }
}