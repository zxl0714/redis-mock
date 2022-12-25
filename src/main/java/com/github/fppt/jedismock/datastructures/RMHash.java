package com.github.fppt.jedismock.datastructures;

import com.github.fppt.jedismock.exception.WrongValueTypeException;
import java.util.LinkedHashMap;
import java.util.Map;

public class RMHash implements RMDataStructure {
    private final LinkedHashMap<Slice, Slice> storedData = new LinkedHashMap<>();

    private String encoding = "ziplist";

    public Map<Slice, Slice> getStoredData() {
        return storedData;
    }

    public void upgradeEncoding() {
        this.encoding = "hashtable";
    }

    public void downgradeEncoding() {
        this.encoding = "ziplist";
    }

    public String getEncoding() {
        return encoding;
    }

    public String getMeta() {
        // here could be added some more metainformation later
        return " encoding:" + encoding + " ";
    }

    public void put(Slice key, Slice data) {
        storedData.put(key, data);
        if (storedData.size() > 31) {
            this.upgradeEncoding();
        }
    }

    @Override
    public void raiseTypeCastException() {
        throw new WrongValueTypeException("WRONGTYPE RMSortedSet value is used in the wrong place");
    }

    @Override
    public String getTypeName() {
        return "hash";
    }
}
