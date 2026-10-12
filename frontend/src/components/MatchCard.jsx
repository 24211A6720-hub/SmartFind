import { StatusBadge, ScoreBar, fmtDate } from "./UI";

const LABELS = { name: "Item name", category: "Category", color: "Color", location: "Location", date: "Date", description: "Description" };

function Side({ title, item }) {
  return (
    <div>
      <h4>{title}</h4>
      <strong>{item.itemName}</strong> <StatusBadge value={item.status} />
      <div className="meta" style={{ marginTop: ".3rem" }}>
        <span>{item.category}</span><span>{item.color}</span>
      </div>
      <div className="small">Location: {item.location}</div>
      <div className="small">Date: {fmtDate(item.date)}</div>
      <div className="small muted">{item.description}</div>
      {item.contactInfo && <div className="small"><strong>Contact:</strong> {item.contactInfo}</div>}
    </div>
  );
}

/** Shows one match: both reports, the percentage, the reasons and the score breakdown. */
export default function MatchCard({ match, children, showBreakdown = true }) {
  return (
    <div className={`card match-card ${match.matchLevel}`}>
      <div className="match-head">
        <div>
          <div className="row">
            <span className="match-score">{Math.round(match.score)}%</span>
            <StatusBadge value={match.matchLevel} />
            <StatusBadge value={match.status} />
          </div>
          <div className="muted small">{match.matchLevel === "STRONG" ? "MATCH FOUND" : match.matchLevel === "POSSIBLE" ? "POSSIBLE MATCH" : "WEAK MATCH"}</div>
        </div>
        <div style={{ minWidth: 160, flex: "0 1 220px" }}><ScoreBar value={match.score} level={match.matchLevel} /></div>
      </div>

      <div className="pair">
        <Side title="Lost report" item={match.lostItem} />
        <Side title="Found report" item={match.foundItem} />
      </div>

      <h4 style={{ marginTop: ".9rem" }}>Why this may be a match</h4>
      {match.reasons.length === 0 ? <p className="muted small">No strong similarities.</p> : (
        <ul className="reasons">{match.reasons.map((r) => <li key={r}>{r}</li>)}</ul>
      )}

      {showBreakdown && (
        <div className="breakdown">
          {Object.entries(match.componentScores).map(([k, v]) => (
            <div key={k}><div className="row-between"><span>{LABELS[k]}</span><strong>{Math.round(v)}%</strong></div><ScoreBar value={v} /></div>
          ))}
        </div>
      )}
      {children && <div className="row" style={{ marginTop: "1rem" }}>{children}</div>}
    </div>
  );
}
