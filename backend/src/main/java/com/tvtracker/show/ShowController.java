package com.tvtracker.show;

import com.tvtracker.common.security.CurrentUser;
import com.tvtracker.show.dto.AddShowRequest;
import com.tvtracker.show.dto.ShowDetailResponse;
import com.tvtracker.show.dto.ShowSearchResult;
import com.tvtracker.show.dto.ShowSummaryResponse;
import com.tvtracker.show.dto.UpdateLibraryStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
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
public class ShowController {

    private final ShowService showService;

    @GetMapping("/search")
    @Operation(summary = "Search TVmaze for shows (results are not persisted)")
    public List<ShowSearchResult> search(@RequestParam String query) {
        return showService.search(query);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a show to the user's library")
    public ShowDetailResponse add(@Valid @RequestBody AddShowRequest request, @CurrentUser UUID userId) {
        return showService.addToLibrary(userId, request.tvmazeId());
    }

    @GetMapping
    @Operation(summary = "List shows in the user's library")
    public List<ShowSummaryResponse> list(@CurrentUser UUID userId) {
        return showService.listLibrary(userId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get show detail with seasons and episodes")
    public ShowDetailResponse get(@PathVariable UUID id, @CurrentUser UUID userId) {
        return showService.getShowDetail(userId, id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update library status for a show")
    public ShowSummaryResponse updateStatus(
            @PathVariable UUID id, @Valid @RequestBody UpdateLibraryStatusRequest request, @CurrentUser UUID userId) {
        return showService.updateLibraryStatus(userId, id, request.libraryStatus());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a show from the user's library")
    public void remove(@PathVariable UUID id, @CurrentUser UUID userId) {
        showService.removeFromLibrary(userId, id);
    }
}
