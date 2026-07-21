import { QueryClientProvider } from '@tanstack/react-query'
import { RouterProvider } from 'react-router-dom'
import { ConfirmProvider } from './components/ConfirmProvider'
import { queryClient } from './lib/queryClient'
import { router } from './routes/router'
import './index.css'

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ConfirmProvider>
        <RouterProvider router={router} />
      </ConfirmProvider>
    </QueryClientProvider>
  )
}

export default App
