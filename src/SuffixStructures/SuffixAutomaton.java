package SuffixStructures;

import java.util.HashMap;

public class SuffixAutomaton {

    static class State {

        int length;
        State suffixLink;
        HashMap<Character, State> next;

        State() {
            length = 0;
            suffixLink = null;
            next = new HashMap<>();
        }
    }

    private State root;
    private State last;

    public SuffixAutomaton(String text) {

        root = new State();
        root.suffixLink = null;

        last = root;

        for (char ch : text.toCharArray()) {
            extend(ch);
        }
    }

    private void extend(char ch) {

        State current = new State();

        current.length = last.length + 1;

        State pointer = last;

        while (pointer != null
                && !pointer.next.containsKey(ch)) {

            pointer.next.put(ch, current);
            pointer = pointer.suffixLink;
        }

        if (pointer == null) {

            current.suffixLink = root;

        } else {

            State existing = pointer.next.get(ch);

            if (existing.length == pointer.length + 1) {

                current.suffixLink = existing;

            } else {

                State clone = new State();

                clone.length = pointer.length + 1;
                clone.next.putAll(existing.next);
                clone.suffixLink = existing.suffixLink;

                while (pointer != null
                        && pointer.next.get(ch) == existing) {

                    pointer.next.put(ch, clone);
                    pointer = pointer.suffixLink;
                }

                existing.suffixLink = clone;
                current.suffixLink = clone;
            }
        }

        last = current;
    }

    public boolean contains(String pattern) {

        State current = root;

        for (char ch : pattern.toCharArray()) {

            if (!current.next.containsKey(ch)) {
                return false;
            }

            current = current.next.get(ch);
        }

        return true;
    }

    public void search(String pattern) {

        if (contains(pattern)) {

            System.out.println(
                    "Pattern found: " + pattern
            );

        } else {

            System.out.println(
                    "Pattern not found: " + pattern
            );
        }
    }
}