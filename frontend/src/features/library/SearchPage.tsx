import type { FormEvent } from 'react'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import type { ShowSearchResult } from '../../api/showApi'
import { getErrorMessage } from '../../lib/getErrorMessage'
import { useAddShow, useShowSearch } from './hooks/useShowLibrary'
import './LibraryPage.css'

export function SearchPage() {
  const { t } = useTranslation('library')
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
      <h1>{t('search.title')}</h1>
      <p>
        <Link to="/library">{t('backToLibrary')}</Link>
      </p>

      <form className="library-search" onSubmit={handleSearch}>
        <input
          type="search"
          value={searchInput}
          onChange={(event) => setSearchInput(event.target.value)}
          placeholder={t('search.placeholder')}
          aria-label={t('search.ariaLabel')}
        />
        <button type="submit">{t('search.submit')}</button>
      </form>

      {activeQuery.length >= 2 && (
        <section className="library-section">
          <h2>{t('search.results')}</h2>
          {search.isLoading && <p>{t('search.searching')}</p>}
          {search.error && (
            <p className="library-error">{getErrorMessage(search.error, t('search.failed'))}</p>
          )}
          {search.data?.length === 0 && !search.isLoading && <p>{t('search.noResults')}</p>}
          <ul className="library-list">
            {search.data?.map((show: ShowSearchResult) => (
              <li key={show.tvmazeId} className="library-card">
                <div className="library-card__main">
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
                      {t('search.addToLibrary')}
                    </button>
                    {addShow.error && addShow.variables === show.tvmazeId && (
                      <p className="library-error">
                        {getErrorMessage(addShow.error, t('search.addFailed'))}
                      </p>
                    )}
                  </div>
                </div>
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  )
}
