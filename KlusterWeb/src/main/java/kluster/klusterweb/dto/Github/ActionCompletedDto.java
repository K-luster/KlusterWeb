package kluster.klusterweb.dto.Github;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ActionCompletedDto {
    private final String userName;
    private final String repositoryName;
}
