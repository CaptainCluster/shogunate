import { Link } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'

export function HomePage() {
  const { user, logout } = useAuth()

  return (
    <section>
      <h1>Shogunate</h1>
      <p>Track your TV shows, seasons, and episodes.</p>
      {user ? (
        <div>
          <p>Signed in as {user.username}</p>
          <p>
            <Link to="/library">Browse your library</Link>
            {' · '}
            <Link to="/search">Search for shows</Link>
          </p>
          <button type="button" onClick={logout}>
            Log out
          </button>
        </div>
      ) : (
        <p>
          <Link to="/login">Log in</Link> or <Link to="/register">create an account</Link> to get
          started.
        </p>
      )}
    </section>
  )
}
