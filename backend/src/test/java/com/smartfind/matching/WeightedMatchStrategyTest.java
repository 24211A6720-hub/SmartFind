package com.smartfind.matching;

import com.smartfind.entity.FoundItem;
import com.smartfind.entity.LostItem;
import com.smartfind.entity.MatchLevel;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class WeightedMatchStrategyTest {

    private final WeightedMatchStrategy strategy = new WeightedMatchStrategy(new MatchConfig());

    private LostItem lost(String name, String cat, String color, String loc, LocalDate date, String desc) {
        LostItem i = new LostItem();
        i.setItemName(name); i.setCategory(cat); i.setColor(color);
        i.setLocation(loc); i.setItemDate(date); i.setDescription(desc);
        return i;
    }

    private FoundItem found(String name, String cat, String color, String loc, LocalDate date, String desc) {
        FoundItem i = new FoundItem();
        i.setItemName(name); i.setCategory(cat); i.setColor(color);
        i.setLocation(loc); i.setItemDate(date); i.setDescription(desc);
        return i;
    }

    @Test
    void blackWalletPairIsStrongMatch() {
        LocalDate d = LocalDate.of(2026, 10, 1);
        MatchResultDetail r = strategy.evaluate(
                lost("Black Leather Wallet", "Wallet", "Black", "Anurag University Library", d,
                        "Black leather wallet with a student ID card inside."),
                found("Black Wallet", "Wallet", "Black", "College library near Anurag University", d.plusDays(1),
                        "Black wallet found near the library with a student ID card."));

        assertEquals(MatchLevel.STRONG, r.level());
        assertTrue(r.total() >= 80, "score was " + r.total());
        assertEquals(100.0, r.categoryScore());
        assertEquals(100.0, r.colorScore());
        assertTrue(r.reasons().contains("Same category"));
        assertTrue(r.reasons().contains("Same color"));
    }

    @Test
    void unrelatedItemsAreWeakMatch() {
        LocalDate d = LocalDate.of(2026, 10, 1);
        MatchResultDetail r = strategy.evaluate(
                lost("Blue Umbrella", "Umbrella", "Blue", "Bus Stop", d, "Large blue umbrella with wooden handle."),
                found("Black Laptop Bag", "Bag", "Black", "Hostel Gate", d.plusDays(10), "Black bag with a laptop inside."));

        assertEquals(MatchLevel.WEAK, r.level());
        assertTrue(r.total() < 40, "score was " + r.total());
        assertEquals(0.0, r.categoryScore());
    }

    @Test
    void phonePairWithSynonymsIsAtLeastPossible() {
        LocalDate d = LocalDate.of(2026, 10, 3);
        MatchResultDetail r = strategy.evaluate(
                lost("Silver Samsung Phone", "Phone", "Silver", "Canteen", d, "Samsung phone with a cracked screen guard."),
                found("Grey Mobile Phone", "Phone", "Grey", "Cafeteria near main block", d.plusDays(1),
                        "Mobile phone found on a table with a cracked screen protector."));

        assertNotEquals(MatchLevel.WEAK, r.level(), "score was " + r.total());
        assertTrue(r.total() >= 60 && r.total() < 90);
    }

    @Test
    void typoInNameStillMatches() {
        LocalDate d = LocalDate.of(2026, 10, 1);
        MatchResultDetail r = strategy.evaluate(
                lost("Black Wallet", "Wallet", "Black", "Library", d, "Black wallet"),
                found("Black Walet", "Wallet", "Black", "Library", d, "Black walet"));
        assertTrue(r.nameScore() > 80, "name score was " + r.nameScore());
    }

    @Test
    void dateScoreDropsWithDistance() {
        LostItem l = lost("x", "x", "x", "x", LocalDate.of(2026, 10, 1), "x");
        assertEquals(100.0, strategy.dateScore(l, found("x", "x", "x", "x", LocalDate.of(2026, 10, 2), "x")));
        assertEquals(0.0, strategy.dateScore(l, found("x", "x", "x", "x", LocalDate.of(2026, 10, 20), "x")));
        double mid = strategy.dateScore(l, found("x", "x", "x", "x", LocalDate.of(2026, 10, 8), "x"));
        assertTrue(mid > 0 && mid < 100);
    }

    @Test
    void relatedColorsGetPartialScore() {
        assertEquals(100.0, strategy.colorScore("Black", "black leather"));
        assertEquals(50.0, strategy.colorScore("Silver", "Grey"));
        assertEquals(0.0, strategy.colorScore("Blue", "Red"));
    }
}
