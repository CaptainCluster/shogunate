package com.tvtracker.show;

import com.tvtracker.common.openapi.ErrorResponse;
import com.tvtracker.common.security.CurrentUser;
import com.tvtracker.show.dto.AddShowRequest;
import com.tvtracker.show.dto.ShowDetailResponse;
import com.tvtracker.show.dto.ShowSearchResult;
import com.tvtracker.show.dto.ShowSummaryResponse;
import com.tvtracker.show.dto.UpdateLibraryStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
@Tag(name = "Shows")
@SecurityRequirement(name = "bearerAuth")
public class ShowController {

    private final ShowService showService;

    @GetMapping("/search")
    @Operation(
            summary = "Search TVmaze for shows (results are not persisted)",
            description = "Proxies TVmaze search; nothing is written to the database.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Search results"),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<ShowSearchResult> search(
            @Parameter(description = "Search query forwarded to TVmaze", required = true) @RequestParam String query) {
        return showService.search(query);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a show to the user's library")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Show added to library"),
        @ApiResponse(
                responseCode = "400",
                description = "Validation error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Show already in library",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ShowDetailResponse add(@Valid @RequestBody AddShowRequest request, @CurrentUser UUID userId) {
        return showService.addToLibrary(userId, request.tvmazeId());
    }

    @GetMapping
    @Operation(summary = "List shows in the user's library")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Library list"),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<ShowSummaryResponse> list(@CurrentUser UUID userId) {
        return showService.listLibrary(userId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get show detail with seasons and episodes")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Show detail with watch state"),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Show not in library",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ShowDetailResponse get(@PathVariable UUID id, @CurrentUser UUID userId) {
        return showService.getShowDetail(userId, id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update library status for a show")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Library status updated"),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid status transition",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Show not in library",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ShowSummaryResponse updateStatus(
            @PathVariable UUID id, @Valid @RequestBody UpdateLibraryStatusRequest request, @CurrentUser UUID userId) {
        return showService.updateLibraryStatus(userId, id, request.libraryStatus());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a show from the user's library")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Show removed from library"),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Show not in library",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void remove(@PathVariable UUID id, @CurrentUser UUID userId) {
        showService.removeFromLibrary(userId, id);
    }
}
