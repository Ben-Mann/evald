package net.benmann.evald;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Wrapper for parser lists. The parser list needs to always be sorted in descending order of key length,
 * which ensures that we don't match against a short operator when a longer operator shares the same initial characters.
 */
class ParserList<P extends Parser> implements Iterable<P> {
    List<P> parsers = new ArrayList<P>();

    @SafeVarargs final void add(P... ps) {
        parsers.addAll(Arrays.asList(ps));
        Collections.sort(parsers, new Comparator<P>() {
            @Override public int compare(P o1, P o2) {
                if (o1.token == null) {
                    if (o2.token == null)
                        return 0;
                    return -1;
                }

                if (o2.token == null)
                    return 1;

                return Integer.compare(o2.token.length(), o1.token.length());
            }
        });
    }

    final void remove(P parser) {
        parsers.remove(parser);
    }

    final void remove(String key) {
        if (key == null)
            return;

        List<P> toRemove = new ArrayList<P>();
        for (P p : parsers) {
            if (key.equals(p.token))
                toRemove.add(p);
        }
        parsers.removeAll(toRemove);
    }

    @Override public Iterator<P> iterator() {
        return parsers.iterator();
    }
}