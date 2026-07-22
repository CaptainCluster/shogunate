import { ReviewEditor, type ReviewEditorProps } from './ReviewEditor'

interface WatchedReviewEditorProps extends ReviewEditorProps {
  watched: boolean
  className?: string
}

export function WatchedReviewEditor({
  watched,
  className,
  ...reviewProps
}: WatchedReviewEditorProps) {
  if (!watched) {
    return null
  }

  const editor = <ReviewEditor {...reviewProps} />

  if (className) {
    return <div className={className}>{editor}</div>
  }

  return editor
}
