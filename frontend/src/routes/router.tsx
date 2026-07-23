import { createBrowserRouter } from 'react-router-dom'
import { Layout } from '../components/Layout'
import { LoginPage } from '../features/auth/LoginPage'
import { RegisterPage } from '../features/auth/RegisterPage'
import { AboutPage } from '../features/library/AboutPage'
import { LibraryPage } from '../features/library/LibraryPage'
import { SearchPage } from '../features/library/SearchPage'
import { ShowDetailPage } from '../features/library/ShowDetailPage'
import { AnalyticsPage } from '../features/analytics/AnalyticsPage'
import { HomePage } from '../features/home/HomePage'
import { GuestRoute, ProtectedRoute } from './ProtectedRoute'

export const router = createBrowserRouter([
  {
    element: <Layout />,
    children: [
      {
        element: <ProtectedRoute />,
        children: [
          {
            path: '/',
            element: <HomePage />,
          },
          {
            path: '/search',
            element: <SearchPage />,
          },
          {
            path: '/library',
            element: <LibraryPage />,
          },
          {
            path: '/library/:id',
            element: <ShowDetailPage />,
          },
          {
            path: '/analytics',
            element: <AnalyticsPage />,
          },
          {
            path: '/about',
            element: <AboutPage />,
          },
        ],
      },
      {
        element: <GuestRoute />,
        children: [
          { path: '/login', element: <LoginPage /> },
          { path: '/register', element: <RegisterPage /> },
        ],
      },
    ],
  },
])
