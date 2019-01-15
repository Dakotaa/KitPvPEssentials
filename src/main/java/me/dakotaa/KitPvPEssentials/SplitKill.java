package me.dakotaa.KitPvPEssentials;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class SplitKill {

    public static HashMap<String, Float> calculateDamage(HashMap<String, Float> playerDamage) {
        Float totalDamage = 0f;
        HashMap<String, Float> damagePercents = new HashMap<String, Float>();
        for (Float dmg : playerDamage.values()) {
            totalDamage += dmg;
        }
        for (String player : playerDamage.keySet()) {
            damagePercents.put(player, playerDamage.get(player)/totalDamage);
        }
        return damagePercents;
    }

    public static ArrayList<String> getHighestDamagers(HashMap<String, Float> damagePercents) {
        Float highest = 0f;
        Float secondhighest = 0f;
        String first = "";
        String second = "";
        ArrayList<String> pair = new ArrayList<String>();

        for (String username : damagePercents.keySet()) {
            if (damagePercents.get(username) > secondhighest) {
                secondhighest = highest;
                second = first;
                highest = damagePercents.get(username);
                first = username;
            }
        }

        pair.add(first);
        pair.add(second);

        return pair;
    }
}
