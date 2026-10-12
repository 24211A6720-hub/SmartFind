package com.smartfind.util;

import java.util.*;

/**
 * Small, explainable text-comparison helpers used by the matching engine.
 * No machine learning: just word clean-up, word overlap and edit distance (typo tolerance).
 */
public final class TextSimilarity {

    private TextSimilarity() {}

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "and", "or", "of", "in", "on", "at", "near", "with", "inside", "by", "to",
            "for", "from", "is", "was", "has", "have", "had", "my", "our", "it", "its", "this", "that",
            "found", "lost", "item", "there", "some", "around", "behind", "next", "i", "me");

    /** Words that mean the same thing in a campus lost-and-found context. */
    private static final Map<String, String> SYNONYMS = Map.ofEntries(
            Map.entry("canteen", "cafeteria"), Map.entry("mess", "cafeteria"),
            Map.entry("mobile", "phone"), Map.entry("cellphone", "phone"),
            Map.entry("smartphone", "phone"), Map.entry("telephone", "phone"),
            Map.entry("college", "university"), Map.entry("campus", "university"),
            Map.entry("bike", "bicycle"), Map.entry("cycle", "bicycle"),
            Map.entry("specs", "glasses"), Map.entry("spectacles", "glasses"),
            Map.entry("pendrive", "usb"), Map.entry("backpack", "bag"));

    /** Lower-case, remove punctuation, drop stop words, de-pluralise, apply synonyms. */
    public static Set<String> tokenize(String text) {
        Set<String> tokens = new LinkedHashSet<>();
        if (text == null) return tokens;
        for (String raw : text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").split("\\s+")) {
            if (raw.isBlank() || STOP_WORDS.contains(raw)) continue;
            String t = raw;
            if (t.length() > 3 && t.endsWith("s") && !t.endsWith("ss")) t = t.substring(0, t.length() - 1);
            t = SYNONYMS.getOrDefault(t, t);
            tokens.add(t);
        }
        return tokens;
    }

    /** 1.0 = identical, 0.0 = nothing in common. */
    public static double levenshteinSimilarity(String a, String b) {
        if (a.equals(b)) return 1.0;
        int[] prev = new int[b.length() + 1];
        int[] curr = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev; prev = curr; curr = tmp;
        }
        int distance = prev[b.length()];
        return 1.0 - (double) distance / Math.max(a.length(), b.length());
    }

    /**
     * Word-based similarity from 0 to 100.
     * Each word of the shorter text is matched with its closest word in the longer text
     * (a typo such as "walet" still counts). Then:
     *   overlap = matched / words in shorter text      (is one text contained in the other?)
     *   jaccard = matched / words in both texts combined (how similar are they overall?)
     *   score   = overlapWeight*overlap + (1-overlapWeight)*jaccard
     */
    public static double tokenScore(String a, String b, double overlapWeight) {
        Set<String> ta = tokenize(a);
        Set<String> tb = tokenize(b);
        if (ta.isEmpty() || tb.isEmpty()) return 0;

        Set<String> small = ta.size() <= tb.size() ? ta : tb;
        Set<String> large = small == ta ? tb : ta;

        double matched = 0;
        for (String s : small) {
            double best = 0;
            for (String l : large) best = Math.max(best, levenshteinSimilarity(s, l));
            if (best >= 0.8) matched += best;
        }
        double overlap = matched / small.size();
        double union = ta.size() + tb.size() - matched;
        double jaccard = union <= 0 ? 0 : Math.min(1.0, matched / union);
        return 100 * (overlapWeight * overlap + (1 - overlapWeight) * jaccard);
    }

    public static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
