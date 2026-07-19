import type { FormEvent } from 'react'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getErrorMessage } from '../../lib/getErrorMessage'
import { useRegister } from './hooks/useRegister'
import './auth.css'

export function RegisterPage() {
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
      setError(getErrorMessage(err, 'Registration failed'))
    }
  }

  return (
    <section className="auth-page">
      <h1>Create account</h1>
      <form className="auth-form" onSubmit={handleSubmit}>
        <label>
          Username
          <input
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
        <button type="submit" disabled={registerMutation.isPending}>
          Register
        </button>
      </form>
      <div className="auth-links">
        <Link to="/login">Already have an account? Log in</Link>
      </div>
    </section>
  )
}
