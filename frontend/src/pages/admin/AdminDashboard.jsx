import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { adminApi } from "../../api";
import { errMsg } from "../../api/axiosClient";
import { Alert, Loader, StatCard, StatusBadge, fmtDateTime } from "../../components/UI";

function HBar({ label, value, max }) {
  return (
    <div className="hbar">
      <span>{label}</span>
      <div className="bar"><span style={{ width: `${max ? (value / max) * 100 : 0}%` }} /></div>
      <strong>{value}</strong>
    </div>
  );
}

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [matches, setMatches] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([adminApi.stats(), adminApi.matches()])
      .then(([s, m]) => { setStats(s); setMatches(m.slice(0, 5)); })
      .catch((e) => setError(errMsg(e)));
  }, []);

  if (error) return <Alert type="error">{error}</Alert>;
  if (!stats) return <Loader />;
  const max = Math.max(stats.totalLostReports, stats.totalFoundReports, stats.totalMatches, 1);

  return (
    <>
      <div className="page-head"><h1>Admin dashboard</h1><p className="muted">System overview and matching activity.</p></div>
      <div className="grid grid-stats mb">
        <StatCard label="Total users" value={stats.totalUsers} />
        <StatCard label="Lost reports" value={stats.totalLostReports} tone="red" />
        <StatCard label="Found reports" value={stats.totalFoundReports} tone="green" />
        <StatCard label="Matches detected" value={stats.totalMatches} tone="amber" />
        <StatCard label="Pending claims" value={stats.pendingClaims} tone="amber" />
        <StatCard label="Resolved cases" value={stats.resolvedCases} tone="green" />
      </div>

      <div className="grid" style={{ gridTemplateColumns: "repeat(auto-fit, minmax(320px, 1fr))" }}>
        <div className="card">
          <h2>Reports and matches</h2>
          <HBar label="Lost" value={stats.totalLostReports} max={max} />
          <HBar label="Found" value={stats.totalFoundReports} max={max} />
          <HBar label="Matches" value={stats.totalMatches} max={max} />
          <HBar label="Strong" value={stats.strongMatches} max={max} />
        </div>
        <div className="card">
          <h2>Claims</h2>
          <HBar label="Pending" value={stats.pendingClaims} max={Math.max(stats.pendingClaims, stats.approvedClaims, stats.rejectedClaims, 1)} />
          <HBar label="Approved" value={stats.approvedClaims} max={Math.max(stats.pendingClaims, stats.approvedClaims, stats.rejectedClaims, 1)} />
          <HBar label="Rejected" value={stats.rejectedClaims} max={Math.max(stats.pendingClaims, stats.approvedClaims, stats.rejectedClaims, 1)} />
          <Link to="/admin/claims" className="btn btn-outline btn-sm">Review claims</Link>
        </div>
      </div>

      <div className="card">
        <div className="row-between"><h2>Highest scoring matches</h2><Link to="/admin/matches" className="small">All matches</Link></div>
        {matches.length === 0 ? <p className="muted">No matches yet.</p> : (
          <div className="table-wrap" style={{ border: "none" }}><table>
            <thead><tr><th>Lost</th><th>Found</th><th>Score</th><th>Level</th><th>Status</th><th>Detected</th></tr></thead>
            <tbody>{matches.map((m) => (
              <tr key={m.id}><td>{m.lostItem.itemName}</td><td>{m.foundItem.itemName}</td><td><strong>{Math.round(m.score)}%</strong></td>
                <td><StatusBadge value={m.matchLevel} /></td><td><StatusBadge value={m.status} /></td><td>{fmtDateTime(m.createdAt)}</td></tr>))}
            </tbody>
          </table></div>
        )}
      </div>
    </>
  );
}
