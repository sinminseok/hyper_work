package hyper.run.domain.game.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyExerciseResponse {

    private int totalHours;

    private int totalMinutes;

    private double totalDistanceKm;

    public static WeeklyExerciseResponse of(long totalMinutes, double totalDistanceKm) {
        int hours = (int) (totalMinutes / 60);
        int minutes = (int) (totalMinutes % 60);

        return WeeklyExerciseResponse.builder()
                .totalHours(hours)
                .totalMinutes(minutes)
                .totalDistanceKm(totalDistanceKm)
                .build();
    }
}
