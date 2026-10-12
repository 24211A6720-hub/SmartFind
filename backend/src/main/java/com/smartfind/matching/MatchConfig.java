package com.smartfind.matching;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Values come from application.properties (prefix smartfind.match). Defaults are used in unit tests. */
@Component
@ConfigurationProperties(prefix = "smartfind.match")
@Getter @Setter
public class MatchConfig {
    private double nameWeight = 25;
    private double categoryWeight = 20;
    private double colorWeight = 15;
    private double locationWeight = 15;
    private double dateWeight = 10;
    private double descriptionWeight = 15;

    /** score >= strong -> STRONG, score >= possible -> POSSIBLE, otherwise WEAK */
    private double strong = 80;
    private double possible = 60;
    /** a notification is sent when the score reaches this value */
    private double notifyThreshold = 80;
    /** pairs scoring below this are not saved at all */
    private double minStore = 20;
    /** date score drops to 0 when the two dates are this many days apart */
    private int dateMaxDays = 14;
}
