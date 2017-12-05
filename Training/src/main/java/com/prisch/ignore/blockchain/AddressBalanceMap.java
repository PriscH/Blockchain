package com.prisch.ignore.blockchain;

import java.util.HashMap;
import java.util.Set;

public class AddressBalanceMap {

    private final HashMap<String, Integer> internalMap = new HashMap<>();

    public void put(String hash, int amount) {
        internalMap.put(hash, amount);
    }

    public int get(String hash) {
        return internalMap.getOrDefault(hash, 0);
    }

    public boolean containsKey(String hash) {
        return internalMap.containsKey(hash);
    }

    public Set<String> keySet() {
        return internalMap.keySet();
    }

    public void add(String hash, int amount) {
        if (!containsKey(hash)) {
            put(hash, 0);
        }

        internalMap.put(hash, internalMap.get(hash) + amount);
    }

    public void subtract(String hash, int amount) {
        if (!containsKey(hash)) {
            put(hash, 0);
        }

        internalMap.put(hash, internalMap.get(hash) - amount);
    }
}
