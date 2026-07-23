import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { renderWithI18n } from '../test/renderWithI18n'
import { StarRatingInput } from './StarRatingInput'

describe('StarRatingInput', () => {
  it('selects full and half-star values from clicks', async () => {
    const user = userEvent.setup()
    const onChange = vi.fn()

    const { container } = renderWithI18n(<StarRatingInput value={null} onChange={onChange} />)
    const leftHalves = container.querySelectorAll('.star-rating__half--left')
    const rightHalves = container.querySelectorAll('.star-rating__half--right')

    await user.click(leftHalves[0]!)
    expect(onChange).toHaveBeenCalledWith(1)

    await user.click(leftHalves[1]!)
    expect(onChange).toHaveBeenCalledWith(1.5)

    await user.click(rightHalves[2]!)
    expect(onChange).toHaveBeenCalledWith(3)
  })

  it('steps rating with arrow keys', async () => {
    const user = userEvent.setup()
    const onChange = vi.fn()

    const { container } = renderWithI18n(
      <StarRatingInput value={2} onChange={onChange} label="Episode rating" />,
    )

    const slider = container.querySelector('[role="slider"]') as HTMLElement
    slider.focus()
    await user.keyboard('{ArrowUp}')

    expect(onChange).toHaveBeenCalledWith(2.5)
  })
})
