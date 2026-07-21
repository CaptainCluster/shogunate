package com.tvtracker.watch;

import com.tvtracker.common.TargetType;
import com.tvtracker.common.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/watch")
@RequiredArgsConstructor
public class WatchController {

    private final WatchService watchService;

    @PostMapping("/episodes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Mark an episode as watched")
    public void markEpisodeWatched(@PathVariable UUID id, @CurrentUser UUID userId) {
        watchService.markWatched(userId, TargetType.EPISODE, id);
    }

    @DeleteMapping("/episodes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Unmark an episode as watched")
    public void unmarkEpisodeWatched(@PathVariable UUID id, @CurrentUser UUID userId) {
        watchService.unmarkWatched(userId, TargetType.EPISODE, id, true);
    }

    @PostMapping("/seasons/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Mark a season and its episodes as watched")
    public void markSeasonWatched(@PathVariable UUID id, @CurrentUser UUID userId) {
        watchService.markWatched(userId, TargetType.SEASON, id);
    }

    @DeleteMapping("/seasons/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Unmark a season and its episodes as watched")
    public void unmarkSeasonWatched(
            @PathVariable UUID id, @RequestParam(defaultValue = "false") boolean confirm, @CurrentUser UUID userId) {
        watchService.unmarkWatched(userId, TargetType.SEASON, id, confirm);
    }

    @PostMapping("/shows/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Mark a show and all seasons and episodes as watched")
    public void markShowWatched(@PathVariable UUID id, @CurrentUser UUID userId) {
        watchService.markWatched(userId, TargetType.SHOW, id);
    }

    @DeleteMapping("/shows/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Unmark a show and all seasons and episodes as watched")
    public void unmarkShowWatched(
            @PathVariable UUID id, @RequestParam(defaultValue = "false") boolean confirm, @CurrentUser UUID userId) {
        watchService.unmarkWatched(userId, TargetType.SHOW, id, confirm);
    }
}
