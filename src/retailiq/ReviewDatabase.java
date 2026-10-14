package retailiq;

import java.util.ArrayList;

public class ReviewDatabase {

    private ArrayList<Review> reviews;

    public ReviewDatabase() {

        reviews = new ArrayList<>();

        reviews.add(new Review(
                301,
                101,
                201,
                "The iPhone has excellent camera quality and smooth performance",
                5
        ));

        reviews.add(new Review(
                302,
                102,
                202,
                "The iPhone Pro has a premium design and excellent camera",
                5
        ));

        reviews.add(new Review(
                303,
                103,
                203,
                "Samsung Galaxy has a powerful processor and bright display",
                4
        ));

        reviews.add(new Review(
                304,
                104,
                204,
                "The HP laptop provides good performance and storage",
                4
        ));

        reviews.add(new Review(
                305,
                105,
                205,
                "MacBook Air is lightweight with excellent battery life",
                5
        ));

        reviews.add(new Review(
                306,
                106,
                206,
                "The headphones have good sound quality and noise cancellation",
                4
        ));

        reviews.add(new Review(
                307,
                107,
                207,
                "The wireless mouse is comfortable and works smoothly",
                4
        ));

        reviews.add(new Review(
                308,
                108,
                208,
                "The gaming keyboard has good RGB lighting and fast response",
                5
        ));

        reviews.add(new Review(
                309,
                109,
                201,
                "The monitor has a clear display and good viewing experience",
                4
        ));

        reviews.add(new Review(
                310,
                110,
                202,
                "The smart watch has useful activity tracking features",
                4
        ));
    }

    public ArrayList<Review> getReviews() {
        return reviews;
    }

    public void displayAllReviews() {

        System.out.println();
        System.out.println("=================================================");
        System.out.println("                  RETAILIQ REVIEWS");
        System.out.println("=================================================");

        for (Review review : reviews) {

            System.out.println();
            review.displayReview();
            System.out.println("-----------------------------------------------");
        }
    }

    public Review findReviewById(int reviewId) {

        for (Review review : reviews) {

            if (review.getReviewId() == reviewId) {
                return review;
            }
        }

        return null;
    }
}