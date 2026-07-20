package com.tvtracker.show.dto;

import com.tvtracker.show.LibraryStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateLibraryStatusRequest(@NotNull LibraryStatus libraryStatus) {}
