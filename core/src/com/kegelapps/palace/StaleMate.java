package com.kegelapps.palace;

import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by keg45397 on 5/13/2016.
 */
public class StaleMate {

    private Map<Integer, ArrayList<StaleMateItem>> mStaleMap;
    private Table mTable;

    static class StaleMateItem {
        byte[] hash;
        int count;

        public StaleMateItem(byte[] hash) {
            this.hash = hash;
            count = 0;
        }

        @Override
        public int hashCode() {
            int code = 3;
            code = 7 * code + Arrays.hashCode(hash);
            return code;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != getClass())
                return false;
            return ( Arrays.equals(hash, ((StaleMateItem)obj).hash ));
        }
    }

    public StaleMate(Table table) {
        mStaleMap = new HashMap<>();
        mTable = table;
    }

    public boolean CheckStales(int count) {
        int pass = 0;
        for (Hand h : mTable.GetActiveHands()) {
            byte[] hString = h.hashString();
            if (!mStaleMap.containsKey(h.getID())) {
                ArrayList<StaleMateItem> list = new ArrayList<>();
                list.add(new StaleMateItem(hString));
                mStaleMap.put(h.getID(), list);
            }
            else {
                ArrayList<StaleMateItem> list = mStaleMap.get(h.getID());
                int i = list.indexOf(new StaleMateItem(hString));
                if (i >= 0) { //we found it
                    list.get(i).count++;
                    if (list.get(i).count >= count) {
                        pass++;
                        continue;
                    }
                }
                else {
                    list.add(new StaleMateItem(hString));
                }
            }
        }
        return pass >= 2;
    }

    public void clear() {
        mStaleMap.clear();
    }


}
