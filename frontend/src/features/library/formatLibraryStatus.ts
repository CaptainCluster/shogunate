import i18n from '../../i18n/config'
import type { LibraryStatus } from '../../api/showApi'

export function formatLibraryStatus(status: LibraryStatus): string {
  return i18n.t(`status.${status}`, { ns: 'library' })
}
