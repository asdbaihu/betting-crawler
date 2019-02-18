package com.kavou.bettingCrawler.web.entity;


import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private int id;

    @Column(name = "name")
    private String name;

    // associate sport with event
    @ManyToOne()
    @JoinColumn(name="sport_id")
    private Sport sport;

    // associate event with game
    @OneToMany(cascade=CascadeType.ALL, mappedBy="event")
    private List<Game> games;

    public Event(String name) {
        this.name = name;
    }

    public Event(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Event() {
    }

    public Sport getSport() {
        return sport;
    }

    public void setSport(Sport sport) {
        this.sport = sport;
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

    public List<Game> getGames() {
        return games;
    }

    public void setGames(List<Game> games) {
        this.games = games;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
