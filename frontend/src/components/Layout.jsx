import { useEffect, useState } from "react";
import { Link, NavLink, Outlet, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { notificationApi } from "../api";

function Bell({ count }) {
  return (
    <Link to="/notifications" className="bell" aria-label={`Notifications, ${count} unread`}>
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9" /><path d="M13.7 21a2 2 0 0 1-3.4 0" />
      </svg>
      {count > 0 && <span className="bell-count">{count}</span>}
    </Link>
  );
}

export default function Layout() {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [open, setOpen] = useState(false);
  const [unread, setUnread] = useState(0);

  // poll unread count every 30 seconds (simple in-app notifications, no WebSockets)
  useEffect(() => {
    let alive = true;
    const load = () => notificationApi.unreadCount().then((r) => alive && setUnread(r.count)).catch(() => {});
    load();
    const id = setInterval(load, 30000);
    return () => { alive = false; clearInterval(id); };
  }, [location.pathname]);

  useEffect(() => setOpen(false), [location.pathname]);

  const handleLogout = () => { logout(); navigate("/login"); };

  return (
    <>
      <header className="topbar">
        <div className="row">
          <button className="menu-toggle" onClick={() => setOpen(!open)} aria-label="Toggle menu">&#9776;</button>
          <Link to={isAdmin ? "/admin" : "/dashboard"} className="brand"><span className="brand-mark" />SmartFind</Link>
        </div>
        <div className="actions">
          <Bell count={unread} />
          <span className="small">{user.name} <span className={`badge ${user.role}`}>{user.role}</span></span>
          <button className="btn btn-sm btn-ghost" style={{ color: "#fff", borderColor: "#41606a" }} onClick={handleLogout}>Logout</button>
        </div>
      </header>
      <div className="shell">
        <nav className={`sidebar ${open ? "open" : ""}`}>
          {isAdmin && (
            <>
              <div className="group">Administration</div>
              <NavLink to="/admin" end>Dashboard</NavLink>
              <NavLink to="/admin/users">Users</NavLink>
              <NavLink to="/admin/lost">Lost reports</NavLink>
              <NavLink to="/admin/found">Found reports</NavLink>
              <NavLink to="/admin/claims">Claims</NavLink>
              <NavLink to="/admin/matches">Matches</NavLink>
            </>
          )}
          <div className="group">{isAdmin ? "My account" : "Menu"}</div>
          <NavLink to="/dashboard">Dashboard</NavLink>
          <NavLink to="/report/lost">Report lost item</NavLink>
          <NavLink to="/report/found">Report found item</NavLink>
          <NavLink to="/search">Search items</NavLink>
          <NavLink to="/matches">Possible matches</NavLink>
          <NavLink to="/my-reports">My reports</NavLink>
          <NavLink to="/my-claims">My claims</NavLink>
          <NavLink to="/notifications">Notifications {unread > 0 && <span className="badge MATCHED">{unread}</span>}</NavLink>
          <NavLink to="/profile">Profile</NavLink>
        </nav>
        <main className="main"><Outlet /></main>
      </div>
    </>
  );
}
