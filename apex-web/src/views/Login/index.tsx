import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login as apiLogin } from '@/services/auth';
import { useAuthStore } from '@/stores/authStore';

export default function Login() {
  const navigate = useNavigate();
  const { login } = useAuthStore();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username || !password) { setError('请输入用户名和密码'); return; }
    setLoading(true);
    setError('');
    try {
      const result = await apiLogin({ username, password });
      if (result.code === 0 && result.data) {
        const { accessToken, user } = result.data;
        localStorage.setItem('token', accessToken);
        login(accessToken, { id: user.id, username: user.username, displayName: user.displayName });
        navigate('/target-combo');
      } else {
        setError(result.message || '登录失败');
      }
    } catch {
      setError('登录失败，请检查用户名和密码');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="apex-login-page">
      <div className="apex-login-card">
        <div className="apex-login-logo">
          <div className="apex-logo-mark">HB</div>
          <div>
            <div className="apex-login-title">Apex 早期靶点情报分析智能体</div>
            <div className="apex-login-subtitle">Harbour BioMed · 靶点情报分析平台</div>
          </div>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="apex-form-item">
            <label className="apex-form-label">用户名</label>
            <input
              className="apex-input"
              type="text"
              placeholder="请输入用户名"
              value={username}
              onChange={e => setUsername(e.target.value)}
              autoComplete="username"
            />
          </div>

          <div className="apex-form-item">
            <label className="apex-form-label">密码</label>
            <input
              className="apex-input"
              type="password"
              placeholder="请输入密码"
              value={password}
              onChange={e => setPassword(e.target.value)}
              autoComplete="current-password"
            />
          </div>

          {error && <div className="apex-login-error">{error}</div>}

          <button className="apex-login-btn" type="submit" disabled={loading}>
            {loading ? '登录中...' : '登 录'}
          </button>
        </form>
      </div>
    </div>
  );
}
