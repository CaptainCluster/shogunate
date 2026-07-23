import type { FormEvent } from 'react'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { getErrorMessage } from '../../lib/getErrorMessage'
import { useRegister } from './hooks/useRegister'
import './auth.css'

export function RegisterPage() {
  const { t } = useTranslation('auth')
  const navigate = useNavigate()
  const registerMutation = useRegister()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    try {
      await registerMutation.mutateAsync({ username, password })
      navigate('/login')
    } catch (err) {
      setError(getErrorMessage(err, t('register.failed')))
    }
  }

  return (
    <section className="auth-page">
      <h1>{t('register.title')}</h1>
      <form className="auth-form" onSubmit={handleSubmit}>
        <label>
          {t('register.username')}
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
          {t('register.password')}
          <input
            className="ui-input"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            minLength={8}
            required
          />
        </label>
        {error && <p className="auth-error">{error}</p>}
        <button type="submit" className="ui-button ui-button--primary" disabled={registerMutation.isPending}>
          {t('register.submit')}
        </button>
      </form>
      <div className="auth-links">
        <Link to="/login">{t('register.hasAccount')}</Link>
      </div>
    </section>
  )
}
