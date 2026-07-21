import type { LibraryStatus } from '../../api/showApi'

const LIBRARY_STATUS_LABELS: Record<LibraryStatus, string> = {
  NONE: 'None',
  PLAN_TO_WATCH: 'Plan to Watch',
  WATCHED: 'Watched',
}

export function formatLibraryStatus(status: LibraryStatus): string {
  return LIBRARY_STATUS_LABELS[status]
}
