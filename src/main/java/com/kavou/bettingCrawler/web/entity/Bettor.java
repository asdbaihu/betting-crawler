package com.kavou.bettingCrawler.web.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="bettor")
public class Bettor {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="bettor_id")
    private int id;

    @Column(name="name")
    private String name;

    // associate bettor with sport
    @OneToMany(cascade=CascadeType.ALL, mappedBy="bettor")
    private List<Sport> sports;

    public Bettor(String name) {
        this.name = name;
    }

    public Bettor(int id) {
        this.id = id;
    }

    public Bettor(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Bettor() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Sport> getSports() {
        return sports;
    }

    public void setSports(List<Sport> sports) {
        this.sports = sports;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
