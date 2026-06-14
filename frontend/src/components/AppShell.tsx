import type { ReactNode } from "react";
import { Sidebar, type NavigationKey } from "./Sidebar";
import { TopBar } from "./TopBar";

interface AppShellProps {
  activeItem: NavigationKey;
  title: string;
  children: ReactNode;
  referencePanel: ReactNode;
  onNavigate: (item: NavigationKey) => void;
}

export function AppShell({
  activeItem,
  title,
  children,
  referencePanel,
  onNavigate,
}: AppShellProps) {
  return (
    <div className="app-frame">
      <Sidebar activeItem={activeItem} onNavigate={onNavigate} />
      <div className="min-w-0 bg-canvas">
        <TopBar title={title} />
        <div className="workspace-grid">
          <main className="min-w-0">{children}</main>
          <aside className="reference-column">{referencePanel}</aside>
        </div>
      </div>
    </div>
  );
}
