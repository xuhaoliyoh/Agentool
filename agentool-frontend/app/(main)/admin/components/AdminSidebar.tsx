"use client";

import React from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import { 
  Users, 
  Wrench, 
  Bot, 
  Server,
  Settings,
  Home,
  Shield,
  Database
} from "lucide-react";

interface MenuItemProps {
  href: string;
  icon: React.ComponentType<{ className?: string }>;
  label: string;
  isActive: boolean;
}

function MenuItem({ href, icon: Icon, label, isActive }: MenuItemProps) {
  return (
    <Link
      href={href}
      className={cn(
        "flex items-center px-4 py-3 text-sm font-medium rounded-lg transition-all duration-200",
        isActive
          ? "bg-pink-500/10 text-pink-700 border-r-2 border-pink-500 shadow-sm shadow-pink-500/10"
          : "text-slate-600 hover:bg-pink-50 hover:text-pink-700"
      )}
    >
      <Icon className={cn("mr-3 h-5 w-5", isActive ? "text-pink-600" : "text-slate-400")} />
      {label}
    </Link>
  );
}

export function AdminSidebar() {
  const pathname = usePathname();

  const menuItems = [
    {
      href: "/admin",
      icon: Home,
      label: "管理首页",
    },
    {
      href: "/admin/users",
      icon: Users,
      label: "用户列表",
    },
    {
      href: "/admin/tools",
      icon: Wrench,
      label: "工具列表",
    },
    {
      href: "/admin/agents",
      icon: Bot,
      label: "Agent列表",
    },
    {
      href: "/admin/rags",
      icon: Database,
      label: "RAG管理",
    },
    {
      href: "/admin/providers",
      icon: Server,
      label: "服务商管理",
    },
    {
      href: "/admin/auth-settings",
      icon: Shield,
      label: "认证配置",
    },
  ];

  return (
    <div className="w-64 bg-white/80 shadow-sm border-r border-pink-200/80 flex flex-col backdrop-blur-xl" style={{ height: '100vh' }}>
      {/* Logo区域 */}
      <div className="p-6 border-b border-pink-200/80 flex-shrink-0">
        <div className="flex items-center">
          <div className="w-8 h-8 bg-pink-500 rounded-lg flex items-center justify-center shadow-[0_0_24px_rgba(236,72,153,0.28)]">
            <Settings className="w-5 h-5 text-white" />
          </div>
          <span className="ml-3 text-lg font-semibold text-pink-950">agentool Admin</span>
        </div>
      </div>

      {/* 菜单列表 */}
      <nav className="p-4 space-y-1 flex-1 overflow-y-auto">
        {menuItems.map((item) => (
          <MenuItem
            key={item.href}
            href={item.href}
            icon={item.icon}
            label={item.label}
            isActive={
              pathname === item.href || 
              (item.href === "/admin" && pathname === "/admin") ||
              (item.href !== "/admin" && pathname.startsWith(item.href))
            }
          />
        ))}
      </nav>

      {/* 底部信息 */}
      <div className="p-4 border-t border-pink-200/80 flex-shrink-0">
        <div className="text-xs text-slate-500 text-center">
          agentool 管理后台
          <br />
          v1.0.0
        </div>
      </div>
    </div>
  );
}
