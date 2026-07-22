import { useConfirm } from '../../../hooks/useConfirm'
import { useRemoveShow } from '../hooks/useShowLibrary'

interface RemoveFromLibraryButtonProps {
  showId: string
  showTitle: string
  disabled?: boolean
  onSuccess?: () => void
  children?: React.ReactNode
}

export function RemoveFromLibraryButton({
  showId,
  showTitle,
  disabled,
  onSuccess,
  children = 'Remove from library',
}: RemoveFromLibraryButtonProps) {
  const { confirm } = useConfirm()
  const removeShow = useRemoveShow()

  async function handleClick() {
    const confirmed = await confirm({
      title: 'Remove from library?',
      message: `Remove "${showTitle}" from your library? This will permanently delete your reviews, watch history, and watch state for this show.`,
      confirmLabel: 'Remove',
    })

    if (!confirmed) {
      return
    }

    removeShow.mutate(showId, { onSuccess })
  }

  return (
    <button
      type="button"
      disabled={disabled ?? removeShow.isPending}
      onClick={() => void handleClick()}
    >
      {children}
    </button>
  )
}
