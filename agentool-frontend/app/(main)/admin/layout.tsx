"use client";

import React from "react";
import { AdminSidebar } from "./components/AdminSidebar";

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="admin-tech-shell flex w-full bg-pink-50" style={{ height: '100vh' }}>
      {/* 左侧菜单 */}
      <AdminSidebar />
      
      {/* 主内容区域 */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* 顶部标题栏 */}
        <header className="admin-tech-header bg-white/80 shadow-sm border-b border-pink-200/80 flex-shrink-0">
          <div className="px-6 py-4">
            <h1 className="text-xl font-semibold text-pink-950">管理后台</h1>
          </div>
        </header>
        
        {/* 内容区域 */}
        <main className="admin-tech-main flex-1 overflow-auto p-6">
          {children}
        </main>
      </div>
    </div>
  );
}
