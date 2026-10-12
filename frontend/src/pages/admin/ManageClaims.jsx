import { useEffect, useState } from "react";
import { adminApi, claimApi, matchApi } from "../../api";
import { errMsg } from "../../api/axiosClient";
import { Alert, EmptyState, Field, Loader, Modal, StatusBadge, fmtDateTime } from "../../components/UI";
import MatchCard from "../../components/MatchCard";

export default function ManageClaims() {
  const [status, setStatus] = useState("PENDING");
  const [claims, setClaims] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [review, setReview] = useState(null);    // { claim, action, match }
  const [note, setNote] = useState("");
  const [busy, setBusy] = useState(false);

  const load = (s = status) => { setClaims(null); adminApi.claims(s).then(setClaims).catch((e) => { setError(errMsg(e)); setClaims([]); }); };
  useEffect(() => { load(status); /* eslint-disable-next-line */ }, [status]);

  const openReview = async (claim, action) => {
    setNote(""); setError("");
    let match = null;
    try { match = await matchApi.get(claim.matchId); } catch { /* optional */ }
    setReview({ claim, action, match });
  };

  const confirm = async () => {
    setBusy(true); setError("");
    try {
      if (review.action === "approve") await claimApi.approve(review.claim.id, note);
      else await claimApi.reject(review.claim.id, note);
      setSuccess(`Claim ${review.action === "approve" ? "approved" : "rejected"}.`);
      setReview(null); load();
    } catch (e) { setError(errMsg(e)); } finally { setBusy(false); }
  };

  return (
    <>
      <div className="page-head"><h1>Manage claims</h1><p className="muted">Verify claims before contact details are shared.</p></div>
      <Alert type="error" onClose={() => setError("")}>{error}</Alert>
      <Alert type="success" onClose={() => setSuccess("")}>{success}</Alert>
      <div className="tabs">
        {["PENDING", "APPROVED", "REJECTED"].map((s) => <button key={s} className={`tab ${status === s ? "active" : ""}`} onClick={() => setStatus(s)}>{s[0] + s.slice(1).toLowerCase()}</button>)}
      </div>
      {!claims ? <Loader /> : claims.length === 0 ? <EmptyState title={`No ${status.toLowerCase()} claims`} /> : (
        <div className="table-wrap"><table>
          <thead><tr><th>Claimant</th><th>Lost item</th><th>Found item</th><th>Score</th><th>Message</th><th>Status</th><th>Date</th><th /></tr></thead>
          <tbody>{claims.map((c) => (
            <tr key={c.id}>
              <td><strong>{c.claimantName}</strong></td><td>{c.lostItemName}</td><td>{c.foundItemName}</td><td>{Math.round(c.matchScore)}%</td>
              <td style={{ maxWidth: 260 }}>{c.message}{c.adminNote && <div className="small muted">Note: {c.adminNote}</div>}</td>
              <td><StatusBadge value={c.status} /></td><td>{fmtDateTime(c.createdAt)}</td>
              <td>{c.status === "PENDING" && <div className="row">
                <button className="btn btn-sm" onClick={() => openReview(c, "approve")}>Review and approve</button>
                <button className="btn btn-sm btn-danger" onClick={() => openReview(c, "reject")}>Reject</button></div>}</td>
            </tr>))}
          </tbody>
        </table></div>
      )}

      {review && (
        <Modal wide title={review.action === "approve" ? "Approve this claim?" : "Reject this claim?"} onClose={() => setReview(null)}>
          <p><strong>{review.claim.claimantName}</strong> says: "{review.claim.message}"</p>
          {review.match && <div className="mb"><MatchCard match={review.match} showBreakdown={false} /></div>}
          <Field label="Admin note (optional)"><textarea value={note} onChange={(e) => setNote(e.target.value)} placeholder="e.g. Verified student ID matches the claimant" /></Field>
          <div className="row">
            <button className={`btn ${review.action === "reject" ? "btn-danger" : ""}`} onClick={confirm} disabled={busy}>{busy ? "Saving..." : review.action === "approve" ? "Approve claim" : "Reject claim"}</button>
            <button className="btn btn-ghost" onClick={() => setReview(null)}>Cancel</button>
          </div>
        </Modal>
      )}
    </>
  );
}
