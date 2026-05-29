import type React from "react"
import { ThemeProvider } from "@/components/theme-provider"
import { Providers } from "../providers"

export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <ThemeProvider attribute="class" defaultTheme="light" enableSystem={false}>
      <Providers>
        {children}
      </Providers>
    </ThemeProvider>
  )
}
