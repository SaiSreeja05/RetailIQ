package StringAlgorithms;

public class RabinKarp {

    public static int search(String text, String pattern) {

        int n = text.length();
        int m = pattern.length();

        if (m == 0) {
            return 0;
        }
        if (m > n) {
            return -1;
        }

        int base = 256;
        int prime = 101;

        int patternHash = 0;
        int textHash = 0;
        int highestPower = 1;

        for (int i = 0; i < m - 1; i++) {
            highestPower = (highestPower * base) % prime;
        }

        for (int i = 0; i < m; i++) {

            patternHash =
                    (base * patternHash + pattern.charAt(i)) % prime;

            textHash =
                    (base * textHash + text.charAt(i)) % prime;
        }

        for (int i = 0; i <= n - m; i++) {

            if (patternHash == textHash) {

                boolean match = true;

                for (int j = 0; j < m; j++) {

                    if (text.charAt(i + j) != pattern.charAt(j)) {
                        match = false;
                        break;
                    }
                }

                if (match) {
                    return i;
                }
            }

            if (i < n - m) {

                textHash =
                        (base * (textHash
                        - text.charAt(i) * highestPower)
                        + text.charAt(i + m)) % prime;

                if (textHash < 0) {
                    textHash = textHash + prime;
                }
            }
        }

        return -1;
    }

    public static boolean contains(String text, String pattern) {

        return search(text, pattern) != -1;
    }
}