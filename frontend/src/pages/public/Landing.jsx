import { Link, Navigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

export default function Landing() {
  const { user, isAdmin } = useAuth();
  if (user) return <Navigate to={isAdmin ? "/admin" : "/dashboard"} replace />;

  return (
    <div style={{ background: "var(--bg)", minHeight: "100%" }}>
      <header className="landing-nav">
        <span className="brand"><span className="brand-mark" />SmartFind</span>
        <div className="row">
          <Link to="/login" className="btn btn-ghost" style={{ color: "#fff", borderColor: "#41606a" }}>Login</Link>
          <Link to="/register" className="btn btn-amber">Create account</Link>
        </div>
      </header>

      <section className="hero">
        <div className="hero-inner">
          <div>
            <h1>Lost something on campus? Let the reports find each other.</h1>
            <p>Report a lost or found item once. SmartFind compares it with every open report, scores the similarity from 0 to 100 and tells you why it thinks two reports belong together.</p>
            <div className="row" style={{ marginTop: "1.4rem" }}>
              <Link to="/register" className="btn btn-amber">Report an item</Link>
              <Link to="/login" className="btn btn-ghost" style={{ color: "#fff", borderColor: "#41606a" }}>I already have an account</Link>
            </div>
          </div>
          <div className="hero-demo" aria-label="Example match">
            <div className="muted small">Example match</div>
            <div className="score">93%</div>
            <p style={{ margin: ".2rem 0 .6rem" }}><strong>Black Leather Wallet</strong> (lost) and <strong>Black Wallet</strong> (found)</p>
            <ul className="reasons">
              <li>Same category</li><li>Same color</li><li>Similar item name</li>
              <li>Nearby location</li><li>Similar date</li><li>Similar description</li>
            </ul>
          </div>
        </div>
      </section>

      <section className="section">
        <h2>How it works</h2>
        <div className="steps">
          <div className="card"><h3>1. Report</h3><p className="muted">Describe what you lost or found: name, category, color, place and date.</p></div>
          <div className="card"><h3>2. Match</h3><p className="muted">A weighted rule-based algorithm scores every possible pair and explains the result.</p></div>
          <div className="card"><h3>3. Claim</h3><p className="muted">If a strong match appears you get a notification and can submit a claim.</p></div>
          <div className="card"><h3>4. Verify</h3><p className="muted">An administrator reviews the claim before any contact details are shared.</p></div>
        </div>
      </section>
    </div>
  );
}
