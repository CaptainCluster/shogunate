package com.tvtracker.watch;

import com.tvtracker.common.TargetType;
import com.tvtracker.common.openapi.ErrorResponse;
import com.tvtracker.common.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Watch")
@SecurityRequirement(name = "bearerAuth")
public class WatchController {

    private final WatchService watchService;

    @PostMapping("/episodes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Mark an episode as watched")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Episode marked watched"),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Show not in library",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Episode not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void markEpisodeWatched(@PathVariable UUID id, @CurrentUser UUID userId) {
        watchService.markWatched(userId, TargetType.EPISODE, id);
    }

    @DeleteMapping("/episodes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Unmark an episode as watched")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Episode unmarked"),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Show not in library",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Episode not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void unmarkEpisodeWatched(@PathVariable UUID id, @CurrentUser UUID userId) {
        watchService.unmarkWatched(userId, TargetType.EPISODE, id, true);
    }

    @PostMapping("/seasons/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Mark a season and its episodes as watched",
            description = "Cascades down to all episodes under the season.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Season marked watched"),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Show not in library",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Season not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void markSeasonWatched(@PathVariable UUID id, @CurrentUser UUID userId) {
        watchService.markWatched(userId, TargetType.SEASON, id);
    }

    @DeleteMapping("/seasons/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Unmark a season and its episodes as watched",
            description = "Requires confirm=true to cascade unwatch to all episodes under the season.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Season unmarked"),
        @ApiResponse(
                responseCode = "400",
                description = "Missing confirm=true",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Show not in library",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Season not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void unmarkSeasonWatched(
            @PathVariable UUID id,
            @Parameter(description = "Must be true to confirm cascade unwatch") @RequestParam(defaultValue = "false")
                    boolean confirm,
            @CurrentUser UUID userId) {
        watchService.unmarkWatched(userId, TargetType.SEASON, id, confirm);
    }

    @PostMapping("/shows/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Mark a show and all seasons and episodes as watched",
            description = "Cascades down through the full show hierarchy.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Show marked watched"),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Show not in library",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Show not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void markShowWatched(@PathVariable UUID id, @CurrentUser UUID userId) {
        watchService.markWatched(userId, TargetType.SHOW, id);
    }

    @DeleteMapping("/shows/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Unmark a show and all seasons and episodes as watched",
            description = "Requires confirm=true to cascade unwatch through the full show hierarchy.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Show unmarked"),
        @ApiResponse(
                responseCode = "400",
                description = "Missing confirm=true",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Show not in library",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Show not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void unmarkShowWatched(
            @PathVariable UUID id,
            @Parameter(description = "Must be true to confirm cascade unwatch") @RequestParam(defaultValue = "false")
                    boolean confirm,
            @CurrentUser UUID userId) {
        watchService.unmarkWatched(userId, TargetType.SHOW, id, confirm);
    }
}
