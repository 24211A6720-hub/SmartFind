package com.smartfind.matching;

import com.smartfind.entity.FoundItem;
import com.smartfind.entity.LostItem;
import com.smartfind.entity.MatchLevel;
import com.smartfind.util.TextSimilarity;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Rule-based weighted similarity (NOT machine learning).
 * total = sum(componentScore * weight) / sum(weights), giving 0-100.
 */
@Component
public class WeightedMatchStrategy implements MatchStrategy {

    /** Colours in the same family count as "similar shade" (50 points). */
    private static final Map<String, String> COLOR_FAMILY = Map.ofEntries(
            Map.entry("black", "dark"), Map.entry("grey", "dark"), Map.entry("gray", "dark"),
            Map.entry("charcoal", "dark"), Map.entry("silver", "dark"),
            Map.entry("white", "light"), Map.entry("cream", "light"), Map.entry("ivory", "light"),
            Map.entry("beige", "light"),
            Map.entry("blue", "blue"), Map.entry("navy", "blue"), Map.entry("teal", "blue"), Map.entry("cyan", "blue"),
            Map.entry("red", "red"), Map.entry("maroon", "red"), Map.entry("pink", "red"), Map.entry("orange", "red"),
            Map.entry("brown", "brown"), Map.entry("tan", "brown"), Map.entry("khaki", "brown"),
            Map.entry("green", "green"), Map.entry("olive", "green"),
            Map.entry("yellow", "yellow"), Map.entry("gold", "yellow"));

    private final MatchConfig config;

    public WeightedMatchStrategy(MatchConfig config) {
        this.config = config;
    }

    @Override
    public MatchResultDetail evaluate(LostItem lost, FoundItem found) {
        double name = TextSimilarity.tokenScore(lost.getItemName(), found.getItemName(), 0.6);
        double category = sameText(lost.getCategory(), found.getCategory()) ? 100 : 0;
        double color = colorScore(lost.getColor(), found.getColor());
        double location = TextSimilarity.tokenScore(lost.getLocation(), found.getLocation(), 1.0);
        double date = dateScore(lost, found);
        double description = TextSimilarity.tokenScore(
                lost.getDescription() + " " + nullToEmpty(lost.getIdentifyingDetails()),
                found.getDescription() + " " + nullToEmpty(found.getIdentifyingDetails()), 0.5);

        double weightSum = config.getNameWeight() + config.getCategoryWeight() + config.getColorWeight()
                + config.getLocationWeight() + config.getDateWeight() + config.getDescriptionWeight();
        double total = (name * config.getNameWeight()
                + category * config.getCategoryWeight()
                + color * config.getColorWeight()
                + location * config.getLocationWeight()
                + date * config.getDateWeight()
                + description * config.getDescriptionWeight()) / weightSum;

        MatchLevel level = total >= config.getStrong() ? MatchLevel.STRONG
                : total >= config.getPossible() ? MatchLevel.POSSIBLE : MatchLevel.WEAK;

        List<String> reasons = new ArrayList<>();
        if (category >= 100) reasons.add("Same category");
        if (color >= 100) reasons.add("Same color");
        else if (color >= 50) reasons.add("Similar color shade");
        if (name >= 70) reasons.add("Similar item name");
        if (location >= 70) reasons.add("Nearby / similar location");
        if (date >= 70) reasons.add("Similar date");
        if (description >= 70) reasons.add("Similar description");

        return new MatchResultDetail(
                TextSimilarity.round2(name), TextSimilarity.round2(category), TextSimilarity.round2(color),
                TextSimilarity.round2(location), TextSimilarity.round2(date), TextSimilarity.round2(description),
                TextSimilarity.round2(total), level, reasons);
    }

    double colorScore(String a, String b) {
        String ca = normalize(a);
        String cb = normalize(b);
        if (ca.isEmpty() || cb.isEmpty()) return 0;
        if (ca.equals(cb) || ca.contains(cb) || cb.contains(ca)) return 100;

        Set<String> ta = new HashSet<>(Arrays.asList(ca.split(" ")));
        Set<String> tb = new HashSet<>(Arrays.asList(cb.split(" ")));
        for (String t : ta) if (tb.contains(t)) return 80;          // share one colour word

        Set<String> fa = families(ta);
        Set<String> fb = families(tb);
        for (String f : fa) if (fb.contains(f)) return 50;          // same colour family
        return 0;
    }

    double dateScore(LostItem lost, FoundItem found) {
        long days = Math.abs(ChronoUnit.DAYS.between(lost.getItemDate(), found.getItemDate()));
        int max = config.getDateMaxDays();
        if (days <= 1) return 100;
        if (days >= max) return 0;
        return 100.0 * (max - days) / (max - 1);
    }

    private Set<String> families(Set<String> words) {
        Set<String> result = new HashSet<>();
        for (String w : words) {
            String f = COLOR_FAMILY.get(w);
            if (f != null) result.add(f);
        }
        return result;
    }

    private boolean sameText(String a, String b) {
        return normalize(a).equals(normalize(b)) && !normalize(a).isEmpty();
    }

    private String normalize(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").trim().replaceAll("\\s+", " ");
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
