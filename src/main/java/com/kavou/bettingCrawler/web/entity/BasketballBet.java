package com.kavou.bettingCrawler.web.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name="basketball_bet")
// associate Bet with Basketball bet
@PrimaryKeyJoinColumn(name="basketball_bet_id")
public class BasketballBet extends Bet {

    // draw
    @Column(name="draw")
    private BigDecimal draw;

    // half - final score (1/1, X/1, 2/1, ...)
    @Column(name="`1/1`")
    private BigDecimal Home_Home;
    @Column(name="`X/1`")
    private BigDecimal Draw_Home;
    @Column(name="`2/1`")
    private BigDecimal Away_Home;
    @Column(name="`1/2`")
    private BigDecimal Home_Away;
    @Column(name="`X/2`")
    private BigDecimal Draw_Away;
    @Column(name="`2/2`")
    private BigDecimal Away_Away;

    public BasketballBet() {
    }

    public BasketballBet(BigDecimal homeWin, BigDecimal awayWin) {
        super(homeWin, awayWin);
    }

    public BigDecimal getDraw() {
        return draw;
    }

    public void setDraw(BigDecimal draw) {
        this.draw = draw;
    }

    public BigDecimal getHome_Home() {
        return Home_Home;
    }

    public BigDecimal getDraw_Home() {
        return Draw_Home;
    }

    public void setDraw_Home(BigDecimal draw_Home) {
        Draw_Home = draw_Home;
    }

    public BigDecimal getAway_Home() {
        return Away_Home;
    }

    public void setAway_Home(BigDecimal away_Home) {
        Away_Home = away_Home;
    }

    public BigDecimal getHome_Away() {
        return Home_Away;
    }

    public void setHome_Away(BigDecimal home_Away) {
        Home_Away = home_Away;
    }

    public BigDecimal getDraw_Away() {
        return Draw_Away;
    }

    public void setDraw_Away(BigDecimal draw_Away) {
        Draw_Away = draw_Away;
    }

    public BigDecimal getAway_Away() {
        return Away_Away;
    }

    public void setAway_Away(BigDecimal away_Away) {
        Away_Away = away_Away;
    }

    public void setHome_Home(BigDecimal home_Home) {
        Home_Home = home_Home;
    }

    @Override
    public String toString() {
        return "FootballBet{" +
                "draw=" + draw +
                '}';
    }
}

