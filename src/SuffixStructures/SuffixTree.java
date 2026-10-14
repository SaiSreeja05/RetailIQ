package SuffixStructures;

import java.util.HashMap;

public class SuffixTree {

    static class Node {

        HashMap<Character, Node> children;
        int start;
        int end;

        Node(int start, int end) {
            this.start = start;
            this.end = end;
            children = new HashMap<>();
        }
    }

    private String text;
    private Node root;

    public SuffixTree(String text) {

        this.text = text;
        root = new Node(-1, -1);

        buildTree();
    }

    private void buildTree() {

        for (int i = 0; i < text.length(); i++) {
            insertSuffix(i);
        }
    }

    private void insertSuffix(int startPosition) {

        Node current = root;
        int position = startPosition;

        while (position < text.length()) {

            char currentChar = text.charAt(position);

            if (!current.children.containsKey(currentChar)) {

                Node newNode =
                        new Node(position, text.length() - 1);

                current.children.put(currentChar, newNode);
                return;
            }

            Node child = current.children.get(currentChar);

            int nodePosition = child.start;
            int matched = 0;

            while (nodePosition <= child.end
                    && position < text.length()
                    && text.charAt(nodePosition)
                    == text.charAt(position)) {

                nodePosition++;
                position++;
                matched++;
            }

            if (nodePosition > child.end) {

                current = child;
            }
            else {

                Node split =
                        new Node(child.start,
                                 child.start + matched - 1);

                current.children.put(currentChar, split);

                child.start =
                        child.start + matched;

                split.children.put(
                        text.charAt(child.start),
                        child
                );

                Node newLeaf =
                        new Node(position,
                                 text.length() - 1);

                split.children.put(
                        text.charAt(position),
                        newLeaf
                );

                return;
            }
        }
    }

    public boolean contains(String pattern) {

        Node current = root;
        int position = 0;

        while (position < pattern.length()) {

            char currentChar = pattern.charAt(position);

            if (!current.children.containsKey(currentChar)) {
                return false;
            }

            Node child = current.children.get(currentChar);

            int textPosition = child.start;

            while (textPosition <= child.end
                    && position < pattern.length()) {

                if (text.charAt(textPosition)
                        != pattern.charAt(position)) {

                    return false;
                }

                textPosition++;
                position++;
            }

            current = child;
        }

        return true;
    }

    public void search(String pattern) {

        if (contains(pattern)) {
            System.out.println(
                    "Pattern found: " + pattern
            );
        }
        else {
            System.out.println(
                    "Pattern not found: " + pattern
            );
        }
    }
}