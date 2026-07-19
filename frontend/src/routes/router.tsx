import { createBrowserRouter } from 'react-router-dom'
import { Layout } from '../components/Layout'
import { LoginPage } from '../features/auth/LoginPage'
import { RegisterPage } from '../features/auth/RegisterPage'
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
