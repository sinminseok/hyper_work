package hyper.run.domain.game.utils;

public class GamePrizeCalculator {

    private static final int firstPrizeRate = 60;
    private static final int secondPrizeRate = 15;
    private static final int thirdPrizeRate = 6;
    private static final int fourthPrizeRate = 3;
    private static final int otherPrizeRate = 1;

    public static double calculateFirstPlacePrize(double totalPrize) {
        return totalPrize * firstPrizeRate / 100.0;
    }

    public static double calculateSecondPlacePrize(double totalPrize) {
        return totalPrize * secondPrizeRate / 100.0;
    }

    public static double calculateThirdPlacePrize(double totalPrize) {
        return totalPrize * thirdPrizeRate / 100.0;
    }

    public static double calculateFourthPlacePrize(double totalPrize) {
        return totalPrize * fourthPrizeRate / 100.0;
    }

    public static double calculateOtherPlacePrize(double totalPrize) {
        return totalPrize * otherPrizeRate / 100.0;
    }
}
