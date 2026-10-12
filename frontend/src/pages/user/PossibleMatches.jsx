import { useEffect, useState } from "react";
import { matchApi, claimApi } from "../../api";
import { errMsg } from "../../api/axiosClient";
import { Alert, EmptyState, Field, Loader, Modal } from "../../components/UI";
import MatchCard from "../../components/MatchCard";
import { useAuth } from "../../context/AuthContext";

export default function PossibleMatches() {
  const { user } = useAuth();
  const [matches, setMatches] = useState(null);
  const [claims, setClaims] = useState([]);
  const [level, setLevel] = useState("ALL");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [claiming, setClaiming] = useState(null);
  const [message, setMessage] = useState("");
  const [msgError, setMsgError] = useState("");
  const [busy, setBusy] = useState(false);

  const load = () => Promise.all([matchApi.list(), claimApi.list()])
    .then(([m, c]) => { setMatches(m); setClaims(c); })
    .catch((e) => { setError(errMsg(e)); setMatches([]); });
  useEffect(() => { load(); }, []);

  const claimFor = (matchId) => claims.find((c) => c.matchId === matchId && c.status !== "REJECTED");

  const submitClaim = async () => {
    if (message.trim().length < 10) { setMsgError("Please explain in at least 10 characters why this item is yours."); return; }
    setBusy(true); setMsgError("");
    try {
      await claimApi.create({ matchId: claiming.id, message });
      setSuccess("Claim submitted. An administrator will verify it.");
      setClaiming(null); setMessage("");
      load();
    } catch (e) { setMsgError(errMsg(e)); } finally { setBusy(false); }
  };

  if (!matches) return <Loader />;
  const shown = matches.filter((m) => level === "ALL" || m.matchLevel === level);

  return (
    <>
      <div className="page-head"><h1>Possible matches</h1><p className="muted">Reports that SmartFind thinks belong together, with the reasons.</p></div>
      <Alert type="error">{error}</Alert>
      <Alert type="success" onClose={() => setSuccess("")}>{success}</Alert>
      <div className="tabs">
        {["ALL", "STRONG", "POSSIBLE"].map((l) => <button key={l} className={`tab ${level === l ? "active" : ""}`} onClick={() => setLevel(l)}>{l === "ALL" ? "All" : l[0] + l.slice(1).toLowerCase()}</button>)}
      </div>
      {shown.length === 0 ? <EmptyState title="No matches yet">When someone reports a similar item, it will show up here.</EmptyState> : (
        <div className="grid">
          {shown.map((m) => {
            const isLostOwner = m.lostItem.ownerId === user.id;
            const existing = claimFor(m.id);
            return (
              <MatchCard key={m.id} match={m}>
                {isLostOwner && m.status === "ACTIVE" && !existing && <button className="btn btn-amber" onClick={() => setClaiming(m)}>This is mine - submit claim</button>}
                {existing && <span className="badge MATCHED">Your claim: {existing.status}</span>}
                {!isLostOwner && <span className="muted small">You found this item. The owner will contact the admin to claim it.</span>}
              </MatchCard>
            );
          })}
        </div>
      )}

      {claiming && (
        <Modal title="Submit a claim" onClose={() => setClaiming(null)}>
          <p className="muted small">An admin will verify your claim before any contact details are shared. Describe something only the owner would know.</p>
          <Field label="Why is this your item?" error={msgError}><textarea value={message} onChange={(e) => setMessage(e.target.value)} placeholder="e.g. It has my student ID (roll no. ...) and a 500 rupee note folded in the left pocket." /></Field>
          <div className="row"><button className="btn" onClick={submitClaim} disabled={busy}>{busy ? "Submitting..." : "Submit claim"}</button><button className="btn btn-ghost" onClick={() => setClaiming(null)}>Cancel</button></div>
        </Modal>
      )}
    </>
  );
}
