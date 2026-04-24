package pages;

public class HomePage {

    public void openHomePage() {
        System.out.println("Opening District.in homepage");
    }

    public void clickSearchIcon() {
        System.out.println("Clicked search icon");
    }

    public void clickMoviesLink() {
        System.out.println("Clicked movies link");
    }

    public void enterSearchText(String movieName) {
        System.out.println("Entered movie: " + movieName);
    }

    public void verifySearchResult(String expectedResult) {
        System.out.println("Verifying result: " + expectedResult);
    }
}