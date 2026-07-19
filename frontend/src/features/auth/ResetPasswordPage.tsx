import type { FormEvent } from 'react'
import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import * as authApi from '../../api/authApi'
import { getErrorMessage } from '../../hooks/useAuth'
import './auth.css'

export function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const [token, setToken] = useState(searchParams.get('token') ?? '')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setMessage(null)
    try {
      const response = await authApi.resetPassword(token, password)
      setMessage(response.message)
    } catch (err) {
      setError(getErrorMessage(err, 'Password reset failed'))
    }
  }

  return (
    <section className="auth-page">
      <h1>Reset password</h1>
      <form className="auth-form" onSubmit={handleSubmit}>
        <label>
          Reset token
          <input
            type="text"
            value={token}
            onChange={(e) => setToken(e.target.value)}
            required
          />
        </label>
        <label>
          New password
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            minLength={8}
            required
          />
        </label>
        {error && <p className="auth-error">{error}</p>}
        {message && <p className="auth-message">{message}</p>}
        <button type="submit">Reset password</button>
      </form>
      <div className="auth-links">
        <Link to="/login">Back to login</Link>
      </div>
    </section>
  )
}
