import { Navigate, Outlet } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../hooks/useAuth'

export function ProtectedRoute() {
  const { user, isLoading } = useAuth()
  const { t } = useTranslation('common')

  if (isLoading) {
    return <p>{t('loading')}</p>
  }

  if (!user) {
    return <Navigate to="/login" replace />
  }

  return <Outlet />
}

export function GuestRoute() {
  const { user, isLoading } = useAuth()
  const { t } = useTranslation('common')

  if (isLoading) {
    return <p>{t('loading')}</p>
  }

  if (user) {
    return <Navigate to="/" replace />
  }

  return <Outlet />
}
