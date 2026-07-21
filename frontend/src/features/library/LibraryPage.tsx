import type { FormEvent } from 'react'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import type { ShowSearchResult, ShowSummary } from '../../api/showApi'
import { getErrorMessage } from '../../lib/getErrorMessage'
import { useAddShow, useRemoveShow, useShowLibrary, useShowSearch } from './hooks/useShowLibrary'
import './LibraryPage.css'

export function LibraryPage() {
  const [searchInput, setSearchInput] = useState('')
  const [activeQuery, setActiveQuery] = useState('')

  const library = useShowLibrary()
  const search = useShowSearch(activeQuery)
  const addShow = useAddShow()
  const removeShow = useRemoveShow()

  function handleSearch(event: FormEvent) {
    event.preventDefault()
    setActiveQuery(searchInput.trim())
  }

  return (
    <div className="library-page">
      <h1>Your library</h1>

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

      <section className="library-section">
        <h2>In your library</h2>
        {library.isLoading && <p>Loading library…</p>}
        {library.error && (
          <p className="library-error">{getErrorMessage(library.error, 'Failed to load library')}</p>
        )}
        {library.data?.length === 0 && !library.isLoading && (
          <p>No shows yet. Search above to add one.</p>
        )}
        <ul className="library-list">
          {library.data?.map((show: ShowSummary) => (
            <li key={show.id} className="library-card">
              {show.posterUrl && (
                <img src={show.posterUrl} alt="" className="library-poster" />
              )}
              <div>
                <h3>
                  <Link to={`/library/${show.id}`}>{show.title}</Link>
                </h3>
                <p className="library-meta">Status: {show.libraryStatus.replace('_', ' ')}</p>
                <button
                  type="button"
                  disabled={removeShow.isPending}
                  onClick={() => removeShow.mutate(show.id)}
                >
                  Remove
                </button>
              </div>
            </li>
          ))}
        </ul>
      </section>
    </div>
  )
}
