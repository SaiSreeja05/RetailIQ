package StringAlgorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class KeywordMatcher {

    static class Node {

        HashMap<Character, Node> children = new HashMap<>();
        Node failure;
        ArrayList<String> words = new ArrayList<>();
    }
    private Node root;

    public KeywordMatcher() {
        root = new Node();
        root.failure = root;
    }

    public void addKeyword(String word) {

        word = word.toLowerCase();

        Node current = root;

        for (char ch : word.toCharArray()) {

            if (!current.children.containsKey(ch)) {
                current.children.put(ch, new Node());
            }

            current = current.children.get(ch);
        }

        current.words.add(word);
    }

    public void build() {

        Queue<Node> queue = new LinkedList<>();

        for (Node child : root.children.values()) {

            child.failure = root;
            queue.add(child);
        }

        while (!queue.isEmpty()) {

            Node current = queue.remove();

            for (Map.Entry<Character, Node> entry
                    : current.children.entrySet()) {

                char ch = entry.getKey();
                Node child = entry.getValue();

                Node fallback = current.failure;

                while (fallback != root
                        && !fallback.children.containsKey(ch)) {

                    fallback = fallback.failure;
                }

                if (fallback.children.containsKey(ch)) {
                    child.failure = fallback.children.get(ch);
                } else {
                    child.failure = root;
                }

                child.words.addAll(child.failure.words);

                queue.add(child);
            }
        }
    }

    public ArrayList<String> search(String text) {

        ArrayList<String> result = new ArrayList<>();

        text = text.toLowerCase();

        Node current = root;

        for (char ch : text.toCharArray()) {

            if (!Character.isLetter(ch)) {
                current = root;
                continue;
            }

            while (current != root
                    && !current.children.containsKey(ch)) {

                current = current.failure;
            }

            if (current.children.containsKey(ch)) {
                current = current.children.get(ch);
            }

            for (String word : current.words) {

                if (!result.contains(word)) {
                    result.add(word);
                }
            }
        }

        return result;
    }
}