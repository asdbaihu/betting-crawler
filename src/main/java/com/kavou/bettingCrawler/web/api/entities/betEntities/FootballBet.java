package com.kavou.bettingCrawler.web.api.entities.betEntities;

import com.kavou.bettingCrawler.web.api.entities.Bet;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="football_bet")
// associate Bet with FootballBet
@PrimaryKeyJoinColumn(name="bet_id")
public class FootballBet extends Bet {

    // draw
    @Column(name="draw")
    private BigDecimal draw;

    // over goals
    @Column(name="`over_0,5`")
    private BigDecimal over0_5;
    @Column(name="`over_1,5`")
    private BigDecimal over1_5;
    @Column(name="`over_2,5`")
    private BigDecimal over2_5;
    @Column(name="`over_3,5`")
    private BigDecimal over3_5;
    @Column(name="`over_4,5`")
    private BigDecimal over4_5;
    @Column(name="`over_5,5`")
    private BigDecimal over5_5;
    @Column(name="`over_6,5`")
    private BigDecimal over6_5;

    // under goals
    @Column(name="`under_0,5`")
    private BigDecimal under0_5;
    @Column(name="`under_1,5`")
    private BigDecimal under1_5;
    @Column(name="`under_2,5`")
    private BigDecimal under2_5;
    @Column(name="`under_3,5`")
    private BigDecimal under3_5;
    @Column(name="`under_4,5`")
    private BigDecimal under4_5;
    @Column(name="`under_5,5`")
    private BigDecimal under5_5;
    @Column(name="`under_6,5`")
    private BigDecimal under6_5;

    // GG/NG
    @Column(name="GG")
    private BigDecimal goalGoal;
    @Column(name="NG")
    private BigDecimal noGoal;

    // double bet (1X, 2X, 12)
    @Column(name="`1X`")
    private BigDecimal HomeDraw;
    @Column(name="`2X`")
    private BigDecimal AwayDraw;
    @Column(name="`12`")
    private BigDecimal HomeAway;

    // half - final score (1/1, X/1, 2/1, ...)
    @Column(name="`1/1`")
    private BigDecimal Home_Home;
    @Column(name="`X/1`")
    private BigDecimal Draw_Home;
    @Column(name="`2/1`")
    private BigDecimal Away_Home;
    @Column(name="`1/X`")
    private BigDecimal Home_Draw;
    @Column(name="`X/X`")
    private BigDecimal Draw_Draw;
    @Column(name="`2/X`")
    private BigDecimal Away_Draw;
    @Column(name="`1/2`")
    private BigDecimal Home_Away;
    @Column(name="`X/2`")
    private BigDecimal Draw_Away;
    @Column(name="`2/2`")
    private BigDecimal Away_Away;

    public FootballBet() {
    }

    public FootballBet(String sport, String bettor, BigDecimal homeWin, BigDecimal awayWin) {
        super(sport, bettor, homeWin, awayWin);
    }

    public BigDecimal getDraw() {
        return draw;
    }

    public void setDraw(BigDecimal draw) {
        this.draw = draw;
    }

    public BigDecimal getOver0_5() {
        return over0_5;
    }

    public void setOver0_5(BigDecimal over0_5) {
        this.over0_5 = over0_5;
    }

    public BigDecimal getOver1_5() {
        return over1_5;
    }

    public void setOver1_5(BigDecimal over1_5) {
        this.over1_5 = over1_5;
    }

    public BigDecimal getOver2_5() {
        return over2_5;
    }

    public void setOver2_5(BigDecimal over2_5) {
        this.over2_5 = over2_5;
    }

    public BigDecimal getOver3_5() {
        return over3_5;
    }

    public void setOver3_5(BigDecimal over3_5) {
        this.over3_5 = over3_5;
    }

    public BigDecimal getOver4_5() {
        return over4_5;
    }

    public void setOver4_5(BigDecimal over4_5) {
        this.over4_5 = over4_5;
    }

    public BigDecimal getOver5_5() {
        return over5_5;
    }

    public void setOver5_5(BigDecimal over5_5) {
        this.over5_5 = over5_5;
    }

    public BigDecimal getOver6_5() {
        return over6_5;
    }

    public void setOver6_5(BigDecimal over6_5) {
        this.over6_5 = over6_5;
    }

    public BigDecimal getUnder0_5() {
        return under0_5;
    }

    public void setUnder0_5(BigDecimal under0_5) {
        this.under0_5 = under0_5;
    }

    public BigDecimal getUnder1_5() {
        return under1_5;
    }

    public void setUnder1_5(BigDecimal under1_5) {
        this.under1_5 = under1_5;
    }

    public BigDecimal getUnder2_5() {
        return under2_5;
    }

    public void setUnder2_5(BigDecimal under2_5) {
        this.under2_5 = under2_5;
    }

    public BigDecimal getUnder3_5() {
        return under3_5;
    }

    public void setUnder3_5(BigDecimal under3_5) {
        this.under3_5 = under3_5;
    }

    public BigDecimal getUnder4_5() {
        return under4_5;
    }

    public void setUnder4_5(BigDecimal under4_5) {
        this.under4_5 = under4_5;
    }

    public BigDecimal getUnder5_5() {
        return under5_5;
    }

    public void setUnder5_5(BigDecimal under5_5) {
        this.under5_5 = under5_5;
    }

    public BigDecimal getUnder6_5() {
        return under6_5;
    }

    public void setUnder6_5(BigDecimal under6_5) {
        this.under6_5 = under6_5;
    }

    public BigDecimal getGoalGoal() {
        return goalGoal;
    }

    public void setGoalGoal(BigDecimal goalGoal) {
        this.goalGoal = goalGoal;
    }

    public BigDecimal getNoGoal() {
        return noGoal;
    }

    public void setNoGoal(BigDecimal noGoal) {
        this.noGoal = noGoal;
    }

    public BigDecimal getHomeDraw() {
        return HomeDraw;
    }

    public void setHomeDraw(BigDecimal homeDraw) {
        HomeDraw = homeDraw;
    }

    public BigDecimal getAwayDraw() {
        return AwayDraw;
    }

    public void setAwayDraw(BigDecimal awayDraw) {
        AwayDraw = awayDraw;
    }

    public BigDecimal getHomeAway() {
        return HomeAway;
    }

    public void setHomeAway(BigDecimal homeAway) {
        HomeAway = homeAway;
    }

    public BigDecimal getHome_Home() {
        return Home_Home;
    }

    public void setHome_Home(BigDecimal home_Home) {
        Home_Home = home_Home;
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

    public BigDecimal getHome_Draw() {
        return Home_Draw;
    }

    public void setHome_Draw(BigDecimal home_Draw) {
        Home_Draw = home_Draw;
    }

    public BigDecimal getDraw_Draw() {
        return Draw_Draw;
    }

    public void setDraw_Draw(BigDecimal draw_Draw) {
        Draw_Draw = draw_Draw;
    }

    public BigDecimal getAway_Draw() {
        return Away_Draw;
    }

    public void setAway_Draw(BigDecimal away_Draw) {
        Away_Draw = away_Draw;
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

    @Override
    public String toString() {
        return "FootballBet{" +
                "draw=" + draw +
                '}';
    }
}

