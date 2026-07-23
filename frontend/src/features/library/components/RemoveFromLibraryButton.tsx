import { useTranslation } from 'react-i18next'
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
  children,
}: RemoveFromLibraryButtonProps) {
  const { t } = useTranslation('library')
  const { confirm } = useConfirm()
  const removeShow = useRemoveShow()

  async function handleClick() {
    const confirmed = await confirm({
      title: t('removeConfirm.title'),
      message: t('removeConfirm.message', { showTitle }),
      confirmLabel: t('removeConfirm.confirm'),
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
      {children ?? t('removeConfirm.button')}
    </button>
  )
}
