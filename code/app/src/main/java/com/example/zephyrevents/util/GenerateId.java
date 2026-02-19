package com.example.zephyrevents.util;

import java.util.UUID;

/**
 * Utility class to help generate unique id's
 */
public final class GenerateId {
    private GenerateId(){}

    /**
     * Generates a random UUID string without dashes.
     * @return a random unique uuid string identifier without dashes.
     */
    public static String getUniqueId(){
        // Collisions are basically none. Got rid of dashes cause I feel like they are ugly.
        return UUID.randomUUID().toString().replace("-", "");
    }
}
