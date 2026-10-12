import { useEffect, useState } from "react";
import { adminApi } from "../../api";
import { errMsg } from "../../api/axiosClient";
import { Alert, EmptyState, Loader, Modal, ScoreBar, StatusBadge, fmtDateTime } from "../../components/UI";
import MatchCard from "../../components/MatchCard";

export default function ManageMatches() {
  const [matches, setMatches] = useState(null);
  const [level, setLevel] = useState("ALL");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [detail, setDetail] = useState(null);

  const load = () => adminApi.matches().then(setMatches).catch((e) => { setError(errMsg(e)); setMatches([]); });
  useEffect(() => { load(); }, []);

  const close = async (m) => {
    setError(""); setSuccess("");
    try { await adminApi.closeCase(m.id); setSuccess("Case closed. Both reports are now CLOSED."); setDetail(null); load(); }
    catch (e) { setError(errMsg(e)); }
  };

  const shown = (matches || []).filter((m) => level === "ALL" || m.matchLevel === level);

  return (
    <>
      <div className="page-head"><h1>Matches</h1><p className="muted">Every pair the engine has scored, including weak ones (hidden from users).</p></div>
      <Alert type="error" onClose={() => setError("")}>{error}</Alert>
      <Alert type="success" onClose={() => setSuccess("")}>{success}</Alert>
      <div className="tabs">
        {["ALL", "STRONG", "POSSIBLE", "WEAK"].map((l) => <button key={l} className={`tab ${level === l ? "active" : ""}`} onClick={() => setLevel(l)}>{l === "ALL" ? "All" : l[0] + l.slice(1).toLowerCase()}</button>)}
      </div>
      {!matches ? <Loader /> : shown.length === 0 ? <EmptyState title="No matches" /> : (
        <div className="table-wrap"><table>
          <thead><tr><th>Lost report</th><th>Found report</th><th style={{ width: 150 }}>Score</th><th>Level</th><th>Status</th><th>Detected</th><th /></tr></thead>
          <tbody>{shown.map((m) => (
            <tr key={m.id}>
              <td><strong>{m.lostItem.itemName}</strong><div className="small muted">{m.lostItem.ownerName}</div></td>
              <td><strong>{m.foundItem.itemName}</strong><div className="small muted">{m.foundItem.ownerName}</div></td>
              <td><strong>{Math.round(m.score)}%</strong><ScoreBar value={m.score} level={m.matchLevel} /></td>
              <td><StatusBadge value={m.matchLevel} /></td><td><StatusBadge value={m.status} /></td>
              <td>{fmtDateTime(m.createdAt)}</td>
              <td><div className="row">
                <button className="btn btn-sm btn-outline" onClick={() => setDetail(m)}>Details</button>
                {m.status === "CLAIMED" && <button className="btn btn-sm" onClick={() => close(m)}>Close case</button>}
              </div></td>
            </tr>))}
          </tbody>
        </table></div>
      )}
      {detail && <Modal wide title={`Match #${detail.id}`} onClose={() => setDetail(null)}><MatchCard match={detail} /></Modal>}
    </>
  );
}
