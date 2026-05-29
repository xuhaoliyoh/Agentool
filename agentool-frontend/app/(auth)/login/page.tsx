"use client"

import { useState, useEffect } from "react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { toast } from "@/hooks/use-toast"
import { Toaster } from "@/components/ui/toaster"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { loginApi } from "@/lib/api-services"
import { setCookie } from "@/lib/utils"
import { getAuthConfigWithToast } from "@/lib/auth-config-service"
import type { AuthConfig } from "@/lib/types/auth-config"
import { AUTH_FEATURE_KEY } from "@/lib/types/auth-config"

export default function LoginPage() {
  const router = useRouter()
  const [formData, setFormData] = useState({
    account: "",
    password: ""
  })
  const [loading, setLoading] = useState(false)
  const [authConfig, setAuthConfig] = useState<AuthConfig | null>(null)
  const [configLoading, setConfigLoading] = useState(true)

  // 加载认证配置
  useEffect(() => {
    async function fetchAuthConfig() {
      try {
        const response = await getAuthConfigWithToast()
        if (response.code === 200) {
          setAuthConfig(response.data)
        }
      } catch (error) {

      } finally {
        setConfigLoading(false)
      }
    }

    fetchAuthConfig()
  }, [])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    try {
      const { account, password } = formData
      if (!account || !password) {
        toast({
          variant: "destructive",
          title: "错误",
          description: "请输入账号和密码"
        })
        setLoading(false)
        return
      }

      // 使用带toast参数的API
      const res = await loginApi({ account, password }, true)
      if (res.code === 200 && res.data?.token) {
        localStorage.setItem("auth_token", res.data.token)
        setCookie("token", res.data.token, 30)
        router.push("/")
      }
    } catch (error: any) {
      // 错误已由API处理

    } finally {
      setLoading(false)
    }
  }

  // 配置加载中
  if (configLoading) {
    return (
      <div className="container max-w-[400px] py-10 h-screen flex flex-col justify-center">
        <div className="mb-8 space-y-2 text-center">
          <div className="h-8 bg-gray-200 rounded animate-pulse"></div>
          <div className="h-4 bg-gray-200 rounded animate-pulse"></div>
        </div>
        <div className="space-y-4">
          <div className="h-20 bg-gray-200 rounded animate-pulse"></div>
          <div className="h-20 bg-gray-200 rounded animate-pulse"></div>
          <div className="h-10 bg-gray-200 rounded animate-pulse"></div>
        </div>
      </div>
    )
  }

  // 检查是否有可用的登录方式
  const availableLoginMethods = authConfig?.loginMethods || {}
  const hasNormalLogin = availableLoginMethods[AUTH_FEATURE_KEY.NORMAL_LOGIN]?.enabled

  // 如果没有可用的登录方式
  if (!hasNormalLogin) {
    return (
      <div className="container max-w-[400px] py-10 h-screen flex flex-col justify-center">
        <div className="text-center space-y-4">
          <h1 className="text-2xl font-semibold tracking-tight">暂时无法登录</h1>
          <p className="text-sm text-muted-foreground">
            系统暂时关闭了所有登录方式，请稍后再试或联系管理员。
          </p>
        </div>
      </div>
    )
  }

  return (
    <>
      <div className="container max-w-[400px] py-10 h-screen flex flex-col justify-center">
        <div className="mb-8 space-y-2 text-center">
          <h1 className="text-2xl font-semibold tracking-tight">登录</h1>
          <p className="text-sm text-muted-foreground">欢迎回来！请输入您的账号信息。</p>
        </div>

        <div className="space-y-4">
          {/* 普通登录表单 */}
          {hasNormalLogin && (
            <form onSubmit={handleSubmit}>
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="account">
                    账号 <span className="text-red-500">*</span>
                  </Label>
                  <Input
                    id="account"
                    name="account"
                    type="text"
                    placeholder="请输入邮箱"
                    value={formData.account}
                    onChange={handleChange}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="password">
                    密码 <span className="text-red-500">*</span>
                  </Label>
                  <Input
                    id="password"
                    name="password"
                    type="password"
                    placeholder="请输入密码"
                    value={formData.password}
                    onChange={handleChange}
                    required
                  />
                </div>
                <Button type="submit" className="w-full bg-primary text-primary-foreground hover:bg-primary/90" disabled={loading}>
                  {loading ? "登录中..." : "登录"}
                </Button>
              </div>
            </form>
          )}

          {/* 底部链接 */}
          <div className="flex justify-between text-sm text-muted-foreground mb-2 mt-2">
            <div>
              {authConfig?.registerEnabled && (
                <>
                  还没有账号？{" "}
                  <Link href="/register" className="text-primary hover:underline">
                    立即注册
                  </Link>
                </>
              )}
            </div>
            {/* 只有普通登录启用时才显示忘记密码 */}
            {hasNormalLogin && (
              <div>
                <Link href="/reset-password" className="text-primary hover:underline">
                  忘记密码
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
      <Toaster />
    </>
  )
}
