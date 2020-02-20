package com.iba.model.user;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "job_experience")
public class JobExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String workingPeriod;
    private String workPlace;
    private String position;
    private String activity;

    public JobExperience() {
    }

    public JobExperience(String workingPeriod, String workPlace, String position, String activity, Long userId) {
        this.workingPeriod = workingPeriod;
        this.workPlace = workPlace;
        this.position = position;
        this.activity = activity;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getWorkingPeriod() {
        return workingPeriod;
    }

    public void setWorkingPeriod(String workingPeriod) {
        this.workingPeriod = workingPeriod;
    }

    public String getWorkPlace() {
        return workPlace;
    }

    public void setWorkPlace(String workPlace) {
        this.workPlace = workPlace;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobExperience that = (JobExperience) o;
        return Objects.equals(id, that.id);
    }
}
