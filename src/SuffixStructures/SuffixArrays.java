package SuffixStructures;

import java.util.Arrays;

public class SuffixArrays {

    public static int[] build(String text) {

        int n = text.length();

        Integer[] suffixes = new Integer[n];

        for (int i = 0; i < n; i++) {
            suffixes[i] = i;
        }

        Arrays.sort(suffixes, (a, b) -> {

            int i = a;
            int j = b;

            while (i < n && j < n) {

                if (text.charAt(i) != text.charAt(j)) {
                    return text.charAt(i) - text.charAt(j);
                }

                i++;
                j++;
            }

            return (n - a) - (n - b);
        });

        int[] result = new int[n];

        for (int i = 0; i < n; i++) {
            result[i] = suffixes[i];
        }

        return result;
    }

    public static void display(String text) {

        int[] suffixArray = build(text);

        System.out.println("Suffix Array:");

        for (int i = 0; i < suffixArray.length; i++) {

            int position = suffixArray[i];

            System.out.println(
                    position + " -> " + text.substring(position)
            );
        }
    }
}