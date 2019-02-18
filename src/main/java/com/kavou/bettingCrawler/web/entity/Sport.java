package com.kavou.bettingCrawler.web.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="sport")
public class Sport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sport_id")
    private int id;

    @Column(name = "name")
    private String name;

    // associate bettor with sport
    @ManyToOne()
    @JoinColumn(name="bettor_id")
    private Bettor bettor;

    // associate sport with event
    @OneToMany(cascade=CascadeType.ALL, mappedBy="sport")
    private List<Event> events;

    public Sport(String name) {
        this.name = name;
    }

    public Sport(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Sport() {
    }

    public Bettor getBettor() {
        return bettor;
    }

    public void setBettor(Bettor bettor) {
        this.bettor = bettor;
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

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    @Override
    public String toString() {
        return "Sport{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
