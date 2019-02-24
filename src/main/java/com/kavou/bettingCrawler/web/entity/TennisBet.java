package com.kavou.bettingCrawler.web.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name="tennis_bet")
// associate Bet with Tennis bet
@PrimaryKeyJoinColumn(name="tennis_bet_id")
public class TennisBet extends Bet {

    // winner of set 1
    @Column(name="home_winner_set_1")
    private BigDecimal homeWinnerSet1;
    @Column(name="away_winner_set_1")
    private BigDecimal awayWinnerSet1;

    // set score
    @Column(name="`2-0`")
    private BigDecimal Two_Zero;
    @Column(name="`2-1`")
    private BigDecimal Two_One;
    @Column(name="`0-2`")
    private BigDecimal Zero_Two;
    @Column(name="`1-2`")
    private BigDecimal One_Two;

    public TennisBet() {
    }

    public BigDecimal getHomeWinnerSet1() {
        return homeWinnerSet1;
    }

    public void setHomeWinnerSet1(BigDecimal homeWinnerSet1) {
        this.homeWinnerSet1 = homeWinnerSet1;
    }

    public BigDecimal getAwayWinnerSet1() {
        return awayWinnerSet1;
    }

    public void setAwayWinnerSet1(BigDecimal awayWinnerSet1) {
        this.awayWinnerSet1 = awayWinnerSet1;
    }

    public BigDecimal getTwo_Zero() {
        return Two_Zero;
    }

    public void setTwo_Zero(BigDecimal two_Zero) {
        Two_Zero = two_Zero;
    }

    public BigDecimal getTwo_One() {
        return Two_One;
    }

    public void setTwo_One(BigDecimal two_One) {
        Two_One = two_One;
    }

    public BigDecimal getZero_Two() {
        return Zero_Two;
    }

    public void setZero_Two(BigDecimal zero_Two) {
        Zero_Two = zero_Two;
    }

    public BigDecimal getOne_Two() {
        return One_Two;
    }

    public void setOne_Two(BigDecimal one_Two) {
        One_Two = one_Two;
    }

    @Override
    public String toString() {
        return "TennisBet{" +
                "homeWinnerSet1=" + homeWinnerSet1 +
                ", awayWinnerSet1=" + awayWinnerSet1 +
                '}';
    }
}

