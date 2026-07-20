import { Link } from 'react-router-dom'
import './LibraryPage.css'

export function AboutPage() {
  return (
    <article className="about-page">
      <h1>About &amp; credits</h1>
      <p>
        Show metadata and images are provided by{' '}
        <a href="https://www.tvmaze.com" target="_blank" rel="noreferrer">
          TVmaze
        </a>{' '}
        via their public API.
      </p>
      <p>
        TVmaze data is licensed under{' '}
        <a
          href="https://creativecommons.org/licenses/by-sa/4.0/"
          target="_blank"
          rel="noreferrer"
        >
          CC BY-SA 4.0
        </a>
        . Attribution is required when displaying TVmaze-sourced metadata.
      </p>
      <p>
        API documentation:{' '}
        <a href="https://www.tvmaze.com/api" target="_blank" rel="noreferrer">
          tvmaze.com/api
        </a>
      </p>
      <p>
        <Link to="/library">Go to your library</Link>
      </p>
    </article>
  )
}
