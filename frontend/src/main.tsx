/*
 * Shogunate - TV Show Tracker
 * Copyright (C) 2026 Ville Saloranta
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './i18n/config'
import './index.css'
import './styles/ui.css'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
