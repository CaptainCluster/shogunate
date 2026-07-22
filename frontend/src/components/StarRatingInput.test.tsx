import { render } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { StarRatingInput } from './StarRatingInput'

describe('StarRatingInput', () => {
  it('selects half-star values from left and right clicks', async () => {
    const user = userEvent.setup()
    const onChange = vi.fn()

    const { container } = render(<StarRatingInput value={null} onChange={onChange} />)
    const leftHalves = container.querySelectorAll('.star-rating__half--left')

    await user.click(leftHalves[0]!)
    expect(onChange).toHaveBeenCalledWith(1)

    await user.click(container.querySelectorAll('.star-rating__half--right')[0]!)
    expect(onChange).toHaveBeenCalledWith(1.5)
  })

  it('steps rating with arrow keys', async () => {
    const user = userEvent.setup()
    const onChange = vi.fn()

    const { container } = render(<StarRatingInput value={2} onChange={onChange} label="Episode rating" />)

    const slider = container.querySelector('[role="slider"]') as HTMLElement
    slider.focus()
    await user.keyboard('{ArrowUp}')

    expect(onChange).toHaveBeenCalledWith(2.5)
  })
})
