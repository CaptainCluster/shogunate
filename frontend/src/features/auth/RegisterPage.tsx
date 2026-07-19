import type { FormEvent } from 'react'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import * as authApi from '../../api/authApi'
import { getErrorMessage } from '../../hooks/useAuth'
import './auth.css'

export function RegisterPage() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setMessage(null)
    try {
      const response = await authApi.register(email, password)
      setMessage(response.message)
      navigate(`/verify-email?email=${encodeURIComponent(email)}`)
    } catch (err) {
      setError(getErrorMessage(err, 'Registration failed'))
    }
  }

  return (
    <section className="auth-page">
      <h1>Create account</h1>
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
        <label>
          Password
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
        <button type="submit">Register</button>
      </form>
      <div className="auth-links">
        <Link to="/login">Already have an account? Log in</Link>
      </div>
    </section>
  )
}
