package com.maze.simplemaze;

/**
 * @author: chasen
 * @date: 2019/4/24
 * Rank实体类，包括名次，用户名，收集的星数，和上榜时间
 */
public class Rank {
    private String id;
    private String name;
    private String stars;
    private String date;

    public Rank(String id, String name, String stars, String date) {
        this.id = id;
        this.name = name;
        this.stars = stars;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStars() {
        return stars;
    }

    public void setStars(String stars) {
        this.stars = stars;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
