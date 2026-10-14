package retailiq.csv;

import retailiq.Review;
import retailiq.ReviewDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReviewCSVRepository {

    private final String filePath;
    private final List<Review> reviews = new ArrayList<>();

    public ReviewCSVRepository(String filePath) {
        this.filePath = filePath;
        load();
    }

    public synchronized void load() {
        reviews.clear();
        try {
            List<String> lines = CSVUtils.readLines(filePath);
            if (lines.isEmpty()) {
                seedDefaults();
                return;
            }

            boolean firstLine = true;
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                if (firstLine && line.toLowerCase().startsWith("reviewid")) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;

                List<String> cols = CSVUtils.parseLine(line);
                if (cols.size() >= 5) {
                    int id = CSVUtils.parseInt(cols.get(0), 0);
                    int prodId = CSVUtils.parseInt(cols.get(1), 0);
                    int custId = CSVUtils.parseInt(cols.get(2), 0);
                    String text = cols.get(3);
                    int rating = CSVUtils.parseInt(cols.get(4), 5);

                    Review review = new Review(id, prodId, custId, text, rating);
                    DataValidator.ValidationResult val = DataValidator.validateReview(review);
                    if (val.isValid()) {
                        reviews.add(review);
                    } else {
                        System.err.println("Skipping invalid review row: " + val.getErrorMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load reviews CSV: " + e.getMessage());
            seedDefaults();
        }
    }

    private void seedDefaults() {
        ReviewDatabase db = new ReviewDatabase();
        reviews.addAll(db.getReviews());
        persist();
    }

    public synchronized void persist() {
        List<String> lines = new ArrayList<>();
        lines.add("reviewId,productId,customerId,reviewText,rating");
        for (Review r : reviews) {
            lines.add(CSVUtils.formatLine(
                    r.getReviewId(),
                    r.getProductId(),
                    r.getCustomerId(),
                    r.getReviewText(),
                    r.getRating()
            ));
        }
        try {
            CSVUtils.writeLines(filePath, lines);
        } catch (IOException e) {
            System.err.println("Could not write reviews CSV: " + e.getMessage());
        }
    }

    public synchronized List<Review> findAll() {
        return new ArrayList<>(reviews);
    }

    public synchronized Optional<Review> findById(int id) {
        return reviews.stream().filter(r -> r.getReviewId() == id).findFirst();
    }

    public synchronized void save(Review review) {
        int index = -1;
        for (int i = 0; i < reviews.size(); i++) {
            if (reviews.get(i).getReviewId() == review.getReviewId()) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            reviews.set(index, review);
        } else {
            reviews.add(review);
        }
        persist();
    }

    public synchronized boolean deleteById(int id) {
        boolean removed = reviews.removeIf(r -> r.getReviewId() == id);
        if (removed) {
            persist();
        }
        return removed;
    }

    public synchronized void replaceAll(List<Review> newReviews) {
        reviews.clear();
        reviews.addAll(newReviews);
        persist();
    }
}
