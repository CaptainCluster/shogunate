import { Link } from 'react-router-dom'
import { Trans, useTranslation } from 'react-i18next'
import './LibraryPage.css'

export function AboutPage() {
  const { t } = useTranslation('library')

  return (
    <article className="about-page">
      <h1>{t('about.title')}</h1>
      <p>
        <Trans
          i18nKey="about.tvmazeIntro"
          ns="library"
          components={{
            tvmaze: (
              <a href="https://www.tvmaze.com" target="_blank" rel="noreferrer" />
            ),
          }}
        />
      </p>
      <p>
        <Trans
          i18nKey="about.license"
          ns="library"
          components={{
            license: (
              <a
                href="https://creativecommons.org/licenses/by-sa/4.0/"
                target="_blank"
                rel="noreferrer"
              />
            ),
          }}
        />
      </p>
      <p>
        <Trans
          i18nKey="about.apiDocs"
          ns="library"
          components={{
            link: <a href="https://www.tvmaze.com/api" target="_blank" rel="noreferrer" />,
          }}
        />
      </p>
      <p>
        <Link to="/library">{t('about.goToLibrary')}</Link>
      </p>
    </article>
  )
}
