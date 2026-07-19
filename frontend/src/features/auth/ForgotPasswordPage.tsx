import type { FormEvent } from 'react'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import * as authApi from '../../api/authApi'
import { getErrorMessage } from '../../hooks/useAuth'
import './auth.css'

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setMessage(null)
    try {
      const response = await authApi.forgotPassword(email)
      setMessage(response.message)
    } catch (err) {
      setError(getErrorMessage(err, 'Request failed'))
    }
  }

  return (
    <section className="auth-page">
      <h1>Forgot password</h1>
      <form className="auth-form" onSubmit={handleSubmit}>
        <label>
          Email
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </label>
        {error && <p className="auth-error">{error}</p>}
        {message && <p className="auth-message">{message}</p>}
        <button type="submit">Send reset link</button>
      </form>
      <div className="auth-links">
        <Link to="/login">Back to login</Link>
      </div>
    </section>
  )
}
