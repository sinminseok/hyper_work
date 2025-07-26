package hyper.run.domain.game.utils;

public class GamePrizeCalculator {

    private static final int firstPrizeRate = 80;
    private static final int secondPrizeRate = 15;
    private static final int thirdPrizeRate = 5;

    public static double calculateFirstPlacePrize(double totalPrize) {
        return totalPrize * firstPrizeRate / 100.0;
    }

    public static double calculateSecondPlacePrize(double totalPrize) {
        return totalPrize * secondPrizeRate / 100.0;
    }

    public static double calculateThirdPlacePrize(double totalPrize) {
        return totalPrize * thirdPrizeRate / 100.0;
    }
}
