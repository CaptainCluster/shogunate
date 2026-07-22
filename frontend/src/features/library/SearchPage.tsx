import type { FormEvent } from 'react'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import type { ShowSearchResult } from '../../api/showApi'
import { getErrorMessage } from '../../lib/getErrorMessage'
import { useAddShow, useShowSearch } from './hooks/useShowLibrary'
import './LibraryPage.css'

export function SearchPage() {
  const [searchInput, setSearchInput] = useState('')
  const [activeQuery, setActiveQuery] = useState('')

  const search = useShowSearch(activeQuery)
  const addShow = useAddShow()

  function handleSearch(event: FormEvent) {
    event.preventDefault()
    setActiveQuery(searchInput.trim())
  }

  return (
    <div className="library-page">
      <h1>Search TV shows</h1>
      <p>
        <Link to="/library">← Back to library</Link>
      </p>

      <form className="library-search" onSubmit={handleSearch}>
        <input
          type="search"
          value={searchInput}
          onChange={(event) => setSearchInput(event.target.value)}
          placeholder="Search TVmaze for shows…"
          aria-label="Search shows"
        />
        <button type="submit">Search</button>
      </form>

      {activeQuery.length >= 2 && (
        <section className="library-section">
          <h2>Search results</h2>
          {search.isLoading && <p>Searching…</p>}
          {search.error && (
            <p className="library-error">{getErrorMessage(search.error, 'Search failed')}</p>
          )}
          {search.data?.length === 0 && !search.isLoading && <p>No shows found.</p>}
          <ul className="library-list">
            {search.data?.map((show: ShowSearchResult) => (
              <li key={show.tvmazeId} className="library-card">
                {show.posterUrl && (
                  <img src={show.posterUrl} alt="" className="library-poster" />
                )}
                <div>
                  <h3>{show.title}</h3>
                  {show.overview && <p className="library-overview">{show.overview}</p>}
                  <button
                    type="button"
                    disabled={addShow.isPending && addShow.variables === show.tvmazeId}
                    onClick={() => addShow.mutate(show.tvmazeId)}
                  >
                    Add to library
                  </button>
                  {addShow.error && addShow.variables === show.tvmazeId && (
                    <p className="library-error">{getErrorMessage(addShow.error, 'Add failed')}</p>
                  )}
                </div>
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  )
}
