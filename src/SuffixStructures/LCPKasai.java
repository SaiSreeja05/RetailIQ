package SuffixStructures;

public class LCPKasai {

    public static int[] buildLCP(String text, int[] suffixArray) {

        int n = text.length();

        int[] lcp = new int[n];
        int[] rank = new int[n];

        for (int i = 0; i < n; i++) {
            rank[suffixArray[i]] = i;
        }

        int commonLength = 0;

        for (int i = 0; i < n; i++) {

            int position = rank[i];

            if (position == 0) {
                continue;
            }

            int previousSuffix = suffixArray[position - 1];

            while (i + commonLength < n
                    && previousSuffix + commonLength < n
                    && text.charAt(i + commonLength)
                    == text.charAt(previousSuffix + commonLength)) {

                commonLength++;
            }

            lcp[position] = commonLength;

            if (commonLength > 0) {
                commonLength--;
            }
        }

        return lcp;
    }

    public static void display(String text, int[] suffixArray) {

        int[] lcp = buildLCP(text, suffixArray);

        System.out.println("LCP Array:");

        for (int i = 0; i < suffixArray.length; i++) {

            System.out.println(
                    "Suffix: " + text.substring(suffixArray[i])
                    + " | LCP: " + lcp[i]
            );
        }
    }
}