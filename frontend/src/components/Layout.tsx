import { Link } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { Outlet } from 'react-router-dom'
import './Layout.css'

export function Layout() {
  const { user, logout } = useAuth()

  return (
    <div className="layout">
      <header className="layout-header">
        <Link to="/" className="layout-brand">
          Shogunate
        </Link>
        <nav className="layout-nav">
          <Link to="/">Home</Link>
          {user && <Link to="/library">Library</Link>}
          {user && <Link to="/search">Search</Link>}
          <Link to="/about">About</Link>
          {user ? (
            <>
              <span>{user.username}</span>
              <button type="button" onClick={logout}>
                Log out
              </button>
            </>
          ) : (
            <>
              <Link to="/login">Log in</Link>
              <Link to="/register">Register</Link>
            </>
          )}
        </nav>
      </header>
      <main className="layout-main">
        <Outlet />
      </main>
    </div>
  )
}
