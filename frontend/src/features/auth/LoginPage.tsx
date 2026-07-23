import type { FormEvent } from 'react'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useLogin } from './hooks/useLogin'
import { getErrorMessage } from '../../lib/getErrorMessage'
import './auth.css'

export function LoginPage() {
  const { t } = useTranslation('auth')
  const loginMutation = useLogin()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    try {
      await loginMutation.mutateAsync({ username, password })
    } catch (err) {
      setError(getErrorMessage(err, t('login.failed')))
    }
  }

  return (
    <section className="auth-page">
      <h1>{t('login.title')}</h1>
      <form className="auth-form" onSubmit={handleSubmit}>
        <label>
          {t('login.username')}
          <input
            className="ui-input"
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            minLength={3}
            maxLength={32}
            pattern="[a-zA-Z0-9_]+"
            required
          />
        </label>
        <label>
          {t('login.password')}
          <input
            className="ui-input"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </label>
        {error && <p className="auth-error">{error}</p>}
        <button type="submit" className="ui-button ui-button--primary" disabled={loginMutation.isPending}>
          {t('login.submit')}
        </button>
      </form>
      <div className="auth-links">
        <Link to="/register">{t('login.createAccount')}</Link>
      </div>
    </section>
  )
}
