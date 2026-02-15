import { createContext, useContext, useState, type ReactNode } from 'react'

/** Quick-access currencies in the header dropdown. */
export const QUICK_CURRENCIES = ['USD', 'EUR', 'GBP'] as const
export type QuickCurrency = (typeof QUICK_CURRENCIES)[number]

interface CurrencyContextValue {
  currency: string
  setCurrency: (c: string) => void
  currencyModalOpen: boolean
  setCurrencyModalOpen: (open: boolean) => void
}

const CurrencyContext = createContext<CurrencyContextValue | null>(null)

export function CurrencyProvider({ children }: { children: ReactNode }) {
  const [currency, setCurrency] = useState<string>('USD')
  const [currencyModalOpen, setCurrencyModalOpen] = useState(false)
  return (
    <CurrencyContext.Provider
      value={{
        currency,
        setCurrency,
        currencyModalOpen,
        setCurrencyModalOpen,
      }}
    >
      {children}
    </CurrencyContext.Provider>
  )
}

export function useCurrency() {
  const ctx = useContext(CurrencyContext)
  if (!ctx) throw new Error('useCurrency must be used within CurrencyProvider')
  return ctx
}
