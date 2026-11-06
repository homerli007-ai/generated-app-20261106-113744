import { useState } from 'react';

export default function App() {
  const [input, setInput] = useState('');
  const [results, setResults] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleDelete() {
    setError('');
    setResults([]);
    const paths = input.split('\n').map(s => s.trim()).filter(Boolean);
    if (paths.length === 0) {
      setError('Enter at least one path');
      return;
    }
    setLoading(true);
    try {
      const res = await fetch('/api/delete', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ paths })
      });
      const data = await res.json();
      if (!res.ok) {
        setError(data.error || 'Request failed');
      } else {
        setResults(data.results || []);
      }
    } catch (e) {
      setError(String(e));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="container">
      <h1>Safe File Deleter</h1>
      <p className="hint">Paths are relative to the server's allowed_root. Symlinks, directories, and paths outside the root are refused.</p>
      <textarea
        value={input}
        onChange={e => setInput(e.target.value)}
        placeholder={'one/path.txt\nanother/file.log'}
        rows={8}
      />
      <button onClick={handleDelete} disabled={loading}>
        {loading ? 'Deleting...' : 'Delete'}
      </button>
      {error && <div className="error">{error}</div>}
      {results.length > 0 && (
        <table>
          <thead>
            <tr><th>Path</th><th>Status</th><th>Reason</th></tr>
          </thead>
          <tbody>
            {results.map((r, i) => (
              <tr key={i} className={r.status}>
                <td>{r.path}</td>
                <td>{r.status}</td>
                <td>{r.reason || ''}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
