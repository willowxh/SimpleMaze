package com.maze.simplemaze;

/**
 * @author: chasen
 * @date: 2019/4/25
 * 关卡实体类
 */
public class Level {
    private int id;//关数
    private boolean isPassed;//通关与否

    public Level(int id, boolean isPassed) {
        this.id = id;
        this.isPassed = isPassed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isPassed() {
        return isPassed;
    }

    public void setPassed(boolean passed) {
        isPassed = passed;
    }
}
