import type { FormEvent } from 'react'
import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { getErrorMessage } from '../../lib/getErrorMessage'
import { useResendVerification } from './hooks/useResendVerification'
import { useVerifyEmail } from './hooks/useVerifyEmail'
import './auth.css'

export function VerifyEmailPage() {
  const [searchParams] = useSearchParams()
  const verifyMutation = useVerifyEmail()
  const resendMutation = useResendVerification()
  const [email, setEmail] = useState(searchParams.get('email') ?? '')
  const [token, setToken] = useState(searchParams.get('token') ?? '')
  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)

  async function handleVerify(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setMessage(null)
    try {
      const response = await verifyMutation.mutateAsync(token)
      setMessage(response.message)
    } catch (err) {
      setError(getErrorMessage(err, 'Verification failed'))
    }
  }

  async function handleResend(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setMessage(null)
    try {
      const response = await resendMutation.mutateAsync(email)
      setMessage(response.message)
    } catch (err) {
      setError(getErrorMessage(err, 'Could not resend verification email'))
    }
  }

  return (
    <section className="auth-page">
      <h1>Verify email</h1>
      <form className="auth-form" onSubmit={handleVerify}>
        <label>
          Verification token
          <input
            type="text"
            value={token}
            onChange={(e) => setToken(e.target.value)}
            required
          />
        </label>
        <button type="submit" disabled={verifyMutation.isPending}>
          Verify email
        </button>
      </form>
      <form className="auth-form" onSubmit={handleResend}>
        <label>
          Resend to email
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </label>
        <button type="submit" disabled={resendMutation.isPending}>
          Resend verification
        </button>
      </form>
      {error && <p className="auth-error">{error}</p>}
      {message && <p className="auth-message">{message}</p>}
      <div className="auth-links">
        <Link to="/login">Back to login</Link>
      </div>
    </section>
  )
}
