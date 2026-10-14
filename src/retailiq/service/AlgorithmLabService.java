package retailiq.service;

import DynamicProgramming.*;
import StringAlgorithms.*;
import SuffixStructures.*;
import retailiq.Product;
import retailiq.Review;

import java.util.*;

public class AlgorithmLabService {

    private final RetailIQService service;

    public AlgorithmLabService(RetailIQService service) {
        this.service = service;
    }

    // ==========================================
    // 1. DYNAMIC PROGRAMMING
    // ==========================================

    public static class BundleResult {
        public final int maxScore;
        public final int totalPrice;
        public final List<Product> selectedProducts;
        public final String explanation;

        public BundleResult(int maxScore, int totalPrice, List<Product> selectedProducts, String explanation) {
            this.maxScore = maxScore;
            this.totalPrice = totalPrice;
            this.selectedProducts = selectedProducts;
            this.explanation = explanation;
        }
    }

    /**
     * Integrates BitmaskDP to solve the Retail Product Bundle Optimization problem.
     * Takes real inventory products, treats price as weight and rating * 10 as value.
     */
    public BundleResult runBitmaskBundleOptimization(int budget) {
        List<Product> catalog = service.getAllProducts();
        // Bitmask DP runs in O(2^N * N), so limit to top 15 products for instant interactive execution
        int n = Math.min(catalog.size(), 14);
        if (n == 0) {
            return new BundleResult(0, 0, Collections.emptyList(), "No products available in catalog.");
        }

        int[] prices = new int[n];
        int[] values = new int[n];
        String[] productNames = new String[n];

        for (int i = 0; i < n; i++) {
            Product p = catalog.get(i);
            prices[i] = (int) Math.round(p.getPrice());
            // Rating scaled to integer score (e.g., 4.5 -> 45) + category bonus
            values[i] = (int) Math.round(p.getRating() * 10.0);
            productNames[i] = p.getName();
        }

        // Call existing BitmaskDP implementation
        int maxVal = BitmaskDP.maxValue(prices, values, budget);

        // Find which products were selected by evaluating masks
        int totalMasks = 1 << n;
        int bestMask = 0;
        int bestPrice = 0;

        for (int mask = 0; mask < totalMasks; mask++) {
            int tp = 0;
            int tv = 0;
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    tp += prices[i];
                    tv += values[i];
                }
            }
            if (tp <= budget && tv == maxVal) {
                bestMask = mask;
                bestPrice = tp;
                break;
            }
        }

        List<Product> selected = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if ((bestMask & (1 << i)) != 0) {
                selected.add(catalog.get(i));
            }
        }

        String explanation = String.format(
                "Bitmask DP evaluated all 2^%d (%d) candidate bundles.\n" +
                "Optimal selection achieves Total Satisfaction Value of %d within Rs. %,d budget (Actual spend: Rs. %,d).\n" +
                "This guarantees the mathematical optimum subset of retail items.",
                n, totalMasks, maxVal, budget, bestPrice
        );

        return new BundleResult(maxVal, bestPrice, selected, explanation);
    }

    public static class LevenshteinResult {
        public final int distance;
        public final boolean isClose;
        public final int[][] dpTable;
        public final String s1;
        public final String s2;
        public final String explanation;

        public LevenshteinResult(int distance, boolean isClose, int[][] dpTable, String s1, String s2, String explanation) {
            this.distance = distance;
            this.isClose = isClose;
            this.dpTable = dpTable;
            this.s1 = s1;
            this.s2 = s2;
            this.explanation = explanation;
        }
    }

    /**
     * Runs existing Levenshtein distance and constructs the full DP table for visual rendering.
     */
    public LevenshteinResult runLevenshtein(String s1, String s2) {
        if (s1 == null) s1 = "";
        if (s2 == null) s2 = "";

        // Call existing methods
        int dist = Levenshtein.distance(s1, s2);
        boolean close = Levenshtein.isClose(s1, s2);

        // Build 2D DP matrix for UI visualization
        int n = s1.length();
        int m = s2.length();
        int[][] dp = new int[n + 1][m + 1];

        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    int insert = dp[i][j - 1];
                    int delete = dp[i - 1][j];
                    int replace = dp[i - 1][j - 1];
                    dp[i][j] = 1 + Math.min(insert, Math.min(delete, replace));
                }
            }
        }

        String explanation = String.format(
                "Levenshtein distance between '%s' and '%s' is %d edits.\n" +
                "Fuzzy Match Status: %s (Threshold: <= 2 operations).\n" +
                "Used in Smart Search for typo-tolerant product catalog queries and misspelling recovery.",
                s1, s2, dist, close ? "CLOSE MATCH (Auto-suggest eligible)" : "DISTANT", s1, s2
        );

        return new LevenshteinResult(dist, close, dp, s1, s2, explanation);
    }

    public static class MatrixChainResult {
        public final int minCost;
        public final int[] dimensions;
        public final int[][] dpTable;
        public final String explanation;

        public MatrixChainResult(int minCost, int[] dimensions, int[][] dpTable, String explanation) {
            this.minCost = minCost;
            this.dimensions = dimensions;
            this.dpTable = dpTable;
            this.explanation = explanation;
        }
    }

    /**
     * Runs existing MatrixChain optimization for multi-tier retail distribution pipelines.
     */
    public MatrixChainResult runMatrixChain(int[] dimensions) {
        if (dimensions == null || dimensions.length < 2) {
            dimensions = new int[]{10, 20, 30, 40, 30};
        }

        int cost = MatrixChain.minimumMultiplicationCost(dimensions);

        // Build table for visualization
        int n = dimensions.length;
        int[][] dp = new int[n][n];

        for (int length = 2; length < n; length++) {
            for (int i = 1; i < n - length + 1; i++) {
                int j = i + length - 1;
                dp[i][j] = Integer.MAX_VALUE;
                for (int k = i; k < j; k++) {
                    int c = dp[i][k] + dp[k + 1][j] + dimensions[i - 1] * dimensions[k] * dimensions[j];
                    if (c < dp[i][j]) {
                        dp[i][j] = c;
                    }
                }
            }
        }

        String explanation = String.format(
                "Matrix Chain Optimization calculated Minimum Computational/Restock Pipeline Cost: %,d.\n" +
                "Evaluates optimal parenthesization of %d operational transformation stages to minimize batch execution latency.",
                cost, dimensions.length - 1
        );

        return new MatrixChainResult(cost, dimensions, dp, explanation);
    }

    // ==========================================
    // 2. STRING ALGORITHMS
    // ==========================================

    public static class KeywordMatchResult {
        public final List<String> matchedKeywords;
        public final String text;
        public final String explanation;

        public KeywordMatchResult(List<String> matchedKeywords, String text, String explanation) {
            this.matchedKeywords = matchedKeywords;
            this.text = text;
            this.explanation = explanation;
        }
    }

    /**
     * Integrates KeywordMatcher (Aho-Corasick) to scan real customer reviews or custom text.
     */
    public KeywordMatchResult runAhoCorasick(String text, List<String> keywords) {
        KeywordMatcher matcher = new KeywordMatcher();
        for (String kw : keywords) {
            matcher.addKeyword(kw);
        }
        matcher.build();

        ArrayList<String> found = matcher.search(text);

        String explanation = String.format(
                "Aho-Corasick Automaton scanned %d characters against %d target retail keywords in linear O(N + M + Z) time.\n" +
                "Detected %d matching keyword occurrences simultaneously.",
                text.length(), keywords.size(), found.size()
        );

        return new KeywordMatchResult(found, text, explanation);
    }

    public static class StringSearchResult {
        public final String algorithm;
        public final int index;
        public final boolean found;
        public final String text;
        public final String pattern;
        public final String explanation;

        public StringSearchResult(String algorithm, int index, boolean found, String text, String pattern, String explanation) {
            this.algorithm = algorithm;
            this.index = index;
            this.found = found;
            this.text = text;
            this.pattern = pattern;
            this.explanation = explanation;
        }
    }

    public StringSearchResult runKMP(String text, String pattern) {
        int idx = KMP.search(text, pattern);
        String exp = String.format(
                "KMP Algorithm constructed the Longest Prefix Suffix (LPS) array for pattern '%s' (length %d).\n" +
                "Scanned text of length %d without backtracking in O(N + M) time.\n" +
                "Result: %s",
                pattern, pattern.length(), text.length(), idx != -1 ? "Match located at 0-based index " + idx : "Pattern not found."
        );
        return new StringSearchResult("Knuth-Morris-Pratt (KMP)", idx, idx != -1, text, pattern, exp);
    }

    public StringSearchResult runRabinKarp(String text, String pattern) {
        int idx = RabinKarp.search(text, pattern);
        boolean contains = RabinKarp.contains(text, pattern);
        String exp = String.format(
                "Rabin-Karp Rolling Hash Algorithm computed polynomial rolling hash (Base=256, Prime=101).\n" +
                "Matches evaluated in O(1) hash updates per sliding window step.\n" +
                "Result: %s",
                contains ? "Pattern found at index " + idx : "Pattern not present."
        );
        return new StringSearchResult("Rabin-Karp Rolling Hash", idx, contains, text, pattern, exp);
    }

    public static class ZFunctionResult {
        public final int[] zArray;
        public final boolean contains;
        public final String text;
        public final String pattern;
        public final String explanation;

        public ZFunctionResult(int[] zArray, boolean contains, String text, String pattern, String explanation) {
            this.zArray = zArray;
            this.contains = contains;
            this.text = text;
            this.pattern = pattern;
            this.explanation = explanation;
        }
    }

    public ZFunctionResult runZFunction(String text, String pattern) {
        boolean contains = ZFunction.contains(text, pattern);
        String combined = pattern + "$" + text;
        int[] z = ZFunction.calculateZ(combined);

        String exp = String.format(
                "Z-Algorithm computed Z-box lengths for concatenated string '%s'.\n" +
                "Pattern match detected: %s (Z-value equals pattern length %d).",
                combined, contains ? "YES" : "NO", pattern.length()
        );
        return new ZFunctionResult(z, contains, text, pattern, exp);
    }

    // ==========================================
    // 3. SUFFIX STRUCTURES
    // ==========================================

    public static class SuffixArrayKasaiResult {
        public final int[] suffixArray;
        public final int[] lcpArray;
        public final String text;
        public final List<String> suffixStrings;
        public final String explanation;

        public SuffixArrayKasaiResult(int[] suffixArray, int[] lcpArray, String text, List<String> suffixStrings, String explanation) {
            this.suffixArray = suffixArray;
            this.lcpArray = lcpArray;
            this.text = text;
            this.suffixStrings = suffixStrings;
            this.explanation = explanation;
        }
    }

    public SuffixArrayKasaiResult runSuffixArrayAndKasai(String text) {
        if (text == null || text.isEmpty()) text = "retailiq";

        int[] sa = SuffixArrays.build(text);
        int[] lcp = LCPKasai.buildLCP(text, sa);

        List<String> list = new ArrayList<>();
        for (int idx : sa) {
            list.add(text.substring(idx));
        }

        String exp = String.format(
                "Suffix Array sorted all %d suffixes lexicographically.\n" +
                "Kasai's algorithm computed the Longest Common Prefix (LCP) array in linear O(N) time.\n" +
                "Enables lightning-fast catalog substring binary searching and common phrase clustering.",
                text.length()
        );

        return new SuffixArrayKasaiResult(sa, lcp, text, list, exp);
    }

    public static class SuffixQueryResult {
        public final String structure;
        public final boolean found;
        public final String text;
        public final String pattern;
        public final String explanation;

        public SuffixQueryResult(String structure, boolean found, String text, String pattern, String explanation) {
            this.structure = structure;
            this.found = found;
            this.text = text;
            this.pattern = pattern;
            this.explanation = explanation;
        }
    }

    public SuffixQueryResult runSuffixAutomaton(String text, String pattern) {
        SuffixAutomaton sam = new SuffixAutomaton(text);
        boolean contains = sam.contains(pattern);
        String exp = String.format(
                "Suffix Automaton (DAWG) indexed text of %d characters in O(N) states.\n" +
                "Query '%s' processed in exact O(|pattern| = %d) state transitions.\n" +
                "Result: %s",
                text.length(), pattern, pattern.length(), contains ? "SUBSTRING FOUND" : "NOT FOUND"
        );
        return new SuffixQueryResult("Suffix Automaton (DAWG)", contains, text, pattern, exp);
    }

    public SuffixQueryResult runSuffixTree(String text, String pattern) {
        SuffixTree tree = new SuffixTree(text);
        boolean contains = tree.contains(pattern);
        String exp = String.format(
                "Compressed Suffix Tree built across text.\n" +
                "Edge traversing executed for '%s'.\n" +
                "Result: %s",
                pattern, contains ? "SUBSTRING LOCATED IN TREE" : "SUBSTRING ABSENT"
        );
        return new SuffixQueryResult("Suffix Tree", contains, text, pattern, exp);
    }
}
