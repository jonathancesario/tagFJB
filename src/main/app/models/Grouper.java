package app.models;

import java.util.HashMap;
import java.util.Map;


public class Grouper
{
    private Database database;
    private Map<String,Integer> oldTags;
    private Map<String,Tag> todayTags;
    private int maxChar;

    public Grouper(Map todayTags, Map oldTags, int maxChar) throws Exception {
        this.todayTags = todayTags;
        this.oldTags = oldTags;
        this.maxChar = maxChar;
        this.database = new Database();
    }

    public Map<String,Integer> groupSimilarTag() throws Exception {
        Map<String,Integer> newTags = new HashMap<>();
        for (Map.Entry<String,Tag> entry : todayTags.entrySet()) {
            String key = entry.getKey();
            Tag value = entry.getValue();

            /* length based filtering */
            if (key.length() > maxChar) {
                continue;
            }

            int best_distance    = 3;
            int best_counter     = 0;
            String best_tag      = "";

            /* find the most similar tag name on oldTag */
            for (Map.Entry<String,Integer> tag : oldTags.entrySet()) {
                String tagName  = tag.getKey();
                int tagCount    = tag.getValue();

                int distance = levenshteinDistance(key, tagName);

                if (distance < best_distance || (distance == best_distance && best_counter < tagCount)) {
                    best_distance   = distance;
                    best_tag        = tagName;
                    best_counter    = tagCount;
                }
            }

            /* no similar tag found */
            if (best_distance > 1) {
                database.insertTag(key,value.getData().getCounter());
                oldTags.put(key,value.getData().getCounter());
                newTags.put(key,value.getData().getCounter());
            } else {
                String tag;

                /* if new tag is better than old tag */
                if (best_counter < value.getData().getCounter() && best_distance > 0) {
                    int tmp;

                    /* update old tags */
                    tmp = oldTags.containsKey(best_tag) ? oldTags.get(best_tag) : 0;
                    oldTags.remove(best_tag);
                    oldTags.put(key, tmp);

                    /* update new tags */
                    tmp = newTags.containsKey(best_tag) ? newTags.get(best_tag) : 0;
                    newTags.remove(best_tag);
                    newTags.put(key, tmp);

                    database.updateTag(best_tag, key);

                    tag = key;
                } else {
                    tag = best_tag;
                }

                newTags.put(tag, (newTags.containsKey(tag) ? newTags.get(tag) : 0) + value.getData().getCounter());
                oldTags.put(tag, (oldTags.containsKey(tag) ? oldTags.get(tag) : 0) + value.getData().getCounter());
            }
        }
        return newTags;
    }

    public int levenshteinDistance(String s1, String s2) {
        if (Math.abs(s1.length() - s2.length()) > 1) {
            return 2;
        }

        /* faster algorithm to check whether Levenshtein distance > 1 (O(N)) */
        int count = 0;
        if (s1.length() == s2.length()) { // equal size
            int len = s1.length();
            for (int i = 0; i < len; i++) {
                count += (s1.charAt(i) != s2.charAt(i)) ? 1 : 0;
                if (count > 1) return 2;
            }
        } else {
            int l = 0, sizeL = s1.length();
            int r = 0, sizeR = s2.length();
            while (l < sizeL && r < sizeR) {
                if (s1.charAt(l) != s2.charAt(r)) {
                    count++;
                    if (count > 1) return 2;
                    if (sizeL < sizeR) r++;
                    if (sizeL > sizeR) l++;
                }
                else {
                    l++;
                    r++;
                }
            }
            if (l < sizeL) count++;
            if (r < sizeR) count++;
        }

        return count;
    }
}
