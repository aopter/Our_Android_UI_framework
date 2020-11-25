package net.onest.timestoryprj.entity;

import androidx.annotation.NonNull;

import net.onest.timestoryprj.entity.Dynasty;

public class UserUnlockDynasty {
    private Integer userId;//流水号
    private Integer progress;//答对题目个数

    private String dynastyId;//朝代


    @Override
    public String toString() {
        return "UserUnlockDynasty{" +
                "userId=" + userId +
                ", progress=" + progress +
                ", dynastyId='" + dynastyId + '\'' +
                '}';
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getDynastyId() {
        return dynastyId;
    }

    public void setDynastyId(String dynastyId) {
        this.dynastyId = dynastyId;
    }
}
