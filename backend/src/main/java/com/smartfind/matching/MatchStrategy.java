package com.smartfind.matching;

import com.smartfind.entity.FoundItem;
import com.smartfind.entity.LostItem;

/**
 * The matching engine contract. Today we have WeightedMatchStrategy (rule-based).
 * Later an NLP / ML strategy can implement this same interface without changing any service or controller.
 */
public interface MatchStrategy {
    MatchResultDetail evaluate(LostItem lost, FoundItem found);
}
