package retailiq;

public class Review {

    private int reviewId;
    private int productId;
    private int customerId;
    private String reviewText;
    private int rating;

    public Review(int reviewId, int productId, int customerId,
                  String reviewText, int rating) {

        this.reviewId = reviewId;
        this.productId = productId;
        this.customerId = customerId;
        this.reviewText = reviewText;
        this.rating = rating;
    }

    public int getReviewId() {
        return reviewId;
    }

    public int getProductId() {
        return productId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getReviewText() {
        return reviewText;
    }

    public int getRating() {
        return rating;
    }

    public void displayReview() {

        System.out.println("Review ID    : " + reviewId);
        System.out.println("Product ID   : " + productId);
        System.out.println("Customer ID  : " + customerId);
        System.out.println("Rating       : " + rating + "/5");
        System.out.println("Review       : " + reviewText);
    }
}