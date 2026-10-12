import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { notificationApi } from "../../api";
import { errMsg } from "../../api/axiosClient";
import { Alert, EmptyState, Loader, fmtDateTime } from "../../components/UI";

export default function Notifications() {
  const navigate = useNavigate();
  const [items, setItems] = useState(null);
  const [error, setError] = useState("");

  const load = () => notificationApi.list().then(setItems).catch((e) => { setError(errMsg(e)); setItems([]); });
  useEffect(() => { load(); }, []);

  const open = async (n) => {
    try { if (!n.read) await notificationApi.read(n.id); } catch { /* ignore */ }
    if (n.matchId) navigate("/matches"); else load();
  };
  const readAll = async () => { await notificationApi.readAll(); load(); };

  return (
    <>
      <div className="page-head row-between">
        <h1>Notifications</h1>
        {items?.some((n) => !n.read) && <button className="btn btn-outline" onClick={readAll}>Mark all as read</button>}
      </div>
      <Alert type="error">{error}</Alert>
      {!items ? <Loader /> : items.length === 0 ? <EmptyState title="No notifications">You will be notified when a strong match is found.</EmptyState> : (
        <div className="card" style={{ padding: 0, overflow: "hidden" }}>
          {items.map((n) => (
            <div key={n.id} className={`notif ${n.read ? "read" : "unread"}`} onClick={() => open(n)} role="button" tabIndex={0} onKeyDown={(e) => e.key === "Enter" && open(n)}>
              <span className="dot" />
              <div>{n.message}<div className="small muted" style={{ fontWeight: 400 }}>{fmtDateTime(n.createdAt)}</div></div>
            </div>
          ))}
        </div>
      )}
    </>
  );
}
