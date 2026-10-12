import { useEffect, useState } from "react";
import { claimApi } from "../../api";
import { errMsg } from "../../api/axiosClient";
import { Alert, EmptyState, Loader, StatusBadge, fmtDateTime } from "../../components/UI";

export default function MyClaims() {
  const [claims, setClaims] = useState(null);
  const [error, setError] = useState("");
  useEffect(() => { claimApi.list().then(setClaims).catch((e) => { setError(errMsg(e)); setClaims([]); }); }, []);

  return (
    <>
      <div className="page-head"><h1>My claims</h1><p className="muted">Claims you submitted. Each one is verified by an administrator.</p></div>
      <Alert type="error">{error}</Alert>
      {!claims ? <Loader /> : claims.length === 0 ? <EmptyState title="No claims yet">Open Possible Matches and claim a match that is yours.</EmptyState> : (
        <div className="table-wrap"><table>
          <thead><tr><th>Lost item</th><th>Matched with</th><th>Score</th><th>Your message</th><th>Status</th><th>Admin note</th><th>Date</th></tr></thead>
          <tbody>{claims.map((c) => (
            <tr key={c.id}>
              <td><strong>{c.lostItemName}</strong></td><td>{c.foundItemName}</td><td>{Math.round(c.matchScore)}%</td>
              <td style={{ maxWidth: 260 }}>{c.message}</td><td><StatusBadge value={c.status} /></td>
              <td>{c.adminNote || "-"}</td><td>{fmtDateTime(c.createdAt)}</td>
            </tr>))}
          </tbody>
        </table></div>
      )}
    </>
  );
}
