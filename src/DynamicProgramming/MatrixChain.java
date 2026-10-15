package DynamicProgramming;

public class MatrixChain {

    public static int minimumMultiplicationCost(int[] dimensions) {

        int n = dimensions.length;

        int[][] dp = new int[n][n];
        for (int length = 2; length < n; length++) {

            for (int i = 1; i < n - length + 1; i++) {

                int j = i + length - 1;

                dp[i][j] = Integer.MAX_VALUE;

                for (int k = i; k < j; k++) {

                    int cost = dp[i][k]
                            + dp[k + 1][j]
                            + dimensions[i - 1]
                            * dimensions[k]
                            * dimensions[j];

                    if (cost < dp[i][j]) {
                        dp[i][j] = cost;
                    }
                }
            }
        }

        return dp[1][n - 1];
    }

    public static void displayResult(int[] dimensions) {

        int cost = minimumMultiplicationCost(dimensions);

        System.out.println();
        System.out.println("=================================================");
        System.out.println("          MATRIX CHAIN OPTIMIZATION");
        System.out.println("=================================================");

        System.out.println(
                "Minimum Multiplication Cost : " + cost
        );
    }
}