package DynamicProgramming;

public class BitmaskDP {
    public static int maxValue(int[] prices, int[] values, int budget) {
        int n = prices.length;
        int totalMasks = 1 << n;

        int bestValue = 0;

        for (int mask = 0; mask < totalMasks; mask++) {

            int totalPrice = 0;
            int totalValue = 0;

            for (int i = 0; i < n; i++) {

                if ((mask & (1 << i)) != 0) {

                    totalPrice += prices[i];
                    totalValue += values[i];
                }
            }

            if (totalPrice <= budget) {

                if (totalValue > bestValue) {
                    bestValue = totalValue;
                }
            }
        }

        return bestValue;
    }

    public static void displaySelection(
            int[] prices,
            int[] values,
            String[] products,
            int budget) {

        int n = prices.length;
        int totalMasks = 1 << n;

        int bestValue = 0;
        int bestPrice = 0;
        int bestMask = 0;

        for (int mask = 0; mask < totalMasks; mask++) {

            int totalPrice = 0;
            int totalValue = 0;

            for (int i = 0; i < n; i++) {

                if ((mask & (1 << i)) != 0) {

                    totalPrice += prices[i];
                    totalValue += values[i];
                }
            }

            if (totalPrice <= budget
                    && totalValue > bestValue) {

                bestValue = totalValue;
                bestPrice = totalPrice;
                bestMask = mask;
            }
        }

        System.out.println();
        System.out.println("=================================================");
        System.out.println("          PRODUCT COMBINATION RESULT");
        System.out.println("=================================================");

        System.out.println("Budget       : Rs. " + budget);
        System.out.println("Selected Products:");

        for (int i = 0; i < n; i++) {

            if ((bestMask & (1 << i)) != 0) {

                System.out.println(
                        products[i]
                        + " - Rs. " + prices[i]
                );
            }
        }

        System.out.println("-----------------------------------------------");
        System.out.println("Total Price  : Rs. " + bestPrice);
        System.out.println("Total Value  : " + bestValue);
    }
}